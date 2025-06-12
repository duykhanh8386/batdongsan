package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.request.property.PropertyRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PropertyImportService {
    String readQRCode(MultipartFile file) throws Exception;

    String detectQRContentFormat(String qrContent);

    List<PropertyRequest> parseJsonToProperties(String jsonContent) throws Exception;

    List<PropertyRequest> parseCsvToProperties(String csvContent) throws Exception;

    List<PropertyRequest> parseMultiLineToProperties(String multiLineContent) throws Exception;

    List<PropertyRequest> handleUrlContent(String url) throws Exception;

    List<PropertyRequest> handleBase64Content(String base64Content, String format) throws Exception;

    List<PropertyRequest> readExcelFile(MultipartFile file) throws IOException;

    List<PropertyRequest> readPDFFile(MultipartFile file) throws IOException;

    List<PropertyRequest> generateAIProperties(int quantity);

    void validatePropertyRequest(PropertyRequest request, BindingResult result);
}
