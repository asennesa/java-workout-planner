package com.workoutplanner.workoutplanner.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Base exception for Auth0 authentication failures.
 *
 * Industry Best Practice:
 * - Extends Spring Security's AuthenticationException
 * - Properly integrates with Spring Security's exception handling
 * - Provides specific error codes for different failure scenarios
 */
public class Auth0AuthenticationException extends AuthenticationException {

    private final ErrorCode errorCode;

    public Auth0AuthenticationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public Auth0AuthenticationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Error codes for Auth0 authentication failures.
     * These can be used for logging, monitoring, and API responses.
     */
    public enum ErrorCode {
        EMAIL_NOT_VERIFIED("auth0.email_not_verified", "Email address has not been verified"),
        EMAIL_MISSING("auth0.email_missing", "Email claim missing from token"),
        USER_SYNC_FAILED("auth0.user_sync_failed", "Failed to synchronize user profile"),
        INVALID_TOKEN("auth0.invalid_token", "Invalid or malformed JWT token"),
        USER_DISABLED("auth0.user_disabled", "User account has been disabled");

        private final String code;
        private final String defaultMessage;

        ErrorCode(String code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }

        public String getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
}
