package com.kush.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String emailFrom(String token) {
        return parse(token).getSubject();
    }

    public String roleFrom(String token) {
        return parse(token).get("role", String.class);
    }

    public Long userIdFrom(String token) {
        Object uid = parse(token).get("uid");
        if (uid instanceof Number number) {
            return number.longValue();
        }
        if (uid instanceof String value && !value.isBlank()) {
            return Long.parseLong(value);
        }
        return null;
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
