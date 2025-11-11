package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * Unified DTO for user profile updates.
 * 
 * Following Jakarta Bean Validation and Spring Framework best practices:
 * - Uses Default validation group for format/structure validation at controller level
 * - Business logic validation (password verification, uniqueness) handled in service layer
 * - Clean separation of concerns between presentation and business logic
 * 
 * Features:
 * - Basic profile updates (firstName, lastName) - no password required
 * - Secure profile updates (email, password) - password verification in service
 * - Flexible field updates (only provided fields are updated)
 * 
 * @author WorkoutPlanner Team
 * @version 2.0
 * @since 1.0
 */
public class UserUpdateRequest {

    /**
     * Current password for verification (required for secure updates).
     * Validation of requirement is handled in service layer based on update type.
     */
    @Length(min = 8, max = 255, 
            message = "Current password must be between 8 and 255 characters")
    private String currentPassword;

    /**
     * New email address.
     * This field is optional - only provided if user wants to change email.
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

    /**
     * New password.
     * This field is optional - only provided if user wants to change password.
     */
    @Length(min = 8, max = 255, 
            message = "New password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String newPassword;

    /**
     * Confirm new password.
     * This field is required if newPassword is provided.
     */
    @Length(min = 8, max = 255, 
            message = "Password confirmation must be between 8 and 255 characters")
    private String confirmPassword;

    public UserUpdateRequest() {}

    public UserUpdateRequest(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserUpdateRequest(String currentPassword, String email, String firstName, String lastName, 
                           String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /**
     * Check if this is a basic update (no password verification required).
     * Basic updates include: firstName, lastName changes only.
     * 
     * @return true if this is a basic update, false otherwise
     */
    public boolean isBasicUpdate() {
        return (firstName != null || lastName != null) && 
               email == null && 
               newPassword == null && 
               currentPassword == null;
    }

    /**
     * Check if this is a secure update (password verification required).
     * Secure updates include: email changes, password changes.
     * 
     * @return true if this is a secure update, false otherwise
     */
    public boolean isSecureUpdate() {
        return email != null || newPassword != null || currentPassword != null;
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
     * Check if password change is requested.
     * 
     * @return true if the newPassword field is not null and not blank, false otherwise
     */
    public boolean isPasswordChangeRequested() {
        return newPassword != null && !newPassword.trim().isEmpty();
    }

    /**
     * Check if current password is provided (required for secure updates).
     * 
     * @return true if currentPassword is not null and not blank, false otherwise
     */
    public boolean hasCurrentPassword() {
        return currentPassword != null && !currentPassword.trim().isEmpty();
    }

    /**
     * Validates that new password and confirmation match.
     * 
     * @return true if passwords match, false otherwise
     */
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    /**
     * Check if any field is being updated.
     * 
     * @return true if at least one field is provided, false otherwise
     */
    public boolean hasUpdates() {
        return isEmailChangeRequested() || 
               isNameChangeRequested() || 
               isPasswordChangeRequested();
    }

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "currentPassword='[PROTECTED]'" +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", newPassword='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                '}';
    }
}
