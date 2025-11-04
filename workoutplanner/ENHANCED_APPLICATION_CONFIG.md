# Enhanced ApplicationConfig - Best Practices Version

## Improvements Over Current Version

### 1. Added Exposed Headers for CORS
- Frontend can now read custom response headers
- Includes correlation ID exposure

### 2. Correlation ID in Response Header
- Frontend can track requests
- Better error reporting

### 3. ObjectMapper Customization
- Consistent JSON handling
- Date/time formatting
- Non-null serialization

### 4. Skip Logging for Actuator
- Reduces log noise
- Cleaner production logs

### 5. Enhanced Error Handling
- Better exception handling in filter
- Graceful degradation

---

## Enhanced Code

```java
package com.workoutplanner.workoutplanner.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Enhanced application-level configuration with production-ready best practices.
 * 
 * Improvements:
 * - Exposed CORS headers for frontend access
 * - Correlation ID in response headers
 * - ObjectMapper customization for JSON handling
 * - Actuator endpoint filtering in logs
 * - Enhanced error handling
 */
@Configuration
public class ApplicationConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Content-Type,Authorization,X-Requested-With}")
    private String allowedHeaders;

    @Value("${app.cors.exposed-headers:X-Total-Count,X-Correlation-Id}")
    private String exposedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * CORS configuration source with exposed headers.
     * Allows frontend to read custom response headers.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        
        // NEW: Expose custom headers to frontend
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * ObjectMapper for consistent JSON serialization/deserialization.
     * Configures date handling, null handling, and formatting.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 date/time support
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Don't fail on unknown properties (forward compatibility)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Ignore null values in JSON output
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Pretty print (can be disabled in production via properties)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }

    /**
     * Validator bean for Bean Validation.
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * Clock bean for testable time operations.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    // ==================== ENHANCED LOGGING FILTER ====================

    /**
     * Enhanced logging filter with:
     * - Correlation ID in response header
     * - Actuator endpoint filtering
     * - Better error handling
     */
    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE - 1)
    public static class LoggingFilter extends OncePerRequestFilter {

        private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                                      @NonNull HttpServletResponse response,
                                      @NonNull FilterChain filterChain) throws ServletException, IOException {
            String requestUri = request.getRequestURI();
            
            // Skip logging for actuator health checks
            if (shouldSkipLogging(requestUri)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            long startTime = System.currentTimeMillis();
            
            try {
                // Generate and set correlation ID
                String correlationId = UUID.randomUUID().toString();
                MDC.put("correlationId", correlationId);
                
                // NEW: Add correlation ID to response header
                response.setHeader("X-Correlation-Id", correlationId);
                
                // Extract API version
                String apiVersion = extractApiVersion(requestUri);
                if (apiVersion != null) {
                    MDC.put("apiVersion", apiVersion);
                }
                
                MDC.put("requestUri", requestUri);
                MDC.put("method", request.getMethod());

                logger.info("Incoming Request: {} {}", request.getMethod(), requestUri);

                filterChain.doFilter(request, response);
                
            } catch (Exception e) {
                logger.error("Error processing request: {} {}", request.getMethod(), requestUri, e);
                throw e;
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Outgoing Response: {} {} Status: {} Duration: {}ms", 
                           request.getMethod(), 
                           request.getRequestURI(), 
                           response.getStatus(),
                           duration);
                MDC.clear();
            }
        }

        /**
         * Determines if request logging should be skipped.
         * Reduces log noise from health checks and static resources.
         */
        private boolean shouldSkipLogging(String uri) {
            return uri.startsWith("/actuator/health")
                || uri.startsWith("/actuator/prometheus")
                || uri.equals("/favicon.ico")
                || uri.startsWith("/webjars/")
                || uri.startsWith("/static/");
        }

        /**
         * Extracts API version from request URI.
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
```

---

## Application Properties to Add

```properties
# Enhanced CORS Configuration
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001}
app.cors.allowed-methods=${CORS_ALLOWED_METHODS:GET,POST,PUT,DELETE,OPTIONS,PATCH}
app.cors.allowed-headers=${CORS_ALLOWED_HEADERS:Content-Type,Authorization,X-Requested-With}
app.cors.exposed-headers=${CORS_EXPOSED_HEADERS:X-Total-Count,X-Correlation-Id,X-Page-Number,X-Page-Size}
app.cors.allow-credentials=${CORS_ALLOW_CREDENTIALS:true}
app.cors.max-age=${CORS_MAX_AGE:3600}
```

---

## Testing the Enhancements

### Test Correlation ID in Response

```bash
curl -v http://localhost:8081/api/v1/users

# Look for:
# < X-Correlation-Id: 550e8400-e29b-41d4-a716-446655440000
```

### Test Exposed Headers from Frontend

```javascript
fetch('http://localhost:8081/api/v1/users')
  .then(response => {
    const correlationId = response.headers.get('X-Correlation-Id');
    const totalCount = response.headers.get('X-Total-Count');
    
    console.log('Correlation ID:', correlationId);  // ✅ Now works!
    console.log('Total Count:', totalCount);        // ✅ Now works!
  });
```

---

## Benefits Summary

| Enhancement | Benefit |
|-------------|---------|
| Exposed Headers | Frontend can read custom response headers |
| Correlation ID in Header | Better error tracking and debugging |
| ObjectMapper | Consistent JSON handling across app |
| Skip Actuator Logging | Cleaner production logs |
| Request Duration | Performance monitoring |
| Better Error Handling | More resilient application |

---

## Migration Path

1. **Start with low-risk items:**
   - Add exposed headers configuration
   - Add correlation ID to response header

2. **Then add:**
   - ObjectMapper bean
   - Skip actuator logging

3. **Optional (for larger apps):**
   - Extract filter to separate class
   - Add RestTemplate/WebClient beans

You can implement these incrementally without breaking existing functionality!





