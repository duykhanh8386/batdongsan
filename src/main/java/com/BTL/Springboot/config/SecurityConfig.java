package com.BTL.Springboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Danh sách các endpoint GET công khai, không yêu cầu xác thực.
     * Bao gồm các tài nguyên tĩnh (CSS, JS, hình ảnh), favicon, trang đăng nhập, và file dữ liệu mẫu.
     */
    private final String[] GET_PUBLIC_ENDPOINTS = {
            "/auth/login",           // Trang đăng nhập
            "/assets/**",       // Tài nguyên tĩnh (css, js, vendor)
            "/css/**", "/js/**", "/images/**", // Thư mục tĩnh khác
            "/favicon.ico",     // Favicon
            "/pages-login",      // Template trang đăng nhập
            "/property.json",
            "/property.csv"
    };

    /**
     * Danh sách các endpoint POST công khai, không yêu cầu xác thực.
     * Bao gồm API đăng nhập, đăng xuất, và kiểm tra token JWT.
     */
    private final String[] POST_PUBLIC_ENDPOINTS = {
            "/auth/login",      // API đăng nhập
            "/auth/logout",     // API đăng xuất
            "/auth/introspect"  // API kiểm tra token
    };

    /**
     * Tiêm CustomJwtDecoder để giải mã token JWT tùy chỉnh.
     */
    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    /**
     * Tiêm JwtCookieFilter để xử lý token JWT từ cookie.
     */
    @Autowired
    private JwtCookieFilter jwtCookieFilter;

    /**
     * Cấu hình chuỗi bộ lọc bảo mật (SecurityFilterChain) cho ứng dụng.
     * - Cho phép truy cập công khai vào các endpoint GET và POST được chỉ định.
     * - Yêu cầu xác thực cho tất cả các yêu cầu khác.
     * - Sử dụng OAuth2 Resource Server với JWT để xác thực.
     * - Tắt CSRF để phù hợp với API sử dụng token.
     * - Thêm bộ lọc JwtCookieFilter trước BearerTokenAuthenticationFilter để xử lý token từ cookie.
     *
     * @param httpSecurity Đối tượng HttpSecurity để cấu hình bảo mật
     * @return SecurityFilterChain Chuỗi bộ lọc bảo mật đã được cấu hình
     * @throws Exception Nếu có lỗi trong quá trình cấu hình
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.GET, GET_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, POST_PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.addFilterBefore(jwtCookieFilter, BearerTokenAuthenticationFilter.class);

        return httpSecurity.build();
    }

    /**
     * Cấu hình bộ lọc CORS để cho phép truy cập từ các nguồn khác nhau.
     * - Cho phép tất cả các nguồn (origin), phương thức HTTP, và tiêu đề (header).
     * - Áp dụng cấu hình CORS cho tất cả các đường dẫn (/**).
     *
     * @return CorsFilter Bộ lọc CORS đã được cấu hình
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    /**
     * Cấu hình bộ chuyển đổi JWT để ánh xạ các claim trong token thành quyền (authorities).
     * - Sử dụng JwtGrantedAuthoritiesConverter để trích xuất quyền từ claim.
     * - Loại bỏ tiền tố mặc định của quyền để phù hợp với cấu hình ứng dụng.
     *
     * @return JwtAuthenticationConverter Bộ chuyển đổi JWT
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    /**
     * Cấu hình mã hóa mật khẩu sử dụng BCrypt với độ mạnh (strength) là 10.
     * - BCrypt là thuật toán mã hóa mật khẩu an toàn, được sử dụng để mã hóa và kiểm tra mật khẩu.
     *
     * @return PasswordEncoder Đối tượng mã hóa mật khẩu
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}