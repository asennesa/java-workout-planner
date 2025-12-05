package com.workoutplanner.workoutplanner.security;

import com.workoutplanner.workoutplanner.security.exception.EmailNotVerifiedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that synchronizes Auth0 users with local database.
 *
 * Industry Best Practice:
 * - Runs AFTER Spring Security authentication (proper filter ordering)
 * - Uses @Transactional service for DB operations (proper transaction context)
 * - Converts JwtAuthenticationToken to Auth0AuthenticationToken with user info
 * - Handles email verification
 *
 * Filter Chain Order:
 * <pre>
 * BearerTokenAuthenticationFilter → JwtDecoder → JwtAuthenticationConverter
 *                                        ↓
 *                              JwtAuthenticationToken in SecurityContext
 *                                        ↓
 *                              THIS FILTER (Auth0UserSyncFilter)
 *                                        ↓
 *                              Auth0AuthenticationToken in SecurityContext
 *                                        ↓
 *                              Controller/Service
 * </pre>
 *
 * Why a filter instead of converter?
 * - Proper transaction context for DB operations
 * - Runs after authentication is complete
 * - Can properly handle errors and send HTTP responses
 * - Follows separation of concerns
 */
@Component
@Profile("!test & !dev")  // Only active in production (Auth0 mode)
@Order(Ordered.LOWEST_PRECEDENCE - 10) // Run after Spring Security filters
public class Auth0UserSyncFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(Auth0UserSyncFilter.class);

    private final Auth0UserSyncService userSyncService;

    public Auth0UserSyncFilter(Auth0UserSyncService userSyncService) {
        this.userSyncService = userSyncService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Only process JWT authenticated requests
        if (authentication instanceof JwtAuthenticationToken jwtAuth && authentication.isAuthenticated()) {
            try {
                Jwt jwt = jwtAuth.getToken();
                String auth0UserId = jwt.getSubject();

                log.debug("Syncing user for request: {}", auth0UserId);

                // Verify email for non-social providers
                verifyEmailIfRequired(jwt, auth0UserId);

                // Sync user with database (transactional, cached)
                Auth0Principal principal = userSyncService.syncUser(jwt);

                // Replace authentication with our custom token containing user info
                Auth0AuthenticationToken auth0Token = new Auth0AuthenticationToken(
                    principal,
                    jwt,
                    jwtAuth.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(auth0Token);

                log.debug("User synced successfully: userId={}, email={}",
                    principal.userId(), principal.email());

            } catch (EmailNotVerifiedException e) {
                log.warn("Email not verified: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"email_not_verified\",\"message\":\"" + e.getMessage() + "\"}"
                );
                return; // Don't continue filter chain
            } catch (Exception e) {
                log.error("Error syncing user: {}", e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"user_sync_failed\",\"message\":\"Failed to synchronize user profile\"}"
                );
                return; // Don't continue filter chain
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifies email is confirmed for non-social providers.
     * Social providers (Google, GitHub, etc.) inherently verify emails.
     */
    private void verifyEmailIfRequired(Jwt jwt, String auth0UserId) {
        if (isSocialLoginProvider(auth0UserId)) {
            log.debug("Social login provider - email verified by provider: {}", auth0UserId);
            return;
        }

        Boolean emailVerified = jwt.getClaim("email_verified");
        if (emailVerified == null || !emailVerified) {
            throw new EmailNotVerifiedException(auth0UserId);
        }
    }

    /**
     * Checks if the Auth0 user ID indicates a social login provider.
     * Social providers verify emails as part of their authentication process.
     */
    private boolean isSocialLoginProvider(String auth0UserId) {
        if (auth0UserId == null) return false;
        return auth0UserId.startsWith("google-oauth2|") ||
               auth0UserId.startsWith("github|") ||
               auth0UserId.startsWith("facebook|") ||
               auth0UserId.startsWith("apple|") ||
               auth0UserId.startsWith("linkedin|") ||
               auth0UserId.startsWith("microsoft|") ||
               auth0UserId.startsWith("twitter|");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip filter for public endpoints
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator") ||
               path.contains("/check-username") ||
               path.contains("/check-email");
    }
}
