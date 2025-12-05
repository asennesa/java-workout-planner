package com.workoutplanner.workoutplanner.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Web configuration for CORS and request tracing (MDC).
 */
@Configuration
public class WebConfig {

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
     * CORS configuration for frontend integration.
     * Uses exact origins (not patterns) when credentials are enabled for security.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));

        // Security: Use exact origins when credentials enabled to prevent token theft
        if (allowCredentials) {
            config.setAllowedOrigins(origins);
        } else {
            config.setAllowedOriginPatterns(origins);
        }

        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * MDC logging filter for request tracing.
     * Adds correlationId and apiVersion to all log entries within a request.
     */
    @Component
    @Order(-100)
    public static class LoggingFilter extends OncePerRequestFilter {

        private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain) throws ServletException, IOException {
            try {
                MDC.put("correlationId", UUID.randomUUID().toString());

                String uri = request.getRequestURI();
                if (uri != null && uri.startsWith("/api/")) {
                    String[] parts = uri.split("/");
                    if (parts.length > 2 && parts[2].startsWith("v")) {
                        MDC.put("apiVersion", parts[2]);
                    }
                }

                log.debug("Request: {} {}", request.getMethod(), uri);
                filterChain.doFilter(request, response);
            } finally {
                log.debug("Response: {} {} -> {}", request.getMethod(), request.getRequestURI(), response.getStatus());
                MDC.clear();
            }
        }
    }
}