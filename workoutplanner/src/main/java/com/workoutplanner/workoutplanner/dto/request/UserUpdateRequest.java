package com.workoutplanner.workoutplanner.dto.request;

import com.workoutplanner.workoutplanner.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * Unified DTO for user profile updates.
 * 
 * This DTO consolidates all user update operations into a single, flexible
 * request object that supports both basic and secure updates through validation groups.
 * 
 * Features:
 * - Basic profile updates (email, name) - no password required
 * - Secure profile updates (with password verification)
 * - Password changes (with current password verification)
 * - Flexible field updates (only provided fields are updated)
 * 
 * Usage:
 * - Basic updates: Use ValidationGroups.BasicUpdate.class
 * - Secure updates: Use ValidationGroups.SecureUpdate.class
 * - Password changes: Include currentPassword, newPassword, confirmPassword
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
public class UserUpdateRequest {

    /**
     * Current password for verification (required for secure updates).
     * This field is mandatory when performing sensitive operations like
     * email changes or password updates.
     */
    @NotBlank(groups = {ValidationGroups.SecureUpdate.class}, 
              message = "Current password is required for secure profile updates")
    @Length(min = 8, max = 255, 
            groups = {ValidationGroups.SecureUpdate.class}, 
            message = "Current password must be between 8 and 255 characters")
    private String currentPassword;

    /**
     * New email address.
     * This field is optional - only provided if user wants to change email.
     * For email changes, currentPassword is required for security.
     */
    @Email(groups = {ValidationGroups.BasicUpdate.class, ValidationGroups.SecureUpdate.class}, 
           message = "Email must be a valid email address")
    @Length(max = 255, 
            groups = {ValidationGroups.BasicUpdate.class, ValidationGroups.SecureUpdate.class}, 
            message = "Email must not exceed 255 characters")
    private String email;

    /**
     * New first name.
     * This field is optional - only provided if user wants to change first name.
     */
    @Length(min = 1, max = 50, 
            groups = {ValidationGroups.BasicUpdate.class, ValidationGroups.SecureUpdate.class}, 
            message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", 
             groups = {ValidationGroups.BasicUpdate.class, ValidationGroups.SecureUpdate.class}, 
             message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    /**
     * New last name.
     * This field is optional - only provided if user wants to change last name.
     */
    @Length(min = 1, max = 50, 
            groups = {ValidationGroups.BasicUpdate.class, ValidationGroups.SecureUpdate.class}, 
            message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", 
             groups = {ValidationGroups.BasicUpdate.class, ValidationGroups.SecureUpdate.class}, 
             message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    /**
     * New password.
     * This field is optional - only provided if user wants to change password.
     * When provided, currentPassword is required for verification.
     */
    @Length(min = 8, max = 255, 
            groups = {ValidationGroups.SecureUpdate.class}, 
            message = "New password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             groups = {ValidationGroups.SecureUpdate.class}, 
             message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String newPassword;

    /**
     * Confirm new password.
     * This field is required if newPassword is provided.
     */
    @Length(min = 8, max = 255, 
            groups = {ValidationGroups.SecureUpdate.class}, 
            message = "Password confirmation must be between 8 and 255 characters")
    private String confirmPassword;

    // Constructors
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

    // Getters and Setters
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

    // Helper methods for business logic
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
