package com.cyyaw.application.parking.gate.config;

import com.cyyaw.application.parking.gate.common.JwtUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GateProperties.class)
public class WebConfig {

    /**
     * 注册 JWT 鉴权过滤器，仅拦截 /api/**。
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilter(JwtUtil jwtUtil, GateUserStore userStore) {
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, userStore);
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
