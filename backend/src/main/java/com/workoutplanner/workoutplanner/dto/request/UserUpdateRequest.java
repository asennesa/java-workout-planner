package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for user profile updates.
 * All fields are optional but at least one must be provided.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    public boolean isBasicUpdate() {
        return (firstName != null || lastName != null) && email == null;
    }

    public boolean isSecureUpdate() {
        return email != null;
    }

    public boolean isEmailChangeRequested() {
        return email != null && !email.trim().isEmpty();
    }

    public boolean isNameChangeRequested() {
        return (firstName != null && !firstName.trim().isEmpty()) ||
               (lastName != null && !lastName.trim().isEmpty());
    }

    @AssertTrue(message = "At least one field (email, firstName, or lastName) must be provided for update")
    private boolean isAtLeastOneFieldProvided() {
        return email != null || firstName != null || lastName != null;
    }
}
