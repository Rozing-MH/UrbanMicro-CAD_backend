package com.urbanmicrocad.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimiter sliding window tests")
class RateLimiterTest {

    @Test
    @DisplayName("allows requests up to the limit")
    void allowsRequestsUpToLimit() {
        RateLimiter limiter = new RateLimiter(3, 60);
        assertThat(limiter.tryAcquire("ip1")).isTrue();
        assertThat(limiter.tryAcquire("ip1")).isTrue();
        assertThat(limiter.tryAcquire("ip1")).isTrue();
    }

    @Test
    @DisplayName("rejects requests exceeding the limit")
    void rejectsRequestsExceedingLimit() {
        RateLimiter limiter = new RateLimiter(2, 60);
        limiter.tryAcquire("ip1");
        limiter.tryAcquire("ip1");
        assertThat(limiter.tryAcquire("ip1")).isFalse();
    }

    @Test
    @DisplayName("different keys have independent limits")
    void differentKeysIndependent() {
        RateLimiter limiter = new RateLimiter(1, 60);
        assertThat(limiter.tryAcquire("ip1")).isTrue();
        assertThat(limiter.tryAcquire("ip2")).isTrue();
        assertThat(limiter.tryAcquire("ip1")).isFalse();
        assertThat(limiter.tryAcquire("ip2")).isFalse();
    }

    @Test
    @DisplayName("cleanup removes expired buckets")
    void cleanupRemovesExpiredBuckets() {
        RateLimiter limiter = new RateLimiter(1, 1); // 1-second window
        limiter.tryAcquire("ip1");
        assertThat(limiter.bucketCount()).isEqualTo(1);

        // Wait for bucket to become stale (2x window = 2 seconds)
        try { Thread.sleep(2100); } catch (InterruptedException ignored) {}

        limiter.cleanup();
        assertThat(limiter.bucketCount()).isZero();
    }

    @Test
    @DisplayName("window resets after expiry allowing new requests")
    void windowResetsAfterExpiry() {
        RateLimiter limiter = new RateLimiter(1, 1); // 1-second window
        assertThat(limiter.tryAcquire("ip1")).isTrue();
        assertThat(limiter.tryAcquire("ip1")).isFalse();

        // Wait for window to expire
        try { Thread.sleep(1100); } catch (InterruptedException ignored) {}

        assertThat(limiter.tryAcquire("ip1")).isTrue();
    }
}
