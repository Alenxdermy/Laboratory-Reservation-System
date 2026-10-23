package com.lab.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成与解析 Token
 */
@Component
public class JwtUtil {

    @Value("${lab.jwt.secret}")
    private String secret;

    @Value("${lab.jwt.expire-hours}")
    private long expireHours;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token，载荷包含用户 id、用户名、角色
     */
    public String generateToken(Integer userId, String username, String role) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireHours * 3600 * 1000L);
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 Token，非法或过期时抛出异常
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
