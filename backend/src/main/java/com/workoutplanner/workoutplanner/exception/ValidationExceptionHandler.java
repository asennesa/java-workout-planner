package com.workoutplanner.workoutplanner.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("EXCEPTION: Validation failed. Fields: {}", errors.keySet());
        logger.debug("EXCEPTION: Validation error details: {}", errors);
        
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
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
        
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
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
        
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        
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
        
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.CONFLICT.value());
        
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
        
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles rate limit exceeded exceptions.
     * 
     * Following industry best practices (RFC 6585, Google Cloud, AWS):
     * - Returns HTTP 429 (Too Many Requests) - standard for rate limiting
     * - Includes Retry-After header - tells client when to retry
     * - Includes rate limit info in response body
     * - Logs for security monitoring
     * 
     * Response format follows industry standards:
     * {
     *   "message": "Rate limit exceeded. Maximum 3 requests per 15 minutes allowed.",
     *   "status": 429,
     *   "retryAfter": 900
     * }
     * 
     * Headers:
     * - Retry-After: 900 (seconds until retry allowed)
     * - X-RateLimit-Limit: Maximum requests allowed
     * - X-RateLimit-Reset: Unix timestamp when limit resets
     * 
     * @param ex RateLimitExceededException containing error details
     * @return ResponseEntity with error details and 429 TOO MANY REQUESTS
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceededException(RateLimitExceededException ex) {
        Map<String, Object> response = new HashMap<>();
        
        // Log for security monitoring (high severity)
        logger.warn("SECURITY: Rate limit exceeded: {}", ex.getMessage());
        
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("retryAfter", ex.getRetryAfterSeconds());
        
        // Add Retry-After header (HTTP standard for rate limiting)
        // Tells client when they can retry the request
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        
        // Optional: Add rate limit headers (industry standard)
        // X-RateLimit-* headers are used by GitHub, Stripe, Twitter, etc.
        long resetTime = System.currentTimeMillis() / 1000 + ex.getRetryAfterSeconds();
        headers.add("X-RateLimit-Reset", String.valueOf(resetTime));
        
        return new ResponseEntity<>(response, headers, HttpStatus.TOO_MANY_REQUESTS);
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
        response.put("message", "An unexpected error occurred. Please contact support if the problem persists.");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
