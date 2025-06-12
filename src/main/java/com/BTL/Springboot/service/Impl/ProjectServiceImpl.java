package com.BTL.Springboot.service.Impl;

import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.ProjectTrashBin;
import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.mapper.Map;
import com.BTL.Springboot.repository.*;
import com.BTL.Springboot.service.ProjectService;
import jakarta.transaction.Transactional;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(Map::toProjectDto).collect(Collectors.toList());
    }

    @Override
    public Project getProjectById(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
    }

    @Override
    public List<Project> findByProjectName(String projectName) {
        return projectRepository.findByProjectName(projectName);
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

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
    @Override
    public List<ProjectDto> getProjectByProjectTypeId(int typeId) {
        List<ProjectDto> list = projectRepository.findByProjectTypeIdAndNotDeleted(typeId)
                .stream()
                .map(Map::toProjectDto)
                .collect(Collectors.toList());
        return list;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
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

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
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

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DIRECTOR', 'DEPUTY_DIRECTOR')")
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

    //Hàm đọc lấy dữ liệu từ excel
    public void importFromExcel(MultipartFile file, String type) throws IOException {
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

                PropertyType propertyType = propertyTypeRepository.findByTypeName(type);

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
                project.setStatus("true");

                projects.add(project);
            }
        }

        projectRepository.saveAll(projects);
    }


    //Start : Các hàm giúp hỗ trợ map kiểu dữ liệu từ excel
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
    //End

    //Dọc dữ liệu từ file pdf
    @Override
    public void readProjectsFromPdf(MultipartFile file, String type) throws IOException {
        List<Project> projects = new ArrayList<>();
        PropertyType defaultType = propertyTypeRepository.findByTypeName(type);
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
                        if (cells.size() < 9) continue;

                        try {
                            Project p = new Project();

                            p.setProjectName(smartCleanCell(cells.get(0)));
                            p.setDeveloper(smartCleanCell(cells.get(1)));
                            p.setLocation(smartCleanCell(cells.get(2)));
                            p.setState(smartCleanCell(cells.get(3)));
                            p.setCity(smartCleanCell(cells.get(4)));

                            String areaStr = smartCleanCell(cells.get(5)).replaceAll("[^0-9.]", "");
                            p.setTotalArea(areaStr.isEmpty() ? 0 : Double.parseDouble(areaStr));
                            p.setTotalUnits(Integer.parseInt(smartCleanCell(cells.get(6))));

                            String startDateStr = smartCleanCell(cells.get(6)).replaceAll("[^0-9\\-]", "");
                            String endDateStr = smartCleanCell(cells.get(7)).replaceAll("[^0-9\\-]", "");

                            p.setStartDate(LocalDate.parse(startDateStr, formatter));
                            p.setCompletionDate(LocalDate.parse(endDateStr, formatter));

                            p.setStatus("true");
                            p.setDescription("");
                            p.setProjectType(defaultType);

                            projects.add(p);
                        } catch (Exception e) {
                            System.err.println(" Lỗi khi đọc dòng " + i + ": " + e.getMessage());
                        }
                    }
                }
            }

            if (projects.isEmpty()) {
                System.out.println(" Không có dự án nào được đọc từ file.");
            } else {
                projectRepository.saveAll(projects);
            }

        } catch (Exception e) {
            throw new IOException("Lỗi khi xử lý file PDF: " + e.getMessage(), e);
        }
    }

    //Format dữ liệu để đọc từ file pdf
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


    @Override
    public Project parse(String text, PropertyType defaultType) {
        Project project = new Project();

        System.out.println(text);
        String[] lines = text.split("\\n");

        for (String line : lines) {
            String lower = removeDiacritics(line.toLowerCase());

            if (containsAny(lower, "ten du an", "project name")) {
                project.setProjectName(extractValue(line));
            } else if (containsAny(lower, "chu dau tu", "cong ty", "developer")) {
                project.setDeveloper(extractValue(line));
            } else if (containsAny(lower, "dien tich", "area")) {
                project.setTotalArea(extractDouble(line));
            } else if (containsAny(lower, "so luong", "can", "units")) {
                project.setTotalUnits(extractInt(line));
            } else if (containsAny(lower, "thanh pho", "city")) {
                project.setCity(extractValue(line));
            } else if (containsAny(lower, "tinh", "state")) {
                project.setState(extractValue(line));
            } else if (containsAny(lower, "vi tri", "dia diem", "location")) {
                project.setLocation(extractValue(line));
            } else if (containsAny(lower, "bat dau", "khoi cong", "start date")) {
                project.setStartDate(extractDate(line));
            } else if (containsAny(lower, "hoan thanh", "completion")) {
                project.setCompletionDate(extractDate(line));
            } else if (containsAny(lower, "trang thai", "status")) {
                project.setStatus(extractValue(line));
            } else if (containsAny(lower, "mo ta", "m6 ta", "gioi thieu", "description")) {
                project.setDescription(extractValue(line));
            }
        }

        project.setProjectType(defaultType);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        // fallback nếu thiếu dữ liệu
        if (project.getProjectName() == null) project.setProjectName("Không tên");
        if (project.getDeveloper() == null) project.setDeveloper("Không rõ");
        if (project.getCity() == null) project.setCity("Không rõ");
        if (project.getState() == null) project.setState("Không rõ");
        if (project.getLocation() == null) project.setLocation("Không rõ");
        if (project.getStatus() == null) project.setStatus("Đang cập nhật");
        if (project.getTotalArea() == null) project.setTotalArea(0.0);
        if (project.getTotalUnits() == null) project.setTotalUnits(0);

        return project;
    }

    private boolean containsAny(String line, String... keywords) {
        for (String keyword : keywords) {
            if (line.contains(keyword)) return true;
        }
        return false;
    }

    private String removeDiacritics(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d");
    }


    // =================== OCR =====================

    public String extractTextFromImage(MultipartFile file) throws IOException, TesseractException {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        File convFile = File.createTempFile("uploaded_", "." + ext);
        file.transferTo(convFile);

        BufferedImage originalImage = ImageIO.read(convFile);
        if (originalImage == null) {
            throw new IllegalArgumentException("Không thể đọc ảnh hoặc định dạng không hỗ trợ: " + file.getOriginalFilename());
        }

        // Tiền xử lý ảnh: chuyển sang grayscale và nhị phân (binarization)
        BufferedImage processedImage = preprocessImage(convFile);


        Tesseract tesseract = new Tesseract();
        File tessdataDir = extractTessdataFromResources(); // giữ nguyên như bạn có
        tesseract.setDatapath(tessdataDir.getAbsolutePath());
        tesseract.setLanguage("vie+eng");

        // Tùy chỉnh cấu hình Tesseract
        tesseract.setOcrEngineMode(1); // OEM_LSTM_ONLY
        tesseract.setPageSegMode(3);  // PSM_AUTO (tự động xác định bố cục)

        return tesseract.doOCR(processedImage);
    }

    private BufferedImage preprocessImage(File imageFile) throws IOException {
        // Đọc ảnh từ file
        Mat image = opencv_imgcodecs.imread(imageFile.getAbsolutePath());

        // Resize nếu ảnh quá nhỏ (OCR kém với ảnh bé)
        if (image.cols() < 800) {
            double scale = 800.0 / image.cols();
            opencv_imgproc.resize(image, image, new Size((int)(image.cols() * scale), (int)(image.rows() * scale)));
        }

        // Chuyển grayscale
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY);

        // Binarization – Adaptive threshold
        Mat binary = new Mat();
        opencv_imgproc.adaptiveThreshold(
                gray, binary, 255,
                opencv_imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                opencv_imgproc.THRESH_BINARY,
                31, 10
        );

        // Giảm nhiễu nhẹ (median blur)
        opencv_imgproc.medianBlur(binary, binary, 3);

        // Chuyển sang BufferedImage để dùng cho Tesseract
        return matToBufferedImage(binary);
    }

    private BufferedImage matToBufferedImage(Mat mat) throws IOException {
        BytePointer bytePointer = new BytePointer();
        opencv_imgcodecs.imencode(".png", mat, bytePointer);

        byte[] byteArray = new byte[(int) bytePointer.limit()];
        bytePointer.get(byteArray);
        bytePointer.deallocate();

        return ImageIO.read(new ByteArrayInputStream(byteArray));
    }

    // =================== Dữ liệu từng dòng =====================

    private String extractValue(String line) {
        return line.contains(":") ? line.split(":", 2)[1].trim() : line.trim();
    }

    private Double extractDouble(String line) {
        Matcher matcher = Pattern.compile("(\\d+[.,]?\\d*)").matcher(line);
        return matcher.find() ? Double.parseDouble(matcher.group(1).replace(",", ".")) : 0.0;
    }

    private Integer extractInt(String line) {
        Matcher matcher = Pattern.compile("(\\d+)").matcher(line);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private LocalDate extractDate(String line) {
        // Dạng ngày hỗ trợ: 01/01/2022, 1-1-22, 12.5.2023...
        Matcher matcher = Pattern.compile("(\\d{1,2}[-/\\.]{1}\\d{1,2}[-/\\.]{1}\\d{2,4})").matcher(line);

        if (matcher.find()) {
            String rawDate = matcher.group(1).replace(".", "/").replace("-", "/"); // chuẩn hóa
            DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("d/M/yyyy"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("d/M/yy"),
                    DateTimeFormatter.ofPattern("dd/MM/yy")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(rawDate, formatter);
                } catch (DateTimeParseException ignored) {}
            }
        }
        return null;
    }


    // =================== Trích tessdata =====================

    private File extractTessdataFromResources() throws IOException {
        InputStream engStream = getClass().getClassLoader().getResourceAsStream("tessdata/eng.traineddata");
        InputStream vieStream = getClass().getClassLoader().getResourceAsStream("tessdata/vie.traineddata");

        File tempTessdata = new File(System.getProperty("java.io.tmpdir"), "tessdata");
        if (!tempTessdata.exists()) tempTessdata.mkdirs();

        Files.copy(engStream, new File(tempTessdata, "eng.traineddata").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.copy(vieStream, new File(tempTessdata, "vie.traineddata").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        return tempTessdata;
    }

    // =================== Tiện ích =====================

}
