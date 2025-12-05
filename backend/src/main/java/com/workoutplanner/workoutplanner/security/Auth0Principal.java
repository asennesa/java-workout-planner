package com.workoutplanner.workoutplanner.security;

import com.workoutplanner.workoutplanner.enums.UserRole;

import java.io.Serializable;
import java.security.Principal;

/**
 * Immutable principal for Auth0 authenticated users.
 * Uses record (thread-safe) with primitive/immutable data to avoid LazyInitializationException.
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
