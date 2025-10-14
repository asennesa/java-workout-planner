package com.workoutplanner.workoutplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Global CORS configuration for the application.
 * 
 * This centralizes CORS settings instead of using @CrossOrigin on each controller.
 * Supports API versioning by allowing the X-API-Version header.
 * 
 * API Versioning:
 * - Current version: v1 (endpoints: /api/v1/*)
 * - CORS configuration covers all API versions via /api/**
 * 
 * PRODUCTION: Update allowedOrigins to specific domains instead of "*"
 * Example: setAllowedOrigins(Arrays.asList("https://yourdomain.com", "https://app.yourdomain.com"))
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // Allow credentials (cookies, authorization headers)
        corsConfiguration.setAllowCredentials(true);
        
        // TODO: CHANGE THIS IN PRODUCTION!
        // For development: allow all origins
        // For production: specify exact domains
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        
        // Allow common HTTP methods
        corsConfiguration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allow common headers including API version header
        corsConfiguration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control",
            ApiVersionConfig.VERSION_HEADER
        ));
        
        // Expose headers that frontend can read
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition",
            ApiVersionConfig.VERSION_HEADER
        ));
        
        // Cache preflight requests for 1 hour
        corsConfiguration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
}

