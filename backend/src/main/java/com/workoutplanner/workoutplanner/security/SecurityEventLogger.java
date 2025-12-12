package com.workoutplanner.workoutplanner.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;

/**
 * Structured logging service for security-relevant events.
 *
 * This service provides standardized logging for security events that can be:
 * - Aggregated and analyzed in log management systems (ELK, Splunk, CloudWatch)
 * - Used for security incident detection and forensics
 * - Required for compliance auditing (SOC2, GDPR, etc.)
 *
 * All log entries include:
 * - Event type for easy filtering
 * - Correlation ID from MDC for request tracing
 * - Structured key=value format for parsing
 *
 * @see <a href="https://owasp.org/www-project-proactive-controls/v3/en/c9-security-logging">OWASP Security Logging</a>
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html">OWASP Logging Cheat Sheet</a>
 */
@Component
public class SecurityEventLogger {

    /**
     * Dedicated logger for security audit events.
     * Configured in logback-spring.xml to write to a separate file.
     */
    private static final Logger securityLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    /**
     * Marker for filtering security events in log aggregation systems.
     */
    private static final Marker SECURITY_MARKER = MarkerFactory.getMarker("SECURITY");

    /**
     * Log successful authentication event.
     *
     * @param userId      Local user ID
     * @param auth0UserId Auth0 subject identifier
     * @param ipAddress   Client IP address
     */
    public void logAuthenticationSuccess(Long userId, String auth0UserId, String ipAddress) {
        securityLog.info(SECURITY_MARKER,
            "event=AUTHENTICATION_SUCCESS userId={} auth0UserId={} ip={} correlationId={}",
            userId, auth0UserId, ipAddress, getCorrelationId());
    }

    /**
     * Log failed authentication attempt.
     *
     * @param auth0UserId Auth0 subject identifier (if available)
     * @param reason      Failure reason
     * @param ipAddress   Client IP address
     */
    public void logAuthenticationFailure(String auth0UserId, String reason, String ipAddress) {
        securityLog.warn(SECURITY_MARKER,
            "event=AUTHENTICATION_FAILURE auth0UserId={} reason={} ip={} correlationId={}",
            auth0UserId != null ? auth0UserId : "unknown", reason, ipAddress, getCorrelationId());
    }

    /**
     * Log authorization denial (access control failure).
     *
     * @param userId       User attempting access
     * @param resourceType Type of resource (WORKOUT, USER, etc.)
     * @param resourceId   ID of the resource
     * @param action       Attempted action (READ, WRITE, DELETE)
     */
    public void logAuthorizationDenied(Long userId, String resourceType, Long resourceId, String action) {
        securityLog.warn(SECURITY_MARKER,
            "event=AUTHORIZATION_DENIED userId={} resourceType={} resourceId={} action={} correlationId={}",
            userId, resourceType, resourceId, action, getCorrelationId());
    }

    /**
     * Log rate limit exceeded event.
     *
     * @param ipAddress Client IP address
     * @param endpoint  Endpoint that was rate limited
     */
    public void logRateLimitExceeded(String ipAddress, String endpoint) {
        securityLog.warn(SECURITY_MARKER,
            "event=RATE_LIMIT_EXCEEDED ip={} endpoint={} correlationId={}",
            ipAddress, endpoint, getCorrelationId());
    }

    /**
     * Log suspicious activity detection.
     *
     * @param description Description of suspicious activity
     * @param ipAddress   Client IP address
     * @param userId      User ID (if authenticated)
     */
    public void logSuspiciousActivity(String description, String ipAddress, Long userId) {
        securityLog.warn(SECURITY_MARKER,
            "event=SUSPICIOUS_ACTIVITY description=\"{}\" ip={} userId={} correlationId={}",
            description, ipAddress, userId != null ? userId : "anonymous", getCorrelationId());
    }

    /**
     * Log email verification failure.
     *
     * @param auth0UserId Auth0 subject identifier
     * @param email       Email address
     * @param ipAddress   Client IP address
     */
    public void logEmailNotVerified(String auth0UserId, String email, String ipAddress) {
        securityLog.warn(SECURITY_MARKER,
            "event=EMAIL_NOT_VERIFIED auth0UserId={} email={} ip={} correlationId={}",
            auth0UserId, maskEmail(email), ipAddress, getCorrelationId());
    }

    /**
     * Log sensitive resource access for audit trail.
     *
     * @param userId       User accessing the resource
     * @param resourceType Type of resource
     * @param resourceId   ID of the resource
     * @param action       Action performed (READ, WRITE, DELETE)
     */
    public void logResourceAccess(Long userId, String resourceType, Long resourceId, String action) {
        securityLog.info(SECURITY_MARKER,
            "event=RESOURCE_ACCESS userId={} resourceType={} resourceId={} action={} correlationId={}",
            userId, resourceType, resourceId, action, getCorrelationId());
    }

    /**
     * Get correlation ID from MDC for request tracing.
     *
     * @return Correlation ID or "N/A" if not set
     */
    private String getCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId != null ? correlationId : "N/A";
    }

    /**
     * Mask email address for privacy (show first char + domain).
     * Example: john@example.com -> j***@example.com
     *
     * @param email Email address to mask
     * @return Masked email
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "unknown";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
