package com.cyyaw.application.parking.gate.common;

import com.cyyaw.application.parking.gate.common.entity.GateUser;
import com.cyyaw.application.parking.gate.config.GateProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与校验工具。
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final GateProperties props;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token，账号写入 subject，角色/姓名等写入自定义 claim。
     */
    public String generate(GateUser user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .claim("id", user.getId())
                .issuedAt(new Date(now))
                .expiration(new Date(now + props.getJwt().getExpire() * 1000L))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 token，失败抛 JwtException。
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
