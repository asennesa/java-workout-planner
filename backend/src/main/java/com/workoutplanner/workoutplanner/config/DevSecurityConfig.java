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
 * Provides permissive security for local development without Auth0.
 * Production uses Auth0SecurityConfig with full OAuth2/JWT security.
 *
 * @see Auth0SecurityConfig
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("dev")
public class DevSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevSecurityConfig.class);

    private final CorsConfigurationSource corsConfigurationSource;

    public DevSecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Permissive security filter chain for development.
     * All requests are permitted without authentication.
     */
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            );

        logger.warn("DEV PROFILE ACTIVE - Security disabled, all endpoints permitted");
        return http.build();
    }
}