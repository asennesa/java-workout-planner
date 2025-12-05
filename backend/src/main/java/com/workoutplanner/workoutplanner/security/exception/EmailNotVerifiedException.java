package com.workoutplanner.workoutplanner.security.exception;

/**
 * Exception thrown when a user attempts to authenticate with an unverified email.
 *
 * Security Consideration:
 * - Email verification prevents account takeover via email squatting
 * - Users must verify their email before accessing protected resources
 * - Social login providers (Google, GitHub, etc.) are exempt as they verify emails
 */
public class EmailNotVerifiedException extends Auth0AuthenticationException {

    public EmailNotVerifiedException(String auth0UserId) {
        super(
            ErrorCode.EMAIL_NOT_VERIFIED,
            String.format("Email not verified for user: %s. Please verify your email address to continue.", auth0UserId)
        );
    }
}
