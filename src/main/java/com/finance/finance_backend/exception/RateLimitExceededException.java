package com.finance.finance_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user exceeds the allowed request rate limit.
 * Maps to HTTP 429 Too Many Requests.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Too many requests. Please try again later.");
    }
}
