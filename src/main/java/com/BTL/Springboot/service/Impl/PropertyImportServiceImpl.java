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

    @NonFinal
    @Value("${ai.chatgpt.apiUrl}")
    private String CHATGPT_APIURL;

    @NonFinal
    @Value("${ai.chatgpt.apiKey}")
    private String CHATGPT_APIKEY;

    @NonFinal
    @Value("${ai.gemini.apiUrl}")
    private String GEMINI_APIURL;

    @NonFinal
    @Value("${ai.gemini.apiKey}")
    private String GEMINI_APIKEY;

    /**
     * Đọc nội dung mã QR từ file hình ảnh sử dụng thư viện BoofCV.
     * Hàm này kiểm tra định dạng file, kích thước, chuyển hình ảnh thành định dạng GrayU8,
     * và sử dụng QrCodeDetector để giải mã nội dung QR.
     *
     * @param file File hình ảnh chứa mã QR
     * @return Nội dung mã QR dưới dạng chuỗi
     * @throws Exception Nếu file không hợp lệ, kích thước quá lớn, hoặc không chứa mã QR
     */
    @Override
    public String readQRCode(MultipartFile file) throws Exception {
        try {
            // Kiểm tra file hợp lệ
            if (!file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("File phải là hình ảnh (jpg, png, ...).");
            }
            if (file.getSize() > 100 * 1024 * 1024) {
                throw new IllegalArgumentException("Hình ảnh mã QR không được vượt quá 100MB.");
            }

            // Đọc hình ảnh
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                throw new IllegalArgumentException("Không thể đọc hình ảnh mã QR. Vui lòng kiểm tra file upload.");
            }

            // Chuyển BufferedImage sang GrayU8 (định dạng BoofCV sử dụng)
            GrayU8 grayImage = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);

            // Khởi tạo QR Code Detector từ BoofCV
            ConfigQrCode config = new ConfigQrCode();
            QrCodeDetector<GrayU8> detector = FactoryFiducial.qrcode(config, GrayU8.class);

            // Thực hiện quét
            detector.process(grayImage);
            List<QrCode> qrCodes = detector.getDetections();

            if (!qrCodes.isEmpty()) {
                String qrContent = qrCodes.get(0).message;
                System.out.println("Nội dung mã QR thô: [" + qrContent + "]");
                return qrContent;
            }

            throw new IllegalArgumentException("Không tìm thấy mã QR trong hình ảnh. Vui lòng kiểm tra: (1) Hình ảnh có chứa mã QR rõ ràng; (2) Đảm bảo mã QR không bị mờ, méo mó hoặc ánh sáng phản chiếu.");

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Không thể giải mã QR. Vui lòng kiểm tra hình ảnh mã QR.";
            System.out.println("Lỗi giải mã QR: " + errorMessage);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    /**
     * Xác định định dạng nội dung của mã QR (JSON, CSV, URL, Base64, hoặc Multi-line).
     * Hàm kiểm tra chuỗi nội dung để quyết định định dạng dựa trên cấu trúc hoặc ký tự đặc trưng.
     *
     * @param qrContent Nội dung mã QR cần xác định định dạng
     * @return Chuỗi biểu thị định dạng: "JSON", "CSV", "URL", "BASE64_JSON", "BASE64_CSV",
     *         "BASE64_EXCEL", hoặc "MULTI_LINE"
     * @throws IllegalArgumentException Nếu nội dung trống hoặc không xác định được định dạng
     */
    @Override
    public String detectQRContentFormat(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung mã QR không được để trống.");
        }
        if (qrContent.startsWith("http://") || qrContent.startsWith("https://")) {
            return "URL";
        }
        if (qrContent.startsWith("[") || qrContent.startsWith("{")) {
            return "JSON";
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(qrContent);
            String decodedStr = new String(decoded, StandardCharsets.UTF_8);
            if (decodedStr.contains("|")) {
                return "BASE64_MULTI_LINE";
            }
            if (decodedStr.startsWith("[") || decodedStr.startsWith("{")) {
                return "BASE64_JSON";
            }
            if (decodedStr.contains(",")) {
                return "BASE64_CSV";
            }
            return "BASE64_EXCEL";
        } catch (IllegalArgumentException e) {
            if (qrContent.contains("|")) {
                return "MULTI_LINE";
            }
            if (qrContent.contains(",")) {
                return "CSV";
            }
            throw new IllegalArgumentException("Không thể xác định định dạng mã QR.", e);
        }
    }

    /**
     * Phân tích nội dung JSON từ mã QR thành danh sách PropertyRequest.
     * Hàm sử dụng ObjectMapper để chuyển chuỗi JSON thành danh sách PropertyQRData,
     * sau đó ánh xạ thành PropertyRequest.
     *
     * @param jsonContent Nội dung JSON từ mã QR
     * @return Danh sách PropertyRequest
     * @throws Exception Nếu JSON không hợp lệ hoặc có lỗi khi phân tích
     */
    @Override
    public List<PropertyRequest> parseJsonToProperties(String jsonContent) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<PropertyQRData> qrDataList = objectMapper.readValue(jsonContent, objectMapper.getTypeFactory().constructCollectionType(List.class, PropertyQRData.class));
            return mapQRDataToProperties(qrDataList);
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi khi phân tích dữ liệu JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Phân tích nội dung CSV từ mã QR thành danh sách PropertyRequest.
     * Hàm sử dụng CSVParser để đọc từng dòng CSV, ánh xạ thành PropertyQRData,
     * sau đó chuyển thành PropertyRequest.
     *
     * @param csvContent Nội dung CSV từ mã QR
     * @return Danh sách PropertyRequest
     * @throws Exception Nếu CSV không hợp lệ hoặc có lỗi khi phân tích
     */
    @Override
    public List<PropertyRequest> parseCsvToProperties(String csvContent) throws Exception {
        List<PropertyQRData> qrDataList = new ArrayList<>();
        try (CSVParser csvParser = CSVParser.parse(csvContent, CSVFormat.DEFAULT.withHeader().withTrim())) {
            for (CSVRecord record : csvParser) {
                PropertyQRData qrData = new PropertyQRData();
                qrData.setPropertyCode(record.isSet("propertyCode") ? record.get("propertyCode") : "");
                qrData.setPropertyTypeId(record.isSet("propertyTypeId") && !record.get("propertyTypeId").isEmpty() ? Integer.parseInt(record.get("propertyTypeId")) : null);
                qrData.setTitle(record.isSet("title") ? record.get("title") : "");
                qrData.setDescription(record.isSet("description") ? record.get("description") : "");
                qrData.setAddress(record.isSet("address") ? record.get("address") : "");
                qrData.setCity(record.isSet("city") ? record.get("city") : "");
                qrData.setState(record.isSet("state") ? record.get("state") : "");
                qrData.setPostalCode(record.isSet("postalCode") ? record.get("postalCode") : "");
                qrData.setPrice(record.isSet("price") && !record.get("price").isEmpty() ? Double.parseDouble(record.get("price")) : null);
                qrData.setArea(record.isSet("area") && !record.get("area").isEmpty() ? Double.parseDouble(record.get("area")) : null);
                qrData.setBedrooms(record.isSet("bedrooms") && !record.get("bedrooms").isEmpty() ? Byte.parseByte(record.get("bedrooms")) : null);
                qrData.setBathrooms(record.isSet("bathrooms") && !record.get("bathrooms").isEmpty() ? Byte.parseByte(record.get("bathrooms")) : null);
                qrData.setFloors(record.isSet("floors") && !record.get("floors").isEmpty() ? Byte.parseByte(record.get("floors")) : null);
                qrData.setYearBuilt(record.isSet("yearBuilt") ? record.get("yearBuilt") : "");
                qrData.setIsFurnished(record.isSet("isFurnished") && !record.get("isFurnished").isEmpty() ? Boolean.parseBoolean(record.get("isFurnished")) : null);
                qrData.setListingType(record.isSet("listingType") ? record.get("listingType") : "");
                qrData.setStatus(record.isSet("status") ? record.get("status") : "");
                qrData.setProjectId(record.isSet("projectId") && !record.get("projectId").isEmpty() ? Integer.parseInt(record.get("projectId")) : null);
                qrData.setOwnerId(record.isSet("ownerId") && !record.get("ownerId").isEmpty() ? Integer.parseInt(record.get("ownerId")) : null);
                qrData.setListingAgentId(record.isSet("listingAgentId") && !record.get("listingAgentId").isEmpty() ? Integer.parseInt(record.get("listingAgentId")) : null);
                qrData.setCreatedAt(record.isSet("createdAt") ? record.get("createdAt") : "");
                qrData.setUpdatedAt(record.isSet("updatedAt") ? record.get("updatedAt") : "");
                qrDataList.add(qrData);
            }
        }
        return mapQRDataToProperties(qrDataList);
    }

    /**
     * Phân tích nội dung dạng multi-line (dòng phân cách bởi dấu |) từ mã QR.
     * Hàm tách từng dòng, kiểm tra số lượng trường, ánh xạ thành PropertyQRData,
     * và chuyển thành PropertyRequest.
     *
     * @param multiLineContent Nội dung multi-line từ mã QR
     * @return Danh sách PropertyRequest
     * @throws Exception Nếu số trường không đúng hoặc dữ liệu không hợp lệ
     */
    @Override
    public List<PropertyRequest> parseMultiLineToProperties(String multiLineContent) throws Exception {
        List<PropertyQRData> qrDataList = new ArrayList<>();
        System.out.println("Nội dung QR thô: [" + multiLineContent + "]");

        multiLineContent = multiLineContent.trim();
        System.out.println("Nội dung QR sau khi làm sạch: [" + multiLineContent + "]");

        String[] lines = multiLineContent.split("\\r?\\n");
        System.out.println("Số dòng: " + lines.length);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                System.out.println("Bỏ qua dòng trống: [" + line + "]");
                continue;
            }
            System.out.println("Dòng xử lý: [" + line + "]");
            String[] fields = line.split("\\|", -1);
            if (fields.length != 22) {
                throw new IllegalArgumentException("Dòng " + (i + 1) + " không đúng số trường (yêu cầu 22, nhận được: " + fields.length + "): " + line);
            }

            PropertyQRData qrData = new PropertyQRData();
            try {
                qrData.setPropertyCode(fields[0].isEmpty() ? null : fields[0]);
                qrData.setPropertyTypeId(fields[1].isEmpty() ? null : Integer.parseInt(fields[1]));
                qrData.setTitle(fields[2].isEmpty() ? null : fields[2]);
                qrData.setDescription(fields[3].isEmpty() ? null : fields[3]);
                qrData.setAddress(fields[4].isEmpty() ? null : fields[4]);
                qrData.setCity(fields[5].isEmpty() ? null : fields[5]);
                qrData.setState(fields[6].isEmpty() ? null : fields[6]);
                qrData.setPostalCode(fields[7].isEmpty() ? null : fields[7]);
                qrData.setPrice(fields[8].isEmpty() ? null : Double.parseDouble(fields[8]));
                qrData.setArea(fields[9].isEmpty() ? null : Double.parseDouble(fields[9]));
                qrData.setBedrooms(fields[10].isEmpty() ? null : Byte.parseByte(fields[10]));
                qrData.setBathrooms(fields[11].isEmpty() ? null : Byte.parseByte(fields[11]));
                qrData.setFloors(fields[12].isEmpty() ? null : Byte.parseByte(fields[12]));
                qrData.setYearBuilt(fields[13].isEmpty() ? null : fields[13]);
                qrData.setIsFurnished(fields[14].isEmpty() ? null : Boolean.parseBoolean(fields[14]));
                qrData.setListingType(fields[15].isEmpty() ? null : fields[15]);
                qrData.setStatus(fields[16].isEmpty() ? null : fields[16]);
                qrData.setProjectId(fields[17].isEmpty() ? null : Integer.parseInt(fields[17]));
                qrData.setOwnerId(fields[18].isEmpty() ? null : Integer.parseInt(fields[18]));
                qrData.setListingAgentId(fields[19].isEmpty() ? null : Integer.parseInt(fields[19]));
                qrData.setCreatedAt(fields[20].isEmpty() ? null : fields[20]);
                qrData.setUpdatedAt(fields[21].isEmpty() ? null : fields[21]);
                qrDataList.add(qrData);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Dòng " + (i + 1) + " chứa giá trị số không hợp lệ: " + line, e);
            }
        }
        return mapQRDataToProperties(qrDataList);
    }

    /**
     * Xử lý nội dung mã QR chứa URL, tải nội dung từ URL và phân tích thành PropertyRequest.
     * Hàm kiểm tra đuôi URL (.json hoặc .csv) và gọi hàm phân tích tương ứng.
     *
     * @param url URL từ mã QR
     * @return Danh sách PropertyRequest
     * @throws Exception Nếu URL không hợp lệ hoặc không tải được nội dung
     */
    @Override
    public List<PropertyRequest> handleUrlContent(String url) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                String content = EntityUtils.toString(response.getEntity());
                if (url.endsWith(".json")) {
                    return parseJsonToProperties(content);
                } else if (url.endsWith(".csv")) {
                    return parseCsvToProperties(content);
                } else {
                    throw new IllegalArgumentException("URL phải trỏ đến file JSON hoặc CSV.");
                }
            }
        }
    }

    /**
     * Xử lý nội dung mã QR mã hóa Base64, giải mã và phân tích theo định dạng tương ứng.
     * Hàm giải mã Base64, xác định định dạng nội dung (JSON, CSV, multi-line, Excel)
     * và gọi hàm phân tích phù hợp.
     *
     * @param base64Content Nội dung Base64 từ mã QR
     * @param format Định dạng nội dung (BASE64_JSON, BASE64_CSV, BASE64_MULTI_LINE, BASE64_EXCEL)
     * @return Danh sách PropertyRequest
     * @throws Exception Nếu Base64 không hợp lệ hoặc định dạng không được hỗ trợ
     */
    @Override
    public List<PropertyRequest> handleBase64Content(String base64Content, String format) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64Content);
        String decodedContent = new String(decoded, StandardCharsets.UTF_8);
        switch (format) {
            case "BASE64_JSON":
                return parseJsonToProperties(decodedContent);
            case "BASE64_CSV":
                return parseCsvToProperties(decodedContent);
            case "BASE64_MULTI_LINE":
                return parseMultiLineToProperties(decodedContent);
            case "BASE64_EXCEL":
                return readExcelFromBytes(decoded);
            default:
                throw new IllegalArgumentException("Định dạng Base64 không được hỗ trợ: " + format);
        }
    }

    /**
     * Ánh xạ danh sách PropertyQRData thành danh sách PropertyRequest.
     * Hàm chuyển đổi từng PropertyQRData thành PropertyRequest, kiểm tra tính hợp lệ
     * của các ID (propertyType, project, owner, listingAgent) và định dạng ngày tháng.
     *
     * @param qrDataList Danh sách PropertyQRData
     * @return Danh sách PropertyRequest
     * @throws Exception Nếu có lỗi trong quá trình ánh xạ (ID không tồn tại, định dạng sai)
     */
    private List<PropertyRequest> mapQRDataToProperties(List<PropertyQRData> qrDataList) throws Exception {
        List<PropertyRequest> properties = new ArrayList<>();
        for (PropertyQRData qrData : qrDataList) {
            PropertyRequest request = new PropertyRequest();

            request.setPropertyCode(qrData.getPropertyCode());

            if (qrData.getPropertyTypeId() != null) {
                PropertyType propertyType = propertyTypeService.getPropertyTypeById(qrData.getPropertyTypeId());
                if (propertyType == null) {
                    throw new IllegalArgumentException("Loại bất động sản không tồn tại: " + qrData.getPropertyTypeId());
                }
                request.setPropertyType(propertyType);
            }

            request.setTitle(qrData.getTitle());
            request.setDescription(qrData.getDescription());
            request.setAddress(qrData.getAddress());
            request.setCity(qrData.getCity());
            request.setState(qrData.getState());
            request.setPostalCode(qrData.getPostalCode());
            request.setPrice(qrData.getPrice());
            request.setArea(qrData.getArea());
            request.setBedrooms(qrData.getBedrooms());
            request.setBathrooms(qrData.getBathrooms());
            request.setFloors(qrData.getFloors());

            if (qrData.getYearBuilt() != null && !qrData.getYearBuilt().isEmpty()) {
                try {
                    LocalDate localDate = LocalDate.parse(qrData.getYearBuilt(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    request.setYearBuilt(java.sql.Date.valueOf(localDate));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Năm xây dựng không đúng định dạng: " + qrData.getYearBuilt());
                }
            }

            request.setIsFurnished(qrData.getIsFurnished());
            request.setListingType(qrData.getListingType());
            request.setStatus(qrData.getStatus());

            if (qrData.getProjectId() != null) {
                Project project = projectService.getProjectById(qrData.getProjectId());
                if (project == null) {
                    throw new IllegalArgumentException("Dự án không tồn tại: " + qrData.getProjectId());
                }
                request.setProject(project);
            }

            if (qrData.getOwnerId() != null) {
                Customer owner = customerService.getCustomerById(qrData.getOwnerId());
                if (owner == null) {
                    throw new IllegalArgumentException("Chủ sở hữu không tồn tại: " + qrData.getOwnerId());
                }
                request.setOwner(owner);
            }

            if (qrData.getListingAgentId() != null) {
                Employee listingAgent = employeeService.getEmployeeById(qrData.getListingAgentId());
                if (listingAgent == null) {
                    throw new IllegalArgumentException("Nhân viên phụ trách không tồn tại: " + qrData.getListingAgentId());
                }
                request.setListingAgent(listingAgent);
            }

            if (qrData.getCreatedAt() != null && !qrData.getCreatedAt().trim().isEmpty()) {
                try {
                    request.setCreatedAt(LocalDateTime.parse(qrData.getCreatedAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Ngày tạo không đúng định dạng: " + qrData.getCreatedAt());
                }
            }

            if (qrData.getUpdatedAt() != null && !qrData.getUpdatedAt().trim().isEmpty()) {
                try {
                    request.setUpdatedAt(LocalDateTime.parse(qrData.getUpdatedAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Ngày cập nhật không đúng định dạng: " + qrData.getUpdatedAt());
                }
            }

            properties.add(request);
        }
        return properties;
    }

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
     * Tạo danh sách PropertyRequest bằng cách gọi API AI hoặc dữ liệu dự phòng.
     * Hàm tạo dữ liệu bất động sản dựa trên thông tin địa phương (tỉnh, quận),
     * loại bất động sản, và gọi API AI để sinh dữ liệu chi tiết.
     *
     * @param quantity Số lượng bất động sản cần tạo
     * @return Danh sách PropertyRequest
     * @throws IllegalStateException Nếu danh sách dữ liệu hệ thống rỗng hoặc API không khả dụng
     */
    @Override
    public List<PropertyRequest> generateAIProperties(int quantity) {
        List<PropertyRequest> properties = new ArrayList<>();
        Random random = new Random();

        // Danh sách tỉnh/thành Việt Nam (mở rộng để thực tế hơn)
        List<String> provinces = Arrays.asList(
                "Hà Nội", "TP Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
                "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu",
                "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước",
                "Bình Thuận", "Cà Mau", "Cao Bằng", "Đắk Lắk", "Đắk Nông",
                "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang",
                "Hà Nam", "Hà Tĩnh", "Hải Dương", "Hậu Giang", "Hòa Bình",
                "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu",
                "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định",
                "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Quảng Bình",
                "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng",
                "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa",
                "Thừa Thiên Huế", "Tiền Giang", "Trà Vinh", "Tuyên Quang",
                "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
        );

        // Danh sách quận/huyện mẫu
        Map<String, List<String>> provinceDistricts = new HashMap<>();
        provinceDistricts.put("Hà Nội", Arrays.asList("Ba Đình", "Hoàn Kiếm", "Cầu Giấy", "Đống Đa", "Hai Bà Trưng", "Thanh Xuân"));
        provinceDistricts.put("TP Hồ Chí Minh", Arrays.asList("Quận 1", "Quận 3", "Quận 7", "Bình Thạnh", "Tân Bình", "Gò Vấp"));
        provinceDistricts.put("Đà Nẵng", Arrays.asList("Hải Châu", "Thanh Khê", "Sơn Trà", "Ngũ Hành Sơn"));
        provinceDistricts.put("Hải Phòng", Arrays.asList("Hồng Bàng", "Ngô Quyền", "Lê Chân", "Hải An"));
        provinceDistricts.put("Cần Thơ", Arrays.asList("Ninh Kiều", "Bình Thủy", "Cái Răng"));
        provinces.forEach(p -> provinceDistricts.computeIfAbsent(p, k -> Arrays.asList("TT. " + k, "Huyện A", "Huyện B")));

        List<String> listingTypes = Arrays.asList("Bán", "Cho thuê");

        // Lấy danh sách DTO
        List<PropertyTypeDto> propertyTypeDtos = propertyTypeService.getAllPropertyTypes();
        List<EmployeeDto> employeeDtos = employeeService.getAllEmployees();
        List<CustomerDto> customerDtos = customerService.getAllCustomer();
        List<ProjectDto> projectDtos = projectService.getAllProjects();

        // Kiểm tra danh sách rỗng
        if (propertyTypeDtos.isEmpty() || employeeDtos.isEmpty() || customerDtos.isEmpty()) {
            throw new IllegalStateException("Danh sách loại bất động sản, nhân viên, hoặc khách hàng rỗng.");
        }

        // Chuyển DTO thành Entity
        List<PropertyType> propertyTypes = propertyTypeDtos.stream()
                .map(dto -> propertyTypeService.getPropertyTypeById(dto.getTypeId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Employee> listingAgents = employeeDtos.stream()
                .map(dto -> employeeService.getEmployeeById(dto.getEmployeeId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Customer> owners = customerDtos.stream()
                .map(dto -> customerService.getCustomerById(dto.getCustomerId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Project> projects = projectDtos.stream()
                .map(dto -> projectService.getProjectById(dto.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (propertyTypes.isEmpty() || listingAgents.isEmpty() || owners.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy thực thể hợp lệ cho PropertyType, Employee, hoặc Customer.");
        }

        // Cấu hình API với key cố định
        Map<String, ApiRequest> apiRequests = new HashMap<>();
        apiRequests.put("ChatGPT", new ApiRequest(CHATGPT_APIURL, CHATGPT_APIKEY));
        apiRequests.put("Gemini", new ApiRequest(GEMINI_APIURL, GEMINI_APIKEY));

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> availableApis = Arrays.asList("ChatGPT", "Gemini");

        if (availableApis.isEmpty()) {
            throw new IllegalStateException("Không có API key nào được cấu hình.");
        }

        for (int i = 0; i < quantity; i++) {
            PropertyRequest request = new PropertyRequest();
            request.setPropertyCode(generateSafePropertyCode(i + 1));

            // Chọn ngẫu nhiên thông tin cơ bản
            String province = provinces.get(random.nextInt(provinces.size()));
            String district = provinceDistricts.get(province).get(random.nextInt(provinceDistricts.get(province).size()));
            String listingType = listingTypes.get(random.nextInt(listingTypes.size()));
            PropertyType propertyType = propertyTypes.get(random.nextInt(propertyTypes.size()));

            boolean aiSuccess = false;

            // Thử gọi API theo thứ tự ưu tiên: ChatGPT -> Gemini
            String prompt = createDetailedPrompt(province, district, listingType, propertyType.getTypeName());

            for (String apiName : availableApis) {
                ApiRequest apiRequest = apiRequests.get(apiName);
                try {
                    String aiResponse = callAIAPI(apiName, apiRequest, prompt, restTemplate, objectMapper);
                    if (aiResponse != null) {
                        aiSuccess = parseAIResponse(request, aiResponse, objectMapper);
                        if (aiSuccess) {
                            System.out.println("Thành công tạo bất động sản " + (i + 1) + " bằng " + apiName);
                            break; // Thoát vòng lặp nếu API thành công
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi gọi API " + apiName + " cho bất động sản " + (i + 1) + ": " + e.getMessage());
                }
            }

            // Nếu cả hai API đều thất bại, tạo dữ liệu dự phòng
            if (!aiSuccess) {
                generateRealisticPropertyData(request, province, district, listingType, propertyType, random);
                System.out.println("Tạo bất động sản " + (i + 1) + " bằng dữ liệu dự phòng");
            }

            // Gán các trường hệ thống
            request.setCity(province);
            request.setState(district);
            request.setPropertyType(propertyType);
            request.setListingType(listingType);
            request.setListingAgent(listingAgents.get(random.nextInt(listingAgents.size())));
            request.setOwner(owners.get(random.nextInt(owners.size())));
            request.setProject(projects.isEmpty() ? null : projects.get(random.nextInt(projects.size())));
            request.setStatus("true");
            request.setCreatedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());

            properties.add(request);
        }

        System.out.println("Hoàn thành tạo " + quantity + " bất động sản");
        return properties;
    }

    /**
     * Tạo prompt chi tiết để gửi đến API AI.
     * Prompt yêu cầu API tạo dữ liệu bất động sản với các thông tin cụ thể (địa chỉ, giá, diện tích,...)
     * theo định dạng JSON.
     *
     * @param province Tỉnh/thành phố
     * @param district Quận/huyện
     * @param listingType Loại giao dịch (Bán/Cho thuê)
     * @param propertyType Tên loại bất động sản
     * @return Chuỗi prompt dạng JSON
     */
    private String createDetailedPrompt(String province, String district, String listingType, String propertyType) {
        return String.format(
                "Generate a realistic property listing in Vietnam for a %s in %s, %s, for %s. " +
                        "Provide the following details in JSON format:\n" +
                        "{\n" +
                        "  \"address\": \"A specific street address in %s, %s (e.g., '123 Nguyễn Huệ, %s')\",\n" +
                        "  \"title\": \"A descriptive title (e.g., 'Modern %s in %s')\",\n" +
                        "  \"description\": \"A detailed description (80-120 words, realistic for Vietnam's market, mention nearby amenities like markets, schools, or hospitals)\",\n" +
                        "  \"price\": \"A realistic price in VND (%s, return as an integer)\",\n" +
                        "  \"area\": \"A realistic area in square meters (%s, round to 2 decimal places)\",\n" +
                        "  \"bedrooms\": \"Number of bedrooms (between 1 and %d)\",\n" +
                        "  \"bathrooms\": \"Number of bathrooms (between 1 and %d)\",\n" +
                        "  \"floors\": \"Number of floors (between 1 and %d, or null for apartments)\",\n" +
                        "  \"yearBuilt\": \"Year built (2000-2025)\",\n" +
                        "  \"isFurnished\": \"true or false\",\n" +
                        "  \"postalCode\": \"A 5-digit postal code for %s (e.g., '70000')\"\n" +
                        "}\n" +
                        "Return only JSON, no additional text.",
                propertyType, province, district, listingType,
                province, district, district,
                propertyType, province,
                listingType.equals("Bán")
                        ? "between 1,000,000,000 and 100,000,000,000, based on property type"
                        : "between 5,000,000 and 200,000,000 per month, based on property type",
                propertyType.toLowerCase().contains("apartment") ? "30-150" : "50-500",
                propertyType.toLowerCase().contains("villa") ? 6 : 4,
                propertyType.toLowerCase().contains("villa") ? 5 : 3,
                propertyType.toLowerCase().contains("apartment") ? 1 : 5,
                province
        );
    }

    /**
     * Gọi API AI (ChatGPT hoặc Gemini) để tạo dữ liệu bất động sản.
     * Hàm xây dựng request HTTP, gửi đến API, và trích xuất nội dung JSON từ phản hồi.
     *
     * @param apiName Tên API (ChatGPT hoặc Gemini)
     * @param apiRequest Thông tin API (URL và key)
     * @param prompt Prompt gửi đến API
     * @param restTemplate Đối tượng RestTemplate để gửi request
     * @param objectMapper Đối tượng để parse JSON
     * @return Nội dung JSON từ API
     * @throws Exception Nếu API không hợp lệ hoặc gặp lỗi
     */
    private String callAIAPI(String apiName, ApiRequest apiRequest, String prompt,
                             RestTemplate restTemplate, ObjectMapper objectMapper) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity;
        String apiUrl = apiRequest.apiUrl;

        switch (apiName) {
            case "ChatGPT":
                headers.setBearerAuth(apiRequest.apiKey);
                Map<String, Object> chatgptRequest = new HashMap<>();
                chatgptRequest.put("model", "gpt-3.5-turbo");
                chatgptRequest.put("max_tokens", 1000);
                chatgptRequest.put("temperature", 0.7);
                chatgptRequest.put("messages", List.of(Map.of("role", "user", "content", prompt)));

                entity = new HttpEntity<>(objectMapper.writeValueAsString(chatgptRequest), headers);
                ResponseEntity<String> chatgptResponse = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
                JsonNode chatgptRoot = objectMapper.readTree(chatgptResponse.getBody());
                return chatgptRoot.path("choices").get(0).path("message").path("content").asText();

            case "Gemini":
                apiUrl += "?key=" + apiRequest.apiKey;
                Map<String, Object> geminiRequest = new HashMap<>();
                geminiRequest.put("contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )));
                geminiRequest.put("generationConfig", Map.of(
                        "maxOutputTokens", 1000,
                        "temperature", 0.7
                ));

                entity = new HttpEntity<>(objectMapper.writeValueAsString(geminiRequest), headers);
                ResponseEntity<String> geminiResponse = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
                JsonNode geminiRoot = objectMapper.readTree(geminiResponse.getBody());
                return geminiRoot.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            default:
                throw new IllegalArgumentException("API không được hỗ trợ: " + apiName);
        }
    }

    /**
     * Parse phản hồi JSON từ API AI thành PropertyRequest.
     * Hàm trích xuất các trường từ JSON và gán vào PropertyRequest, kiểm tra tính hợp lệ.
     *
     * @param request Đối tượng PropertyRequest để gán dữ liệu
     * @param aiResponse Phản hồi JSON từ API
     * @param objectMapper Đối tượng để parse JSON
     * @return true nếu parse thành công, false nếu không
     */
    private boolean parseAIResponse(PropertyRequest request, String aiResponse, ObjectMapper objectMapper) {
        try {
            aiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
            JsonNode propertyData = objectMapper.readTree(aiResponse);

            if (propertyData.path("address").isMissingNode() || propertyData.path("title").isMissingNode() ||
                    propertyData.path("price").isMissingNode() || propertyData.path("area").isMissingNode()) {
                return false;
            }

            request.setAddress(propertyData.path("address").asText());
            request.setTitle(propertyData.path("title").asText());
            request.setDescription(propertyData.path("description").asText());
            request.setPrice((double) propertyData.path("price").asLong());
            request.setArea(BigDecimal.valueOf(propertyData.path("area").asDouble())
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());
            request.setBedrooms((byte) (propertyData.path("bedrooms").isMissingNode() ? 1 : propertyData.path("bedrooms").asInt()));
            request.setBathrooms((byte) (propertyData.path("bathrooms").isMissingNode() ? 1 : propertyData.path("bathrooms").asInt()));
            if (!propertyData.path("floors").isMissingNode() && !propertyData.path("floors").isNull()) {
                request.setFloors((byte) propertyData.path("floors").asInt());
            }
            if (!propertyData.path("yearBuilt").isMissingNode()) {
                int year = propertyData.path("yearBuilt").asInt();
                if (year >= 2000 && year <= 2025) {
                    request.setYearBuilt(java.sql.Date.valueOf(LocalDate.of(year, 1, 1)));
                }
            }
            request.setIsFurnished(propertyData.path("isFurnished").asBoolean(false));
            request.setPostalCode(propertyData.path("postalCode").isMissingNode()
                    ? String.format("%05d", 70000 + new Random().nextInt(10000))
                    : propertyData.path("postalCode").asText());

            return true;
        } catch (Exception e) {
            System.err.println("Lỗi parse AI response: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tạo dữ liệu dự phòng cho bất động sản khi API AI thất bại.
     * Hàm sinh dữ liệu thực tế dựa trên tỉnh, quận, loại bất động sản và các tham số ngẫu nhiên.
     *
     * @param request Đối tượng PropertyRequest để gán dữ liệu
     * @param province Tỉnh/thành phố
     * @param district Quận/huyện
     * @param listingType Loại giao dịch
     * @param propertyType Loại bất động sản
     * @param random Đối tượng Random để sinh dữ liệu ngẫu nhiên
     */
    private void generateRealisticPropertyData(PropertyRequest request, String province, String district,
                                               String listingType, PropertyType propertyType, Random random) {
        // Địa chỉ dự phòng
        String street = "Đường " + randomAlphabetic(5);
        request.setAddress(String.format("Số %d, %s, %s", random.nextInt(200) + 1, street, district));
        request.setTitle(String.format("%s %s tại %s, %s", propertyType.getTypeName(), listingType, district, province));
        request.setDescription(String.format(
                "%s hiện đại tại %s, %s, gần chợ, trường học, và bệnh viện. Thiết kế tiện nghi, phù hợp cho gia đình hoặc đầu tư.",
                propertyType.getTypeName(), district, province
        ));

        // Giá cả (số nguyên, chuyển sang double)
        long price;
        if (listingType.equals("Bán")) {
            if (propertyType.getTypeName().toLowerCase().contains("apartment")) {
                price = 2_000_000_000L + (long) (random.nextDouble() * 8_000_000_000L);
            } else if (propertyType.getTypeName().toLowerCase().contains("villa")) {
                price = 15_000_000_000L + (long) (random.nextDouble() * 35_000_000_000L);
            } else {
                price = 5_000_000_000L + (long) (random.nextDouble() * 15_000_000_000L);
            }
        } else {
            if (propertyType.getTypeName().toLowerCase().contains("apartment")) {
                price = 10_000_000L + (long) (random.nextDouble() * 40_000_000L);
            } else if (propertyType.getTypeName().toLowerCase().contains("villa")) {
                price = 50_000_000L + (long) (random.nextDouble() * 100_000_000L);
            } else {
                price = 15_000_000L + (long) (random.nextDouble() * 65_000_000L);
            }
        }
        request.setPrice((double) price);

        // Diện tích (làm tròn 2 chữ số)
        double area = propertyType.getTypeName().toLowerCase().contains("apartment")
                ? 30 + random.nextDouble() * 120
                : 50 + random.nextDouble() * 450;
        request.setArea(BigDecimal.valueOf(area).setScale(2, RoundingMode.HALF_UP).doubleValue());

        request.setBedrooms((byte) (1 + random.nextInt(propertyType.getTypeName().toLowerCase().contains("villa") ? 6 : 4)));
        request.setBathrooms((byte) (1 + random.nextInt(propertyType.getTypeName().toLowerCase().contains("villa") ? 5 : 3)));
        if (!propertyType.getTypeName().toLowerCase().contains("apartment")) {
            request.setFloors((byte) (1 + random.nextInt(5)));
        }
        request.setYearBuilt(Date.valueOf(LocalDate.of(2000 + random.nextInt(26), 1, 1)));
        request.setIsFurnished(random.nextBoolean());
        request.setPostalCode(String.format("%05d", 70000 + random.nextInt(10000)));
    }

    /**
     * Tạo mã property_code an toàn, duy nhất dựa trên thời gian và số thứ tự.
     *
     * @param sequence Số thứ tự của bất động sản
     * @return Mã property_code (tối đa 20 ký tự)
     */
    private String generateSafePropertyCode(int sequence) {
        long currentTime = System.currentTimeMillis();
        String timeStr = String.valueOf(currentTime).substring(5);
        String seqStr = String.format("%03d", sequence);
        String propertyCode = "AI" + timeStr + seqStr;
        return propertyCode.length() > 20 ? propertyCode.substring(0, 20) : propertyCode;
    }

    /**
     * Sinh chuỗi ngẫu nhiên gồm các ký tự chữ cái.
     *
     * @param length Độ dài chuỗi
     * @return Chuỗi ngẫu nhiên
     */
    private String randomAlphabetic(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
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
