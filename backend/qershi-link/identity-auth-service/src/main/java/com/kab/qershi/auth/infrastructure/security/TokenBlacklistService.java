package com.kab.qershi.auth.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service managing real-time JWT revocation (token blacklisting) using Redis in-memory storage.
 * Provides sub-millisecond status checks and automatic key TTL expiration matching token lifespans.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String REDIS_KEY_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Revokes a token by storing its unique JWT ID (jti) in Redis.
     * The Redis key will automatically expire after the remaining token lifespan passes.
     *
     * @param jti                      Unique JWT identifier.
     * @param remainingExpirationMillis Remaining token lifetime in milliseconds.
     */
    public void blacklistToken(String jti, long remainingExpirationMillis) {
        if (jti == null || jti.isBlank()) {
            log.warn("Cannot blacklist token: jti is null or blank.");
            return;
        }
        if (remainingExpirationMillis <= 0) {
            log.debug("Token with jti {} has already expired naturally; skipping Redis blacklist entry.", jti);
            return;
        }

        try {
            String redisKey = REDIS_KEY_PREFIX + jti;
            redisTemplate.opsForValue().set(redisKey, "REVOKED", remainingExpirationMillis, TimeUnit.MILLISECONDS);
            log.info("Blacklisted JWT token in Redis: jti={}, TTL={}ms", jti, remainingExpirationMillis);
        } catch (Exception ex) {
            log.error("Failed to write token blacklist entry to Redis for jti {}: {}", jti, ex.getMessage(), ex);
        }
    }

    /**
     * Checks whether a token's jti is in the Redis blacklist.
     *
     * @param jti Unique JWT identifier.
     * @return true if the token has been revoked/blacklisted; false otherwise.
     */
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        try {
            String redisKey = REDIS_KEY_PREFIX + jti;
            Boolean hasKey = redisTemplate.hasKey(redisKey);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception ex) {
            log.warn("Redis check failed for jti {}: {}. Defaulting to non-blacklisted.", jti, ex.getMessage());
            return false;
        }
    }
}
