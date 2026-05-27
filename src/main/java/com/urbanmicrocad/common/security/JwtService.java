package com.urbanmicrocad.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateToken(CurrentUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getExpiresMinutes() * 60);
        return Jwts.builder()
            .subject(user.id().toString())
            .claim("username", user.username())
            .claim("role", user.role())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey())
            .compact();
    }

    public CurrentUser parseToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return new CurrentUser(
            Long.valueOf(claims.getSubject()),
            claims.get("username", String.class),
            claims.get("role", String.class)
        );
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
