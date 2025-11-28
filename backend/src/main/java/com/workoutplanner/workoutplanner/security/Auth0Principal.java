package com.workoutplanner.workoutplanner.security;

import com.workoutplanner.workoutplanner.enums.UserRole;

import java.io.Serializable;
import java.security.Principal;

/**
 * DTO principal for Auth0 authenticated users.
 *
 * Industry Best Practice:
 * - Immutable record (thread-safe)
 * - Contains only primitive/immutable data (no JPA entities)
 * - Serializable for distributed environments
 * - Implements Principal for Spring Security compatibility
 *
 * Why NOT use JPA Entity:
 * - Avoids LazyInitializationException
 * - No detached entity issues
 * - Serializable without special handling
 * - Decoupled from persistence layer
 *
 * Usage:
 * <pre>
 * {@code
 * Auth0Principal principal = SecurityContextHelper.getCurrentPrincipal();
 * Long userId = principal.userId();
 * String email = principal.email();
 * }
 * </pre>
 */
public record Auth0Principal(
    Long userId,
    String auth0UserId,
    String email,
    String username,
    String firstName,
    String lastName,
    UserRole role
) implements Principal, Serializable {

    @Override
    public String getName() {
        return auth0UserId;
    }

    /**
     * Checks if the user has admin role.
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Checks if this principal owns a resource with the given user ID.
     */
    public boolean ownsResource(Long resourceUserId) {
        return userId.equals(resourceUserId);
    }

    @Override
    public String toString() {
        return "Auth0Principal{" +
               "userId=" + userId +
               ", auth0UserId='" + auth0UserId + '\'' +
               ", email='" + email + '\'' +
               ", role=" + role +
               '}';
    }
}
