package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.ProjectDto;
import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.ProjectTrashBin;
import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.mapper.Map;
import com.BTL.Springboot.repository.*;
import com.BTL.Springboot.service.ProjectService;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectTrashRepository projectTrashRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public List<ProjectDto> getProjectByProjectTypeId(int typeId) {
        List<ProjectDto> list = projectRepository.findByProjectTypeIdAndNotDeleted(typeId)
                .stream()
                .map(Map::toProjectDto)
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public List<Project> getProjectByTypeId(int typeId) {
        List<Project> list = new ArrayList<>(projectRepository.findByProjectTypeIdAndNotDeleted(typeId));
        return list;
    }

    @Override
    public Project getProjectById(int id){
        return projectRepository.findById(id);
    }

    @Override
    public void updateProject(int id,Project updatedProject) {
        Project project = projectRepository.findById(id);
        project.setProjectName(updatedProject.getProjectName());
        project.setProjectType(updatedProject.getProjectType());
        project.setDeveloper(updatedProject.getDeveloper());
        project.setLocation(updatedProject.getLocation());
        project.setCity(updatedProject.getCity());
        project.setState(updatedProject.getState());
        project.setTotalArea(updatedProject.getTotalArea());
        project.setTotalUnits(updatedProject.getTotalUnits());
        project.setStartDate(updatedProject.getStartDate());
        project.setCompletionDate(updatedProject.getCompletionDate());
        project.setStatus(updatedProject.getStatus());
        project.setDescription(updatedProject.getDescription());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    @Override
    public void deleteProject(int id) {
        Project project = projectRepository.findById(id);
        project.setStatus("false");
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);

        propertyRepository.softDeletePropertiesByProject(id);
        propertyImageRepository.softDeleteImagesByProject(id);
        appointmentRepository.cancelAppointmentsByProject(id);
        transactionRepository.cancelTransactionsByProject(id);
        paymentRepository.cancelPaymentsByProject(id);

        ProjectTrashBin projectTrashBinEntity = new ProjectTrashBin();
        projectTrashBinEntity.setProject(project);
        projectTrashBinEntity.setDeletedAt(LocalDateTime.now());
        projectTrashRepository.save(projectTrashBinEntity);
    }

    @Override
    @Transactional
    public void saveProject(Project project) {
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    @Override
    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    public void importFromExcel(MultipartFile file) throws IOException {
        List<Project> projects = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean skipHeader = true;

            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                if (row == null || row.getCell(0) == null) continue;

                Project project = new Project();

                PropertyType propertyType = propertyTypeRepository.findByTypeName("Căn hộ chung cư");

                project.setProjectName(getString(row.getCell(0)));
                project.setProjectType(propertyType);
                project.setDeveloper(getString(row.getCell(1)));
                project.setLocation(getString(row.getCell(2)));
                project.setState(getString(row.getCell(3)));
                project.setCity(getString(row.getCell(4)));
                project.setTotalArea(getDouble(row.getCell(5)));
                project.setTotalUnits(getInteger(row.getCell(6)));
                project.setStartDate(getDate(row.getCell(7)));
                project.setCompletionDate(getDate(row.getCell(8)));
                project.setStatus(getString(row.getCell(9)));

                projects.add(project);
            }
        }

        projectRepository.saveAll(projects);
    }
    // === Helper methods ===

    private String getString(Cell cell) {
        return cell != null ? cell.toString().trim() : null;
    }

    private Double getDouble(Cell cell) {
        try {
            return cell != null ? cell.getNumericCellValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInteger(Cell cell) {
        try {
            return cell != null ? (int) cell.getNumericCellValue() : null;
        } catch (Exception e) {
            return null;
        }
    }


    private LocalDate getDate(Cell cell) {
        try {
            if (cell != null && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void readProjectsFromPdf(MultipartFile file) throws IOException {
        List<Project> projects = new ArrayList<>();
        PropertyType defaultType = propertyTypeRepository.findByTypeName("Căn hộ chung cư");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try (InputStream input = file.getInputStream(); PDDocument document = PDDocument.load(input)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm algorithm = new SpreadsheetExtractionAlgorithm();

            for (int pageNum = 1; pageNum <= document.getNumberOfPages(); pageNum++) {
                Page page = extractor.extract(pageNum);
                List<technology.tabula.Table> tables = algorithm.extract(page);

                for (technology.tabula.Table table : tables) {
                    List<List<RectangularTextContainer>> rows = table.getRows();

                    for (int i = 1; i < rows.size(); i++) {
                        List<RectangularTextContainer> cells = rows.get(i);
                        if (cells.size() < 8) continue;

                        try {
                            Project p = new Project();

                            p.setProjectName(smartCleanCell(cells.get(0)));
                            p.setDeveloper(smartCleanCell(cells.get(1)));
                            p.setLocation(smartCleanCell(cells.get(2)));
                            p.setState(smartCleanCell(cells.get(3)));
                            p.setCity(smartCleanCell(cells.get(4)));

                            String areaStr = smartCleanCell(cells.get(5)).replaceAll("[^0-9.]", "");
                            p.setTotalArea(areaStr.isEmpty() ? 0 : Double.parseDouble(areaStr));

                            String startDateStr = smartCleanCell(cells.get(6)).replaceAll("[^0-9\\-]", "");
                            String endDateStr = smartCleanCell(cells.get(7)).replaceAll("[^0-9\\-]", "");

                            p.setStartDate(LocalDate.parse(startDateStr, formatter));
                            p.setCompletionDate(LocalDate.parse(endDateStr, formatter));

                            p.setStatus("true");
                            p.setDescription("");
                            p.setTotalUnits(0);
                            p.setProjectType(defaultType);

                            projects.add(p);
                        } catch (Exception e) {
                            System.err.println("❌ Lỗi khi đọc dòng " + i + ": " + e.getMessage());
                        }
                    }
                }
            }

            if (projects.isEmpty()) {
                System.out.println("⚠ Không có dự án nào được đọc từ file.");
            } else {
                projectRepository.saveAll(projects);
            }

        } catch (Exception e) {
            throw new IOException("❌ Lỗi khi xử lý file PDF: " + e.getMessage(), e);
        }
    }
    private String smartCleanCell(RectangularTextContainer cell) {
        if (cell == null) return "";
        String text = cell.getText();

        // 1. Gộp dòng -> cách bằng 1 khoảng trắng
        text = text.replaceAll("[\\n\\t\\r]+", " ");

        // 2. Cách giữa chữ thường và HOA (VD: MasteriseHomes -> Masterise Homes)
        text = text.replaceAll("(?<=[\\p{Ll}])(?=[\\p{Lu}])", " ");

        // 3. Cách giữa chữ và số
        text = text.replaceAll("(?<=[A-Za-z])(?=[0-9])", " ");
        text = text.replaceAll("(?<=[0-9])(?=[A-Za-z])", " ");

        // 4. Loại khoảng trắng dư
        return text.trim().replaceAll(" +", " ");
    }
}
