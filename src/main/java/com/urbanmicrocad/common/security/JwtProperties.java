package com.urbanmicrocad.common.security;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long expiresMinutes;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be configured with at least 32 characters");
        }
        if (expiresMinutes <= 0) {
            throw new IllegalStateException("JWT_EXPIRES_MINUTES must be greater than 0");
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiresMinutes() {
        return expiresMinutes;
    }

    public void setExpiresMinutes(long expiresMinutes) {
        this.expiresMinutes = expiresMinutes;
    }
}
