package com.workoutplanner.workoutplanner.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Static helper for accessing the current authenticated user from SecurityContext.
 *
 * Industry Best Practice:
 * - Provides type-safe access to Auth0Principal DTO
 * - Encapsulates SecurityContext access in one place
 * - Returns DTO (not JPA entity) - no lazy loading issues
 * - Fails fast with clear error messages
 *
 * Usage:
 * <pre>
 * {@code
 * // Get current user info
 * Auth0Principal principal = SecurityContextHelper.getCurrentPrincipal();
 * Long userId = SecurityContextHelper.getCurrentUserId();
 * String email = principal.email();
 *
 * // Optional access (for mixed authenticated/anonymous endpoints)
 * Optional<Auth0Principal> principal = SecurityContextHelper.getPrincipalOptional();
 *
 * // Check ownership
 * boolean canAccess = SecurityContextHelper.isOwnerOrAdmin(resourceUserId);
 * }
 * </pre>
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the current authenticated principal.
     *
     * @return the Auth0Principal DTO
     * @throws IllegalStateException if no user is authenticated
     */
    public static Auth0Principal getCurrentPrincipal() {
        return getPrincipalOptional()
            .orElseThrow(() -> new IllegalStateException(
                "No authenticated user found. This method should only be called from authenticated endpoints."
            ));
    }

    /**
     * Gets the current user's database ID.
     *
     * @return the user's database ID
     * @throws IllegalStateException if no user is authenticated
     */
    public static Long getCurrentUserId() {
        return getCurrentPrincipal().userId();
    }

    /**
     * Gets the current user's Auth0 ID.
     *
     * @return the Auth0 user ID (e.g., "google-oauth2|123456")
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentAuth0UserId() {
        return getCurrentPrincipal().auth0UserId();
    }

    /**
     * Gets the current user's email.
     *
     * @return the user's email address
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentEmail() {
        return getCurrentPrincipal().email();
    }

    /**
     * Gets the current principal optionally (for endpoints that support anonymous access).
     *
     * @return Optional containing the principal if authenticated, empty otherwise
     */
    public static Optional<Auth0Principal> getPrincipalOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication instanceof Auth0AuthenticationToken auth0Token) {
            return Optional.of(auth0Token.getPrincipal());
        }

        return Optional.empty();
    }

    /**
     * Checks if a user is currently authenticated with Auth0.
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return getPrincipalOptional().isPresent();
    }

    /**
     * Checks if the current user has admin role.
     *
     * @return true if user is admin, false otherwise
     */
    public static boolean isAdmin() {
        return getPrincipalOptional()
            .map(Auth0Principal::isAdmin)
            .orElse(false);
    }

    /**
     * Checks if the current user owns the resource with the given user ID.
     *
     * @param resourceUserId the user ID of the resource owner
     * @return true if current user owns the resource
     */
    public static boolean isOwner(Long resourceUserId) {
        return getPrincipalOptional()
            .map(principal -> principal.userId().equals(resourceUserId))
            .orElse(false);
    }

    /**
     * Checks if the current user owns the resource OR is an admin.
     *
     * @param resourceUserId the user ID of the resource owner
     * @return true if current user owns the resource or is admin
     */
    public static boolean isOwnerOrAdmin(Long resourceUserId) {
        return getPrincipalOptional()
            .map(principal -> principal.userId().equals(resourceUserId) || principal.isAdmin())
            .orElse(false);
    }
}
