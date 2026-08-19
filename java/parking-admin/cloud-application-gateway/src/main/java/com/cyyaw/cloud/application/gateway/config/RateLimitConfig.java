package com.cyyaw.cloud.application.gateway.config;//package com.cyyaw.spring.gateway.config;
//
//import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
///**
// * Rate limiting configuration for Spring Cloud Gateway
// */
//@Configuration
//public class RateLimitConfig {
//
//    /**
//     * Key resolver based on IP address
//     * Limits requests per IP address
//     */
//    @Bean
//    public KeyResolver ipKeyResolver() {
//        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
//    }
//
//    /**
//     * Key resolver based on user ID (from token)
//     * This requires extracting user ID from the request context
//     */
//    @Bean
//    public KeyResolver userKeyResolver() {
//        return exchange -> {
//            // Try to get user ID from header (set by AuthFilter)
//            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
//            if (userId != null && !userId.isEmpty()) {
//                return Mono.just(userId);
//            }
//            // Fallback to IP address
//            return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
//        };
//    }
//
//    /**
//     * Key resolver based on request path
//     * Limits requests per API endpoint
//     */
//    @Bean
//    public KeyResolver pathKeyResolver() {
//        return exchange -> Mono.just(exchange.getRequest().getPath().toString());
//    }
//}