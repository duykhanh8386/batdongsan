package com.BTL.Springboot.controller;

import com.BTL.Springboot.entity.Employee;
import com.BTL.Springboot.service.EmployeeImportService;
import com.BTL.Springboot.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class EmployeeImportController {

    @Autowired
    private EmployeeImportService employeeImportService;

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/employee/importQR")
    public ModelAndView importEmployeesFromQR(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "qrContent", required = false) String qrContent,
            @RequestParam(value = "sourcePage", required = false) String sourcePage,
            RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        String redirectView = "employee";
        String viewName = "staff-profile";

        try {
            String contentToProcess = null;
            if (qrContent != null && !qrContent.trim().isEmpty()) {
                contentToProcess = qrContent;
            } else if (file != null && !file.isEmpty()) {
                contentToProcess = employeeImportService.readQRCode(file);
            } else {
                throw new IllegalArgumentException("Vui lòng quét mã QR bằng camera hoặc upload hình ảnh mã QR.");
            }

            List<Employee> employees = new ArrayList<>();
            String format = employeeImportService.detectQRContentFormat(contentToProcess);

            switch (format) {
                case "JSON":
                    employees = employeeImportService.parseJsonToEmployees(contentToProcess);
                    break;
                case "CSV":
                    employees = employeeImportService.parseCsvToEmployees(contentToProcess);
                    break;
                case "URL":
                    employees = employeeImportService.handleUrlContent(contentToProcess);
                    break;
                case "MULTI_LINE":
                    employees = employeeImportService.parseMultiLineToEmployees(contentToProcess);
                    break;
                default:
                    throw new IllegalArgumentException("Định dạng mã QR không được hỗ trợ.");
            }

            List<String> errors = new ArrayList<>();
            int successCount = 0;

            for (int i = 0; i < employees.size(); i++) {
                Employee employee = employees.get(i);
                try {
                    if (employeeService.existsByEmail(employee.getEmail())) {
                        errors.add("Nhân viên " + (i + 1) + ": Email đã tồn tại: " + employee.getEmail());
                        continue;
                    }
                    employeeService.saveEmployee(employee);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Nhân viên " + (i + 1) + ": Lỗi khi thêm nhân viên: " + e.getMessage());
                }
            }

            if (successCount > 0) {
                if (errors.isEmpty()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã thêm thành công " + successCount + " nhân viên!");
                } else {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã thêm thành công " + successCount + " nhân viên, nhưng có lỗi: " + String.join("; ", errors));
                }
            } else if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thêm được nhân viên: " + String.join("; ", errors));
            }
            mav.setViewName("redirect:/" + redirectView);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Không thể đọc hoặc xử lý mã QR. Vui lòng kiểm tra lại.";
            System.out.println("Lỗi khi nhập từ mã QR: " + errorMessage);
            e.printStackTrace();
            mav.setViewName(viewName);
            mav.addObject("employee", new Employee()); // Thêm đối tượng employee để tránh lỗi Thymeleaf
            mav.addObject("employees", employeeService.getAllEmployee());
            mav.addObject("errorMessage", "Lỗi khi nhập từ mã QR: " + errorMessage);
        }

        return mav;
    }
}