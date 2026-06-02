package com.urbanmicrocad.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * JWT token 黑名单接口。
 * Redis 可用时自动使用 Redis 实现（多实例共享），否则使用内存实现（单实例）。
 */
public interface TokenBlacklist {

    void add(String jti, Instant expiresAt);

    boolean isBlacklisted(String jti);

    /**
     * 内存实现（默认，单实例部署）。
     * 定时清理过期条目防止内存泄漏。
     */
    @Component
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    class InMemoryTokenBlacklist implements TokenBlacklist {

        private static final Logger log = LoggerFactory.getLogger(InMemoryTokenBlacklist.class);

        private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

        @Override
        public void add(String jti, Instant expiresAt) {
            blacklistedTokens.put(jti, expiresAt);
            log.debug("Token blacklisted (in-memory): jti={}", jti);
        }

        @Override
        public boolean isBlacklisted(String jti) {
            if (jti == null) return false;
            Instant expiry = blacklistedTokens.get(jti);
            if (expiry == null) return false;
            if (expiry.isBefore(Instant.now())) {
                blacklistedTokens.remove(jti);
                return false;
            }
            return true;
        }

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

    /**
     * Redis 实现（生产环境，多实例共享）。
     * 利用 Redis TTL 自动过期，无需手动清理。
     */
    @Component
    @org.springframework.boot.autoconfigure.condition.ConditionalOnBean(StringRedisTemplate.class)
    class RedisTokenBlacklist implements TokenBlacklist {

        private static final Logger log = LoggerFactory.getLogger(RedisTokenBlacklist.class);
        private static final String KEY_PREFIX = "token:blacklist:";

        private final StringRedisTemplate redisTemplate;

        public RedisTokenBlacklist(StringRedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void add(String jti, Instant expiresAt) {
            if (jti == null || expiresAt == null) return;
            long ttlSeconds = Math.max(1, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
            redisTemplate.opsForValue().set(KEY_PREFIX + jti, "1", ttlSeconds, TimeUnit.SECONDS);
            log.debug("Token blacklisted (Redis): jti={}, ttl={}s", jti, ttlSeconds);
        }

        @Override
        public boolean isBlacklisted(String jti) {
            if (jti == null) return false;
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
        }
    }
}
