package com.cyyaw.cloud.application.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Custom rate limiting filter for Spring Cloud Gateway
 * This filter works in conjunction with Redis-based rate limiting
 */
@Component
@Slf4j
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        // Skip rate limiting for health check and actuator endpoints
        if (isExemptFromRateLimit(path)) {
            return chain.filter(exchange);
        }

        // Log the request for monitoring
        log.debug("Processing request from IP: {} to path: {}", ip, path);

        // The actual rate limiting is handled by Spring Cloud Gateway's RequestRateLimiter filter
        // This custom filter provides additional logging and control
        return chain.filter(exchange);
    }

    /**
     * Check if the endpoint should be exempt from rate limiting
     */
    private boolean isExemptFromRateLimit(String path) {
        String[] exemptPaths = {
            "/health",
            "/actuator/**",
            "/metrics",
            "/info"
        };

        for (String exemptPath : exemptPaths) {
            if (exemptPath.endsWith("/**")) {
                String basePath = exemptPath.substring(0, exemptPath.length() - 3);
                if (path.startsWith(basePath)) {
                    return true;
                }
            } else if (path.equals(exemptPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        // Run after auth filter but before routing
        return -50;
    }
}