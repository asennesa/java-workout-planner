package com.workoutplanner.workoutplanner.config;

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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Logging filter to add MDC (Mapped Diagnostic Context) for request tracking.
 * 
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
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String API_VERSION_HEADER = "X-API-Version";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Get or generate correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // Extract API version from path (e.g., /api/v1/users -> v1)
            String apiVersion = extractApiVersion(request.getRequestURI());
            
            // Add to MDC
            MDC.put("correlationId", correlationId);
            MDC.put("apiVersion", apiVersion);
            MDC.put("method", request.getMethod());
            MDC.put("requestUri", request.getRequestURI());
            
            // Add correlation ID to response header
            response.addHeader(CORRELATION_ID_HEADER, correlationId);
            
            // Log incoming request
            logger.info("Incoming request: {} {}", request.getMethod(), request.getRequestURI());
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
            
            // Log response
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed request: {} {} - Status: {} - Duration: {}ms", 
                       request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            
        } catch (Exception e) {
            logger.error("Error processing request: {} {}", request.getMethod(), request.getRequestURI(), e);
            throw e;
        } finally {
            // Always clear MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    /**
     * Extract API version from request URI.
     * 
     * @param uri the request URI
     * @return the API version or "unknown" if not found
     */
    private String extractApiVersion(String uri) {
        if (uri != null && uri.contains("/api/")) {
            String[] parts = uri.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("api".equals(parts[i]) && i + 1 < parts.length) {
                    return parts[i + 1];
                }
            }
        }
        return "unknown";
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filtering for actuator endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/health");
    }
}

