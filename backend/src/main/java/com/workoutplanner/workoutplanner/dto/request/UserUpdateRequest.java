package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for user profile updates (Auth0 mode).
 * 
 * With Auth0 integration:
 * - Profile updates are authorized via JWT token validation
 * - No password verification needed (Auth0 handles authentication)
 * - Email changes in Auth0 require email verification through Auth0's flow
 * - Password changes are handled entirely by Auth0
 * 
 * Features:
 * - Update email, firstName, lastName
 * - Flexible field updates (only provided fields are updated)
 * - At least one field required (enforced by @AssertTrue)
 * 
 * @author WorkoutPlanner Team
 * @version 5.0 (Auth0 Integration)
 * @since 1.0
 */
public class UserUpdateRequest {

    /**
     * New email address.
     * This field is optional - only provided if user wants to change email.
     * 
     * Note: With Auth0, email changes should be done through Auth0's dashboard
     * or API for proper email verification flow.
     */
    @Email(message = "Email must be a valid email address")
    @Length(max = 255, 
            message = "Email must not exceed 255 characters")
    private String email;

    /**
     * New first name.
     * This field is optional - only provided if user wants to change first name.
     */
    @Length(min = 1, max = 50, 
            message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", 
             message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    /**
     * New last name.
     * This field is optional - only provided if user wants to change last name.
     */
    @Length(min = 1, max = 50, 
            message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", 
             message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    public UserUpdateRequest() {}

    public UserUpdateRequest(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Check if this is a basic update (name changes only).
     * 
     * @return true if only name fields are provided, false otherwise
     */
    public boolean isBasicUpdate() {
        return (firstName != null || lastName != null) && email == null;
    }

    /**
     * Check if this is a secure update (email change).
     * 
     * Note: With Auth0, all updates are secured via JWT validation.
     * Email changes should ideally be done through Auth0 for proper verification.
     * 
     * @return true if email change is requested, false otherwise
     */
    public boolean isSecureUpdate() {
        return email != null;
    }

    /**
     * Check if email change is requested.
     * 
     * @return true if the email field is not null and not blank, false otherwise
     */
    public boolean isEmailChangeRequested() {
        return email != null && !email.trim().isEmpty();
    }

    /**
     * Check if name change (first or last name) is requested.
     * 
     * @return true if either first name or last name is not null and not blank, false otherwise
     */
    public boolean isNameChangeRequested() {
        return (firstName != null && !firstName.trim().isEmpty()) ||
               (lastName != null && !lastName.trim().isEmpty());
    }

    /**
     * Validation method using built-in @AssertTrue annotation.
     * Ensures at least one field is provided for the update request.
     * 
     * @return true if at least one field is non-null, false otherwise
     */
    @AssertTrue(message = "At least one field (email, firstName, or lastName) must be provided for update")
    private boolean isAtLeastOneFieldProvided() {
        return email != null || firstName != null || lastName != null;
    }

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
