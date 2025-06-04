//package com.BTL.Springboot.config;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DataSourceConfig {
//
//    @Bean
//    public DataSource dataSource() {
//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl("jdbc:mysql://localhost:3306/nicehomelander");
//        config.setUsername("root");
//        config.setPassword("19681902");
//        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
//
//        // Cấu hình pool
//        config.setMaximumPoolSize(10);
//        config.setMinimumIdle(5);
//        config.setIdleTimeout(60000);
//        config.setConnectionTimeout(30000);
//        config.setMaxLifetime(1800000);
//
//        return new HikariDataSource(config);
//    }
//}