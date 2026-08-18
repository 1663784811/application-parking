package com.cyyaw.admin.user;

import com.cyyaw.admin.user.security.AuthFilter;
import com.cyyaw.admin.user.security.JwtUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：BCrypt 编码器、鉴权过滤器注册、CORS。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注册鉴权过滤器：拦截所有路径，order=1。
     * <p>
     * 仅以 FilterRegistrationBean 暴露，避免 Spring Boot 对 Filter 的默认自动注册造成重复注册。
     */
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration(JwtUtil jwtUtil) {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthFilter(jwtUtil));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("authFilter");
        return registration;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
