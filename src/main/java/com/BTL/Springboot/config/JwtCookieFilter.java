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
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return authHeader;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return Collections.enumeration(Collections.singletonList(authHeader));
                    }
                    return super.getHeaders(name);
                }

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