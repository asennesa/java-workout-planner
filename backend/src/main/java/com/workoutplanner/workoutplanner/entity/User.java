package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * User entity with Auth0 integration for authentication.
 * Auth0 handles all password/authentication; this entity stores local business data.
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

    @Pattern(regexp = "^[a-z0-9-]+\\|.+$", message = "Auth0 user ID must follow the format: {provider}|{id}")
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

    public boolean isAuth0User() {
        return auth0UserId != null && !auth0UserId.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        if (userId != null && user.userId != null) {
            return Objects.equals(userId, user.userId);
        }
        if (auth0UserId != null && user.auth0UserId != null) {
            return Objects.equals(auth0UserId, user.auth0UserId);
        }
        return Objects.equals(username, user.username) && Objects.equals(email, user.email);
    }

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
