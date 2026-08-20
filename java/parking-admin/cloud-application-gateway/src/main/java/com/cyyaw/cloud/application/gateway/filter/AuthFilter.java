package com.cyyaw.cloud.application.gateway.filter;

import com.cyyaw.cloud.application.gateway.client.AuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Token authentication filter for Spring Cloud Gateway
 */
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final ObjectProvider<AuthClient> authClientProvider;

    public AuthFilter(ObjectProvider<AuthClient> authClientProvider) {
        this.authClientProvider = authClientProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(request)) {
            return chain.filter(exchange);
        }

        // Get token from Authorization header
        String authorizationHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            // Try to get token from query parameter as fallback
            String tokenParam = request.getQueryParams().getFirst("token");
            if (tokenParam != null && !tokenParam.isEmpty()) {
                authorizationHeader = TOKEN_PREFIX + tokenParam;
            }
        }

        // Validate token
        if (authorizationHeader == null || authorizationHeader.isEmpty() ||
            !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            log.warn("Missing or invalid Authorization header for request: {}", request.getPath());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        try {
            AuthClient authClient = authClientProvider.getIfAvailable();
            if (authClient == null) {
                log.warn("AuthClient not available, skipping token validation");
                return chain.filter(exchange);
            }

            boolean isValid = authClient.validateToken(authorizationHeader);
            if (!isValid) {
                log.warn("Invalid token for request: {}", request.getPath());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            log.debug("Token validated successfully for request: {}", request.getPath());
        } catch (Exception e) {
            log.error("Error validating token for request: {}", request.getPath(), e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }

    /**
     * Check if the endpoint is public (no authentication required)
     */
    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getPath().toString();

        // Public endpoints that don't require authentication
        String[] publicEndpoints = {
            "/auth/**",
            "/login",
            "/register",
            "/health",
            "/actuator/**",
            // knife4j 文档相关路径放行，否则 UI 被网关 401 拦截
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/favicon.ico"
        };

        for (String publicPath : publicEndpoints) {
            if (publicPath.endsWith("/**")) {
                String basePath = publicPath.substring(0, publicPath.length() - 3);
                if (path.startsWith(basePath)) {
                    return true;
                }
            } else if (path.equals(publicPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        // Run this filter early in the chain
        return -100;
    }
}