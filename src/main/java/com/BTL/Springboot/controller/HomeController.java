package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.response.transaction.SaleTransactionDto;
import com.BTL.Springboot.dto.response.transaction.TransactionDto;
import com.BTL.Springboot.dto.response.user.UserAccountDto;
import com.BTL.Springboot.service.TransactionService;
import com.BTL.Springboot.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/home")
    public ModelAndView getHomePage(@RequestParam(value = "salesFilter", defaultValue = "today") String salesFilter) {
        ModelAndView modelAndView = new ModelAndView("index");

        // Lấy thông tin người dùng
        try {
            UserAccountDto user = userAccountService.getMyInfo();
            modelAndView.addObject("user", user != null ? user : new Object());
        } catch (Exception e) {
            System.out.println("Error fetching user info: " + e.getMessage());
            modelAndView.addObject("user", new Object());
        }

        // Lấy thống kê dashboard
        Map<String, Object> data = transactionService.getDataComparison();
        modelAndView.addAllObjects(data);
        modelAndView.addObject("soldToday", transactionService.getTotalSoldToday());
        modelAndView.addObject("monthlyRevenue", transactionService.getRevenueThisMonth());
        modelAndView.addObject("customersThisYear", transactionService.getCustomerThisYear());


        // Lấy recent activities
        List<TransactionDto> recentActivities = transactionService.getRecentActivities();
        modelAndView.addObject("recentActivities", recentActivities);

        // Lọc doanh số theo filter
        List<SaleTransactionDto> sales;
        switch (salesFilter) {
            case "month" -> sales = transactionService.getSalesThisMonth();
            case "year" -> sales = transactionService.getSalesThisYear();
            default -> sales = transactionService.getSalesToday();
        }
        modelAndView.addObject("salesFilter", salesFilter);
        modelAndView.addObject("salesList", sales);

        return modelAndView;
    }

    @GetMapping("/property.json")
    public String getJson() {
        return "property.json";
    }

    @GetMapping("/property.csv")
    public String getCsv() {
        return "property.txt";
    }

}