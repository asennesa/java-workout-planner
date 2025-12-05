package com.workoutplanner.workoutplanner.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Static helper for accessing the current authenticated user from SecurityContext.
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    public static Auth0Principal getCurrentPrincipal() {
        return getPrincipalOptional()
            .orElseThrow(() -> new IllegalStateException(
                "No authenticated user found. This method should only be called from authenticated endpoints."
            ));
    }

    public static Long getCurrentUserId() {
        return getCurrentPrincipal().userId();
    }

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
}
