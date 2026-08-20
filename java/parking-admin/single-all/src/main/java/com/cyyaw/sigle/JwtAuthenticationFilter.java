package com.cyyaw.sigle;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.cyyaw.admin.config.utils.JwtTokenUtil;
import com.cyyaw.admin.entity.utils.LoginInfo;
import com.cyyaw.admin.entity.utils.LoginInfoContext;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String authHeader = request.getHeader(JwtTokenUtil.TOKEN_HEADER);
        String username = null;
        String jwt = null;
        try {
            if (authHeader != null && authHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
                jwt = authHeader.substring(JwtTokenUtil.TOKEN_PREFIX.length());
                username = JwtTokenUtil.getSubjectFromToken(jwt);
            }
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && JwtTokenUtil.verifierToken(jwt)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                String json = JwtTokenUtil.getTokenData(jwt);
                if (StrUtil.isNotBlank(json)) {
                    LoginInfo loginInfo = new JSONObject(json).toBean(LoginInfo.class);
                    LoginInfoContext.setLoginInfo(loginInfo);
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("token 过期： {}", e.getMessage());
        }
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            throw e;
        } finally {
            LoginInfoContext.clearLoginInfo();
        }
    }


}