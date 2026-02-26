package com.wzu.travelsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // 允许所有前端来源访问
        config.addAllowedHeader("*");        // 允许所有请求头
        config.addAllowedMethod("*");        // 允许所有方法（GET, POST, PUT, DELETE）
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}