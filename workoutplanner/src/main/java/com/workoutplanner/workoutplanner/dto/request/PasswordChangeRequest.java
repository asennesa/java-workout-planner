package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for password change requests.
 * 
 * Following industry best practices from major APIs (Google, Auth0, AWS):
 * - Separate endpoint for password changes (security best practice)
 * - Current password verification (prevents session hijacking)
 * - Strong password validation
 * - Password confirmation (prevents typos)
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
public class PasswordChangeRequest {

    /**
     * Current password for verification.
     * Required to prevent session hijacking attacks.
     */
    @NotBlank(message = "Current password is required")
    @Length(min = 8, max = 255, 
            message = "Current password must be between 8 and 255 characters")
    private String currentPassword;

    /**
     * New password.
     * Must meet strength requirements.
     */
    @NotBlank(message = "New password is required")
    @Length(min = 8, max = 255, 
            message = "New password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String newPassword;

    /**
     * Confirmation of new password.
     * Must match newPassword exactly.
     */
    @NotBlank(message = "Password confirmation is required")
    @Length(min = 8, max = 255, 
            message = "Password confirmation must be between 8 and 255 characters")
    private String confirmPassword;

    public PasswordChangeRequest() {}

    public PasswordChangeRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
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
     * Validates that new password and confirmation match.
     * 
     * @return true if passwords match, false otherwise
     */
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    @Override
    public String toString() {
        return "PasswordChangeRequest{" +
                "currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                '}';
    }
}

