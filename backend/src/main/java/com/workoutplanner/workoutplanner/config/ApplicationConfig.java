package com.workoutplanner.workoutplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Application-level configuration consolidating all non-security configurations.
 * Follows Spring Boot best practices for configuration organization.
 * 
 * This configuration consolidates:
 * - Application metrics and monitoring
 * - Web layer configuration (CORS, validation, logging)
 * - Infrastructure concerns
 * - JPA Auditing for automatic timestamp management
 * 
 * Benefits:
 * - Single place for application configuration
 * - Reduced configuration complexity
 * - Better maintainability
 * - Follows industry best practices
 */
@Configuration
public class ApplicationConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Content-Type,Authorization,X-Requested-With}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * CORS configuration source.
     * Configures global CORS settings for the application.
     *
     * SECURITY: When credentials are enabled, EXACT origins must be specified.
     * Using patterns with credentials creates a security vulnerability where
     * malicious applications could steal JWT tokens.
     *
     * @return CorsConfigurationSource with security-focused CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOriginsList = Arrays.asList(allowedOrigins.split(","));

        // SECURITY FIX: Use setAllowedOrigins (exact match) when credentials are enabled
        // This prevents CORS attacks from malicious localhost applications
        if (allowCredentials) {
            configuration.setAllowedOrigins(allowedOriginsList);
        } else {
            // If credentials are disabled, patterns are safe to use
            configuration.setAllowedOriginPatterns(allowedOriginsList);
        }

        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ==================== LOGGING FILTER ====================

    /**
     * Logging filter to add MDC (Mapped Diagnostic Context) for request tracking.
     * This filter adds contextual information to every log entry within a request:
     * - correlationId: Unique identifier for the request
     * - userId: User ID if available (can be set by authentication)
     * - apiVersion: API version from the request path
     * - requestUri: The requested URI
     * - method: HTTP method
     *
     * MDC allows this information to appear in all log statements for the request
     * without explicitly passing it to every method.
     */
    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE - 1) // Ensure it runs before other filters
    public static class LoggingFilter extends OncePerRequestFilter {

        private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                                      @NonNull HttpServletResponse response,
                                      @NonNull FilterChain filterChain) throws ServletException, IOException {
            try {
                String correlationId = UUID.randomUUID().toString();
                MDC.put("correlationId", correlationId);
                
                String requestUri = request.getRequestURI();
                String apiVersion = extractApiVersion(requestUri);
                if (apiVersion != null) {
                    MDC.put("apiVersion", apiVersion);
                }
                
                MDC.put("requestUri", requestUri);
                MDC.put("method", request.getMethod());

                logger.info("Incoming Request: {} {}", request.getMethod(), requestUri);

                filterChain.doFilter(request, response);
            } finally {
                logger.info("Outgoing Response: {} {} Status: {}", request.getMethod(), request.getRequestURI(), response.getStatus());
                MDC.clear();
            }
        }

        /**
         * Extracts the API version from the request URI.
         * Assumes a URL path versioning strategy like /api/v1/{resource}.
         *
         * @param requestUri The request URI.
         * @return The API version (e.g., "v1") or null if not found.
         */
        private String extractApiVersion(String requestUri) {
            if (requestUri != null && requestUri.startsWith("/api/")) {
                String[] parts = requestUri.split("/");
                if (parts.length > 2 && parts[2].startsWith("v")) {
                    return parts[2];
                }
            }
            return null;
        }
    }
}