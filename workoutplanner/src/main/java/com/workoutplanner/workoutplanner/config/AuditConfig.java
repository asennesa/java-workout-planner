package com.workoutplanner.workoutplanner.config;

import com.workoutplanner.workoutplanner.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Configuration for JPA auditing functionality.
 * 
 * This configuration enables automatic audit field management across all entities
 * that extend AuditableEntity. It provides:
 * 
 * - Automatic timestamp management (created_at, updated_at)
 * - User tracking (created_by, updated_by)
 * - Spring Security integration for current user detection
 * - Enterprise-grade audit trail functionality
 * 
 * Features:
 * - @CreatedDate and @LastModifiedDate annotations
 * - @CreatedBy and @LastModifiedBy annotations
 * - Spring Security integration
 * - Automatic user context detection
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * Provides the current user for audit fields.
     * 
     * This bean is responsible for determining the current user context
     * when setting created_by and updated_by fields. It integrates with
     * Spring Security to automatically detect the authenticated user.
     * 
     * Features:
     * - Spring Security integration
     * - Anonymous user handling
     * - System user fallback
     * - Thread-safe user context detection
     * 
     * @return AuditorAware implementation for user context
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    /**
     * Spring Security-aware auditor implementation.
     * 
     * This class implements AuditorAware to provide the current user
     * for audit field population. It integrates with Spring Security
     * to automatically detect the authenticated user.
     * 
     * Features:
     * - Automatic user detection from SecurityContext
     * - Anonymous user handling (returns empty for anonymous users)
     * - Extracts actual user ID from authenticated User entity
     * - Thread-safe implementation
     * - Graceful error handling
     */
    public static class SpringSecurityAuditorAware implements AuditorAware<Long> {

        private static final Logger logger = LoggerFactory.getLogger(SpringSecurityAuditorAware.class);

        /**
         * Get the current auditor (user) for audit fields.
         * 
         * This method is called by Spring Data JPA when setting audit fields.
         * It extracts the current user ID from the Spring Security context.
         * 
         * @return Optional containing the current user ID, or empty if no user is authenticated
         */
        @Override
        @NonNull
        public Optional<Long> getCurrentAuditor() {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication == null || !authentication.isAuthenticated()) {
                    logger.debug("No authentication found in security context");
                    return Optional.empty();
                }
                
                // Handle anonymous users
                Object principal = authentication.getPrincipal();
                if ("anonymousUser".equals(principal.toString())) {
                    logger.debug("Anonymous user detected, returning empty auditor");
                    return Optional.empty();
                }
                
                // Extract user ID from authentication principal
                // Since our User entity implements UserDetails, we can cast it directly
                if (principal instanceof User) {
                    User user = (User) principal;
                    Long userId = user.getUserId();
                    logger.debug("Current auditor: userId={}", userId);
                    return Optional.ofNullable(userId);
                }
                
                // Fallback: if principal is a string (username), we can't easily get the ID
                // without a database lookup, so log a warning and return empty
                logger.warn("Unexpected principal type: {}. Cannot determine user ID for auditing.", 
                           principal.getClass().getName());
                return Optional.empty();
                
            } catch (Exception e) {
                // Log the exception and return empty to avoid breaking the application
                logger.error("Error getting current auditor: {}", e.getMessage(), e);
                return Optional.empty();
            }
        }
    }
}
