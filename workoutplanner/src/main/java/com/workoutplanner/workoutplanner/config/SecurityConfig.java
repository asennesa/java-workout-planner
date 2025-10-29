package com.workoutplanner.workoutplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Basic security configuration for the application.
 * Uses standard session-based authentication with BCrypt password encoding.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Security filter chain with session-based authentication and role-based authorization.
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())  // Disable CSRF for REST API
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/users/check-username", "/api/v1/users/check-email").permitAll()
                .requestMatchers("POST", "/api/v1/users").permitAll()  // User registration
                
                // Health check endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Protected endpoints with role-based access
                .requestMatchers("DELETE", "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "USER")
                
                .requestMatchers("POST", "/api/v1/exercises").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("PUT", "/api/v1/exercises/**").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("DELETE", "/api/v1/exercises/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/exercises/**").hasAnyRole("ADMIN", "USER", "MODERATOR")
                
                .requestMatchers("/api/v1/workouts/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/sets/**").hasAnyRole("ADMIN", "USER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {})  // Enable HTTP Basic authentication
            .formLogin(form -> form.disable());  // Disable form login for REST API
        
        return http.build();
    }

    /**
     * Authentication manager bean.
     * 
     * @param authConfig AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

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
