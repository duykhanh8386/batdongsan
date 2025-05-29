package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.entity.UserAccount;
import com.BTL.Springboot.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @Autowired
    private UserAccountService userAccountService;

    @GetMapping("/home")
    public ModelAndView getHomePage() {
        ModelAndView modelAndView = new ModelAndView();
        try {
            UserAccountDto user = userAccountService.getMyInfo();
            System.out.println("User data: " + user);
            System.out.println("Role: " + user.getRole());
            System.out.println("Employee: " + user.getEmployee());
            System.out.println("Customer: " + user.getCustomer());
            modelAndView.addObject("user", user != null ? user : new Object()); // Truyền object rỗng nếu null
        } catch (Exception e) {
            System.out.println("Error fetching user info: " + e.getMessage());
            modelAndView.addObject("user", new Object()); // Truyền object rỗng nếu có lỗi
        }
        modelAndView.setViewName("index");
        return modelAndView;
    }
}