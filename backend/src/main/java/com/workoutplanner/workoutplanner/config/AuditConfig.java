package com.workoutplanner.workoutplanner.config;

import com.workoutplanner.workoutplanner.service.AuditService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
 * - Spring Security integration via AuditService
 * - Automatic user context detection
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    private final AuditService auditService;

    public AuditConfig(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Provides the current user for audit fields.
     * 
     * This bean is responsible for determining the current user context
     * when setting created_by and updated_by fields. It delegates to
     * AuditService to extract user information from Spring Security.
     * 
     * @return AuditorAware implementation that delegates to AuditService
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> auditService.getCurrentUserId();
    }
}
