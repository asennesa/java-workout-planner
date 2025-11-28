package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.security.Auth0Principal;
import com.workoutplanner.workoutplanner.security.SecurityContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for handling audit-related operations.
 *
 * This service provides functionality to determine the current user
 * for audit trail purposes. It integrates with Spring Security via
 * SecurityContextHelper to extract user information from Auth0Principal.
 *
 * Features:
 * - Retrieves current authenticated user ID from Auth0Principal
 * - Retrieves current authenticated username from Auth0Principal
 * - Handles anonymous/unauthenticated users gracefully
 * - No database lookups required (user info already in Auth0Principal)
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    /**
     * Get the current authenticated user's ID.
     *
     * This method uses SecurityContextHelper to access the Auth0Principal
     * which already contains the user ID (set during Auth0UserSyncFilter).
     *
     * @return Optional containing the current user ID, or empty if no user is authenticated
     */
    public Optional<Long> getCurrentUserId() {
        try {
            Optional<Auth0Principal> principal = SecurityContextHelper.getPrincipalOptional();

            if (principal.isEmpty()) {
                logger.debug("No authenticated user found in security context");
                return Optional.empty();
            }

            Long userId = principal.get().userId();
            logger.debug("Current user ID for audit: {}", userId);
            return Optional.of(userId);

        } catch (Exception e) {
            logger.error("Error getting current user ID: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Get the current authenticated username (email).
     *
     * @return Optional containing the current user's email, or empty if no user is authenticated
     */
    public Optional<String> getCurrentUsername() {
        try {
            Optional<Auth0Principal> principal = SecurityContextHelper.getPrincipalOptional();

            if (principal.isEmpty()) {
                logger.debug("No authenticated user found in security context");
                return Optional.empty();
            }

            String email = principal.get().email();
            logger.debug("Current username for audit: {}", email);
            return Optional.of(email);

        } catch (Exception e) {
            logger.error("Error getting current username: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}

