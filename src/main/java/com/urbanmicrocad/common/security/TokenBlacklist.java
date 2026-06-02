package com.urbanmicrocad.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory JWT token blacklist for logout revocation.
 * <p>
 * When a user logs out, the token's {@code jti} claim is added to the blacklist
 * along with its expiration time. The {@link JwtAuthenticationFilter} checks
 * incoming tokens against this blacklist.
 * <p>
 * Stale entries (past their expiration) are cleaned up periodically via
 * {@link #cleanup()} to prevent unbounded memory growth.
 */
@Component
public class TokenBlacklist {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklist.class);

    /** jti → expiration instant */
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist.
     *
     * @param jti       the JWT ID claim
     * @param expiresAt the token's expiration instant
     */
    public void add(String jti, Instant expiresAt) {
        blacklistedTokens.put(jti, expiresAt);
        log.debug("Token blacklisted: jti={}, expiresAt={}", jti, expiresAt);
    }

    /**
     * Check if a token has been revoked.
     *
     * @param jti the JWT ID claim
     * @return true if the token is blacklisted
     */
    public boolean isBlacklisted(String jti) {
        return jti != null && blacklistedTokens.containsKey(jti);
    }

    /**
     * Remove expired entries to prevent memory leaks.
     * Runs every 5 minutes via Spring scheduling.
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanup() {
        Instant now = Instant.now();
        int before = blacklistedTokens.size();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        int removed = before - blacklistedTokens.size();
        if (removed > 0) {
            log.debug("Cleaned up {} expired blacklist entries", removed);
        }
    }

    int size() {
        return blacklistedTokens.size();
    }
}
