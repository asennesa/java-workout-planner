package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving the currently authenticated Auth0 user.
 * 
 * This service provides a convenient way for controllers and services to:
 * 1. Get the JWT token of the current request
 * 2. Get or sync the User entity from the database
 * 3. Extract user claims from the JWT
 * 
 * Usage in controllers:
 * <pre>
 * {@code
 * @GetMapping("/workouts")
 * public ResponseEntity<List<WorkoutResponse>> getMyWorkouts() {
 *     User currentUser = currentUserService.getCurrentUser();
 *     // Use currentUser.getUserId() for database queries
 *     return ResponseEntity.ok(workoutService.getUserWorkouts(currentUser.getUserId()));
 * }
 * }
 * </pre>
 * 
 * Security:
 * - Always validates that JWT is present (throws exception if not authenticated)
 * - Automatically syncs Auth0 user profile with local database
 * - Thread-safe (uses SecurityContextHolder)
 */
@Service
public class Auth0CurrentUserService {

    private static final Logger logger = LoggerFactory.getLogger(Auth0CurrentUserService.class);

    private final Auth0UserSyncService auth0UserSyncService;

    public Auth0CurrentUserService(Auth0UserSyncService auth0UserSyncService) {
        this.auth0UserSyncService = auth0UserSyncService;
    }

    /**
     * Gets the currently authenticated user's entity from the database.
     * 
     * This method:
     * 1. Extracts JWT from SecurityContext
     * 2. Syncs user with database (creates/updates local record)
     * 3. Returns User entity with populated ID for use in queries
     * 
     * @return the current User entity
     * @throws ResourceNotFoundException if no authentication present or user not found
     */
    public User getCurrentUser() {
        Jwt jwt = getCurrentJwt();
        
        // Sync user from JWT (creates or updates local record)
        User user = auth0UserSyncService.syncUserFromJwt(jwt);
        
        logger.debug("Retrieved current user. userId={}, auth0UserId={}", 
            user.getUserId(), user.getAuth0UserId());
        
        return user;
    }

    /**
     * Gets the current user's database ID.
     * Convenient shorthand for getCurrentUser().getUserId()
     * 
     * @return the user's database ID
     * @throws ResourceNotFoundException if not authenticated
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * Gets the current user's Auth0 ID.
     * 
     * @return the Auth0 user ID (e.g., "auth0|507f1f77bcf86cd799439011")
     * @throws ResourceNotFoundException if not authenticated
     */
    public String getCurrentAuth0UserId() {
        return getCurrentJwt().getSubject();
    }

    /**
     * Gets the JWT token of the current request.
     * 
     * @return the JWT token
     * @throws ResourceNotFoundException if no authentication present
     */
    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found in security context");
        }
        
        if (!(authentication.getPrincipal() instanceof Jwt)) {
            throw new ResourceNotFoundException(
                "Authentication principal is not a JWT. Found: " + 
                authentication.getPrincipal().getClass().getName()
            );
        }
        
        return (Jwt) authentication.getPrincipal();
    }

    /**
     * Gets the current user's email from JWT.
     * 
     * @return the user's email address
     * @throws ResourceNotFoundException if not authenticated
     */
    public String getCurrentUserEmail() {
        Jwt jwt = getCurrentJwt();
        String email = jwt.getClaimAsString("email");
        
        if (email == null || email.isEmpty()) {
            throw new ResourceNotFoundException("Email claim missing from JWT");
        }
        
        return email;
    }

    /**
     * Checks if a user is currently authenticated.
     * 
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && 
                   authentication.isAuthenticated() && 
                   authentication.getPrincipal() instanceof Jwt;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets a specific claim from the current user's JWT.
     * 
     * @param claimName the name of the claim
     * @return the claim value as string, or null if not present
     */
    public String getClaim(String claimName) {
        try {
            return getCurrentJwt().getClaimAsString(claimName);
        } catch (Exception e) {
            logger.warn("Error retrieving claim '{}': {}", claimName, e.getMessage());
            return null;
        }
    }
}

