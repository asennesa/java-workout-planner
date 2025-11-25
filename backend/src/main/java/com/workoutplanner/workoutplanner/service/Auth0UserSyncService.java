package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for synchronizing Auth0 users with local database.
 * 
 * When a user authenticates via Auth0, their profile data needs to be stored
 * in the local database for referential integrity (workouts, exercises belong to users).
 * 
 * This service:
 * - Creates local user records for first-time Auth0 users
 * - Updates existing user records with latest Auth0 profile data
 * - Handles role assignment and user metadata
 * 
 * Called automatically when a JWT token is validated and user data is needed.
 * 
 * Industry Best Practices:
 * - Sync on first login (create user record)
 * - Update on subsequent logins (refresh profile data)
 * - Store minimal data locally (Auth0 is source of truth)
 * - Use Auth0 user ID as primary foreign key
 */
@Service
public class Auth0UserSyncService {

    private static final Logger logger = LoggerFactory.getLogger(Auth0UserSyncService.class);

    private final UserRepository userRepository;

    public Auth0UserSyncService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Synchronizes an Auth0 user with the local database.
     *
     * SECURITY: Email verification is checked on EVERY sync request to prevent
     * users from changing their email to an unverified address in Auth0 and
     * continuing to access the application.
     *
     * If the user doesn't exist locally, creates a new record.
     * If the user exists, updates their profile with latest Auth0 data.
     *
     * @param jwt the JWT token containing user claims from Auth0
     * @return the synchronized User entity
     * @throws SecurityException if email is not verified
     */
    @Transactional
    public User syncUserFromJwt(Jwt jwt) {
        String auth0UserId = jwt.getSubject(); // Auth0 user ID (e.g., "auth0|507f1f77bcf86cd799439011")

        logger.debug("Syncing Auth0 user: {}", auth0UserId);

        // SECURITY: Verify email is confirmed BEFORE any sync operation
        // This prevents users from changing email to unverified address and continuing access
        Boolean emailVerified = jwt.getClaim("email_verified");
        if (emailVerified == null || !emailVerified) {
            logger.warn("SECURITY: Rejecting access for user with unverified email: {}", auth0UserId);
            throw new SecurityException(
                "Email not verified. Please verify your email address to continue."
            );
        }

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByAuth0UserId(auth0UserId);

        if (existingUser.isPresent()) {
            // Update existing user with latest Auth0 data
            return updateUserFromJwt(existingUser.get(), jwt);
        } else {
            // Create new user from Auth0 data
            return createUserFromJwt(jwt);
        }
    }

    /**
     * Creates a new user record from Auth0 JWT claims.
     *
     * Note: Email verification is checked in syncUserFromJwt before this method is called.
     *
     * @param jwt the JWT token
     * @return newly created User entity
     */
    private User createUserFromJwt(Jwt jwt) {
        User user = new User();
        
        // Set Auth0 user ID
        user.setAuth0UserId(jwt.getSubject());
        
        // Extract profile data from JWT claims
        user.setEmail(extractEmail(jwt));
        user.setUsername(extractUsername(jwt));
        user.setFirstName(extractFirstName(jwt));
        user.setLastName(extractLastName(jwt));
        
        // Set role from JWT or default to USER
        user.setRole(extractRole(jwt));
        
        User savedUser = userRepository.save(user);
        
        logger.info("Created new user from Auth0. userId={}, auth0UserId={}, email={}, role={}", 
            savedUser.getUserId(), savedUser.getAuth0UserId(), savedUser.getEmail(), savedUser.getRole());
        
        return savedUser;
    }

    /**
     * Updates an existing user record with latest Auth0 data.
     * 
     * @param user existing user entity
     * @param jwt JWT token with latest claims
     * @return updated User entity
     */
    private User updateUserFromJwt(User user, Jwt jwt) {
        // Update email (may have changed in Auth0)
        String newEmail = extractEmail(jwt);
        if (!user.getEmail().equals(newEmail)) {
            logger.info("Updating email for user {}. Old: {}, New: {}", 
                user.getAuth0UserId(), user.getEmail(), newEmail);
            user.setEmail(newEmail);
        }
        
        // Update name (may have changed in Auth0)
        user.setFirstName(extractFirstName(jwt));
        user.setLastName(extractLastName(jwt));
        
        // Update role (may have changed in Auth0)
        UserRole newRole = extractRole(jwt);
        if (user.getRole() != newRole) {
            logger.info("Updating role for user {}. Old: {}, New: {}", 
                user.getAuth0UserId(), user.getRole(), newRole);
            user.setRole(newRole);
        }
        
        User updatedUser = userRepository.save(user);
        
        logger.debug("Updated user from Auth0. userId={}, auth0UserId={}", 
            updatedUser.getUserId(), updatedUser.getAuth0UserId());
        
        return updatedUser;
    }

    /**
     * Finds a user by Auth0 user ID.
     * 
     * @param auth0UserId the Auth0 user ID
     * @return Optional containing the user if found
     */
    public Optional<User> findByAuth0UserId(String auth0UserId) {
        return userRepository.findByAuth0UserId(auth0UserId);
    }

    /**
     * Extracts email from JWT claims.
     * Auth0 standard claim: "email"
     */
    private String extractEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email claim missing from JWT");
        }
        return email;
    }

    /**
     * Extracts username from JWT claims.
     * Auth0 claim: "nickname" or "preferred_username"
     */
    private String extractUsername(Jwt jwt) {
        String username = jwt.getClaimAsString("nickname");
        if (username == null || username.isEmpty()) {
            username = jwt.getClaimAsString("preferred_username");
        }
        if (username == null || username.isEmpty()) {
            // Fallback: use email prefix as username
            String email = extractEmail(jwt);
            username = email.substring(0, email.indexOf('@'));
        }
        return username;
    }

    /**
     * Extracts first name from JWT claims.
     * Auth0 claim: "given_name" or custom claim
     */
    private String extractFirstName(Jwt jwt) {
        String firstName = jwt.getClaimAsString("given_name");
        if (firstName == null || firstName.isEmpty()) {
            firstName = jwt.getClaimAsString("https://api.workout-planner.com/first_name");
        }
        if (firstName == null || firstName.isEmpty()) {
            // Fallback: use part of name or username
            String name = jwt.getClaimAsString("name");
            if (name != null && name.contains(" ")) {
                firstName = name.substring(0, name.indexOf(' '));
            } else {
                firstName = extractUsername(jwt);
            }
        }
        return firstName;
    }

    /**
     * Extracts last name from JWT claims.
     * Auth0 claim: "family_name" or custom claim
     */
    private String extractLastName(Jwt jwt) {
        String lastName = jwt.getClaimAsString("family_name");
        if (lastName == null || lastName.isEmpty()) {
            lastName = jwt.getClaimAsString("https://api.workout-planner.com/last_name");
        }
        if (lastName == null || lastName.isEmpty()) {
            // Fallback: use part of name or empty string
            String name = jwt.getClaimAsString("name");
            if (name != null && name.contains(" ")) {
                lastName = name.substring(name.lastIndexOf(' ') + 1);
            } else {
                lastName = ""; // Will need update later
            }
        }
        return lastName;
    }

    /**
     * Extracts role from JWT custom claims.
     * 
     * You configure this in Auth0 Actions (Post-Login):
     * api.accessToken.setCustomClaim('https://api.workout-planner.com/role', 
     *   event.user.app_metadata?.role || 'USER');
     * 
     * @param jwt the JWT token
     * @return UserRole enum value
     */
    private UserRole extractRole(Jwt jwt) {
        // Try new format first (singular - current standard)
        String roleClaim = "https://api.workout-planner.com/role";
        
        try {
            Object roleObj = jwt.getClaim(roleClaim);
            
            // New format: single role as string
            if (roleObj instanceof String) {
                String roleStr = (String) roleObj;
                return UserRole.valueOf(roleStr.toUpperCase());
            }
            
            // Fallback: Try old format (plural, array) for backward compatibility
            String rolesClaim = "https://api.workout-planner.com/roles";
            Object rolesObj = jwt.getClaim(rolesClaim);
            
            if (rolesObj instanceof java.util.List<?>) {
                java.util.List<?> roles = (java.util.List<?>) rolesObj;
                if (!roles.isEmpty() && roles.get(0) instanceof String) {
                    String roleStr = (String) roles.get(0);
                    return UserRole.valueOf(roleStr.toUpperCase());
                }
            } else if (rolesObj instanceof String) {
                return UserRole.valueOf(((String) rolesObj).toUpperCase());
            }
        } catch (Exception e) {
            logger.warn("Error extracting role from JWT, defaulting to USER. Error: {}", e.getMessage());
        }
        
        // Default to USER role
        return UserRole.USER;
    }
}

