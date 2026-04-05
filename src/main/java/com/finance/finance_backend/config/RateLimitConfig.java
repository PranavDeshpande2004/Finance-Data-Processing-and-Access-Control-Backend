package com.finance.finance_backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration using Bucket4j.
 * Maintains one bucket per user (keyed by email).
 * Each bucket allows a configurable number of requests
 * per minute before returning 429 Too Many Requests.
 */
@Configuration
public class RateLimitConfig {

    @Value("${app.rate-limit.requests}")
    private int requests;

    @Value("${app.rate-limit.duration-minutes}")
    private int durationMinutes;

    private final Map<String, Bucket> bucketCache =
            new ConcurrentHashMap<>();

    /**
     * Returns the rate limit bucket for a given user email.
     * Creates a new bucket if one does not exist.
     *
     * @param email the user's email address
     * @return the Bucket4j Bucket for this user
     */
    public Bucket resolveBucket(String email) {
        return bucketCache.computeIfAbsent(email, this::createNewBucket);
    }

    /**
     * Creates a new Bucket with the configured capacity and refill rate.
     */
    private Bucket createNewBucket(String email) {
        Bandwidth limit = Bandwidth.classic(
                requests,
                Refill.greedy(requests, Duration.ofMinutes(durationMinutes))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
