package com.workoutplanner.workoutplanner.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Application-level configuration consolidating all non-security configurations.
 * Follows Spring Boot best practices for configuration organization.
 * 
 * This configuration consolidates:
 * - Application metrics and monitoring
 * - Data layer configuration (Redis)
 * - Web layer configuration (CORS, validation, logging)
 * - Infrastructure concerns
 * 
 * Benefits:
 * - Single place for application configuration
 * - Reduced configuration complexity
 * - Better maintainability
 * - Follows industry best practices
 */
@Configuration
public class ApplicationConfig {

    // ==================== REDIS CONFIGURATION ====================
    
    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    /**
     * Redis connection factory for production token storage.
     * Provides Redis connection with proper configuration.
     * 
     * @return RedisConnectionFactory configured for the application
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        
        return new LettuceConnectionFactory(config);
    }

    /**
     * Redis template for token storage and OAuth2 state management.
     * Optimized for security data with proper serialization.
     * 
     * @param connectionFactory Redis connection factory
     * @return RedisTemplate configured for security operations
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys and values for security data
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    // ==================== METRICS CONFIGURATION ====================

    /**
     * Security metrics bean for monitoring authentication and authorization events.
     * Provides comprehensive security monitoring and alerting capabilities.
     * 
     * @param meterRegistry Micrometer meter registry for metrics collection
     * @return SecurityMetrics instance
     */
    @Bean
    public SecurityMetrics securityMetrics(MeterRegistry meterRegistry) {
        return new SecurityMetrics(meterRegistry);
    }

    // ==================== WEB CONFIGURATION ====================

    // CORS Configuration Properties
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * CORS configuration source.
     * Configures global CORS settings for the application.
     *
     * @return CorsConfigurationSource with security-focused CORS settings
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures the local validator factory bean for validation.
     * This ensures proper validation message resolution and custom validators.
     *
     * @return LocalValidatorFactoryBean configured for the application
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        return factory;
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
        protected void doFilterInternal(HttpServletRequest request,
                                      HttpServletResponse response,
                                      FilterChain filterChain) throws ServletException, IOException {
            try {
                // Generate a unique correlation ID for each request
                String correlationId = UUID.randomUUID().toString();
                MDC.put("correlationId", correlationId);
                
                // Extract API version from request URI
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
                MDC.clear(); // Clear MDC to prevent memory leaks
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