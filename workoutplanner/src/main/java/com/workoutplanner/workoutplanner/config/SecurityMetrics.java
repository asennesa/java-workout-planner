package com.workoutplanner.workoutplanner.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Security metrics for monitoring authentication and authorization events.
 * Provides comprehensive security monitoring and alerting capabilities.
 * 
 * This component follows security monitoring best practices:
 * - Tracks authentication success/failure rates
 * - Monitors authorization events
 * - Records security-related performance metrics
 * - Provides alerting capabilities for security incidents
 */
@Component
public class SecurityMetrics {

    private static final Logger logger = LoggerFactory.getLogger(SecurityMetrics.class);

    private final MeterRegistry meterRegistry;

    // Authentication metrics
    private final Counter authenticationSuccessCounter;
    private final Counter authenticationFailureCounter;
    private final Counter oauth2SuccessCounter;
    private final Counter oauth2FailureCounter;
    
    // Authorization metrics
    private final Counter authorizationSuccessCounter;
    private final Counter authorizationFailureCounter;
    private final Counter tokenRevocationCounter;
    
    // Security event metrics
    private final Counter rateLimitExceededCounter;
    private final Counter suspiciousActivityCounter;
    private final Counter tokenValidationFailureCounter;
    
    // Performance metrics
    private final Timer jwtGenerationTimer;
    private final Timer jwtValidationTimer;
    private final Timer oauth2ProcessingTimer;

    public SecurityMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.authenticationSuccessCounter = Counter.builder("security.auth.success")
            .description("Number of successful authentications")
            .register(meterRegistry);
            
        this.authenticationFailureCounter = Counter.builder("security.auth.failures")
            .description("Number of failed authentications")
            .register(meterRegistry);
            
        this.oauth2SuccessCounter = Counter.builder("security.oauth2.success")
            .description("Number of successful OAuth2 authentications")
            .register(meterRegistry);
            
        this.oauth2FailureCounter = Counter.builder("security.oauth2.failures")
            .description("Number of failed OAuth2 authentications")
            .register(meterRegistry);
            
        this.authorizationSuccessCounter = Counter.builder("security.authorization.success")
            .description("Number of successful authorizations")
            .register(meterRegistry);
            
        this.authorizationFailureCounter = Counter.builder("security.authorization.failures")
            .description("Number of failed authorizations")
            .register(meterRegistry);
            
        this.tokenRevocationCounter = Counter.builder("security.token.revocations")
            .description("Number of token revocations")
            .register(meterRegistry);
            
        this.rateLimitExceededCounter = Counter.builder("security.rate_limit.exceeded")
            .description("Number of rate limit violations")
            .register(meterRegistry);
            
        this.suspiciousActivityCounter = Counter.builder("security.suspicious.activity")
            .description("Number of suspicious activities detected")
            .register(meterRegistry);
            
        this.tokenValidationFailureCounter = Counter.builder("security.token.validation.failures")
            .description("Number of token validation failures")
            .register(meterRegistry);
        
        // Initialize timers
        this.jwtGenerationTimer = Timer.builder("security.jwt.generation.time")
            .description("Time taken to generate JWT tokens")
            .register(meterRegistry);
            
        this.jwtValidationTimer = Timer.builder("security.jwt.validation.time")
            .description("Time taken to validate JWT tokens")
            .register(meterRegistry);
            
        this.oauth2ProcessingTimer = Timer.builder("security.oauth2.processing.time")
            .description("Time taken to process OAuth2 authentication")
            .register(meterRegistry);
    }

    /**
     * Record successful authentication.
     * 
     * @param authType type of authentication (jwt, oauth2, etc.)
     * @param userId user ID
     */
    public void recordAuthenticationSuccess(String authType, String userId) {
        authenticationSuccessCounter.increment();
        logger.info("Authentication success recorded for user: {} with type: {}", userId, authType);
    }

    /**
     * Record failed authentication.
     * 
     * @param authType type of authentication
     * @param reason reason for failure
     * @param userId user ID (if available)
     */
    public void recordAuthenticationFailure(String authType, String reason, String userId) {
        authenticationFailureCounter.increment();
        logger.warn("Authentication failure recorded for user: {} with type: {} - reason: {}", 
                   userId, authType, reason);
    }

    /**
     * Record successful OAuth2 authentication.
     * 
     * @param provider OAuth2 provider (google, github, facebook)
     * @param userId user ID
     */
    public void recordOAuth2Success(String provider, String userId) {
        oauth2SuccessCounter.increment();
        logger.info("OAuth2 success recorded for user: {} with provider: {}", userId, provider);
    }

    /**
     * Record failed OAuth2 authentication.
     * 
     * @param provider OAuth2 provider
     * @param reason reason for failure
     * @param userId user ID (if available)
     */
    public void recordOAuth2Failure(String provider, String reason, String userId) {
        oauth2FailureCounter.increment();
        logger.warn("OAuth2 failure recorded for user: {} with provider: {} - reason: {}", 
                   userId, provider, reason);
    }

    /**
     * Record successful authorization.
     * 
     * @param resource requested resource
     * @param userId user ID
     */
    public void recordAuthorizationSuccess(String resource, String userId) {
        authorizationSuccessCounter.increment();
        logger.debug("Authorization success recorded for user: {} accessing: {}", userId, resource);
    }

    /**
     * Record failed authorization.
     * 
     * @param resource requested resource
     * @param reason reason for failure
     * @param userId user ID
     */
    public void recordAuthorizationFailure(String resource, String reason, String userId) {
        authorizationFailureCounter.increment();
        logger.warn("Authorization failure recorded for user: {} accessing: {} - reason: {}", 
                   userId, resource, reason);
    }

    /**
     * Record token revocation.
     * 
     * @param tokenType type of token (access, refresh)
     * @param userId user ID
     */
    public void recordTokenRevocation(String tokenType, String userId) {
        tokenRevocationCounter.increment();
        logger.info("Token revocation recorded for user: {} - token type: {}", userId, tokenType);
    }

    /**
     * Record rate limit exceeded.
     * 
     * @param endpoint endpoint that exceeded rate limit
     * @param clientIp client IP address
     */
    public void recordRateLimitExceeded(String endpoint, String clientIp) {
        rateLimitExceededCounter.increment();
        logger.warn("Rate limit exceeded for endpoint: {} from IP: {}", endpoint, clientIp);
    }

    /**
     * Record suspicious activity.
     * 
     * @param activityType type of suspicious activity
     * @param details additional details
     * @param clientIp client IP address
     */
    public void recordSuspiciousActivity(String activityType, String details, String clientIp) {
        suspiciousActivityCounter.increment();
        logger.warn("Suspicious activity detected: {} - details: {} from IP: {}", 
                   activityType, details, clientIp);
    }

    /**
     * Record token validation failure.
     * 
     * @param reason reason for validation failure
     * @param userId user ID (if available)
     */
    public void recordTokenValidationFailure(String reason, String userId) {
        tokenValidationFailureCounter.increment();
        logger.warn("Token validation failure for user: {} - reason: {}", userId, reason);
    }

    /**
     * Record JWT generation time.
     * 
     * @param duration time taken to generate JWT
     */
    public void recordJwtGenerationTime(Duration duration) {
        jwtGenerationTimer.record(duration);
    }

    /**
     * Record JWT validation time.
     * 
     * @param duration time taken to validate JWT
     */
    public void recordJwtValidationTime(Duration duration) {
        jwtValidationTimer.record(duration);
    }

    /**
     * Record OAuth2 processing time.
     * 
     * @param duration time taken to process OAuth2
     */
    public void recordOAuth2ProcessingTime(Duration duration) {
        oauth2ProcessingTimer.record(duration);
    }

    /**
     * Get current security metrics summary.
     * 
     * @return security metrics summary
     */
    public String getSecurityMetricsSummary() {
        return String.format(
            "Security Metrics Summary:\n" +
            "- Authentication Success: %.0f\n" +
            "- Authentication Failures: %.0f\n" +
            "- OAuth2 Success: %.0f\n" +
            "- OAuth2 Failures: %.0f\n" +
            "- Authorization Success: %.0f\n" +
            "- Authorization Failures: %.0f\n" +
            "- Token Revocations: %.0f\n" +
            "- Rate Limit Violations: %.0f\n" +
            "- Suspicious Activities: %.0f\n" +
            "- Token Validation Failures: %.0f",
            authenticationSuccessCounter.count(),
            authenticationFailureCounter.count(),
            oauth2SuccessCounter.count(),
            oauth2FailureCounter.count(),
            authorizationSuccessCounter.count(),
            authorizationFailureCounter.count(),
            tokenRevocationCounter.count(),
            rateLimitExceededCounter.count(),
            suspiciousActivityCounter.count(),
            tokenValidationFailureCounter.count()
        );
    }
}
