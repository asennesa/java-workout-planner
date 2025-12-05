package com.workoutplanner.workoutplanner.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for Bucket4j rate limiting SpEL expressions.
 *
 * Used by bucket4j-spring-boot-starter to evaluate cache keys and conditions.
 * Referenced in application-ratelimit.yml as @securityService.
 *
 * @see <a href="https://github.com/MarcGiffing/bucket4j-spring-boot-starter">Bucket4j Spring Boot Starter</a>
 */
@Service("securityService")
public class RateLimitSecurityService {

    /**
     * Get the current authenticated username for rate limit key.
     *
     * @return username or "anonymous" if not authenticated
     */
    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return auth.getName();
    }

    /**
     * Check if the current request is authenticated.
     * Used for execute-condition in rate limit filters.
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
}
