package com.BTL.Springboot.service;

import com.BTL.Springboot.entity.Employee;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeImportService {
    String readQRCode(MultipartFile file) throws Exception;
    String detectQRContentFormat(String qrContent);
    List<Employee> parseJsonToEmployees(String jsonContent) throws Exception;
    List<Employee> parseCsvToEmployees(String csvContent) throws Exception;
    List<Employee> parseMultiLineToEmployees(String multiLineContent) throws Exception;
    List<Employee> handleUrlContent(String url) throws Exception;
}