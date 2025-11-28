package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.entity.*;
import com.workoutplanner.workoutplanner.repository.*;
import com.workoutplanner.workoutplanner.security.SecurityContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Centralized security service for resource ownership validation.
 * 
 * This service implements the industry best practice for domain object security
 * as recommended by Spring Security documentation and OAuth2 standards.
 * 
 * Pattern: @PreAuthorize with custom bean methods (SpEL expressions)
 * 
 * Usage in service methods:
 * <pre>
 * {@code
 * @PreAuthorize("@resourceSecurityService.canAccessWorkout(#sessionId)")
 * public WorkoutResponse getWorkoutSessionById(Long sessionId) {
 *     // Business logic
 * }
 * }
 * </pre>
 * 
 * Security Layers:
 * 1. Authentication: Auth0 JWT validation
 * 2. Authorization (Permissions): hasAuthority('read:workouts') - OAuth2 scopes
 * 3. Authorization (Ownership): This service - checks user owns specific resource
 * 
 * This approach follows:
 * - Spring Security best practices for method-level security
 * - OAuth2 standards (scopes + ownership)
 * - Auth0 documentation recommendations
 * - OWASP A01:2021 - Broken Access Control prevention
 * 
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@Service("resourceSecurityService")
public class ResourceSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceSecurityService.class);

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final StrengthSetRepository strengthSetRepository;
    private final CardioSetRepository cardioSetRepository;
    private final FlexibilitySetRepository flexibilitySetRepository;

    public ResourceSecurityService(
            WorkoutSessionRepository workoutSessionRepository,
            WorkoutExerciseRepository workoutExerciseRepository,
            StrengthSetRepository strengthSetRepository,
            CardioSetRepository cardioSetRepository,
            FlexibilitySetRepository flexibilitySetRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.strengthSetRepository = strengthSetRepository;
        this.cardioSetRepository = cardioSetRepository;
        this.flexibilitySetRepository = flexibilitySetRepository;
    }

    // ==================== WORKOUT SESSION SECURITY ====================

    /**
     * Check if current user can access a workout session.
     * 
     * Access granted if:
     * - User owns the workout, OR
     * - User is admin (has read:users permission)
     * 
     * @param sessionId the workout session ID
     * @return true if user can access, false otherwise
     */
    public boolean canAccessWorkout(Long sessionId) {
        try {
            Long currentUserId = SecurityContextHelper.getCurrentUserId();

            // Admin bypass - can access all workouts
            if (isAdmin()) {
                logger.debug("Admin access granted to workout sessionId={}", sessionId);
                return true;
            }

            // Check ownership
            WorkoutSession workout = workoutSessionRepository.findById(sessionId)
                .orElse(null);

            if (workout == null) {
                logger.debug("Workout not found: sessionId={}", sessionId);
                return false; // Will result in 404
            }

            boolean isOwner = workout.getUser().getUserId().equals(currentUserId);

            if (!isOwner) {
                logger.warn("SECURITY: User {} attempted to access workout {} owned by user {}",
                           currentUserId, sessionId, workout.getUser().getUserId());
            }

            return isOwner;

        } catch (Exception e) {
            logger.error("Error checking workout access for sessionId={}: {}",
                        sessionId, e.getMessage());
            return false; // Deny by default on error
        }
    }

    /**
     * Check if current user can modify a workout session.
     * Currently same as canAccessWorkout, but kept separate for future flexibility.
     */
    public boolean canModifyWorkout(Long sessionId) {
        return canAccessWorkout(sessionId);
    }

    // ==================== WORKOUT EXERCISE SECURITY ====================

    /**
     * Check if current user can access a workout exercise.
     * Access based on owning the parent workout session.
     */
    public boolean canAccessWorkoutExercise(Long workoutExerciseId) {
        try {
            WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElse(null);
            
            if (workoutExercise == null) {
                return false;
            }
            
            // Check access to parent workout
            return canAccessWorkout(workoutExercise.getWorkoutSession().getSessionId());
            
        } catch (Exception e) {
            logger.error("Error checking workout exercise access: {}", e.getMessage());
            return false;
        }
    }

    // ==================== SET SECURITY ====================

    /**
     * Check if current user can access any set (strength, cardio, or flexibility).
     * Access based on owning the parent workout.
     *
     * This method checks all set types and returns true if the user owns
     * the workout containing the set. Used by BaseSetController for generic set operations.
     *
     * @param setId the set ID
     * @return true if user can access the set, false otherwise
     */
    public boolean canAccessSet(Long setId) {
        // Try strength set first
        if (canAccessStrengthSet(setId)) {
            return true;
        }

        // Try cardio set
        if (canAccessCardioSet(setId)) {
            return true;
        }

        // Try flexibility set
        if (canAccessFlexibilitySet(setId)) {
            return true;
        }

        // Set not found or user doesn't own it
        return false;
    }

    /**
     * Check if current user can access a strength set.
     * Access based on owning the parent workout.
     */
    public boolean canAccessStrengthSet(Long setId) {
        try {
            StrengthSet set = strengthSetRepository.findById(setId).orElse(null);
            if (set == null) {
                return false;
            }
            
            Long sessionId = set.getWorkoutExercise().getWorkoutSession().getSessionId();
            return canAccessWorkout(sessionId);
            
        } catch (Exception e) {
            logger.error("Error checking strength set access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user can access a cardio set.
     * Access based on owning the parent workout.
     */
    public boolean canAccessCardioSet(Long setId) {
        try {
            CardioSet set = cardioSetRepository.findById(setId).orElse(null);
            if (set == null) {
                return false;
            }
            
            Long sessionId = set.getWorkoutExercise().getWorkoutSession().getSessionId();
            return canAccessWorkout(sessionId);
            
        } catch (Exception e) {
            logger.error("Error checking cardio set access: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user can access a flexibility set.
     * Access based on owning the parent workout.
     */
    public boolean canAccessFlexibilitySet(Long setId) {
        try {
            FlexibilitySet set = flexibilitySetRepository.findById(setId).orElse(null);
            if (set == null) {
                return false;
            }
            
            Long sessionId = set.getWorkoutExercise().getWorkoutSession().getSessionId();
            return canAccessWorkout(sessionId);
            
        } catch (Exception e) {
            logger.error("Error checking flexibility set access: {}", e.getMessage());
            return false;
        }
    }

    // ==================== EXERCISE LIBRARY SECURITY ====================

    /**
     * Check if current user can modify an exercise.
     * Exercises are community resources, so only moderators/admins can modify.
     * 
     * Access granted if:
     * - User has write:exercises permission (moderator), OR
     * - User is admin
     */
    public boolean canModifyExercise(Long exerciseId) {
        try {
            // Check if user has write:exercises permission
            if (hasAuthority("write:exercises")) {
                return true;
            }
            
            // Admin can also modify
            if (isAdmin()) {
                return true;
            }
            
            logger.debug("User does not have permission to modify exercise");
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking exercise modification permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if current user can delete an exercise.
     * Only admins can delete exercises.
     */
    public boolean canDeleteExercise(Long exerciseId) {
        try {
            boolean isAdmin = hasAuthority("delete:exercises");
            
            if (!isAdmin) {
                logger.warn("SECURITY: Non-admin user attempted to delete exercise {}", exerciseId);
            }
            
            return isAdmin;
            
        } catch (Exception e) {
            logger.error("Error checking exercise deletion permission: {}", e.getMessage());
            return false;
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if current user is an admin.
     * Admins have the read:users permission.
     */
    private boolean isAdmin() {
        return hasAuthority("read:users");
    }

    /**
     * Check if current user has a specific authority (permission).
     * 
     * @param authority the authority to check (e.g., "read:users", "write:exercises")
     * @return true if user has the authority, false otherwise
     */
    private boolean hasAuthority(String authority) {
        try {
            return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
        } catch (Exception e) {
            logger.error("Error checking authority '{}': {}", authority, e.getMessage());
            return false;
        }
    }
}

