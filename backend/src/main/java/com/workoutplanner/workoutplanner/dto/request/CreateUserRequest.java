package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for user creation (Auth0 synchronization only).
 * 
 * Note: User registration is handled by Auth0, not through this API.
 * This DTO is used internally by Auth0UserSyncService to create local
 * user records when Auth0 users first authenticate.
 * 
 * Following Jakarta Bean Validation best practices:
 * - Constraints without groups belong to the Default group
 * - This allows validation with simple @Valid annotation
 * - Keeps validation simple and maintainable
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Length(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Length(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "First name is required")
    @Length(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Length(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;
}
