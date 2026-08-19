package com.cyyaw.admin.application.user.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 鉴权过滤器：校验 access token。
 * <p>
 * 失败统一返回 HTTP 200 + {code:6001}，使前端 axios 成功回调里判断 code 并触发刷新。
 * 跳过登录 / 注册 / 验证码 / refreshToken 等公开路径。
 */
@Slf4j
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** 公开路径（无需鉴权） */
    private static final String[] SKIP_PATHS = {
            "/api/admin/login/**",
            "/api/common/verify/**",
            "/api/common/token/refreshToken"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        for (String pattern : SKIP_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && !header.isBlank()) {
            // 兼容可选 "Bearer " 前缀
            token = header.startsWith("Bearer ") ? header.substring(7) : header;
        }

        if (token == null || token.isBlank()) {
            writeUnauthorized(response);
            return;
        }

        try {
            Claims claims = jwtUtil.parse(token);
            String type = jwtUtil.getType(claims);
            if (!"access".equals(type)) {
                writeUnauthorized(response);
                return;
            }
            String tid = claims.getSubject();
            String account = claims.get("account", String.class);
            String name = claims.get("name", String.class);
            UserContext.set(new LoginUser(tid, account, name));
        } catch (Exception e) {
            writeUnauthorized(response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 鉴权失败：HTTP 200 + {code:6001,msg:"登录已过期，请重新登录",data:null}
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":6001,\"msg\":\"登录已过期，请重新登录\",\"data\":null}");
        response.getWriter().flush();
    }

}
