package com.workoutplanner.workoutplanner.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ValidationExceptionHandler.
 * Tests that each exception handler returns correct HTTP status and response format.
 */
@DisplayName("ValidationExceptionHandler Unit Tests")
class ValidationExceptionHandlerTest {

    private ValidationExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ValidationExceptionHandler();
    }

    // ==================== VALIDATION EXCEPTION TESTS ====================

    @Nested
    @DisplayName("handleValidationExceptions Tests")
    class HandleValidationExceptionsTests {

        @Test
        @DisplayName("Should return 400 with field errors for validation exception")
        void shouldReturn400WithFieldErrors() {
            // Arrange
            Object target = new Object();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
            bindingResult.addError(new FieldError("request", "username", "Username is required"));
            bindingResult.addError(new FieldError("request", "email", "Invalid email format"));

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                .isNotNull()
                .containsEntry("message", "Validation failed")
                .containsEntry("status", 400);

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
            assertThat(errors)
                .containsEntry("username", "Username is required")
                .containsEntry("email", "Invalid email format");
        }

        @Test
        @DisplayName("Should handle single field error")
        void shouldHandleSingleFieldError() {
            // Arrange
            Object target = new Object();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
            bindingResult.addError(new FieldError("request", "password", "Password must be at least 8 characters"));

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
            assertThat(errors)
                .hasSize(1)
                .containsEntry("password", "Password must be at least 8 characters");
        }
    }

    // ==================== CONSTRAINT VIOLATION EXCEPTION TESTS ====================

    @Nested
    @DisplayName("handleConstraintViolationException Tests")
    class HandleConstraintViolationExceptionTests {

        @Test
        @DisplayName("Should return 400 with constraint violations")
        void shouldReturn400WithConstraintViolations() {
            // Arrange
            Set<ConstraintViolation<?>> violations = new HashSet<>();

            ConstraintViolation<?> violation1 = createMockViolation("userId", "must not be null");
            ConstraintViolation<?> violation2 = createMockViolation("name", "size must be between 1 and 100");
            violations.add(violation1);
            violations.add(violation2);

            ConstraintViolationException exception = new ConstraintViolationException(violations);

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConstraintViolationException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                .isNotNull()
                .containsEntry("message", "Validation failed")
                .containsEntry("status", 400);

            @SuppressWarnings("unchecked")
            Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
            assertThat(errors)
                .containsEntry("userId", "must not be null")
                .containsEntry("name", "size must be between 1 and 100");
        }

        private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn(propertyPath);
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn(message);
            return violation;
        }
    }

    // ==================== RESOURCE NOT FOUND EXCEPTION TESTS ====================

    @Nested
    @DisplayName("handleResourceNotFoundException Tests")
    class HandleResourceNotFoundExceptionTests {

        @Test
        @DisplayName("Should return 404 with error message")
        void shouldReturn404WithErrorMessage() {
            // Arrange
            ResourceNotFoundException exception = new ResourceNotFoundException("User", "id", 123L);

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceNotFoundException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", 404);
            assertThat(response.getBody().get("message").toString()).contains("User");
        }

        @Test
        @DisplayName("Should return 404 with custom message")
        void shouldReturn404WithCustomMessage() {
            // Arrange
            ResourceNotFoundException exception = new ResourceNotFoundException("Workout session not found");

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceNotFoundException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).containsEntry("message", "Workout session not found");
        }
    }

    // ==================== RESOURCE CONFLICT EXCEPTION TESTS ====================

    @Nested
    @DisplayName("handleResourceConflictException Tests")
    class HandleResourceConflictExceptionTests {

        @Test
        @DisplayName("Should return 409 with conflict message")
        void shouldReturn409WithConflictMessage() {
            // Arrange
            ResourceConflictException exception = new ResourceConflictException("User", "username", "testuser");

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceConflictException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", 409);
            assertThat(response.getBody().get("message").toString()).contains("username");
        }

        @Test
        @DisplayName("Should return 409 for duplicate email")
        void shouldReturn409ForDuplicateEmail() {
            // Arrange
            ResourceConflictException exception = new ResourceConflictException("User", "email", "test@example.com");

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceConflictException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody().get("message").toString()).contains("email");
        }
    }

    // ==================== BUSINESS LOGIC EXCEPTION TESTS ====================

    @Nested
    @DisplayName("handleBusinessLogicException Tests")
    class HandleBusinessLogicExceptionTests {

        @Test
        @DisplayName("Should return 400 with business logic error message")
        void shouldReturn400WithBusinessLogicErrorMessage() {
            // Arrange
            BusinessLogicException exception = new BusinessLogicException("Cannot delete user with active workouts");

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBusinessLogicException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", 400)
                .containsEntry("message", "Cannot delete user with active workouts");
        }

        @Test
        @DisplayName("Should return 400 for invalid state transition")
        void shouldReturn400ForInvalidStateTransition() {
            // Arrange
            BusinessLogicException exception = new BusinessLogicException(
                    "Cannot transition workout from COMPLETED to IN_PROGRESS");

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBusinessLogicException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("message", "Cannot transition workout from COMPLETED to IN_PROGRESS");
        }
    }

    // ==================== RUNTIME EXCEPTION TESTS ====================

    @Nested
    @DisplayName("handleRuntimeException Tests")
    class HandleRuntimeExceptionTests {

        @Test
        @DisplayName("Should return 500 with generic error message")
        void shouldReturn500WithGenericErrorMessage() {
            // Arrange
            RuntimeException exception = new RuntimeException("Unexpected database error");

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntimeException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", 500);
            // Should NOT expose internal error message
            String message = response.getBody().get("message").toString();
            assertThat(message)
                .doesNotContain("database")
                .contains("unexpected error");
        }

        @Test
        @DisplayName("Should not expose stack trace in response")
        void shouldNotExposeStackTraceInResponse() {
            // Arrange
            RuntimeException exception = new RuntimeException("SQL injection attempt detected: DROP TABLE users");

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntimeException(exception);

            // Assert
            assertThat(response.getBody().get("message").toString())
                    .doesNotContain("SQL");
            assertThat(response.getBody().get("message").toString())
                    .doesNotContain("DROP TABLE");
        }

        @Test
        @DisplayName("Should handle NullPointerException")
        void shouldHandleNullPointerException() {
            // Arrange
            NullPointerException exception = new NullPointerException("Object reference is null");

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntimeException(exception);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().get("message").toString())
                    .doesNotContain("null");
        }
    }

    // ==================== RESPONSE FORMAT TESTS ====================

    @Nested
    @DisplayName("Response Format Tests")
    class ResponseFormatTests {

        @Test
        @DisplayName("All responses should contain status field")
        void allResponsesShouldContainStatusField() {
            // Test each handler returns status field
            ResourceNotFoundException notFound = new ResourceNotFoundException("Test");
            ResponseEntity<Map<String, Object>> notFoundResponse =
                    exceptionHandler.handleResourceNotFoundException(notFound);
            assertThat(notFoundResponse.getBody()).containsKey("status");

            ResourceConflictException conflict = new ResourceConflictException("Resource", "field", "value");
            ResponseEntity<Map<String, Object>> conflictResponse =
                    exceptionHandler.handleResourceConflictException(conflict);
            assertThat(conflictResponse.getBody()).containsKey("status");

            BusinessLogicException business = new BusinessLogicException("Test");
            ResponseEntity<Map<String, Object>> businessResponse =
                    exceptionHandler.handleBusinessLogicException(business);
            assertThat(businessResponse.getBody()).containsKey("status");

            RuntimeException runtime = new RuntimeException("Test");
            ResponseEntity<Map<String, Object>> runtimeResponse =
                    exceptionHandler.handleRuntimeException(runtime);
            assertThat(runtimeResponse.getBody()).containsKey("status");
        }

        @Test
        @DisplayName("All responses should contain message field")
        void allResponsesShouldContainMessageField() {
            // Test each handler returns message field
            ResourceNotFoundException notFound = new ResourceNotFoundException("Test");
            ResponseEntity<Map<String, Object>> notFoundResponse =
                    exceptionHandler.handleResourceNotFoundException(notFound);
            assertThat(notFoundResponse.getBody()).containsKey("message");

            ResourceConflictException conflict = new ResourceConflictException("Resource", "field", "value");
            ResponseEntity<Map<String, Object>> conflictResponse =
                    exceptionHandler.handleResourceConflictException(conflict);
            assertThat(conflictResponse.getBody()).containsKey("message");

            BusinessLogicException business = new BusinessLogicException("Test");
            ResponseEntity<Map<String, Object>> businessResponse =
                    exceptionHandler.handleBusinessLogicException(business);
            assertThat(businessResponse.getBody()).containsKey("message");

            RuntimeException runtime = new RuntimeException("Test");
            ResponseEntity<Map<String, Object>> runtimeResponse =
                    exceptionHandler.handleRuntimeException(runtime);
            assertThat(runtimeResponse.getBody()).containsKey("message");
        }

        @Test
        @DisplayName("Validation responses should contain errors field")
        void validationResponsesShouldContainErrorsField() {
            // Arrange
            Object target = new Object();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
            bindingResult.addError(new FieldError("request", "field", "error"));
            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

            // Act
            ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

            // Assert
            assertThat(response.getBody()).containsKey("errors");
        }
    }
}
