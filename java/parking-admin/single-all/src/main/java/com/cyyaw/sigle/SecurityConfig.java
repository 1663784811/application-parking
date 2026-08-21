package com.cyyaw.sigle;

import cn.hutool.json.JSONObject;
import com.cyyaw.admin.common.WebErrCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> {
                    auth.
                            // 允许公开访问的URI
                                    requestMatchers(
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/doc.html",
                                    "/webjars/**",
                                    "/admin/login/**",
                                    "/app/login/**",
                                    "/store/login/**",
                                    "/common/**",
                                    "/root/sql/downloadExcel",
                                    // 验证码
                                    "/common/verify/getVerifyCode",
                                    // 产品
                                    "/app/product/**",
                                    "/root/login/registerEnterprise",
                                    // 扫码点餐
                                    "/app/mqtt/mqttConnectInfo/**",
                                    "/app/food/h5/**"
                            ).permitAll()
                            // 需要ADMIN角色
                            // .requestMatchers("/admin/**").hasRole("ADMIN")
                            // 其他请求需要认证
                            .anyRequest().authenticated();
                }).sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
//                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.exceptionHandling(ex -> {
            ex.accessDeniedHandler(accessDeniedHandler()).authenticationEntryPoint(authenticationEntryPoint());
        });
        return http.build();
    }

//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // 关键修改点
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.FORBIDDEN.value());
            body.put("error", "Forbidden");
            body.put("message", "您没有访问该资源的权限");
            body.put("path", request.getRequestURI());
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            authException.getCause().printStackTrace();
            authException.getMessage();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            JSONObject json = new JSONObject();
            json.set("code", WebErrCodeEnum.WEB_NOT_LOGIN.getCode());
            json.set("msg", WebErrCodeEnum.WEB_NOT_LOGIN.getMsg());
            response.getWriter().write(json.toString());
            response.getWriter().flush();
            response.getWriter().close();
        };
    }

}