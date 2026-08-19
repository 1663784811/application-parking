package com.cyyaw.admin.application.user.security;

import com.cyyaw.admin.common.WebErrCodeEnum;
import com.cyyaw.admin.common.WebException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 工具：HS256 签发 / 解析。
 * <p>
 * 使用 jjwt 的 gson 桥接（不依赖 Jackson），兼容 Spring Boot 4。
 * <ul>
 *   <li>access token：sub=tid，claims 含 account、name、type=access，短有效期</li>
 *   <li>refresh token：sub=tid，type=refresh，长有效期</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtUtil implements InitializingBean {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expire:7200}")
    private long accessExpireSeconds;

    @Value("${jwt.refresh-expire:604800}")
    private long refreshExpireSeconds;

    private SecretKey key;

    @Override
    public void afterPropertiesSet() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret 必须不少于 32 字节（HS256 要求），当前长度: " + bytes.length);
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /** 签发 access token */
    public String createAccessToken(String tid, String account, String name) {
        return build(tid, account, name, "access", accessExpireSeconds * 1000L);
    }

    /** 签发 refresh token */
    public String createRefreshToken(String tid) {
        return build(tid, null, null, "refresh", refreshExpireSeconds * 1000L);
    }

    private String build(String tid, String account, String name, String type, long ttlMillis) {
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .subject(tid)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .claim("type", type);
        if (account != null) {
            builder.claim("account", account);
        }
        if (name != null) {
            builder.claim("name", name);
        }
        // 由 SecretKey 长度推断为 HS256
        return builder.signWith(key).compact();
    }

    /**
     * 解析并校验签名 / 过期；失败抛 {@link WebException}（WEB_LOGIN_TIME_OUT）。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            throw new WebException(WebErrCodeEnum.WEB_LOGIN_TIME_OUT);
        }
    }

    public String getType(Claims claims) {
        return claims.get("type", String.class);
    }

}
