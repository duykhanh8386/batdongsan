package com.BTL.Springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(HttpSession session) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "pages-login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model,
                               HttpSession session) {
        if ("admin@gmail.com".equals(username) && "admin".equals(password)) {
            // Lưu thông tin đăng nhập vào session
            session.setAttribute("loggedInUser", username);
            return "redirect:/home";
        } else {
            model.addAttribute("errorMessage", "Tài khoản hoặc mật khẩu không đúng!");
            return "pages-login";
        }
    }

    // Thêm phương thức đăng xuất (logout) để xóa session
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Xóa session khi đăng xuất
        session.invalidate();
        return "redirect:/login";
    }
}