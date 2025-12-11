package com.workoutplanner.workoutplanner.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for validation and business logic errors.
 * Demonstrates best practices for handling errors in REST APIs with correct HTTP status codes.
 */
@RestControllerAdvice
public class ValidationExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ValidationExceptionHandler.class);

    private static final String MESSAGE_KEY = "message";
    private static final String STATUS_KEY = "status";
    
    /**
     * Handles validation errors from @Valid annotations.
     * Returns a structured error response with field-specific error messages.
     * 
     * @param ex MethodArgumentNotValidException containing validation errors
     * @return ResponseEntity with validation error details and 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("EXCEPTION: Validation failed. Fields: {}", errors.keySet());
        logger.debug("EXCEPTION: Validation error details: {}", errors);
        
        response.put(MESSAGE_KEY, "Validation failed");
        response.put("errors", errors);
        response.put(STATUS_KEY, HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violation exceptions.
     *
     * @param ex ConstraintViolationException containing validation error
     * @return ResponseEntity with error details and 400 BAD REQUEST
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("EXCEPTION: Constraint violation. Fields: {}", errors.keySet());
        logger.debug("EXCEPTION: Constraint violation details: {}", errors);

        response.put(MESSAGE_KEY, "Validation failed");
        response.put("errors", errors);
        response.put(STATUS_KEY, HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles resource not found exceptions.
     *
     * @param ex ResourceNotFoundException containing error details
     * @return ResponseEntity with error details and 404 NOT FOUND
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();

        logger.debug("EXCEPTION: Resource not found: {}", ex.getMessage());

        response.put(MESSAGE_KEY, ex.getMessage());
        response.put(STATUS_KEY, HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles resource conflict exceptions (e.g., duplicate username/email).
     *
     * @param ex ResourceConflictException containing error details
     * @return ResponseEntity with error details and 409 CONFLICT
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, Object>> handleResourceConflictException(ResourceConflictException ex) {
        Map<String, Object> response = new HashMap<>();

        logger.warn("EXCEPTION: Resource conflict: {}", ex.getMessage());

        response.put(MESSAGE_KEY, ex.getMessage());
        response.put(STATUS_KEY, HttpStatus.CONFLICT.value());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles business logic exceptions.
     *
     * @param ex BusinessLogicException containing error details
     * @return ResponseEntity with error details and 400 BAD REQUEST
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessLogicException(BusinessLogicException ex) {
        Map<String, Object> response = new HashMap<>();

        logger.warn("EXCEPTION: Business logic error: {}", ex.getMessage());
        logger.debug("EXCEPTION: Business logic exception stack trace", ex);

        response.put(MESSAGE_KEY, ex.getMessage());
        response.put(STATUS_KEY, HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles access denied exceptions from @PreAuthorize security annotations.
     * Returned when user attempts to access resources they don't own.
     *
     * @param ex AccessDeniedException thrown by Spring Security
     * @return ResponseEntity with error details and 403 FORBIDDEN
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> response = new HashMap<>();

        logger.warn("EXCEPTION: Access denied: {}", ex.getMessage());

        response.put(MESSAGE_KEY, "Access denied. You don't have permission to access this resource.");
        response.put(STATUS_KEY, HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles illegal argument exceptions (invalid input parameters).
     *
     * @param ex IllegalArgumentException containing error details
     * @return ResponseEntity with error details and 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();

        logger.warn("EXCEPTION: Illegal argument: {}", ex.getMessage());

        response.put(MESSAGE_KEY, ex.getMessage());
        response.put(STATUS_KEY, HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic runtime exceptions as fallback.
     *
     * @param ex RuntimeException containing error details
     * @return ResponseEntity with error details and 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();

        logger.error("EXCEPTION: Unexpected runtime error: {}", ex.getMessage(), ex);

        // Don't expose internal error details to clients in production
        response.put(MESSAGE_KEY, "An unexpected error occurred. Please contact support if the problem persists.");
        response.put(STATUS_KEY, HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
