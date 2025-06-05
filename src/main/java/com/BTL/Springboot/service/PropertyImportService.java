package com.BTL.Springboot.service;

import com.BTL.Springboot.dto.request.property.PropertyRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PropertyImportService {

    List<PropertyRequest> readExcelFile(MultipartFile file) throws IOException;

    List<PropertyRequest> readPDFFile(MultipartFile file) throws IOException;

    void validatePropertyRequest(PropertyRequest request, BindingResult result);
}
