package com.BTL.Springboot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtCookieFilter extends OncePerRequestFilter {

    /**
     * Phương thức chính của bộ lọc, được gọi cho mỗi yêu cầu HTTP.
     * - Kiểm tra cookie có tên "JWT_TOKEN" trong yêu cầu.
     * - Nếu tìm thấy, thêm token vào header Authorization với định dạng "Bearer <token>".
     * - Sử dụng HttpServletRequestWrapper để bọc yêu cầu gốc và thêm header Authorization.
     * - Nếu không tìm thấy token, tiếp tục chuỗi bộ lọc mà không thay đổi yêu cầu.
     *
     * @param request Yêu cầu HTTP từ client
     * @param response Phản hồi HTTP gửi về client
     * @param filterChain Chuỗi bộ lọc để tiếp tục xử lý yêu cầu
     * @throws ServletException Nếu có lỗi liên quan đến servlet
     * @throws IOException Nếu có lỗi I/O khi xử lý yêu cầu hoặc phản hồi
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
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

        // Nếu tìm thấy token, thêm vào header Authorization của request
        if (token != null && !token.isEmpty()) {
            final String authHeader = "Bearer " + token;
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                /**
                 * Ghi đè phương thức getHeader để trả về header Authorization với giá trị token.
                 * Nếu tên header không phải "Authorization", trả về giá trị từ yêu cầu gốc.
                 *
                 * @param name Tên header
                 * @return Giá trị header, "Bearer <token>" nếu là Authorization
                 */
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return authHeader;
                    }
                    return super.getHeader(name);
                }

                /**
                 * Ghi đè phương thức getHeaders để trả về danh sách giá trị header Authorization.
                 * Nếu tên header là "Authorization", trả về danh sách chứa token.
                 *
                 * @param name Tên header
                 * @return Enumeration chứa giá trị header
                 */
                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return Collections.enumeration(Collections.singletonList(authHeader));
                    }
                    return super.getHeaders(name);
                }

                /**
                 * Ghi đè phương thức getHeaderNames để thêm "Authorization" vào danh sách tên header
                 * nếu nó chưa tồn tại trong yêu cầu gốc.
                 *
                 * @return Enumeration chứa danh sách tên header
                 */
                @Override
                public Enumeration<String> getHeaderNames() {
                    List<String> headerNames = Collections.list(super.getHeaderNames());
                    if (!headerNames.contains("Authorization")) {
                        headerNames.add("Authorization");
                    }
                    return Collections.enumeration(headerNames);
                }
            };
            log.debug("Added Authorization header from JWT_TOKEN cookie: {}", authHeader);
            filterChain.doFilter(wrappedRequest, response);
        } else {
            log.warn("No JWT_TOKEN cookie found in request to {}", request.getRequestURI());
            filterChain.doFilter(request, response);
        }
    }
}