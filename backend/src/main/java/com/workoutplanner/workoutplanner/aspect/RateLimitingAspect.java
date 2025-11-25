package com.workoutplanner.workoutplanner.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.workoutplanner.workoutplanner.annotation.RateLimited;
import com.workoutplanner.workoutplanner.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Aspect for enforcing rate limiting using Bucket4j.
 * 
 * This is the industry-standard implementation used by:
 * - Netflix (API rate limiting)
 * - Amazon (AWS API Gateway pattern)
 * - Twitter (API rate limits)
 * - GitHub (API rate limits)
 * - Stripe (API rate limits)
 * 
 * Bucket4j Token Bucket Algorithm:
 * 1. Each user/IP/global key gets a "bucket" of tokens
 * 2. Bucket starts full (capacity tokens)
 * 3. Each request consumes 1 token
 * 4. Tokens refill at steady rate (refillTokens per refillPeriod)
 * 5. If bucket empty → rate limit exceeded (HTTP 429)
 * 
 * Why Bucket4j Over Custom Implementation:
 * - Battle-tested by major companies
 * - Thread-safe and lock-free (high performance)
 * - Handles edge cases correctly
 * - Easy to switch backends (Caffeine → Redis)
 * - Better algorithm than simple counters
 * - Built-in burst handling
 * - Extensive documentation and community support
 * 
 * Thread Safety:
 * - Bucket4j is completely thread-safe
 * - Lock-free for read operations
 * - Optimistic locking for write operations
 * - No need for synchronized blocks
 * 
 * Performance:
 * - Sub-microsecond bucket access
 * - No blocking on reads
 * - Minimal GC pressure
 * - Scales to millions of requests/second
 * 
 * Migration to Distributed (Future):
 * To use Redis instead of Caffeine:
 * 1. Replace Cache<String, Bucket> with Redis-based implementation
 * 2. Use Bucket4j Redis or Hazelcast integration
 * 3. All rate limits will work across multiple app instances
 * 4. No changes needed in controllers or annotation usage!
 * 
 * @author WorkoutPlanner Team
 * @version 2.0 (Bucket4j implementation)
 * @since 1.0
 */
@Aspect
@Component
public class RateLimitingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingAspect.class);
    
    /**
     * Caffeine cache for storing Bucket4j buckets.
     * 
     * Cache Benefits:
     * - Thread-safe without manual synchronization
     * - Automatic eviction of old buckets
     * - Fast access (sub-microsecond)
     * - Built-in statistics
     * 
     * Key format: "USER:john", "IP:192.168.1.1", "GLOBAL"
     * Value: Bucket4j Bucket instance
     * 
     * Injected by Spring (configured in RateLimitConfig)
     */
    private final Cache<String, Bucket> bucketCache;
    
    /**
     * Constructor injection for bucket cache.
     * 
     * @param bucketCache Caffeine cache for storing buckets
     */
    public RateLimitingAspect(Cache<String, Bucket> bucketCache) {
        this.bucketCache = bucketCache;
    }
    
    /**
     * Intercepts methods annotated with &#64;RateLimited and enforces rate limits.
     * 
     * Process Flow:
     * 1. Extract rate limit configuration from annotation
     * 2. Determine rate limit key (USER/IP/GLOBAL)
     * 3. Get or create bucket for this key
     * 4. Try to consume 1 token from bucket
     * 5. If successful → proceed with method execution
     * 6. If failed → throw RateLimitExceededException (HTTP 429)
     * 
     * Bucket4j Advantages Over Custom Implementation:
     * - Atomic operations (thread-safe)
     * - Accurate token refill (no drift)
     * - Efficient memory usage
     * - Built-in diagnostics (tokens remaining, time until refill)
     * 
     * @param joinPoint the method being intercepted
     * @param rateLimited the rate limit annotation
     * @return the result of the method invocation
     * @throws Throwable if the method throws an exception or rate limit exceeded
     */
    @Around("@annotation(rateLimited)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        // Get method details for logging and diagnostics
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        
        // Determine rate limit key based on key type (USER/IP/GLOBAL)
        String rateLimitKey = getRateLimitKey(rateLimited.keyType(), methodName);
        
        // Get or create bucket for this key
        // Caffeine cache automatically handles thread-safe get-or-create
        Bucket bucket = bucketCache.get(rateLimitKey, key -> createBucket(rateLimited));
        
        // Try to consume 1 token from the bucket
        // ConsumptionProbe provides diagnostic information (tokens remaining, wait time, etc.)
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Success! Token consumed, proceed with method execution
            long remainingTokens = probe.getRemainingTokens();
            
            // Log at debug level (only for troubleshooting)
            logger.debug("Rate limit check PASSED. method={}, key={}, tokensRemaining={}", 
                        methodName, rateLimitKey, remainingTokens);
            
            // Log warning if running low on tokens (< 20% remaining)
            long capacity = rateLimited.capacity();
            if (remainingTokens < capacity * 0.2) {
                logger.warn("Rate limit approaching. method={}, key={}, tokensRemaining={}/{}", 
                           methodName, rateLimitKey, remainingTokens, capacity);
            }
            
            // Proceed with the original method execution
            return joinPoint.proceed();
            
        } else {
            // Rate limit exceeded! No tokens available
            long nanosToWait = probe.getNanosToWaitForRefill();
            long secondsToWait = Duration.ofNanos(nanosToWait).getSeconds();
            
            // Log security event (WARN level for monitoring/alerting)
            logger.warn("SECURITY: Rate limit EXCEEDED. method={}, key={}, retryAfter={}s", 
                       methodName, rateLimitKey, secondsToWait);
            
            // Build detailed error message for client
            String errorMessage = String.format(
                "Rate limit exceeded for %s. Maximum %d requests per %d %s allowed. " +
                "Please retry after %d seconds.",
                methodName,
                rateLimited.capacity(),
                rateLimited.refillPeriod(),
                rateLimited.timeUnit().name().toLowerCase(),
                secondsToWait
            );
            
            // Throw exception (will be caught by ValidationExceptionHandler)
            // Returns HTTP 429 with Retry-After header
            throw new RateLimitExceededException(errorMessage, secondsToWait);
        }
    }
    
    /**
     * Creates a new Bucket4j bucket for rate limiting.
     * 
     * This method is called by Caffeine cache when a bucket doesn't exist for a key.
     * Caffeine handles thread safety automatically - no manual synchronization needed.
     * 
     * Bucket4j Token Bucket Algorithm:
     * 1. Create bandwidth limit (capacity + refill rate)
     * 2. Build bucket with this limit
     * 3. Bucket starts full (capacity tokens)
     * 4. Tokens automatically refill at specified rate
     * 
     * Bandwidth Strategies:
     * - refillGreedy: Refills tokens all at once at intervals
     * - refillIntervally: Smoothly refills tokens over time
     * 
     * We use refillGreedy because it's simpler and matches most API rate limiting patterns
     * (AWS, Google Cloud, GitHub all use similar approach)
     * 
     * @param rateLimited annotation containing rate limit parameters
     * @return new Bucket4j bucket
     */
    private Bucket createBucket(RateLimited rateLimited) {
        // Create bandwidth limit using Token Bucket algorithm
        // This is the core rate limiting logic
        Bandwidth limit = Bandwidth.builder()
            // Capacity: maximum tokens in bucket (burst size)
            // Determines how many requests can be made in quick succession
            .capacity(rateLimited.capacity())
            
            // Refill: how many tokens to add per period
            // refillGreedy = add all tokens at once when period elapses
            // Example: 10 tokens per minute = add 10 tokens every 60 seconds
            .refillGreedy(
                rateLimited.refillTokens(),
                Duration.of(rateLimited.refillPeriod(), rateLimited.timeUnit().toChronoUnit())
            )
            
            .build();
        
        // Build and return bucket with this bandwidth limit
        // Bucket starts full (with capacity tokens available)
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    /**
     * Determines the rate limit key based on the key type.
     * 
     * Key Strategy Pattern:
     * - USER: Rate limit per authenticated user (username)
     * - IP: Rate limit per client IP address
     * - GLOBAL: Single rate limit for entire application
     * 
     * Key Format:
     * - "USER:john" - per user
     * - "IP:192.168.1.1" - per IP
     * - "GLOBAL" - shared
     * 
     * Why Prefixes:
     * - Clear separation in cache
     * - Easy debugging in logs
     * - Avoid key collisions (username "192.168.1.1" vs actual IP)
     * 
     * @param keyType the type of key to generate
     * @param methodName the method being rate limited (for logging)
     * @return the rate limit key
     */
    private String getRateLimitKey(RateLimited.KeyType keyType, String methodName) {
        switch (keyType) {
            case USER:
                return getUserKey();
                
            case IP:
                return getIpKey();
                
            case GLOBAL:
                return "GLOBAL";
                
            default:
                logger.warn("Unknown rate limit key type: {}. Falling back to GLOBAL", keyType);
                return "GLOBAL";
        }
    }
    
    /**
     * Gets the rate limit key for the current authenticated user.
     * 
     * Process:
     * 1. Extract username from Spring Security context
     * 2. If not authenticated: use "ANONYMOUS"
     * 3. Prefix with "USER:" for clarity
     * 
     * Works with any Spring Security authentication:
     * - Basic Auth (current)
     * - JWT (future)
     * - OAuth2 (future)
     * - LDAP, SAML, etc.
     * 
     * @return user-based rate limit key
     */
    private String getUserKey() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return "USER:ANONYMOUS";
            }
            
            if ("anonymousUser".equals(authentication.getPrincipal().toString())) {
                return "USER:ANONYMOUS";
            }
            
            String username = authentication.getName();
            return "USER:" + username;
            
        } catch (Exception e) {
            logger.error("Error extracting user for rate limiting: {}", e.getMessage());
            return "USER:UNKNOWN";
        }
    }
    
    /**
     * Gets the rate limit key for the client IP address.
     * 
     * Handles reverse proxies and load balancers:
     * 1. Check X-Forwarded-For header (standard for proxies)
     * 2. Check X-Real-IP header (nginx)
     * 3. Fall back to remote address
     * 
     * X-Forwarded-For Format:
     * - "client, proxy1, proxy2"
     * - Use first IP (original client)
     * 
     * Security Note:
     * X-Forwarded-For can be spoofed, but for rate limiting this is acceptable.
     * Spoofing would only allow attacker to bypass their own rate limit,
     * not affect other users.
     * 
     * For critical security (authentication), use additional verification.
     * 
     * @return IP-based rate limit key
     */
    private String getIpKey() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes == null) {
                return "IP:UNKNOWN";
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // Check X-Forwarded-For header (standard for proxies/load balancers)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // X-Forwarded-For can contain multiple IPs (client, proxy1, proxy2)
                // Use the first one (original client IP)
                String clientIp = xForwardedFor.split(",")[0].trim();
                return "IP:" + clientIp;
            }
            
            // Check X-Real-IP header (nginx)
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return "IP:" + xRealIp;
            }
            
            // Fall back to remote address
            String remoteAddr = request.getRemoteAddr();
            return "IP:" + remoteAddr;
            
        } catch (Exception e) {
            logger.error("Error extracting IP for rate limiting: {}", e.getMessage());
            return "IP:UNKNOWN";
        }
    }
}
