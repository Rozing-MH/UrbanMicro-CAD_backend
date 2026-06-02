package com.urbanmicrocad.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenBlacklist revocation tests")
class TokenBlacklistTest {

    private final TokenBlacklist.InMemoryTokenBlacklist blacklist = new TokenBlacklist.InMemoryTokenBlacklist();

    @Test
    @DisplayName("blacklisted token is detected")
    void blacklistedTokenDetected() {
        String jti = "test-jti-123";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        blacklist.add(jti, expiresAt);

        assertThat(blacklist.isBlacklisted(jti)).isTrue();
    }

    @Test
    @DisplayName("non-blacklisted token is not detected")
    void nonBlacklistedTokenNotDetected() {
        assertThat(blacklist.isBlacklisted("unknown-jti")).isFalse();
    }

    @Test
    @DisplayName("null jti is not blacklisted")
    void nullJtiNotBlacklisted() {
        assertThat(blacklist.isBlacklisted(null)).isFalse();
    }

    @Test
    @DisplayName("cleanup removes expired entries")
    void cleanupRemovesExpiredEntries() {
        // Add an already-expired token
        blacklist.add("expired-jti", Instant.now().minusSeconds(1));
        // Add a still-valid token
        blacklist.add("valid-jti", Instant.now().plusSeconds(3600));

        assertThat(blacklist.size()).isEqualTo(2);
        blacklist.cleanup();
        assertThat(blacklist.size()).isEqualTo(1);
        assertThat(blacklist.isBlacklisted("valid-jti")).isTrue();
        assertThat(blacklist.isBlacklisted("expired-jti")).isFalse();
    }

    @Test
    @DisplayName("same jti can be added multiple times (idempotent)")
    void sameJtiIdempotent() {
        Instant expiresAt = Instant.now().plusSeconds(3600);

        blacklist.add("jti-1", expiresAt);
        blacklist.add("jti-1", expiresAt);

        assertThat(blacklist.size()).isEqualTo(1);
    }
}
