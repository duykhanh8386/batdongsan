package com.BTL.Springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Phương thức xử lý các yêu cầu không được xác thực.
     * - Kiểm tra header "Accept" để xác định yêu cầu là API (chấp nhận JSON) hay HTML.
     * - Nếu là API, trả về phản hồi JSON với mã lỗi 1006 và thông báo "Unauthenticated".
     * - Nếu là yêu cầu HTML, chuyển hướng đến trang đăng nhập với tham số redirectUrl
     *   chứa đường dẫn yêu cầu ban đầu.
     *
     * @param request Yêu cầu HTTP từ client
     * @param response Phản hồi HTTP gửi về client
     * @param authException Ngoại lệ xác thực, cung cấp thông tin về lỗi xác thực
     * @throws IOException Nếu có lỗi khi ghi phản hồi
     * @throws ServletException Nếu có lỗi liên quan đến servlet
     */
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        // Kiểm tra header Accept để xác định loại yêu cầu
        String acceptHeader = request.getHeader("Accept");
        boolean isApiRequest = acceptHeader != null && acceptHeader.contains("application/json");

        if (isApiRequest) {
            // Yêu cầu API, trả về JSON
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 1006);
            errorResponse.put("message", "Unauthenticated");

            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } else {
            // Yêu cầu HTML, chuyển hướng đến /login với redirectUrl
            String redirectUrl = "/auth/login?redirectUrl=" + request.getRequestURI();
            response.sendRedirect(redirectUrl);
        }
    }
}