package com.cyyaw.application.parking.gate.auth;

import com.cyyaw.application.parking.gate.auth.model.GateUser;
import com.cyyaw.application.parking.gate.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;

/**
 * JWT 鉴权过滤器：拦截 /api/**，校验 Authorization: Bearer <token>。
 * <p>白名单路径（/api/login、/api/logout）直接放行；
 * 校验通过则把当前用户写入 request 属性 currentUser，供 Controller 使用；
 * 未通过则返回 HTTP 401 + {code:401, message:"未登录或登录已过期", data:null}。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {

    private static final Set<String> WHITELIST = Set.of("/api/login", "/api/logout");

    private final JwtUtil jwtUtil;
    private final GateUserStore userStore;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        if (WHITELIST.contains(path)) {
            chain.doFilter(req, res);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtil.parse(token);
                GateUser user = userStore.findByUsername(claims.getSubject());
                if (user != null) {
                    request.setAttribute("currentUser", user);
                    chain.doFilter(req, res);
                    return;
                }
            } catch (Exception e) {
                log.debug("JWT 校验失败: {}", e.getMessage());
            }
        }

        writeUnauthorized(response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
    }
}
