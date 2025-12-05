package com.workoutplanner.workoutplanner.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception for Auth0 authentication failures.
 */
public class Auth0AuthenticationException extends AuthenticationException {

    private final ErrorCode errorCode;

    public Auth0AuthenticationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum ErrorCode {
        EMAIL_NOT_VERIFIED,
        EMAIL_MISSING
    }
}
