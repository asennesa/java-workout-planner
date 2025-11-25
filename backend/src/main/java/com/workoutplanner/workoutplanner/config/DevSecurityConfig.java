package com.workoutplanner.workoutplanner.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Development Security Configuration.
 *
 * ONLY ACTIVE IN 'dev' PROFILE - NOT FOR PRODUCTION!
 *
 * This configuration provides a permissive security setup for:
 * - Local development without Auth0
 * - Unit and integration tests
 * - Quick prototyping
 *
 * Security Model (DEV ONLY):
 * - All endpoints are permitAll() for easy testing
 * - No authentication required
 * - CSRF disabled
 * - CORS configured
 * - Stateless sessions
 *
 * IMPORTANT:
 * Production uses Auth0SecurityConfig with full OAuth2/JWT security.
 * This config is NEVER active in production environments.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("dev") // ONLY active in 'dev' profile
public class DevSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevSecurityConfig.class);

    private final CorsConfigurationSource corsConfigurationSource;

    public DevSecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;

        // Log prominent security warning on application startup
        logSecurityWarning();
    }

    /**
     * Permissive security filter chain for development and testing.
     *
     * WARNING: This configuration allows all requests without authentication.
     * Only use in development/test environments!
     *
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // CSRF disabled for easier testing
            .csrf(csrf -> csrf.disable())

            // Stateless session (consistent with production)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Permit all requests - NO AUTHENTICATION REQUIRED
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            );

        logger.warn("╔═══════════════════════════════════════════════════════════════╗");
        logger.warn("║              SECURITY FILTER CHAIN CONFIGURED                 ║");
        logger.warn("║          Dev profile active - all requests permitted          ║");
        logger.warn("╚═══════════════════════════════════════════════════════════════╝");

        return http.build();
    }

    /**
     * Logs prominent security warning on application startup.
     * This warning ensures developers are aware that authentication is disabled.
     *
     * Uses ERROR level logging to ensure maximum visibility in logs.
     * The warning appears in:
     * - Local development when dev profile is active
     * - Test runs (since tests use dev profile)
     * - Any environment where dev profile is accidentally enabled
     */
    private void logSecurityWarning() {
        logger.error("");
        logger.error("╔════════════════════════════════════════════════════════════════════════╗");
        logger.error("║                                                                        ║");
        logger.error("║                    ⚠️  SECURITY WARNING  ⚠️                            ║");
        logger.error("║                                                                        ║");
        logger.error("║                  DEV PROFILE IS ACTIVE                                 ║");
        logger.error("║                                                                        ║");
        logger.error("║  • All authentication and authorization is DISABLED                    ║");
        logger.error("║  • All API endpoints are publicly accessible                           ║");
        logger.error("║  • @PreAuthorize annotations are IGNORED                               ║");
        logger.error("║  • JWT token validation is BYPASSED                                    ║");
        logger.error("║                                                                        ║");
        logger.error("║  This configuration is ONLY for local development and testing.         ║");
        logger.error("║  NEVER use 'dev' profile in production or shared environments!        ║");
        logger.error("║                                                                        ║");
        logger.error("║  To enable security, remove '--spring.profiles.active=dev'            ║");
        logger.error("║  or ensure no profile is set (Auth0SecurityConfig will activate)      ║");
        logger.error("║                                                                        ║");
        logger.error("╚════════════════════════════════════════════════════════════════════════╝");
        logger.error("");
    }
}