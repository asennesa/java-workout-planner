package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.validation.ValidationGroups;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Auth.class}, 
              message = "Username is required for registration and authentication")
    @Length(min = 3, max = 50, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class, ValidationGroups.Auth.class}, 
            message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", 
             groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Column(name = "password_hash", nullable = false)
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Auth.class}, 
              message = "Password is required for registration and authentication")
    @Length(min = 8, max = 255, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class, ValidationGroups.Auth.class}, 
            message = "Password must be between 8 and 255 characters")
    private String passwordHash;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
              message = "Email is required")
    @Email(groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
           message = "Email must be a valid email address")
    @Length(max = 255, 
            groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
            message = "Email must not exceed 255 characters")
    private String email;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
              message = "First name is required")
    @Length(min = 1, max = 50, 
            groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
            message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", 
             groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
             message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
              message = "Last name is required")
    @Length(min = 1, max = 50, 
            groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
            message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", 
             groups = {ValidationGroups.Create.class, ValidationGroups.SecureUpdate.class}, 
             message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * JPA lifecycle callbacks for automatic timestamp management
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // UserDetails implementation methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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
        
        // If both entities are persisted (have IDs), use ID for equality
        if (userId != null && user.userId != null) {
            return Objects.equals(userId, user.userId);
        }
        
        // For transient entities, compare unique fields (username and email)
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
        // If entity is persisted, use ID for hash
        if (userId != null) {
            return Objects.hash(userId);
        }
        
        // For transient entities, use unique fields
        return Objects.hash(username, email);
    }
}
