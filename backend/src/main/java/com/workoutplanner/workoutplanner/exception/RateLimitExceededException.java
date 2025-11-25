package com.workoutplanner.workoutplanner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a rate limit is exceeded.
 * 
 * Following HTTP standards:
 * - Returns HTTP 429 (Too Many Requests)
 * - Industry standard status code used by Google, AWS, Cloudflare
 * 
 * Best Practices:
 * - Should include Retry-After header (handled by exception handler)
 * - Should include rate limit info in response
 * - Should be logged for security monitoring
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {
    
    private final long retryAfterSeconds;
    
    /**
     * Constructs a new RateLimitExceededException with a custom message.
     * 
     * @param message the exception message
     */
    public RateLimitExceededException(String message) {
        super(message);
        this.retryAfterSeconds = 60; // Default: retry after 1 minute
    }
    
    /**
     * Constructs a new RateLimitExceededException with message and retry-after hint.
     * 
     * @param message the exception message
     * @param retryAfterSeconds number of seconds before retry is allowed
     */
    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    /**
     * Get the number of seconds before retry is allowed.
     * Used for Retry-After header (HTTP standard).
     * 
     * @return retry-after seconds
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

