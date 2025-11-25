package com.workoutplanner.workoutplanner.exception;

/**
 * Exception thrown when business logic validation fails.
 * Results in HTTP 400 BAD REQUEST response.
 * Used for business rule violations that are not covered by bean validation.
 */
public class BusinessLogicException extends RuntimeException {
    
    public BusinessLogicException(String message) {
        super(message);
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}

