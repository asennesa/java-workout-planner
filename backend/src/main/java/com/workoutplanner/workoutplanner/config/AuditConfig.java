package com.workoutplanner.workoutplanner.config;

import com.workoutplanner.workoutplanner.security.Auth0Principal;
import com.workoutplanner.workoutplanner.security.SecurityContextHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing configuration.
 *
 * Enables automatic population of @CreatedDate, @LastModifiedDate,
 * @CreatedBy, and @LastModifiedBy fields on entities extending AuditableEntity.
 *
 * @see <a href="https://docs.spring.io/spring-data/jpa/reference/auditing.html">Spring Data JPA Auditing</a>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * Provides current user ID for @CreatedBy and @LastModifiedBy fields.
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> SecurityContextHelper.getPrincipalOptional()
            .map(Auth0Principal::userId);
    }
}
