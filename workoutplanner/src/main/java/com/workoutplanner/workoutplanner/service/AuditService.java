package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for handling audit-related operations.
 * 
 * This service provides functionality to determine the current user
 * for audit trail purposes. It integrates with Spring Security to
 * extract user information from the security context.
 * 
 * Features:
 * - Retrieves current authenticated user ID
 * - Handles different authentication principal types
 * - Supports both session-based and HTTP Basic authentication
 * - Performs database lookups when needed
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private final UserRepository userRepository;

    public AuditService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get the current authenticated user's ID.
     * 
     * This method extracts the current user ID from the Spring Security context.
     * It handles different types of authentication principals:
     * - User entity (from session-based auth)
     * - UserDetails (from HTTP Basic auth)
     * - String username (fallback)
     * 
     * @return Optional containing the current user ID, or empty if no user is authenticated
     */
    public Optional<Long> getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.debug("No authentication found in security context");
                return Optional.empty();
            }
            
            // Handle anonymous users
            Object principal = authentication.getPrincipal();
            if ("anonymousUser".equals(principal.toString())) {
                logger.debug("Anonymous user detected, returning empty");
                return Optional.empty();
            }
            
            // Case 1: Principal is our User entity (session-based auth)
            if (principal instanceof User) {
                User user = (User) principal;
                Long userId = user.getUserId();
                logger.debug("Current user from User entity: userId={}", userId);
                return Optional.ofNullable(userId);
            }
            
            // Case 2: Principal is UserDetails (HTTP Basic auth)
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                logger.debug("Principal is UserDetails, looking up user by username: {}", username);
                return userRepository.findByUsername(username)
                        .map(user -> {
                            logger.debug("Found user ID from username lookup: userId={}", user.getUserId());
                            return user.getUserId();
                        });
            }
            
            // Case 3: Principal is String username (fallback)
            if (principal instanceof String) {
                String username = (String) principal;
                logger.debug("Principal is String, looking up user by username: {}", username);
                return userRepository.findByUsername(username)
                        .map(user -> {
                            logger.debug("Found user ID from username lookup: userId={}", user.getUserId());
                            return user.getUserId();
                        });
            }
            
            logger.warn("Unexpected principal type: {}. Cannot determine user ID.", 
                       principal.getClass().getName());
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error getting current user ID: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get the current authenticated username.
     * 
     * @return Optional containing the current username, or empty if no user is authenticated
     */
    public Optional<String> getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            
            Object principal = authentication.getPrincipal();
            if ("anonymousUser".equals(principal.toString())) {
                return Optional.empty();
            }
            
            if (principal instanceof User) {
                return Optional.of(((User) principal).getUsername());
            }
            
            if (principal instanceof UserDetails) {
                return Optional.of(((UserDetails) principal).getUsername());
            }
            
            if (principal instanceof String) {
                return Optional.of((String) principal);
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error getting current username: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}

