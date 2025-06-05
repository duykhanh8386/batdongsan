package com.BTL.Springboot.service.Impl;

import boofcv.abst.fiducial.QrCodeDetector;
import boofcv.alg.fiducial.qrcode.QrCode;
import boofcv.factory.fiducial.ConfigQrCode;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import com.BTL.Springboot.dto.request.ApiRequest;
import com.BTL.Springboot.dto.request.PropertyQRData;
import com.BTL.Springboot.dto.request.property.PropertyRequest;
import com.BTL.Springboot.dto.response.customer.CustomerDto;
import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.dto.response.property_type.PropertyTypeDto;
import com.BTL.Springboot.entity.Customer;
import com.BTL.Springboot.entity.Employee;
import com.BTL.Springboot.entity.Project;
import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.NonFinal;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.BTL.Springboot.dto.response.project.ProjectDto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PropertyImportServiceImpl implements PropertyImportService {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PropertyTypeService propertyTypeService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CustomerService customerService;

    /**
     * Đọc file Excel từ mảng byte và chuyển thành danh sách PropertyRequest.
     * Hàm sử dụng Apache POI để đọc từng dòng Excel, ánh xạ thành PropertyRequest,
     * và kiểm tra tính hợp lệ của các trường liên quan (propertyType, project, owner, listingAgent).
     *
     * @param content Mảng byte của file Excel
     * @return Danh sách PropertyRequest
     * @throws IOException Nếu file Excel không hợp lệ hoặc không đọc được
     */
    private List<PropertyRequest> readExcelFromBytes(byte[] content) throws IOException {
        List<PropertyRequest> properties = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 2;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                boolean isRowEmpty = true;
                for (int i = 0; i < 20; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null && getCellStringValue(cell) != null && !getCellStringValue(cell).trim().isEmpty()) {
                        isRowEmpty = false;
                        break;
                    }
                }
                if (isRowEmpty) {
                    System.out.println("Bỏ qua dòng trống: " + rowNum);
                    continue;
                }

                PropertyRequest request = new PropertyRequest();
                request.setPropertyCode(getCellStringValue(row.getCell(0)));

                Long propertyTypeId = getCellLongValue(row.getCell(1));
                if (propertyTypeId != null) {
                    PropertyType propertyType = propertyTypeService.getPropertyTypeById(propertyTypeId.intValue());
                    if (propertyType == null) {
                        throw new IllegalArgumentException("Loại bất động sản không tồn tại: " + propertyTypeId + " tại dòng " + rowNum);
                    }
                    request.setPropertyType(propertyType);
                }

                request.setTitle(getCellStringValue(row.getCell(2)));
                request.setDescription(getCellStringValue(row.getCell(3)));
                request.setAddress(getCellStringValue(row.getCell(4)));
                request.setCity(getCellStringValue(row.getCell(5)));
                request.setState(getCellStringValue(row.getCell(6)));
                request.setPostalCode(getCellStringValue(row.getCell(7)));
                request.setPrice(getCellDoubleValue(row.getCell(8)));
                request.setArea(getCellDoubleValue(row.getCell(9)));
                request.setBedrooms(getCellByteValue(row.getCell(10)));
                request.setBathrooms(getCellByteValue(row.getCell(11)));
                request.setFloors(getCellByteValue(row.getCell(12)));
                request.setYearBuilt(getCellSqlDateValue(row.getCell(13)));
                request.setIsFurnished(getCellBooleanValue(row.getCell(14)));
                request.setListingType(getCellStringValue(row.getCell(15)));
                request.setStatus(getCellStringValue(row.getCell(16)));

                Long projectId = getCellLongValue(row.getCell(17));
                if (projectId != null) {
                    Project project = projectService.getProjectById(projectId.intValue());
                    if (project == null) {
                        throw new IllegalArgumentException("Dự án không tồn tại: " + projectId + " tại dòng " + rowNum);
                    }
                    request.setProject(project);
                }

                Long ownerId = getCellLongValue(row.getCell(18));
                if (ownerId != null) {
                    Customer owner = customerService.getCustomerById(ownerId.intValue());
                    if (owner == null) {
                        throw new IllegalArgumentException("Chủ sở hữu không tồn tại: " + ownerId + " tại dòng " + rowNum);
                    }
                    request.setOwner(owner);
                }

                Long listingAgentId = getCellLongValue(row.getCell(19));
                if (listingAgentId != null) {
                    Employee listingAgent = employeeService.getEmployeeById(listingAgentId.intValue());
                    if (listingAgent == null) {
                        throw new IllegalArgumentException("Nhân viên phụ trách không tồn tại: " + listingAgentId + " tại dòng " + rowNum);
                    }
                    request.setListingAgent(listingAgent);
                }

                request.setCreatedAt(getCellLocalDateTimeValue(row.getCell(20)));
                request.setUpdatedAt(getCellLocalDateTimeValue(row.getCell(21)));

                properties.add(request);
                rowNum++;
            }
        }
        return properties;
    }

    /**
     * Đọc file Excel từ MultipartFile và chuyển thành danh sách PropertyRequest.
     * Hàm gọi readExcelFromBytes để xử lý nội dung file.
     *
     * @param file File Excel
     * @return Danh sách PropertyRequest
     * @throws IOException Nếu file không hợp lệ
     */
    @Override
    public List<PropertyRequest> readExcelFile(MultipartFile file) throws IOException {
        return readExcelFromBytes(file.getBytes());
    }

    /**
     * Lấy giá trị chuỗi từ ô Excel, xử lý lỗi và trả về null nếu ô rỗng hoặc không hợp lệ.
     *
     * @param cell Ô Excel
     * @return Giá trị chuỗi hoặc null
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        try {
            cell.setCellType(CellType.STRING);
            String value = cell.getStringCellValue();
            return value != null ? value.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lấy giá trị Double từ ô Excel, trả về null nếu ô rỗng hoặc không phải số.
     *
     * @param cell Ô Excel
     * @return Giá trị Double hoặc null
     */
    private Double getCellDoubleValue(Cell cell) {
        if (cell == null) return null;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lấy giá trị Byte từ ô Excel, trả về null nếu ô rỗng hoặc không phải số.
     *
     * @param cell Ô Excel
     * @return Giá trị Byte hoặc null
     */
    private Byte getCellByteValue(Cell cell) {
        if (cell == null) return null;
        try {
            return (byte) cell.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lấy giá trị Long từ ô Excel, trả về null nếu ô rỗng hoặc không phải số.
     *
     * @param cell Ô Excel
     * @return Giá trị Long hoặc null
     */
    private Long getCellLongValue(Cell cell) {
        if (cell == null) return null;
        try {
            return (long) cell.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lấy giá trị Boolean từ ô Excel, hỗ trợ cả dạng chuỗi ("true"/"false").
     *
     * @param cell Ô Excel
     * @return Giá trị Boolean hoặc null
     */
    private Boolean getCellBooleanValue(Cell cell) {
        if (cell == null) return null;
        try {
            return cell.getBooleanCellValue();
        } catch (Exception e) {
            return "true".equalsIgnoreCase(getCellStringValue(cell));
        }
    }

    /**
     * Lấy giá trị java.sql.Date từ ô Excel, hỗ trợ cả dạng chuỗi định dạng "dd/MM/yyyy".
     *
     * @param cell Ô Excel
     * @return Giá trị Date hoặc null
     */
    private java.sql.Date getCellSqlDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            return new java.sql.Date(cell.getDateCellValue().getTime());
        } catch (Exception e) {
            try {
                String value = getCellStringValue(cell);
                if (value != null && !value.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate localDate = LocalDate.parse(value, formatter);
                    return java.sql.Date.valueOf(localDate);
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * Lấy giá trị LocalDateTime từ ô Excel, hỗ trợ định dạng "yyyy-MM-dd HH:mm:ss".
     *
     * @param cell Ô Excel
     * @return Giá trị LocalDateTime hoặc null
     */
    private LocalDateTime getCellLocalDateTimeValue(Cell cell) {
        if (cell == null) return null;
        try {
            return cell.getLocalDateTimeCellValue();
        } catch (Exception e) {
            try {
                String value = getCellStringValue(cell);
                if (value != null && !value.isEmpty()) {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                return null;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * Đọc file PDF và chuyển thành danh sách PropertyRequest.
     * Hàm sử dụng Apache PDFBox để trích xuất văn bản, sau đó phân tích thành danh sách PropertyRequest.
     *
     * @param file File PDF
     * @return Danh sách PropertyRequest
     * @throws IOException Nếu file PDF không hợp lệ hoặc mã hóa
     */
    @Override
    public List<PropertyRequest> readPDFFile(MultipartFile file) throws IOException {
        List<PropertyRequest> properties = new ArrayList<>();
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                properties = parsePDFTextToProperties(text);
            } else {
                throw new IOException("File PDF được mã hóa và không thể đọc.");
            }
        }
        return properties;
    }

    /**
     * Phân tích văn bản từ PDF thành danh sách PropertyRequest.
     * Hàm sử dụng CSVParser để parse văn bản dạng CSV, xử lý các trường và ánh xạ thành PropertyRequest.
     *
     * @param text Văn bản trích xuất từ PDF
     * @return Danh sách PropertyRequest
     * @throws IOException Nếu văn bản không hợp lệ
     */
    private List<PropertyRequest> parsePDFTextToProperties(String text) throws IOException {
        List<PropertyRequest> properties = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Trước tiên, làm sạch text từ PDF - loại bỏ xuống dòng không mong muốn trong header
        String cleanedText = preprocessPDFText(text);

        try (CSVParser csvParser = CSVParser.parse(cleanedText,
                CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withTrim()
                        .withAllowMissingColumnNames()
                        .withIgnoreEmptyLines())) {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowNum = 2; // Bắt đầu từ dòng 2 (sau header)

            // Debug: In ra header để kiểm tra
            System.out.println("Headers found: " + csvParser.getHeaderNames());

            for (CSVRecord record : csvParser) {
                try {
                    // Debug: In ra record để kiểm tra
                    System.out.println("Processing row " + rowNum + ": " + record.toString());

                    PropertyRequest request = new PropertyRequest();

                    // Xử lý các trường cơ bản
                    request.setPropertyCode(getCleanValue(record, "propertyCode"));
                    request.setTitle(getCleanValue(record, "title"));
                    request.setDescription(getCleanValue(record, "description"));
                    request.setAddress(getCleanValue(record, "address"));
                    request.setCity(getCleanValue(record, "city"));
                    request.setState(getCleanValue(record, "state"));
                    request.setPostalCode(getCleanValue(record, "postalCode"));

                    // Xử lý các trường số
                    request.setPrice(parseDoubleFromRecord(record, "price"));
                    request.setArea(parseDoubleFromRecord(record, "area"));
                    request.setBedrooms(parseByteFromRecord(record, "bedrooms"));
                    request.setBathrooms(parseByteFromRecord(record, "bathrooms"));
                    request.setFloors(parseByteFromRecord(record, "floors"));

                    // Xử lý ngày tháng
                    String yearBuiltStr = getCleanValue(record, "yearBuilt");
                    if (yearBuiltStr != null) {
                        try {
                            LocalDate localDate = LocalDate.parse(yearBuiltStr, dateFormatter);
                            request.setYearBuilt(java.sql.Date.valueOf(localDate));
                        } catch (Exception e) {
                            errors.add("Dòng " + rowNum + ": Lỗi định dạng năm xây dựng: " + yearBuiltStr);
                        }
                    }

                    // Xử lý boolean
                    request.setIsFurnished(parseBooleanFromRecord(record, "isFurnished"));

                    // Xử lý listing type và status
                    request.setListingType(getCleanValue(record, "listingType"));
                    request.setStatus(getCleanValue(record, "status"));
//                    String status = getCleanValue(record, "status");
//                    if ("true".equalsIgnoreCase(status)) {
//                        request.setStatus("active");
//                    } else if ("false".equalsIgnoreCase(status)) {
//                        request.setStatus("inactive");
//                    } else {
//                        request.setStatus(status != null ? status.toLowerCase() : "active");
//                    }

                    // Xử lý các ID
                    boolean hasValidPropertyType = handlePropertyTypeId(request, record, rowNum, errors);
                    boolean hasValidOwner = handleOwnerId(request, record, rowNum, errors);
                    boolean hasValidAgent = handleListingAgentId(request, record, rowNum, errors);
                    handleProjectId(request, record, rowNum, errors); // Project là tùy chọn

                    // Chỉ thêm vào danh sách nếu có đủ thông tin bắt buộc
                    if (hasValidPropertyType && hasValidOwner && hasValidAgent) {
                        properties.add(request);
                    } else {
                        System.out.println("Bỏ qua dòng " + rowNum + " do thiếu thông tin bắt buộc");
                    }

                } catch (Exception e) {
                    System.out.println("Lỗi khi phân tích dòng " + rowNum + ": " + e.getMessage());
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
                rowNum++;
            }

            // Nếu không có bản ghi nào hợp lệ và có lỗi, throw exception
            if (properties.isEmpty() && !errors.isEmpty()) {
                throw new IllegalArgumentException("Không thể xử lý file: " + String.join("; ", errors));
            }

            // Nếu có một số bản ghi hợp lệ, chỉ log warning cho các lỗi
            if (!errors.isEmpty()) {
                System.out.println("Cảnh báo: Một số dòng có lỗi: " + String.join("; ", errors));
            }

        } catch (Exception e) {
            throw new IOException("Lỗi khi parse CSV: " + e.getMessage(), e);
        }

        return properties;
    }

    /**
     * Làm sạch văn bản từ PDF trước khi parse.
     * Hàm loại bỏ dòng trống, xử lý header, và thay thế xuống dòng trong quotes để đảm bảo CSV hợp lệ.
     *
     * @param text Văn bản gốc từ PDF
     * @return Văn bản đã làm sạch
     */
    private String preprocessPDFText(String text) {
        if (text == null) return "";

        // Tách thành các dòng
        String[] lines = text.split("\n");
        StringBuilder cleanedText = new StringBuilder();

        boolean isFirstLine = true;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (isFirstLine) {
                // Xử lý header line - loại bỏ xuống dòng trong header
                String headerLine = line.replaceAll("\\s+", "");
                // Nếu header bị cắt, nối với dòng tiếp theo
                int nextIndex = 1;
                while (nextIndex < lines.length && !headerLine.endsWith("updatedAt")) {
                    if (nextIndex < lines.length) {
                        String nextLine = lines[nextIndex].trim().replaceAll("\\s+", "");
                        headerLine += nextLine;
                        nextIndex++;
                    }
                }
                cleanedText.append(headerLine).append("\n");

                // Bỏ qua các dòng đã được xử lý
                for (int i = nextIndex; i < lines.length; i++) {
                    String dataLine = lines[i].trim();
                    if (!dataLine.isEmpty()) {
                        // Làm sạch dữ liệu - chỉ thay thế xuống dòng trong quotes
                        dataLine = cleanDataLine(dataLine);
                        cleanedText.append(dataLine).append("\n");
                    }
                }
                break;
            }
        }

        System.out.println("Cleaned CSV text:");
        System.out.println(cleanedText.toString());
        return cleanedText.toString();
    }

    /**
     * Làm sạch từng dòng dữ liệu từ văn bản PDF.
     * Hàm thay thế xuống dòng trong quotes bằng khoảng trắng để giữ cấu trúc CSV.
     *
     * @param line Dòng văn bản cần làm sạch
     * @return Dòng văn bản đã làm sạch
     */
    private String cleanDataLine(String line) {
        // Thay thế xuống dòng trong quotes bằng khoảng trắng
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                result.append(c);
            } else if (c == '\n' || c == '\r') {
                if (inQuotes) {
                    result.append(' '); // Thay xuống dòng bằng khoảng trắng trong quotes
                }
                // Bỏ qua xuống dòng ngoài quotes
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Lấy giá trị chuỗi từ CSVRecord, làm sạch và chuẩn hóa giá trị.
     *
     * @param record Bản ghi CSV
     * @param columnName Tên cột
     * @return Giá trị chuỗi đã làm sạch hoặc null
     */
    private String getCleanValue(CSVRecord record, String columnName) {
        try {
            if (record.isMapped(columnName)) {
                String value = record.get(columnName);
                if (value != null && !value.trim().isEmpty()) {
                    return value.replaceAll("\\s+", " ").trim();
                }
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy giá trị cột " + columnName + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Parse giá trị Double từ CSVRecord.
     *
     * @param record Bản ghi CSV
     * @param columnName Tên cột
     * @return Giá trị Double hoặc null
     */
    private Double parseDoubleFromRecord(CSVRecord record, String columnName) {
        String value = getCleanValue(record, columnName);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("Cảnh báo: Không thể parse " + columnName + " = " + value);
            }
        }
        return null;
    }

    /**
     * Parse giá trị Byte từ CSVRecord.
     *
     * @param record Bản ghi CSV
     * @param columnName Tên cột
     * @return Giá trị Byte hoặc null
     */
    private Byte parseByteFromRecord(CSVRecord record, String columnName) {
        String value = getCleanValue(record, columnName);
        if (value != null) {
            try {
                return Byte.parseByte(value);
            } catch (NumberFormatException e) {
                System.out.println("Cảnh báo: Không thể parse " + columnName + " = " + value);
            }
        }
        return null;
    }

    /**
     * Parse giá trị Boolean từ CSVRecord, hỗ trợ cả dạng chuỗi ("true"/"false").
     *
     * @param record Bản ghi CSV
     * @param columnName Tên cột
     * @return Giá trị Boolean hoặc null
     */
    private Boolean parseBooleanFromRecord(CSVRecord record, String columnName) {
        String value = getCleanValue(record, columnName);
        if (value != null) {
            String normalized = value.toLowerCase();
            if ("true".equals(normalized) || "1".equals(normalized)) {
                return true;
            } else if ("false".equals(normalized) || "0".equals(normalized)) {
                return false;
            }
        }
        return null;
    }

    /**
     * Xử lý ID loại bất động sản từ CSVRecord, kiểm tra tính hợp lệ và gán vào PropertyRequest.
     *
     * @param request Đối tượng PropertyRequest
     * @param record Bản ghi CSV
     * @param rowNum Số dòng hiện tại
     * @param errors Danh sách lỗi
     * @return true nếu ID hợp lệ và được gán, false nếu không
     */
    private boolean handlePropertyTypeId(PropertyRequest request, CSVRecord record, int rowNum, List<String> errors) {
        try {
            String propertyTypeIdStr = getCleanValue(record, "propertyTypeId");
            if (propertyTypeIdStr != null) {
                Integer propertyTypeId = Integer.parseInt(propertyTypeIdStr);
                PropertyType propertyType = propertyTypeService.getPropertyTypeById(propertyTypeId);
                if (propertyType != null) {
                    request.setPropertyType(propertyType);
                    return true;
                } else {
                    errors.add("Dòng " + rowNum + ": Không tìm thấy loại bất động sản với ID: " + propertyTypeId);
                }
            } else {
                errors.add("Dòng " + rowNum + ": ID loại bất động sản không được để trống");
            }
        } catch (NumberFormatException e) {
            errors.add("Dòng " + rowNum + ": ID loại bất động sản không hợp lệ: " + getCleanValue(record, "propertyTypeId"));
        } catch (Exception e) {
            errors.add("Dòng " + rowNum + ": Lỗi xử lý ID loại bất động sản: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xử lý ID dự án từ CSVRecord, kiểm tra và gán nếu hợp lệ (dự án là tùy chọn).
     *
     * @param request Đối tượng PropertyRequest
     * @param record Bản ghi CSV
     * @param rowNum Số dòng hiện tại
     * @param errors Danh sách lỗi
     */
    private void handleProjectId(PropertyRequest request, CSVRecord record, int rowNum, List<String> errors) {
        try {
            String projectIdStr = getCleanValue(record, "projectId");
            if (projectIdStr != null) {
                Integer projectId = Integer.parseInt(projectIdStr);
                Project project = projectService.getProjectById(projectId);
                if (project != null) {
                    request.setProject(project);
                } else {
                    System.out.println("Cảnh báo dòng " + rowNum + ": Không tìm thấy dự án với ID: " + projectId);
                }
            }
        } catch (Exception e) {
            System.out.println("Cảnh báo dòng " + rowNum + ": Lỗi xử lý ID dự án: " + e.getMessage());
        }
    }

    /**
     * Xử lý ID chủ sở hữu từ CSVRecord, kiểm tra tính hợp lệ và gán vào PropertyRequest.
     *
     * @param request Đối tượng PropertyRequest
     * @param record Bản ghi CSV
     * @param rowNum Số dòng hiện tại
     * @param errors Danh sách lỗi
     * @return true nếu ID hợp lệ và được gán, false nếu không
     */
    private boolean handleOwnerId(PropertyRequest request, CSVRecord record, int rowNum, List<String> errors) {
        try {
            String ownerIdStr = getCleanValue(record, "ownerId");
            if (ownerIdStr != null) {
                Integer ownerId = Integer.parseInt(ownerIdStr);
                Customer owner = customerService.getCustomerById(ownerId);
                if (owner != null) {
                    request.setOwner(owner);
                    return true;
                } else {
                    errors.add("Dòng " + rowNum + ": Không tìm thấy chủ sở hữu với ID: " + ownerId);
                }
            } else {
                errors.add("Dòng " + rowNum + ": ID chủ sở hữu không được để trống");
            }
        } catch (NumberFormatException e) {
            errors.add("Dòng " + rowNum + ": ID chủ sở hữu không hợp lệ: " + getCleanValue(record, "ownerId"));
        } catch (Exception e) {
            errors.add("Dòng " + rowNum + ": Lỗi xử lý ID chủ sở hữu: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xử lý ID nhân viên phụ trách từ CSVRecord, kiểm tra tính hợp lệ và gán vào PropertyRequest.
     *
     * @param request Đối tượng PropertyRequest
     * @param record Bản ghi CSV
     * @param rowNum Số dòng hiện tại
     * @param errors Danh sách lỗi
     * @return true nếu ID hợp lệ và được gán, false nếu không
     */
    private boolean handleListingAgentId(PropertyRequest request, CSVRecord record, int rowNum, List<String> errors) {
        try {
            String listingAgentIdStr = getCleanValue(record, "listingAgentId");
            if (listingAgentIdStr != null) {
                Integer listingAgentId = Integer.parseInt(listingAgentIdStr);
                Employee listingAgent = employeeService.getEmployeeById(listingAgentId);
                if (listingAgent != null) {
                    request.setListingAgent(listingAgent);
                    return true;
                } else {
                    errors.add("Dòng " + rowNum + ": Không tìm thấy nhân viên với ID: " + listingAgentId);
                }
            } else {
                errors.add("Dòng " + rowNum + ": ID nhân viên không được để trống");
            }
        } catch (NumberFormatException e) {
            errors.add("Dòng " + rowNum + ": ID nhân viên không hợp lệ: " + getCleanValue(record, "listingAgentId"));
        } catch (Exception e) {
            errors.add("Dòng " + rowNum + ": Lỗi xử lý ID nhân viên: " + e.getMessage());
        }
        return false;
    }

    /**
     * Kiểm tra tính hợp lệ của PropertyRequest.
     * Hàm kiểm tra các trường bắt buộc (propertyCode, address, city, state, price, area, title,
     * propertyType, listingType, listingAgent, owner) và thêm lỗi vào BindingResult nếu vi phạm.
     *
     * @param request Đối tượng PropertyRequest cần kiểm tra
     * @param result BindingResult để lưu trữ lỗi
     */
    @Override
    public void validatePropertyRequest(PropertyRequest request, BindingResult result) {
        if (request.getPropertyCode() == null || request.getPropertyCode().trim().isEmpty()) {
            result.rejectValue("propertyCode", "error.propertyCode", "Mã bất động sản không được để trống.");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            result.rejectValue("address", "error.address", "Địa chỉ không được để trống.");
        }
        if (request.getCity() == null || request.getCity().trim().isEmpty()) {
            result.rejectValue("city", "error.city", "Thành phố không được để trống.");
        }
        if (request.getState() == null || request.getState().trim().isEmpty()) {
            result.rejectValue("state", "error.state", "Quận/Huyện không được để trống.");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            result.rejectValue("price", "error.price", "Giá phải lớn hơn 0.");
        }
        if (request.getArea() == null || request.getArea() <= 0) {
            result.rejectValue("area", "error.area", "Diện tích phải lớn hơn 0.");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            result.rejectValue("title", "error.title", "Tiêu đề không được để trống.");
        }
        if (request.getPropertyType() == null) {
            result.rejectValue("propertyType", "error.propertyType", "Loại bất động sản không được để trống.");
        }
        if (request.getListingType() == null || (!request.getListingType().equals("Bán") && !request.getListingType().equals("Cho thuê"))) {
            result.rejectValue("listingType", "error.listingType", "Loại giao dịch phải là 'Bán' hoặc 'Cho thuê'.");
        }
        if (request.getListingAgent() == null) {
            result.rejectValue("listingAgent", "error.listingAgent", "Nhân viên phụ trách không được để trống.");
        }
        if (request.getOwner() == null) {
            result.rejectValue("owner", "error.owner", "Chủ sở hữu không được để trống.");
        }
    }
}
