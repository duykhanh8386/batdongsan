package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.request.property.PropertyRequest;
import com.BTL.Springboot.mapper.*;
import com.BTL.Springboot.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PropertyImportController {

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

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private PropertyImportService propertyImportService;

    @Autowired
    private PropertyTypeMapper propertyTypeMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * Xử lý yêu cầu nhập dữ liệu bất động sản từ file Excel.
     * Hàm đọc file Excel, kiểm tra tính hợp lệ của từng PropertyRequest, và thực hiện tạo/cập nhật
     * bất động sản dựa trên tham số action.
     *
     * @param file File Excel chứa dữ liệu bất động sản
     * @param sourcePage Trang nguồn để xác định redirect
     * @param action Hành động: "create" hoặc "update"
     * @param redirectAttributes Đối tượng để lưu thông báo
     * @return ModelAndView Chuyển hướng hoặc trả về view với thông báo lỗi
     */
    @PostMapping("/properties/importExcel")
    public ModelAndView importPropertiesFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sourcePage", required = false) String sourcePage,
            @RequestParam(value = "action", required = false, defaultValue = "create") String action,
            RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        String redirectView = determineRedirectView(sourcePage);
        String viewName = determineViewName(sourcePage);

        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File Excel không được để trống.");
            }

            List<PropertyRequest> properties = propertyImportService.readExcelFile(file);
            List<String> errors = new ArrayList<>();
            int successCount = 0;

            for (int i = 0; i < properties.size(); i++) {
                PropertyRequest request = properties.get(i);
                BindingResult result = new BeanPropertyBindingResult(request, "property");

                propertyImportService.validatePropertyRequest(request, result);

                if (result.hasErrors()) {
                    errors.add("Dòng " + (i + 2) + ": " + result.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
                    continue;
                }

                try {
                    if ("create".equals(action)) {
                        if (propertyService.existsByPropertyCode(request.getPropertyCode())) {
                            errors.add("Dòng " + (i + 2) + ": Mã bất động sản đã tồn tại: " + request.getPropertyCode());
                            continue;
                        }
                        propertyService.createProperty(request);
                        successCount++;
                    } else if ("update".equals(action)) {
                        propertyService.updateProperty(request);
                        successCount++;
                    }
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 2) + ": Lỗi khi " + ("create".equals(action) ? "thêm mới" : "cập nhật") + " bất động sản: " + e.getMessage());
                }
            }

            if (successCount > 0) {
                if (errors.isEmpty()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã " + ("create".equals(action) ? "thêm mới" : "cập nhật") + " thành công " + successCount + " bất động sản!");
                } else {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã " + ("create".equals(action) ? "thêm mới" : "cập nhật") + " thành công " + successCount + " bất động sản, nhưng có lỗi: " + String.join("; ", errors));
                }
            } else if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không " + ("create".equals(action) ? "thêm mới" : "cập nhật") + " được bất động sản: " + String.join("; ", errors));
            }
            mav.setViewName("redirect:/properties/" + redirectView);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Không thể xử lý file Excel. Vui lòng kiểm tra lại.";
            System.out.println("Lỗi khi nhập file Excel: " + errorMessage);
            e.printStackTrace();
            mav.setViewName(viewName);
            mav.addObject("properties", propertyService.findAllByListingTypeAndStatus(
                    viewName.equals("house-landforsale") ? "Bán" : "Cho thuê", "true"));
            mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
            mav.addObject("projects", projectService.getAllProjects());
            mav.addObject("listingAgents", employeeService.getAllEmployees());
            mav.addObject("owners", customerService.getAllCustomer());
            mav.addObject("errorMessage", "Lỗi khi nhập file Excel: " + errorMessage);
            mav.addObject("property", new PropertyRequest());
        }

        return mav;
    }

    // Hàm xác định trang chuyển hướng
    private String determineRedirectView(String sourcePage) {
        if ("for-rent".equals(sourcePage)) {
            return "for-rent";
        }
        return "for-sale"; // Mặc định là for-sale
    }

    // Hàm xác định tên view
    private String determineViewName(String sourcePage) {
        if ("for-rent".equals(sourcePage)) {
            return "house-landforrent";
        }
        return "house-landforsale"; // Mặc định là house-landforsale
    }

    /**
     * Xử lý yêu cầu nhập dữ liệu bất động sản từ file PDF.
     * Hàm đọc file PDF, trích xuất văn bản, phân tích thành PropertyRequest,
     * và thực hiện tạo/cập nhật bất động sản.
     *
     * @param file File PDF
     * @param sourcePage Trang nguồn
     * @param action Hành động: "create" hoặc "update"
     * @param redirectAttributes Đối tượng lưu thông báo
     * @return ModelAndView Chuyển hướng hoặc trả về view với thông báo lỗi
     */
    @PostMapping("/properties/importPDF")
    public ModelAndView importPropertiesFromPDF(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sourcePage", required = false) String sourcePage,
            @RequestParam(value = "action", required = false, defaultValue = "create") String action,
            RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        String redirectView = determineRedirectView(sourcePage);
        String viewName = determineViewName(sourcePage);

        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File PDF không được để trống.");
            }

            List<PropertyRequest> properties = propertyImportService.readPDFFile(file);
            List<String> errors = new ArrayList<>();
            int successCount = 0;

            for (int i = 0; i < properties.size(); i++) {
                PropertyRequest request = properties.get(i);
                BindingResult result = new BeanPropertyBindingResult(request, "property");

                propertyImportService.validatePropertyRequest(request, result);

                if (result.hasErrors()) {
                    errors.add("Dòng " + (i + 2) + ": " + result.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
                    continue;
                }

                try {
                    if ("create".equals(action)) {
                        if (propertyService.existsByPropertyCode(request.getPropertyCode())) {
                            errors.add("Dòng " + (i + 2) + ": Mã bất động sản đã tồn tại: " + request.getPropertyCode());
                            continue;
                        }
                        propertyService.createProperty(request);
                        successCount++;
                    } else if ("update".equals(action)) {
                        propertyService.updateProperty(request);
                        successCount++;
                    }
                } catch (Exception e) {
                    errors.add("Dòng " + (i + 2) + ": Lỗi khi " + ("create".equals(action) ? "thêm mới" : "cập nhật") + " bất động sản: " + e.getMessage());
                }
            }

            if (successCount > 0) {
                if (errors.isEmpty()) {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã " + ("create".equals(action) ? "thêm mới" : "cập nhật") + " thành công " + successCount + " bất động sản!");
                } else {
                    redirectAttributes.addFlashAttribute("successMessage", "Đã " + ("create".equals(action) ? "thêm mới" : "cập nhật") + " thành công " + successCount + " bất động sản, nhưng có lỗi: " + String.join("; ", errors));
                }
            } else if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không " + ("create".equals(action) ? "thêm mới" : "cập nhật") + " được bất động sản: " + String.join("; ", errors));
            }
            mav.setViewName("redirect:/properties/" + redirectView);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Không thể xử lý file PDF. Vui lòng kiểm tra lại.";
            System.out.println("Lỗi khi nhập file PDF: " + errorMessage);
            e.printStackTrace();
            mav.setViewName(viewName);
            mav.addObject("properties", propertyService.findAllByListingTypeAndStatus(
                    viewName.equals("house-landforsale") ? "Bán" : "Cho thuê", "true"));
            mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
            mav.addObject("projects", projectService.getAllProjects());
            mav.addObject("listingAgents", employeeService.getAllEmployees());
            mav.addObject("owners", customerService.getAllCustomer());
            mav.addObject("errorMessage", "Lỗi khi nhập file PDF: " + errorMessage);
            mav.addObject("property", new PropertyRequest());
        }

        return mav;
    }
}
