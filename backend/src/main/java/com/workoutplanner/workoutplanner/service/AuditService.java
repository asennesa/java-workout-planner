package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for handling audit-related operations (Auth0 JWT mode).
 * 
 * This service provides functionality to determine the current user
 * for audit trail purposes. It integrates with Spring Security to
 * extract user information from JWT tokens issued by Auth0.
 * 
 * Features:
 * - Retrieves current authenticated user ID from JWT
 * - Extracts Auth0 user ID (sub claim) from JWT
 * - Performs database lookups to find local user records
 * - Handles anonymous/unauthenticated users gracefully
 * 
 * @author WorkoutPlanner Team
 * @version 2.0 (Auth0 Integration)
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
     * Get the current authenticated user's ID (Auth0 JWT mode).
     * 
     * This method extracts the current user ID from the Spring Security context
     * by reading the JWT token issued by Auth0. It extracts the Auth0 user ID
     * (from the 'sub' claim) and looks up the local user record.
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
            
            // Auth0 JWT authentication
            if (principal instanceof Jwt) {
                Jwt jwt = (Jwt) principal;
                String auth0UserId = jwt.getSubject(); // 'sub' claim contains Auth0 user ID
                
                logger.debug("Principal is JWT, extracting Auth0 user ID: {}", auth0UserId);
                
                return userRepository.findByAuth0UserId(auth0UserId)
                        .map(user -> {
                            logger.debug("Found user ID from Auth0 ID lookup: userId={}, auth0UserId={}", 
                                       user.getUserId(), auth0UserId);
                            return user.getUserId();
                        });
            }
            
            logger.warn("Unexpected principal type: {}. Expected Jwt for Auth0 authentication.", 
                       principal.getClass().getName());
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error getting current user ID from JWT: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get the current authenticated username (from JWT email claim).
     * 
     * Extracts the email or preferred_username claim from the JWT token
     * as the username representation.
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
            
            // Auth0 JWT authentication
            if (principal instanceof Jwt) {
                Jwt jwt = (Jwt) principal;
                
                // Try to get email claim (most common)
                String email = jwt.getClaimAsString("email");
                if (email != null && !email.isEmpty()) {
                    logger.debug("Extracted email from JWT: {}", email);
                    return Optional.of(email);
                }
                
                // Fallback to preferred_username
                String preferredUsername = jwt.getClaimAsString("preferred_username");
                if (preferredUsername != null && !preferredUsername.isEmpty()) {
                    logger.debug("Extracted preferred_username from JWT: {}", preferredUsername);
                    return Optional.of(preferredUsername);
                }
                
                // Last resort: use Auth0 user ID (sub claim)
                String auth0UserId = jwt.getSubject();
                logger.debug("Using Auth0 user ID as username: {}", auth0UserId);
                return Optional.ofNullable(auth0UserId);
            }
            
            logger.warn("Unexpected principal type for username extraction: {}", 
                       principal.getClass().getName());
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Error getting current username from JWT: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}

