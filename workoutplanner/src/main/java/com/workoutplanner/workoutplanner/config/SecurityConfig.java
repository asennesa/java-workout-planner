package com.workoutplanner.workoutplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for the application.
 * Provides password encoding beans for secure password handling.
 */
@Configuration
public class SecurityConfig {
    
    /**
     * Configures BCrypt password encoder for secure password hashing.
     * BCrypt is the industry standard for password hashing in Spring applications.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
