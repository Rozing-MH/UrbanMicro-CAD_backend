package com.urbanmicrocad.common.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory token-bucket rate limiter.
 * <p>
 * Tracks request counts per key (e.g. IP address) within fixed time windows.
 * Thread-safe via {@link ConcurrentHashMap} and {@link AtomicLong}.
 * Stale entries are evicted lazily on {@link #cleanup()} calls.
 */
public class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * @param maxRequests   maximum allowed requests per window
     * @param windowSeconds window duration in seconds
     */
    public RateLimiter(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000L;
    }

    /**
     * Try to consume one request slot for the given key.
     *
     * @return true if the request is allowed, false if rate limit exceeded
     */
    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(now));
        return bucket.tryAcquire(now, maxRequests, windowMillis);
    }

    /**
     * Evict expired buckets. Should be called periodically.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> entry.getValue().isExpired(now, windowMillis));
    }

    int bucketCount() {
        return buckets.size();
    }

    private static class Bucket {
        private final AtomicLong count;
        private volatile long windowStart;

        Bucket(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicLong(0);
        }

        boolean tryAcquire(long now, int maxRequests, long windowMillis) {
            if (now - windowStart >= windowMillis) {
                synchronized (this) {
                    if (now - windowStart >= windowMillis) {
                        windowStart = now;
                        count.set(0);
                    }
                }
            }
            return count.incrementAndGet() <= maxRequests;
        }

        boolean isExpired(long now, long windowMillis) {
            return now - windowStart >= windowMillis * 2;
        }
    }
}
