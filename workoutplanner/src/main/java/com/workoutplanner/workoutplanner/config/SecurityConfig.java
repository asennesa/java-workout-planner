package com.workoutplanner.workoutplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
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
 * Production security configuration for the application.
 * 
 * Uses session-based authentication with BCrypt password encoding.
 * 
 * This configuration is active by default and in production.
 * For development without authentication, use the 'dev' profile which activates DevSecurityConfig.
 * 
 * Security Features:
 * - HTTP Basic authentication for REST API
 * - BCrypt password hashing
 * - Role-based access control (RBAC)
 * - CORS protection with configurable origins
 * - CSRF disabled for stateless REST API (compensated by CORS and authentication)
 * - Secure session cookies (httpOnly, sameSite=strict)
 * 
 * @see ApplicationConfig for CORS configuration
 * @see DevSecurityConfig for development-only permissive security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!dev") // Active in all profiles EXCEPT 'dev'
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Security filter chain with session-based authentication and role-based authorization.
     * 
     * CSRF Protection:
     * CSRF is disabled for this REST API as it uses:
     * - Stateless authentication (HTTP Basic)
     * - Strict CORS policy (configured in ApplicationConfig)
     * - No browser-based cookie authentication for state
     * 
     * For production SPAs, consider using CSRF tokens if using session-based auth exclusively.
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())  // Disabled for REST API - see method documentation
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - Authentication
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/users/check-username", "/api/v1/users/check-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()  // User registration
                
                // Public endpoints - API Documentation (Swagger/OpenAPI)
                // Note: Swagger is disabled in production via springdoc.swagger-ui.enabled=false
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                
                // Public endpoints - Health checks
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Protected endpoints - User management
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "USER")
                
                // Protected endpoints - Exercise management
                .requestMatchers(HttpMethod.POST, "/api/v1/exercises").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/exercises/**").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/exercises/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/exercises/**").hasAnyRole("ADMIN", "USER", "MODERATOR")
                
                // Protected endpoints - Workout management
                .requestMatchers("/api/v1/workouts/**").hasAnyRole("ADMIN", "USER")
                
                // Protected endpoints - Set management (Strength, Cardio, Flexibility)
                .requestMatchers("/api/v1/strength-sets/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/cardio-sets/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/flexibility-sets/**").hasAnyRole("ADMIN", "USER")
                
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
