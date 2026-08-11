package com.kab.qershi.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Inbound rate limiting filter protecting all public-facing identity-auth-service endpoints.
 * Uses an in-memory sliding-window counter keyed by client IP address.
 *
 * <p>This is an interim solution until a centralized API Gateway (Kong/Nginx) is deployed
 * in front of all microservices. When a gateway is added, this filter should be removed
 * and rate limiting responsibility transferred to the gateway layer.
 *
 * <p><b>Limits applied:</b>
 * <ul>
 *   <li>POST /api/v1/auth/login           -- 5 attempts per IP per 1 minute</li>
 *   <li>POST /api/v1/pin/resend           -- 3 attempts per IP per 10 minutes</li>
 *   <li>POST /api/v1/pin/resend/{userId}  -- 3 attempts per IP per 10 minutes</li>
 *   <li>POST /api/v1/sacco/onboard        -- 10 attempts per IP per 1 hour</li>
 * </ul>
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Window durations in seconds
    private static final long LOGIN_WINDOW_SECONDS      = 60L;       // 1 minute
    private static final long PIN_RESEND_WINDOW_SECONDS = 10L * 60L; // 10 minutes
    private static final long ONBOARD_WINDOW_SECONDS    = 60L * 60L; // 1 hour

    // Maximum allowed requests per window per IP
    private static final int LOGIN_MAX_REQUESTS      = 5;
    private static final int PIN_RESEND_MAX_REQUESTS = 3;
    private static final int ONBOARD_MAX_REQUESTS    = 10;

    /**
     * Internal record holding the request count and the timestamp when
     * the current window started. Stored per (endpoint-prefix + IP).
     */
    private record RateLimitBucket(AtomicInteger count, Instant windowStart) {}

    /**
     * Concurrent map holding all active rate limit buckets.
     * Key format: "{endpoint-prefix}:{clientIp}" e.g. "login:192.168.1.10"
     */
    private final ConcurrentHashMap<String, RateLimitBucket> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path   = request.getRequestURI();
        String method = request.getMethod();

        // Rate limiting only applies to POST requests on specific public endpoints
        if (!"POST".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRule rule = resolveRule(path);
        if (rule == null) {
            // Not a rate-limited path — let it pass through
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp  = resolveClientIp(request);
        String bucketKey = rule.prefix() + ":" + clientIp;

        boolean allowed = checkAndIncrement(bucketKey, rule.maxRequests(), rule.windowSeconds());

        if (!allowed) {
            log.warn("Rate limit exceeded for [{}] from IP [{}]. Returning 429.", path, clientIp);
            sendRateLimitResponse(response, rule);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Resolves which rate limit rule applies to the given request path.
     * Returns null if the path is not subject to rate limiting.
     */
    private RateLimitRule resolveRule(String path) {
        if (path.equals("/api/v1/auth/login")) {
            return new RateLimitRule(
                    "login",
                    LOGIN_MAX_REQUESTS,
                    LOGIN_WINDOW_SECONDS,
                    "Too many login attempts. Please wait 1 minute before trying again."
            );
        }
        if (path.startsWith("/api/v1/pin/resend")) {
            return new RateLimitRule(
                    "pin_resend",
                    PIN_RESEND_MAX_REQUESTS,
                    PIN_RESEND_WINDOW_SECONDS,
                    "Too many PIN resend requests. Please wait 10 minutes before trying again."
            );
        }
        if (path.equals("/api/v1/sacco/onboard")) {
            return new RateLimitRule(
                    "onboard",
                    ONBOARD_MAX_REQUESTS,
                    ONBOARD_WINDOW_SECONDS,
                    "Too many onboarding requests from this IP. Please wait 1 hour."
            );
        }
        return null;
    }

    /**
     * Checks the current request count for this bucket and increments it.
     * Resets the window if the current window has expired.
     *
     * @param bucketKey     Unique key for this IP + endpoint combination.
     * @param maxRequests   Maximum allowed requests within the window.
     * @param windowSeconds Window duration in seconds.
     * @return true if the request is allowed; false if limit is exceeded.
     */
    private boolean checkAndIncrement(String bucketKey, int maxRequests, long windowSeconds) {
        Instant now = Instant.now();

        rateLimitMap.compute(bucketKey, (key, existing) -> {
            if (existing == null || now.isAfter(existing.windowStart().plusSeconds(windowSeconds))) {
                // No bucket yet, or window has expired — start a fresh window at count 1
                return new RateLimitBucket(new AtomicInteger(1), now);
            }
            // Window still active — increment in place
            existing.count().incrementAndGet();
            return existing;
        });

        RateLimitBucket bucket = rateLimitMap.get(bucketKey);
        return bucket != null && bucket.count().get() <= maxRequests;
    }

    /**
     * Resolves the real client IP address. Respects the X-Forwarded-For header
     * that will be set by an upstream Nginx Ingress controller or load balancer
     * when the API Gateway is added later.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For may be a comma-separated list; the first entry is the original client
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Writes a standardized RFC 6585-compliant 429 Too Many Requests JSON response.
     */
    private void sendRateLimitResponse(HttpServletResponse response, RateLimitRule rule) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":429," +
                "\"error\":\"Too Many Requests\"," +
                "\"message\":\"" + rule.message() + "\"}"
        );
    }

    /**
     * Configuration record for a specific rate-limited endpoint.
     *
     * @param prefix        Short identifier used as the bucket key prefix.
     * @param maxRequests   Maximum requests allowed within the window.
     * @param windowSeconds Window duration in seconds.
     * @param message       User-facing message returned in the 429 response body.
     */
    private record RateLimitRule(String prefix, int maxRequests, long windowSeconds, String message) {}
}
