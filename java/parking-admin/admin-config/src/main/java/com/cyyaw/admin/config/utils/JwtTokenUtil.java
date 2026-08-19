package com.cyyaw.admin.config.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class JwtTokenUtil {


    private static String secret = "ThisIsASecretKeyForJWTWithAtLeast32Bytes!";

    private static Long shortExpiration = 3600000L * 24 * 60 * 60;
//    private static Long shortExpiration = 6000L;

    private static Long longExpiration = 604800000L;

    public final static String TOKEN_HEADER = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";

    private JwtTokenUtil() {
    }

    // ===============================================================

    /**
     * 生成jwt
     *
     * @param subject
     * @param data
     * @return
     */
    public static String createToken(String subject, String data) {
        return createToken(subject, data, false);
    }

    public static String createToken(String subject, String data, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        // 可以添加额外的声明
        claims.put("type", rememberMe ? "REFRESH" : "ACCESS");
        claims.put("data", data);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (rememberMe ? longExpiration : shortExpiration));
        return TOKEN_PREFIX + Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(now).setExpiration(expiryDate).signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }


    /**
     * 验证token
     * @param token
     * @return
     */
    public static boolean verifierToken(String token) {
        return !isTokenExpired(token);
    }

    private static Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }
    // ===============================================================

    private static Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String getSubjectFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public static <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private static Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public static Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public static String getTokenType(String token) {
        return getClaimFromToken(token, claims -> claims.get("type", String.class));
    }

    public static String getTokenData(String token) {
        return getClaimFromToken(token, claims -> claims.get("data", String.class));
    }


} 