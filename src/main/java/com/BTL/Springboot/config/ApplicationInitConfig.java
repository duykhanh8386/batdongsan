package com.BTL.Springboot.config;

import com.BTL.Springboot.entity.Role;
import com.BTL.Springboot.entity.UserAccount;
import com.BTL.Springboot.repository.RoleRepository;
import com.BTL.Springboot.repository.UserAccountRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @NonFinal
    static final String ADMIN_EMAIL = "admin@nicehomelander.com";

    /**
     * Tạo ApplicationRunner để thực thi logic khởi tạo khi ứng dụng khởi động.
     * - Kiểm tra và tạo vai trò ADMIN nếu chưa tồn tại.
     * - Tạo tài khoản admin với thông tin mặc định nếu chưa có.
     * - Chỉ thực thi khi driver cơ sở dữ liệu là MySQL (com.mysql.cj.jdbc.Driver).
     *
     * @param userRepository Repository để tương tác với bảng UserAccount
     * @param roleRepository Repository để tương tác với bảng Role
     * @return ApplicationRunner Chứa logic khởi tạo
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            value = "driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserAccountRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                log.info("Creating admin user...");

                // KIỂM TRA VÀ TẠO ROLE ADMIN
                Role adminRole = roleRepository.findByCode("ADMIN")
                        .orElseGet(() -> {
                            log.info("Creating ADMIN role...");
                            return roleRepository.save(Role.builder()
                                    .roleName("Admin")
                                    .code("ADMIN")
                                    .description("Admin role")
                                    .build());
                        });

                // TẠO USER ADMIN - THÊM CÁC TRƯỜNG TIMESTAMP
                LocalDateTime now = LocalDateTime.now();
                UserAccount user = UserAccount.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .email(ADMIN_EMAIL)
//                        .employee()
                        .role(adminRole)
                        .createdAt(now)  // Thêm thời gian tạo
                        .updatedAt(now)  // Thêm thời gian cập nhật
                        .isActive(true)  // Đặt trạng thái active
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            } else {
                log.info("Admin user already exists, skipping creation");
            }
            log.info("Application initialization completed .....");
        };
    }
}