package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.response.customer.CustomerDto;
import com.BTL.Springboot.dto.response.employee.EmployeeDto;
import com.BTL.Springboot.dto.response.project.ProjectDto;
import com.BTL.Springboot.dto.response.property.PropertyDto;
import com.BTL.Springboot.dto.response.property_type.PropertyTypeDto;
import com.BTL.Springboot.dto.request.property.PropertyRequest;
import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.entity.Property;
import com.BTL.Springboot.entity.PropertyImage;
import com.BTL.Springboot.entity.PropertyType;
import com.BTL.Springboot.mapper.PropertyMapper;
import com.BTL.Springboot.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/properties")
@Slf4j
public class PropertyController {

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
    private PropertyImageService propertyImageService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private PropertyMapper mapper;

    @GetMapping("/for-sale")
    public ModelAndView forSale() {
        ModelAndView mav = new ModelAndView("house-landforsale");
        try {
            UserAccountDto user = userAccountService.getMyInfo();
            System.out.println("User data: " + user);
            System.out.println("Role: " + user.getRole());
            System.out.println("Employee: " + user.getEmployee());
            System.out.println("Customer: " + user.getCustomer());
            mav.addObject("user", user != null ? user : new Object()); // Truyền object rỗng nếu null
        } catch (Exception e) {
            System.out.println("Error fetching user info: " + e.getMessage());
            mav.addObject("user", new Object()); // Truyền object rỗng nếu có lỗi
        }
        List<PropertyDto> properties = propertyService.findAllByListingTypeAndStatus("Bán", "true");
        mav.addObject("properties", properties);
        mav.addObject("property", new PropertyRequest());
        mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
        mav.addObject("projects", projectService.getAllProjects());
        mav.addObject("listingAgents", employeeService.getAllEmployees());
        mav.addObject("owners", customerService.getAllCustomer());
        return mav;
    }

    @GetMapping("/for-rent")
    public ModelAndView forRent() {
        ModelAndView mav = new ModelAndView("house-landforrent");
        try {
            UserAccountDto user = userAccountService.getMyInfo();
            System.out.println("User data: " + user);
            System.out.println("Role: " + user.getRole());
            System.out.println("Employee: " + user.getEmployee());
            System.out.println("Customer: " + user.getCustomer());
            mav.addObject("user", user != null ? user : new Object()); // Truyền object rỗng nếu null
        } catch (Exception e) {
            System.out.println("Error fetching user info: " + e.getMessage());
            mav.addObject("user", new Object()); // Truyền object rỗng nếu có lỗi
        }
        List<PropertyDto> properties = propertyService.findAllByListingTypeAndStatus("Cho thuê", "true");
        mav.addObject("properties", properties);
        mav.addObject("property", new PropertyRequest());
        mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
        mav.addObject("projects", projectService.getAllProjects());
        mav.addObject("listingAgents", employeeService.getAllEmployees());
        mav.addObject("owners", customerService.getAllCustomer());
        return mav;
    }

    @GetMapping("/detail/{id}")
    public ModelAndView detail(@PathVariable("id") Integer id,
                               @RequestHeader(value = "referer", required = false) String referer,
                               @ModelAttribute("previousPage") String previousPage) {
        ModelAndView mav = new ModelAndView("house-landdetail");
        try {
            UserAccountDto user = userAccountService.getMyInfo();
            System.out.println("User data: " + user);
            System.out.println("Role: " + user.getRole());
            System.out.println("Employee: " + user.getEmployee());
            System.out.println("Customer: " + user.getCustomer());
            mav.addObject("user", user != null ? user : new Object()); // Truyền object rỗng nếu null
        } catch (Exception e) {
            System.out.println("Error fetching user info: " + e.getMessage());
            mav.addObject("user", new Object()); // Truyền object rỗng nếu có lỗi
        }
        PropertyDto property = propertyService.getPropertyById(id);
        Property propertyImage = mapper.toEntity(property);
        mav.addObject("images", propertyImageService.getImagesByProperty(propertyImage));
        mav.addObject("property", property);
        mav.addObject("propertyId", property.getPropertyId());
        mav.addObject("isEditMode", false);

        String finalPreviousPage = previousPage != null && !previousPage.isEmpty()
                ? previousPage
                : determinePreviousPage(referer);
        mav.addObject("previousPage", finalPreviousPage);

        mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
        mav.addObject("projects", projectService.getAllProjects());
        mav.addObject("listingAgents", employeeService.getAllEmployees());
        mav.addObject("owners", customerService.getAllCustomer());
        mav.addObject("listingTypes", Arrays.asList("Bán", "Cho thuê"));
        return mav;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes,
                                     @RequestHeader(value = "referer", required = false) String referer,
                                     @ModelAttribute("previousPage") String previousPage) {
        ModelAndView mav = new ModelAndView("house-landdetail");
        try {
            UserAccountDto user = userAccountService.getMyInfo();
            System.out.println("User data: " + user);
            System.out.println("Role: " + user.getRole());
            System.out.println("Employee: " + user.getEmployee());
            System.out.println("Customer: " + user.getCustomer());
            mav.addObject("user", user != null ? user : new Object()); // Truyền object rỗng nếu null

            PropertyDto dto = propertyService.getPropertyById(id);
            Property property = mapper.toEntity(dto);

            // Khôi phục ảnh ẩn
            List<String> restoredUrls = propertyImageService.restoreImagesFromHidden(property);
            if (!restoredUrls.isEmpty()) {
                log.info("Restored {} hidden images for property ID {}", restoredUrls.size(), id);
            }

            List<PropertyImage> images = propertyImageService.getImagesByProperty(property);
            // Lấy tempImageUrls từ service
            List<String> tempImageUrls = propertyImageService.getTempImagesByProperty(property);
            log.info("Retrieved tempImageUrls for property ID {}: {}", id, tempImageUrls);

            mav.addObject("property", dto);
            mav.addObject("images", images);
            mav.addObject("propertyId", id);
            mav.addObject("isEditMode", true);
            mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
            mav.addObject("projects", projectService.getAllProjects());
            mav.addObject("listingAgents", employeeService.getAllEmployees());
            mav.addObject("owners", customerService.getAllCustomer());
            mav.addObject("listingTypes", Arrays.asList("Bán", "Cho thuê"));
            mav.addObject("tempImageUrls", tempImageUrls);

            // Lấy hiddenImageUrls và imagesToDelete từ flash attributes
            List<String> hiddenImageUrls = getFlashAttributeAsList(redirectAttributes, "hiddenImageUrls");
            mav.addObject("hiddenImageUrls", hiddenImageUrls);

            List<String> imagesToDelete = getFlashAttributeAsList(redirectAttributes, "imagesToDelete");
            mav.addObject("imagesToDelete", imagesToDelete);

            log.info("Loaded edit form for property ID {}. tempImageUrls: {}, hiddenImageUrls: {}, imagesToDelete: {}",
                    id, tempImageUrls, hiddenImageUrls, imagesToDelete);

            String finalPreviousPage = previousPage != null && !previousPage.isEmpty()
                    ? previousPage
                    : determinePreviousPage(referer);
            mav.addObject("previousPage", finalPreviousPage);
        } catch (Exception e) {
            System.out.println("Error fetching user info: " + e.getMessage());
            mav.addObject("user", new Object()); // Truyền object rỗng nếu có lỗi

            log.error("Error loading edit form for property ID {}: {}", id, e.getMessage(), e);
            mav.addObject("errorMessage", "Lỗi khi tải form chỉnh sửa: " + e.getMessage());
            mav.setViewName("redirect:/properties");
        }
        return mav;
    }

    private List<String> getFlashAttributeAsList(RedirectAttributes redirectAttributes, String attributeName) {
        Object attr = redirectAttributes.getFlashAttributes().get(attributeName);
        log.info("Flash attribute {}: {}", attributeName, attr);
        List<String> result = new ArrayList<>();
        if (attr instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String) {
                    result.add((String) item);
                }
            }
        }
        return result;
    }

    private String determinePreviousPage(String referer) {
        if (referer != null) {
            if (referer.contains("/properties/for-sale")) {
                return "/properties/for-sale";
            } else if (referer.contains("/properties/for-rent")) {
                return "/properties/for-rent";
            }
        }
        return "/properties/for-sale";
    }

    @PostMapping("/createProperty")
    public ModelAndView createProperty(@Valid @ModelAttribute("property") PropertyRequest request,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        if (result.hasErrors()) {
            mav.setViewName("house-landforsale");
            mav.addObject("property", request);
            mav.addObject("properties", propertyService.findAllByListingTypeAndStatus("Bán", "true"));
            mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
            mav.addObject("projects", projectService.getAllProjects());
            mav.addObject("listingAgents", employeeService.getAllEmployees());
            mav.addObject("owners", customerService.getAllCustomer());
            mav.addObject("errorMessage", "Vui lòng sửa các lỗi trong biểu mẫu.");
            return mav;
        }

        try {
            // Kiểm tra propertyCode
            String propertyCode = request.getPropertyCode() != null ? request.getPropertyCode().trim() : null;
            if (propertyCode == null || propertyCode.isEmpty()) {
                throw new IllegalArgumentException("Mã bất động sản không được để trống.");
            }
            if (propertyService.existsByPropertyCode(propertyCode)) {
                throw new IllegalArgumentException("Mã bất động sản đã tồn tại: " + propertyCode);
            }
            if (request.getListingType() == null || (!request.getListingType().equals("Bán") && !request.getListingType().equals("Cho thuê"))) {
                throw new IllegalArgumentException("Loại giao dịch phải là 'Bán' hoặc 'Cho thuê'.");
            }
            if (request.getPropertyType() == null || request.getPropertyType().getTypeId() == null) {
                throw new IllegalArgumentException("Loại bất động sản không được để trống.");
            }
            if (request.getListingAgent() == null || request.getListingAgent().getEmployeeId() == null) {
                throw new IllegalArgumentException("Nhân viên phụ trách không được để trống.");
            }
            if (request.getOwner() == null || request.getOwner().getCustomerId() == null) {
                throw new IllegalArgumentException("Chủ sở hữu không được để trống.");
            }

            PropertyDto savedProperty = propertyService.createProperty(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm bất động sản thành công!");
            String redirectPage = request.getListingType().equals("Cho thuê") ? "/properties/for-rent" : "/properties/for-sale";
            mav.setViewName("redirect:" + redirectPage);
        } catch (Exception e) {
            mav.setViewName("house-landforsale");
            mav.addObject("property", request);
            mav.addObject("properties", propertyService.findAllByListingTypeAndStatus("Bán", "true"));
            mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
            mav.addObject("projects", projectService.getAllProjects());
            mav.addObject("listingAgents", employeeService.getAllEmployees());
            mav.addObject("owners", customerService.getAllCustomer());
            mav.addObject("errorMessage", "Lỗi khi thêm bất động sản: " + e.getMessage());
        }

        return mav;
    }

    @PostMapping("/save")
    public ModelAndView saveProperty(@Valid @ModelAttribute("property") PropertyRequest request,
                                     BindingResult result,
                                     @RequestParam(value = "propertyId", required = false) Integer propertyId,
                                     @RequestParam(value = "previousPage", required = false) String previousPage,
                                     RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        if (result.hasErrors()) {
            mav.setViewName("house-landdetail");
            mav.addObject("property", request);
            Property propertyEntity = mapper.toEntity(propertyService.getPropertyById(propertyId));
            mav.addObject("images", propertyImageService.getImagesByProperty(propertyEntity));
            mav.addObject("propertyId", propertyId);
            mav.addObject("isEditMode", true);
            mav.addObject("previousPage", previousPage != null ? previousPage : "/properties/for-sale");
            mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
            mav.addObject("projects", projectService.getAllProjects());
            mav.addObject("listingAgents", employeeService.getAllEmployees());
            mav.addObject("owners", customerService.getAllCustomer());
            mav.addObject("listingTypes", Arrays.asList("Bán", "Cho thuê"));
            mav.addObject("error", "Vui lòng sửa các lỗi trong biểu mẫu.");
            return mav;
        }

        try {
            // Kiểm tra propertyCode
            String propertyCode = request.getPropertyCode() != null ? request.getPropertyCode().trim() : null;
            if (propertyCode == null || propertyCode.isEmpty()) {
                throw new IllegalArgumentException("Mã bất động sản không được để trống.");
            }
            if (propertyId != null) {
                // Cập nhật bất động sản
                PropertyDto existingProperty = propertyService.getPropertyById(propertyId);
                if (existingProperty == null) {
                    throw new IllegalArgumentException("Không tìm thấy bất động sản với ID: " + propertyId);
                }
                String existingPropertyCode = existingProperty.getPropertyCode() != null ? existingProperty.getPropertyCode().trim() : "";
                // Kiểm tra trùng chỉ khi propertyCode thay đổi
                if (!propertyCode.equals(existingPropertyCode) &&
                        propertyService.existsByPropertyCode(propertyCode)) {
                    throw new IllegalArgumentException("Mã bất động sản đã tồn tại: " + propertyCode);
                }
            } else {
                // Tạo mới bất động sản
                if (propertyService.existsByPropertyCode(propertyCode)) {
                    throw new IllegalArgumentException("Mã bất động sản đã tồn tại: " + propertyCode);
                }
            }
            if (request.getListingType() == null || (!request.getListingType().equals("Bán") && !request.getListingType().equals("Cho thuê"))) {
                throw new IllegalArgumentException("Loại giao dịch phải là 'Bán' hoặc 'Cho thuê'.");
            }
            if (request.getPropertyType() == null || request.getPropertyType().getTypeId() == null) {
                throw new IllegalArgumentException("Loại bất động sản không được để trống.");
            }
            if (request.getListingAgent() == null || request.getListingAgent().getEmployeeId() == null) {
                throw new IllegalArgumentException("Nhân viên phụ trách không được để trống.");
            }
            if (request.getOwner() == null || request.getOwner().getCustomerId() == null) {
                throw new IllegalArgumentException("Chủ sở hữu không được để trống.");
            }

            if (propertyId != null) {
                PropertyDto existingProperty = propertyService.getPropertyById(propertyId);
                request.setCreatedAt(existingProperty.getCreatedAt());
            } else {
                request.setCreatedAt(java.time.LocalDateTime.now());
            }
            request.setUpdatedAt(java.time.LocalDateTime.now());

            PropertyDto savedProperty = propertyService.saveProperty(request, propertyId);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin bất động sản thành công!");
            mav.setViewName("redirect:/properties/detail/" + savedProperty.getPropertyId());
        } catch (Exception e) {
            mav.setViewName("house-landdetail");
            mav.addObject("property", request);
            mav.addObject("propertyId", propertyId);
            mav.addObject("isEditMode", true);
            mav.addObject("previousPage", previousPage != null ? previousPage : "/properties/for-sale");
            mav.addObject("propertyTypes", propertyTypeService.getAllPropertyTypes());
            mav.addObject("projects", projectService.getAllProjects());
            mav.addObject("listingAgents", employeeService.getAllEmployees());
            mav.addObject("owners", customerService.getAllCustomer());
            mav.addObject("listingTypes", Arrays.asList("Bán", "Cho thuê"));
            mav.addObject("error", "Lỗi khi lưu bất động sản: " + e.getMessage());
        }

        return mav;
    }

    @GetMapping("/delete/{id}")
    public ModelAndView deleteById(@PathVariable("id") Integer id,
                                   @RequestHeader(value = "referer", required = false) String referer,
                                   RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        try {
            propertyService.deletePropertyStatus(id, "false");
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bất động sản thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bất động sản với ID: " + id);
        }
        String previousPage = determinePreviousPage(referer);
        mav.setViewName("redirect:" + previousPage);
        return mav;
    }

    @GetMapping("/propertyType")
    @ResponseBody
    public List<PropertyTypeDto> getAllPropertyTypes() {
        return propertyTypeService.getAllPropertyTypes();
    }

    @GetMapping("/project")
    @ResponseBody
    public List<ProjectDto> getAllProjects() {
        return projectService.getAllProjects();
    }


    @GetMapping("/all")
    @ResponseBody
    public List<PropertyDto> getAlls() {
        return propertyService.getAllProperties();
    }

    @GetMapping("/sale")
    @ResponseBody
    public List<PropertyDto> getAllByListingTypeAndStatus() {
        return propertyService.findAllByListingTypeAndStatus("Bán", "True");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable Integer id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }
}