# Entity Validation Implementation

This document outlines the comprehensive validation implementation for the Workout Planner application, following senior engineering best practices.

## Overview

The validation system uses **Bean Validation (JSR-303/380)** with **Hibernate Validator** as the reference implementation, which is the industry standard for Java validation. This approach provides:

- **Declarative validation** using annotations
- **Automatic validation** in Spring Boot applications
- **Custom validators** for business-specific rules
- **Structured error handling** for REST APIs

## Dependencies Added

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## Validation Annotations Used

### Standard Bean Validation Annotations

| Annotation | Purpose | Example Usage |
|------------|---------|---------------|
| `@NotNull` | Ensures field is not null | Required fields like IDs, foreign keys |
| `@NotBlank` | Ensures string is not null, empty, or whitespace | Names, usernames, required text fields |
| `@Email` | Validates email format | User email addresses |
| `@Length` | Validates string length | Text fields with size constraints |
| `@Min` / `@Max` | Validates numeric ranges | Counts, durations, weights |
| `@DecimalMin` / `@DecimalMax` | Validates decimal ranges | Weight values, distances |
| `@Digits` | Validates digit precision | Decimal fields with specific precision |
| `@Pattern` | Validates against regex | Usernames, names, specific formats |
| `@URL` | Validates URL format | Image URLs, external links |

### Hibernate Validator Extensions

| Annotation | Purpose | Example Usage |
|------------|---------|---------------|
| `@Length` | String length validation | Text fields with size limits |

## Entity Validation Details

### 1. User Entity
- **Username**: 3-50 characters, alphanumeric + underscore only
- **Email**: Valid email format, max 255 characters
- **Password Hash**: 60-255 characters (bcrypt hash validation)
- **Names**: 1-50 characters, letters/spaces/hyphens/apostrophes only

### 2. Exercise Entity
- **Name**: 2-100 characters, alphanumeric + spaces/hyphens/parentheses
- **Description**: Max 1000 characters
- **Image URL**: Valid URL format, max 500 characters
- **Enums**: All required (type, target muscle group, difficulty level)

### 3. WorkoutSession Entity
- **Name**: 2-100 characters, alphanumeric + spaces/hyphens/parentheses
- **Description**: Max 1000 characters
- **Duration**: 1-1440 minutes (24 hours max)
- **Custom Validation**: Date validation (startedAt ≤ now, completedAt ≥ startedAt)

### 4. WorkoutExercise Entity
- **Order**: 1-100 (workout sequence)
- **Notes**: Max 500 characters
- **Custom Validation**: Exercise type must match set types

### 5. StrengthSet Entity
- **Set Number**: 1-50
- **Reps**: 1-1000
- **Weight**: 0-999.99 with 2 decimal places
- **Rest Time**: 0-3600 seconds (1 hour max)

### 6. CardioSet Entity
- **Duration**: 1-7200 seconds (2 hours max)
- **Distance**: 0-999999.99 with 2 decimal places
- **Distance Unit**: Valid units (km, m, miles, yards, feet, meters)
- **Rest Time**: 0-3600 seconds (1 hour max)

### 7. FlexibilitySet Entity
- **Duration**: 1-3600 seconds (1 hour max)
- **Intensity**: 1-10 scale
- **Stretch Type**: Max 50 characters, letters/spaces/hyphens/parentheses
- **Rest Time**: 0-3600 seconds (1 hour max)

## Custom Validators

### 1. StrongPassword
- **Purpose**: Validates password strength
- **Requirements**: 8+ characters, uppercase, lowercase, digit, special character
- **Usage**: Applied to password fields (when not hashed)

### 2. ValidWorkoutDates
- **Purpose**: Validates workout session date logic
- **Rules**: 
  - startedAt cannot be in the future
  - completedAt cannot be before startedAt
  - completedAt cannot be in the future

### 3. ValidExerciseType
- **Purpose**: Ensures exercise type matches set types
- **Rules**:
  - STRENGTH exercises → strength sets only
  - CARDIO exercises → cardio sets only
  - FLEXIBILITY exercises → flexibility sets only

## Error Handling

### ValidationExceptionHandler
- **Global exception handler** for validation errors
- **Structured error responses** with field-specific messages
- **HTTP 400 Bad Request** status for validation failures
- **JSON response format**:
  ```json
  {
    "message": "Validation failed",
    "errors": {
      "fieldName": "Error message"
    },
    "status": 400
  }
  ```

## Best Practices Implemented

### 1. **Layered Validation**
- **Entity Level**: Database constraints + Bean Validation
- **Service Level**: Business logic validation
- **Controller Level**: Input validation with `@Valid`

### 2. **Comprehensive Coverage**
- **Required Fields**: All mandatory fields validated
- **Data Types**: Proper type validation (strings, numbers, dates)
- **Ranges**: Realistic min/max values for all numeric fields
- **Formats**: Regex patterns for usernames, names, URLs

### 3. **Security Considerations**
- **Input Sanitization**: Pattern validation prevents injection
- **Data Integrity**: Foreign key validation
- **Business Rules**: Custom validators for domain logic

### 4. **Performance Optimization**
- **Lazy Validation**: Validation occurs only when needed
- **Efficient Patterns**: Compiled regex patterns for reuse
- **Minimal Overhead**: Validation annotations have minimal runtime cost

### 5. **Maintainability**
- **Clear Messages**: Descriptive error messages
- **Modular Design**: Separate custom validators
- **Documentation**: Comprehensive validation documentation

## Usage Examples

### Controller Validation
```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    // Validation happens automatically
    return userService.save(user);
}
```

### Service Layer Validation
```java
@Service
public class UserService {
    public User save(@Valid User user) {
        // Additional business validation can be added here
        return userRepository.save(user);
    }
}
```

### Manual Validation
```java
@Autowired
private Validator validator;

public void validateUser(User user) {
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    if (!violations.isEmpty()) {
        // Handle validation errors
    }
}
```

## Testing Recommendations

1. **Unit Tests**: Test each validator individually
2. **Integration Tests**: Test validation in controller endpoints
3. **Edge Cases**: Test boundary values and invalid inputs
4. **Custom Validators**: Test business logic validation rules

## Monitoring and Logging

- **Validation Errors**: Log validation failures for debugging
- **Performance Metrics**: Monitor validation overhead
- **Error Tracking**: Track common validation failures for UX improvements

This implementation provides a robust, maintainable, and secure validation system that follows industry best practices and senior engineering standards.

