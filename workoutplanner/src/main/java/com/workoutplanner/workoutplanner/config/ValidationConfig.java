package com.workoutplanner.workoutplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration class for validation settings.
 * Demonstrates best practices for Bean Validation setup in Spring Boot.
 */
@Configuration
public class ValidationConfig {
    
    /**
     * Configures the local validator factory bean for validation.
     * This ensures proper validation message resolution and custom validators.
     * 
     * @return LocalValidatorFactoryBean configured for the application
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        // Additional configuration can be added here if needed
        return factory;
    }
}
