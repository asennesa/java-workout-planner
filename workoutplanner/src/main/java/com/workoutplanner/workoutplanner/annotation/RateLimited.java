package com.workoutplanner.workoutplanner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation for applying rate limiting to controller endpoints using Bucket4j.
 * 
 * Bucket4j is the industry-standard rate limiting library for Java:
 * - Used by major companies (Netflix, Amazon, etc.)
 * - Recommended by Spring Boot documentation
 * - Implements Token Bucket algorithm (same as AWS API Gateway, Google Cloud)
 * - Production-proven and battle-tested
 * 
 * Token Bucket Algorithm Explained:
 * - Bucket has capacity (maxRequests)
 * - Tokens refill over time (refillPeriod)
 * - Each request consumes 1 token
 * - If no tokens available → rate limit exceeded
 * - Allows natural bursts while maintaining average rate
 * 
 * Why Token Bucket vs Fixed Window:
 * - More user-friendly (allows reasonable bursts)
 * - Same algorithm used by AWS, Google Cloud, Cloudflare
 * - Better handling of edge cases at window boundaries
 * - Industry standard for API rate limiting
 * 
 * Usage Examples:
 * 
 * // Strict rate limit for password changes (security critical)
 * @RateLimited(capacity = 3, refillTokens = 3, refillPeriod = 15, 
 *              timeUnit = TimeUnit.MINUTES, keyType = KeyType.USER)
 * public void changePassword(...) { }
 * 
 * // Moderate rate limit for existence checks (prevent enumeration)
 * @RateLimited(capacity = 10, refillTokens = 10, refillPeriod = 1, 
 *              timeUnit = TimeUnit.MINUTES, keyType = KeyType.IP)
 * public boolean checkUsername(...) { }
 * 
 * // High-throughput API with burst handling
 * @RateLimited(capacity = 100, refillTokens = 50, refillPeriod = 1, 
 *              timeUnit = TimeUnit.MINUTES, keyType = KeyType.USER)
 * public List<Data> getHighVolumeData(...) { }
 * 
 * @author WorkoutPlanner Team
 * @version 2.0 (Bucket4j implementation)
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * Bucket capacity (maximum tokens in bucket).
     * 
     * This determines the maximum burst size - how many requests can be made
     * in quick succession before rate limiting kicks in.
     * 
     * Industry standards:
     * - Password changes: 3-5 tokens (prevent brute force)
     * - Login attempts: 5-10 tokens (prevent credential stuffing)
     * - API queries: 100-1000 tokens (high throughput)
     * - Public endpoints: 10-60 tokens (prevent abuse)
     * 
     * Bucket4j Token Bucket Pattern:
     * - Capacity = bucket size (max tokens)
     * - Starts full (capacity tokens)
     * - Each request consumes 1 token
     * - Refills at rate specified by refillTokens/refillPeriod
     * 
     * @return bucket capacity (maximum tokens)
     */
    long capacity() default 10;
    
    /**
     * Number of tokens to refill after each refill period.
     * 
     * This controls the sustained rate (average throughput).
     * Can be different from capacity to allow burst but limit sustained rate.
     * 
     * Examples:
     * - capacity=10, refillTokens=10 → allows burst of 10, refills completely
     * - capacity=100, refillTokens=50 → allows burst of 100, but sustains 50/period
     * 
     * Common patterns:
     * - refillTokens = capacity (simple: full refill each period)
     * - refillTokens < capacity (advanced: burst handling)
     * 
     * @return number of tokens to add per refill period
     */
    long refillTokens() default 10;
    
    /**
     * Refill period duration for token bucket.
     * Combined with timeUnit to define how often tokens are added.
     * 
     * Example: refillPeriod = 1, timeUnit = MINUTES means "tokens refill every minute"
     * 
     * With refillTokens, this defines the sustained rate:
     * - refillTokens=10, refillPeriod=1, timeUnit=MINUTES → 10 requests/minute sustained
     * - refillTokens=100, refillPeriod=1, timeUnit=HOURS → 100 requests/hour sustained
     * 
     * @return refill period duration
     */
    long refillPeriod() default 1;
    
    /**
     * Time unit for the time window.
     * 
     * Common patterns:
     * - SECONDS: For high-frequency APIs
     * - MINUTES: For standard APIs and user operations
     * - HOURS: For resource-intensive operations
     * 
     * @return time unit for the time window
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
    
    /**
     * Key type for rate limiting (what to track).
     * 
     * KEY_TYPE determines what we're rate limiting:
     * - USER: Per authenticated user (use for user-specific operations)
     * - IP: Per IP address (use for public endpoints, prevent distributed attacks)
     * - GLOBAL: Shared across all users (use for expensive operations)
     * 
     * @return key type for rate limiting
     */
    KeyType keyType() default KeyType.USER;
    
    /**
     * Custom error message when rate limit is exceeded.
     * Supports {maxRequests}, {timeWindow}, {timeUnit} placeholders.
     * 
     * @return custom error message
     */
    String message() default "Rate limit exceeded. Please try again later.";
    
    /**
     * Key types for rate limiting.
     */
    enum KeyType {
        /**
         * Rate limit per authenticated user.
         * Uses username from Spring Security context.
         * Best for: User-specific operations (password change, profile updates)
         */
        USER,
        
        /**
         * Rate limit per IP address.
         * Uses X-Forwarded-For header or remote address.
         * Best for: Public endpoints, preventing enumeration attacks
         */
        IP,
        
        /**
         * Global rate limit (shared across all users).
         * Uses fixed key for entire application.
         * Best for: Expensive operations (reports, exports)
         */
        GLOBAL
    }
}

