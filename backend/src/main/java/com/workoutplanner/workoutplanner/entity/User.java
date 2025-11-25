package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.workoutplanner.workoutplanner.enums.UserRole;
import java.util.Objects;

/**
 * User entity representing application users.
 * 
 * Auth0 Integration:
 * - Authentication is handled externally by Auth0
 * - auth0UserId contains the Auth0 user ID (e.g., "auth0|507f1f77bcf86cd799439011")
 * - username and email are synced from Auth0 profile via Auth0UserSyncService
 * - Auth0 manages all password/authentication (no local passwords stored)
 * 
 * This entity maintains local business data and relationships (workouts, exercises)
 * while Auth0 handles all authentication, authorization, and user management.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_auth0_id", columnList = "auth0_user_id"),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    /**
     * Auth0 user ID (e.g., "auth0|507f1f77bcf86cd799439011").
     * Required for all users (Auth0 is the only authentication method).
     * Format: {provider}|{id} where provider is auth0, google-oauth2, facebook, etc.
     */
    @Pattern(regexp = "^[a-z0-9-]+\\|.+$",
             message = "Auth0 user ID must follow the format: {provider}|{id}")
    @Column(name = "auth0_user_id", unique = true, length = 100)
    private String auth0UserId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Checks if this user is an Auth0 user.
     * 
     * Note: All users should be Auth0 users in production. This method exists
     * for potential hybrid scenarios during migration.
     * 
     * @return true if user has an Auth0 ID (should always be true in production)
     */
    public boolean isAuth0User() {
        return auth0UserId != null && !auth0UserId.isEmpty();
    }

    /**
     * Equals method following Hibernate best practices.
     * Uses database ID for equality when entity is persisted,
     * falls back to field comparison for transient entities.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        User user = (User) o;
        
        if (userId != null && user.userId != null) {
            return Objects.equals(userId, user.userId);
        }
        
        // For Auth0 users, also check auth0UserId
        if (auth0UserId != null && user.auth0UserId != null) {
            return Objects.equals(auth0UserId, user.auth0UserId);
        }
        
        return Objects.equals(username, user.username) &&
               Objects.equals(email, user.email);
    }

    /**
     * HashCode method following Hibernate best practices.
     * Uses database ID when entity is persisted,
     * falls back to unique fields for transient entities.
     */
    @Override
    public int hashCode() {
        if (userId != null) {
            return Objects.hash(userId);
        }
        
        if (auth0UserId != null) {
            return Objects.hash(auth0UserId);
        }
        
        return Objects.hash(username, email);
    }
}
