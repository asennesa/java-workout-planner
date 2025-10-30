package com.workoutplanner.workoutplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
 * Development-only security configuration.
 * 
 * This configuration is ONLY active when running with 'dev' profile.
 * It allows ALL requests without authentication for easier testing.
 * 
 * ⚠️ WARNING: This should NEVER be used in production!
 * 
 * To activate: Run with -Dspring.profiles.active=dev
 * 
 * Benefits:
 * - Easy API testing during development
 * - No need for authentication headers
 * - Still maintains proper security in production
 * - Can be toggled on/off easily
 * 
 * @see SecurityConfig for production security configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("dev") // Only active in dev profile
public class DevSecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public DevSecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Development security filter chain - permits all requests.
     * 
     * ⚠️ This configuration disables authentication for ALL endpoints.
     * Use ONLY during development for easier testing.
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Allow ALL requests without authentication
            )
            .httpBasic(httpBasic -> httpBasic.disable()) // Disable HTTP Basic
            .formLogin(form -> form.disable()); // Disable form login
        
        return http.build();
    }

    /**
     * Authentication manager bean for development.
     */
    @Bean
    public AuthenticationManager devAuthenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Password encoder - still needed for user creation even in dev mode.
     */
    @Bean
    public PasswordEncoder devPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

