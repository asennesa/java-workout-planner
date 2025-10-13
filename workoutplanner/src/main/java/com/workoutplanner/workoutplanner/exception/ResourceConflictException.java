package com.workoutplanner.workoutplanner.exception;

/**
 * Exception thrown when there is a conflict with existing resources.
 * Results in HTTP 409 CONFLICT response.
 * Used for cases like duplicate username, duplicate email, etc.
 */
public class ResourceConflictException extends RuntimeException {
    
    public ResourceConflictException(String message) {
        super(message);
    }
    
    public ResourceConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
    }
}

