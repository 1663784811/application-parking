package com.cyyaw.cloud.application.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "admin-auth", path = "/auth")
public interface AuthClient {

    /**
     * Validate token by calling auth service
     * @param authorization Bearer token
     * @return true if valid, false otherwise
     */
    @GetMapping("/validate")
    boolean validateToken(@RequestHeader("Authorization") String authorization);
}