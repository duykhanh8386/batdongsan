package com.BTL.Springboot.controller;

import com.BTL.Springboot.dto.request.auth.AuthenticationRequest;
import com.BTL.Springboot.dto.request.auth.LogoutRequest;
import com.BTL.Springboot.dto.response.auth.AuthenticationDto;
import com.BTL.Springboot.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    // Hiển thị form đăng nhập
    @GetMapping("/login")
    public ModelAndView showLoginForm(@RequestParam(value = "redirectUrl", required = false) String redirectUrl) {
        ModelAndView mav = new ModelAndView("pages-login");
        mav.addObject("redirectUrl", redirectUrl);
        return mav;
    }

    // Xử lý đăng nhập từ form
    @PostMapping("/login")
    public ModelAndView authenticate(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpServletResponse response
    ) {
        try {
            // Tạo AuthenticationRequest
            AuthenticationRequest request = new AuthenticationRequest();
            request.setUsername(username);
            request.setPassword(password);

            // Gọi service để xác thực
            AuthenticationDto result = authenticationService.authenticate(request);

            // Tạo cookie để lưu token
            Cookie tokenCookie = new Cookie("JWT_TOKEN", result.getToken());
            tokenCookie.setHttpOnly(true);
            tokenCookie.setSecure(true);
            tokenCookie.setPath("/");
            tokenCookie.setMaxAge(3 * 60); // 3 phút
            tokenCookie.setAttribute("SameSite", "Lax");

            // Thêm cookie vào response
            response.addCookie(tokenCookie);

            // Chuyển hướng đến redirectUrl nếu có, nếu không thì về /home
            String targetUrl = (redirectUrl != null && !redirectUrl.isEmpty() && !redirectUrl.equals("/login")) ? redirectUrl : "/home";
            return new ModelAndView("redirect:" + targetUrl);
        } catch (Exception e) {
            // Đăng nhập thất bại, hiển thị lại trang đăng nhập
            ModelAndView mav = new ModelAndView("pages-login");
            mav.addObject("errorMessage", "Đăng nhập thất bại: " + e.getMessage());
            mav.addObject("redirectUrl", redirectUrl);
            return mav;
        }
    }

    // Xử lý đăng xuất
    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws ParseException, JOSEException {
        // Lấy token từ cookie
        String token = null;
        if (request.getCookies() != null) {
            Optional<Cookie> tokenCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> "JWT_TOKEN".equals(cookie.getName()))
                    .findFirst();
            if (tokenCookie.isPresent()) {
                token = tokenCookie.get().getValue();
            }
        }

        // Tạo LogoutRequest và truyền token
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setToken(token != null ? token : ""); // Truyền token vào body của LogoutRequest

        // Gọi authenticationService.logout
        authenticationService.logout(logoutRequest);

        // Xóa cookie
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);

        // Chuyển hướng đến trang đăng nhập
        return new ModelAndView("redirect:/login");
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
//        AuthenticationDto result = authenticationService.authenticate(request);
//
//        // Tạo cookie để lưu token
//        Cookie tokenCookie = new Cookie("JWT_TOKEN", result.getToken());
//        tokenCookie.setHttpOnly(true); // Ngăn chặn truy cập từ JavaScript
//        tokenCookie.setSecure(true); // Chỉ gửi qua HTTPS
//        tokenCookie.setPath("/"); // Cookie có hiệu lực trên toàn bộ ứng dụng
//        tokenCookie.setMaxAge(10 * 60); // Thời gian sống: 10 phút (có thể điều chỉnh)
//        tokenCookie.setAttribute("SameSite", "Lax"); // Bảo vệ chống CSRF
//
//        // Thêm cookie vào response
//        response.addCookie(tokenCookie);
//
//        return ResponseEntity.ok(result);
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request, HttpServletResponse response) throws ParseException, JOSEException {
//        authenticationService.logout(request);
//
//        // Xóa cookie bằng cách đặt Max-Age = 0
//        Cookie tokenCookie = new Cookie("JWT_TOKEN", null);
//        tokenCookie.setHttpOnly(true);
//        tokenCookie.setSecure(true);
//        tokenCookie.setPath("/");
//        tokenCookie.setMaxAge(0); // Xóa cookie
//        tokenCookie.setAttribute("SameSite", "Lax");
//
//        response.addCookie(tokenCookie);
//
//        return ResponseEntity.ok().build();
//    }
}