//package com.BTL.Springboot.service.Impl;
//
//import boofcv.abst.fiducial.QrCodeDetector;
//import boofcv.alg.fiducial.qrcode.QrCode;
//import boofcv.factory.fiducial.ConfigQrCode;
//import boofcv.factory.fiducial.FactoryFiducial;
//import boofcv.io.image.ConvertBufferedImage;
//import boofcv.struct.image.GrayU8;
//import com.BTL.Springboot.entity.Employee;
//import com.BTL.Springboot.service.EmployeeImportService;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class EmployeeImportServiceImpl implements EmployeeImportService {
//
//    // Custom deserializer để hỗ trợ nhiều định dạng ngày tháng
//    public static class FlexibleLocalDateDeserializer extends JsonDeserializer<LocalDate> {
//        private static final List<DateTimeFormatter> FORMATTERS = List.of(
//                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
//                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
//                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
//                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
//                DateTimeFormatter.ofPattern("MM-dd-yyyy")
//        );
//
//        @Override
//        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//            String dateString = p.getValueAsString();
//
//            for (DateTimeFormatter formatter : FORMATTERS) {
//                try {
//                    return LocalDate.parse(dateString, formatter);
//                } catch (DateTimeParseException e) {
//                    // Thử formatter tiếp theo
//                    continue;
//                }
//            }
//
//            throw new IOException("Không thể parse ngày tháng: " + dateString +
//                    ". Các định dạng được hỗ trợ: dd/MM/yyyy, MM/dd/yyyy, yyyy-MM-dd, dd-MM-yyyy, MM-dd-yyyy");
//        }
//    }
//
//    @Override
//    public String readQRCode(MultipartFile file) throws Exception {
//        try {
//            if (!file.getContentType().startsWith("image/")) {
//                throw new IllegalArgumentException("File phải là hình ảnh (jpg, png, ...).");
//            }
//            if (file.getSize() > 100 * 1024 * 1024) {
//                throw new IllegalArgumentException("Hình ảnh mã QR không được vượt quá 100MB.");
//            }
//
//            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
//            if (image == null) {
//                throw new IllegalArgumentException("Không thể đọc hình ảnh mã QR. Vui lòng kiểm tra file upload.");
//            }
//
//            GrayU8 grayImage = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
//            ConfigQrCode config = new ConfigQrCode();
//            QrCodeDetector<GrayU8> detector = FactoryFiducial.qrcode(config, GrayU8.class);
//
//            detector.process(grayImage);
//            List<QrCode> qrCodes = detector.getDetections();
//
//            if (!qrCodes.isEmpty()) {
//                return qrCodes.get(0).message;
//            }
//
//            throw new IllegalArgumentException("Không tìm thấy mã QR trong hình ảnh.");
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Không thể giải mã QR: " + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public String detectQRContentFormat(String qrContent) {
//        if (qrContent == null || qrContent.trim().isEmpty()) {
//            throw new IllegalArgumentException("Nội dung mã QR không được để trống.");
//        }
//        if (qrContent.startsWith("http://") || qrContent.startsWith("https://")) {
//            return "URL";
//        }
//        if (qrContent.startsWith("[") || qrContent.startsWith("{")) {
//            return "JSON";
//        }
//        if (qrContent.contains("|")) {
//            return "MULTI_LINE";
//        }
//        if (qrContent.contains(",")) {
//            return "CSV";
//        }
//        throw new IllegalArgumentException("Định dạng mã QR không được hỗ trợ.");
//    }
//
//    @Override
//    public List<Employee> parseJsonToEmployees(String jsonContent) throws Exception {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            // Đăng ký module hỗ trợ Java 8 date/time với custom deserializer
//            JavaTimeModule javaTimeModule = new JavaTimeModule();
//            // Sử dụng custom deserializer hỗ trợ nhiều định dạng ngày tháng
//            javaTimeModule.addDeserializer(LocalDate.class, new FlexibleLocalDateDeserializer());
//            objectMapper.registerModule(javaTimeModule);
//            // Bỏ qua các thuộc tính không xác định
//            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//            List<Employee> employees = objectMapper.readValue(jsonContent,
//                    objectMapper.getTypeFactory().constructCollectionType(List.class, Employee.class));
//            return employees;
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Lỗi khi phân tích JSON: " + e.getMessage(), e);
//        }
//    }
//
//    // Phương thức helper để parse ngày tháng linh hoạt
//    private LocalDate parseFlexibleDate(String dateString) {
//        if (dateString == null || dateString.trim().isEmpty()) {
//            return null;
//        }
//
//        List<DateTimeFormatter> formatters = List.of(
//                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
//                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
//                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
//                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
//                DateTimeFormatter.ofPattern("MM-dd-yyyy")
//        );
//
//        for (DateTimeFormatter formatter : formatters) {
//            try {
//                return LocalDate.parse(dateString.trim(), formatter);
//            } catch (DateTimeParseException e) {
//                // Thử formatter tiếp theo
//                continue;
//            }
//        }
//
//        throw new IllegalArgumentException("Không thể parse ngày tháng: " + dateString +
//                ". Các định dạng được hỗ trợ: dd/MM/yyyy, MM/dd/yyyy, yyyy-MM-dd, dd-MM-yyyy, MM-dd-yyyy");
//    }
//
//    @Override
//    public List<Employee> parseCsvToEmployees(String csvContent) throws Exception {
//        List<Employee> employees = new ArrayList<>();
//        try (CSVParser csvParser = CSVParser.parse(csvContent, CSVFormat.DEFAULT.withHeader().withTrim())) {
//            for (CSVRecord record : csvParser) {
//                Employee employee = new Employee();
//                employee.setFirstName(record.isSet("firstName") ? record.get("firstName") : "");
//                employee.setLastName(record.isSet("lastName") ? record.get("lastName") : "");
//                employee.setPosition(record.isSet("position") ? record.get("position") : "");
//                employee.setEmail(record.isSet("email") ? record.get("email") : "");
//                employee.setPhone(record.isSet("phone") ? record.get("phone") : "");
//
//                // Sử dụng phương thức parse linh hoạt cho CSV
//                if (record.isSet("hireDate") && !record.get("hireDate").isEmpty()) {
//                    employee.setHireDate(parseFlexibleDate(record.get("hireDate")));
//                }
//                if (record.isSet("salary") && !record.get("salary").isEmpty()) {
//                    employee.setSalary(Double.parseDouble(record.get("salary")));
//                }
//                employee.setCreatedAt(LocalDateTime.now());
//                employee.setUpdatedAt(LocalDateTime.now());
//                employee.setIsActive(true);
//                employees.add(employee);
//            }
//        }
//        return employees;
//    }
//
//    @Override
//    public List<Employee> parseMultiLineToEmployees(String multiLineContent) throws Exception {
//        List<Employee> employees = new ArrayList<>();
//        String[] lines = multiLineContent.trim().split("\\r?\\n");
//
//        for (int i = 0; i < lines.length; i++) {
//            String line = lines[i].trim();
//            if (line.isEmpty()) continue;
//
//            String[] fields = line.split("\\|", -1);
//            if (fields.length != 7) {
//                throw new IllegalArgumentException("Dòng " + (i + 1) + " không đúng số trường (yêu cầu 7): " + line);
//            }
//
//            Employee employee = new Employee();
//            employee.setFirstName(fields[0].isEmpty() ? null : fields[0]);
//            employee.setLastName(fields[1].isEmpty() ? null : fields[1]);
//            employee.setPosition(fields[2].isEmpty() ? null : fields[2]);
//            employee.setEmail(fields[3].isEmpty() ? null : fields[3]);
//            employee.setPhone(fields[4].isEmpty() ? null : fields[4]);
//
//            // Sử dụng phương thức parse linh hoạt cho Multi-line
//            if (!fields[5].isEmpty()) {
//                employee.setHireDate(parseFlexibleDate(fields[5]));
//            }
//            if (!fields[6].isEmpty()) {
//                employee.setSalary(Double.parseDouble(fields[6]));
//            }
//            employee.setCreatedAt(LocalDateTime.now());
//            employee.setUpdatedAt(LocalDateTime.now());
//            employee.setIsActive(true);
//            employees.add(employee);
//        }
//        return employees;
//    }
//
//    @Override
//    public List<Employee> handleUrlContent(String url) throws Exception {
//        try (CloseableHttpClient client = HttpClients.createDefault()) {
//            HttpGet request = new HttpGet(url);
//            try (CloseableHttpResponse response = client.execute(request)) {
//                String content = EntityUtils.toString(response.getEntity());
//                if (url.endsWith(".json")) {
//                    return parseJsonToEmployees(content);
//                } else if (url.endsWith(".csv")) {
//                    return parseCsvToEmployees(content);
//                } else {
//                    throw new IllegalArgumentException("URL phải trỏ đến file JSON hoặc CSV.");
//                }
//            }
//        }
//    }
//}

package com.BTL.Springboot.service.Impl;

import boofcv.abst.fiducial.QrCodeDetector;
import boofcv.alg.fiducial.qrcode.QrCode;
import boofcv.factory.fiducial.ConfigQrCode;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import com.BTL.Springboot.entity.Employee;
import com.BTL.Springboot.service.EmployeeImportService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeImportServiceImpl implements EmployeeImportService {

    @Override
    public String readQRCode(MultipartFile file) throws Exception {
        try {
            if (!file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("File phải là hình ảnh (jpg, png, ...).");
            }
            if (file.getSize() > 100 * 1024 * 1024) {
                throw new IllegalArgumentException("Hình ảnh mã QR không được vượt quá 100MB.");
            }

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                throw new IllegalArgumentException("Không thể đọc hình ảnh mã QR. Vui lòng kiểm tra file upload.");
            }

            GrayU8 grayImage = ConvertBufferedImage.convertFromSingle(image, null, GrayU8.class);
            ConfigQrCode config = new ConfigQrCode();
            QrCodeDetector<GrayU8> detector = FactoryFiducial.qrcode(config, GrayU8.class);

            detector.process(grayImage);
            List<QrCode> qrCodes = detector.getDetections();

            if (!qrCodes.isEmpty()) {
                return qrCodes.get(0).message;
            }

            throw new IllegalArgumentException("Không tìm thấy mã QR trong hình ảnh.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể giải mã QR: " + e.getMessage(), e);
        }
    }

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
        if (qrContent.contains("|")) {
            return "MULTI_LINE";
        }
        if (qrContent.contains(",")) {
            return "CSV";
        }
        throw new IllegalArgumentException("Định dạng mã QR không được hỗ trợ.");
    }

    @Override
    public List<Employee> parseJsonToEmployees(String jsonContent) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Đăng ký module hỗ trợ Java 8 date/time
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            // Định nghĩa định dạng cho LocalDate
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            objectMapper.registerModule(javaTimeModule);
            // Bỏ qua các thuộc tính không xác định
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Employee> employees = objectMapper.readValue(jsonContent, objectMapper.getTypeFactory().constructCollectionType(List.class, Employee.class));
            return employees;
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi khi phân tích JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Employee> parseCsvToEmployees(String csvContent) throws Exception {
        List<Employee> employees = new ArrayList<>();
        try (CSVParser csvParser = CSVParser.parse(csvContent, CSVFormat.DEFAULT.withHeader().withTrim())) {
            for (CSVRecord record : csvParser) {
                Employee employee = new Employee();
                employee.setFirstName(record.isSet("firstName") ? record.get("firstName") : "");
                employee.setLastName(record.isSet("lastName") ? record.get("lastName") : "");
                employee.setPosition(record.isSet("position") ? record.get("position") : "");
                employee.setEmail(record.isSet("email") ? record.get("email") : "");
                employee.setPhone(record.isSet("phone") ? record.get("phone") : "");
                if (record.isSet("hireDate") && !record.get("hireDate").isEmpty()) {
                    employee.setHireDate(LocalDate.parse(record.get("hireDate"), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                if (record.isSet("salary") && !record.get("salary").isEmpty()) {
                    employee.setSalary(Double.parseDouble(record.get("salary")));
                }
                employee.setCreatedAt(LocalDateTime.now());
                employee.setUpdatedAt(LocalDateTime.now());
                employee.setIsActive(true);
                employees.add(employee);
            }
        }
        return employees;
    }

    @Override
    public List<Employee> parseMultiLineToEmployees(String multiLineContent) throws Exception {
        List<Employee> employees = new ArrayList<>();
        String[] lines = multiLineContent.trim().split("\\r?\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] fields = line.split("\\|", -1);
            if (fields.length != 7) {
                throw new IllegalArgumentException("Dòng " + (i + 1) + " không đúng số trường (yêu cầu 7): " + line);
            }

            Employee employee = new Employee();
            employee.setFirstName(fields[0].isEmpty() ? null : fields[0]);
            employee.setLastName(fields[1].isEmpty() ? null : fields[1]);
            employee.setPosition(fields[2].isEmpty() ? null : fields[2]);
            employee.setEmail(fields[3].isEmpty() ? null : fields[3]);
            employee.setPhone(fields[4].isEmpty() ? null : fields[4]);
            if (!fields[5].isEmpty()) {
                employee.setHireDate(LocalDate.parse(fields[5], DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            if (!fields[6].isEmpty()) {
                employee.setSalary(Double.parseDouble(fields[6]));
            }
            employee.setCreatedAt(LocalDateTime.now());
            employee.setUpdatedAt(LocalDateTime.now());
            employee.setIsActive(true);
            employees.add(employee);
        }
        return employees;
    }

    @Override
    public List<Employee> handleUrlContent(String url) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                String content = EntityUtils.toString(response.getEntity());
                if (url.endsWith(".json")) {
                    return parseJsonToEmployees(content);
                } else if (url.endsWith(".csv")) {
                    return parseCsvToEmployees(content);
                } else {
                    throw new IllegalArgumentException("URL phải trỏ đến file JSON hoặc CSV.");
                }
            }
        }
    }
}