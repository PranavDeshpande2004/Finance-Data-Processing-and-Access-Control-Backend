package com.finance.finance_backend.ratelimit;

import com.finance.finance_backend.config.RateLimitConfig;
import com.finance.finance_backend.exception.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Intercepts incoming requests and enforces per-user rate limiting.
 * Uses Bucket4j token bucket algorithm.
 * Throws RateLimitExceededException (429) when limit is exceeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor {

    private final RateLimitConfig rateLimitConfig;

    /**
     * Checks the rate limit for the currently authenticated user.
     * Falls back to the client IP address for unauthenticated requests.
     *
     * @param request the incoming HTTP request
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public void checkRateLimit(HttpServletRequest request) {
        String key    = resolveKey(request);
        Bucket bucket = rateLimitConfig.resolveBucket(key);

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for key: {}", key);
            throw new RateLimitExceededException();
        }

        log.debug("Rate limit check passed for key: {} | remaining tokens: {}",
                key, bucket.getAvailableTokens());
    }

    /**
     * Resolves the rate limit key.
     * Uses authenticated user email if available,
     * otherwise falls back to the client IP address.
     *
     * @param request the incoming HTTP request
     * @return string key for the rate limit bucket
     */
    private String resolveKey(HttpServletRequest request) {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }

        return getClientIp(request);
    }

    /**
     * Extracts the real client IP address from the request.
     * Handles X-Forwarded-For header for proxied requests.
     *
     * @param request the incoming HTTP request
     * @return client IP address string
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
