package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.service.PropertyTrashService;
import com.BTL.Springboot.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/deleted-property")
public class PropertyTrashController {

    @Autowired
    private PropertyTrashService propertyTrashService;

    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/all")
    public ModelAndView getDeletedProperty() {
        ModelAndView modelAndView = new ModelAndView("house-landdeleted");

        try {
            UserAccountDto user = userAccountService.getMyInfo();
            System.out.println("User data: " + user);
            System.out.println("Role: " + user.getRole());
            System.out.println("Employee: " + user.getEmployee());
            System.out.println("Customer: " + user.getCustomer());
            modelAndView.addObject("user", user != null ? user : new Object());
        } catch (Exception e) {
            System.out.println("Error fetching user info: " + e.getMessage());
            modelAndView.addObject("user", new Object());
        }

        // Lấy tất cả dữ liệu bất động sản đã xóa
        Map<String, Object> result = propertyTrashService.findAllDeletedPropertiesWithDetails();
        modelAndView.addObject("properties", result.get("properties"));
        modelAndView.addObject("remainingTimeMap", result.get("remainingTimeMap"));
        modelAndView.addObject("remainingDaysMap", result.get("remainingDaysMap"));

        return modelAndView;
    }

    @PostMapping("/restore")
    public ModelAndView restoreProperty(
            @RequestParam(value = "propertyId", required = false) Integer propertyId,
            RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView("redirect:/deleted-property/all");

        if (propertyId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một bất động sản để khôi phục.");
            return mav;
        }

        try {
            propertyTrashService.restoreProperty(propertyId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khôi phục thành công bất động sản.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi khôi phục bất động sản: " + e.getMessage());
        }

        return mav;
    }
}
