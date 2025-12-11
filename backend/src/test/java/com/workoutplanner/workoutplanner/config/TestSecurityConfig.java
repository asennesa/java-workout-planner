package com.workoutplanner.workoutplanner.config;

import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.security.Auth0AuthenticationToken;
import com.workoutplanner.workoutplanner.security.Auth0Principal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Test Security Configuration.
 *
 * ONLY ACTIVE IN 'test' PROFILE - for unit and integration tests.
 *
 * This configuration provides a permissive security setup for tests:
 * - All endpoints are permitAll() for easy testing
 * - Auto-authenticates requests with a test user (userId=1) via TestAuthFilter
 * - CSRF disabled
 * - Stateless sessions
 * - Method security enabled (for proper testing of @PreAuthorize)
 *
 * Note: This config is in src/test/java so it's only available during tests.
 * It uses @Profile("test") which is activated by @ActiveProfiles("test") in tests.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("test")
public class TestSecurityConfig {

    /**
     * Filter that auto-authenticates all requests with a test user.
     * This allows integration tests to work with @PreAuthorize protected methods.
     *
     * Uses Spring Security 6's SecurityContextHolderStrategy for proper context management.
     */
    public static class TestAuthFilter extends OncePerRequestFilter {

        private static final Logger log = LoggerFactory.getLogger(TestAuthFilter.class);

        private static Long testUserId = 1L;
        private static boolean adminMode = true; // Default to admin mode for backward compatibility

        private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

        // Use RequestAttributeSecurityContextRepository for stateless sessions
        private final SecurityContextRepository securityContextRepository =
            new RequestAttributeSecurityContextRepository();

        public static void setTestUserId(Long userId) {
            testUserId = userId;
        }

        public static Long getTestUserId() {
            return testUserId;
        }

        /**
         * Enable or disable admin mode.
         * When true (default): Test user has admin permissions (read:users, write:users, delete:users)
         * When false: Test user has only regular user permissions (for testing ownership-based authorization)
         *
         * @param enabled true for admin mode, false for regular user mode
         */
        public static void setAdminMode(boolean enabled) {
            adminMode = enabled;
        }

        public static boolean isAdminMode() {
            return adminMode;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            // Create a test principal with the configured user ID
            Auth0Principal principal = new Auth0Principal(
                    testUserId,
                    "auth0|test-user-" + testUserId,
                    "test" + testUserId + "@example.com",
                    "testuser" + testUserId,
                    "Test",
                    "User",
                    UserRole.USER
            );

            Jwt jwt = new Jwt(
                    "test-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    Map.of("sub", "auth0|test-user-" + testUserId)
            );

            // Grant authorities based on configuration
            // Default: admin-like access (all permissions) for existing tests
            // Can be set to non-admin mode for authorization testing
            List<SimpleGrantedAuthority> authorities;
            if (adminMode) {
                authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        // Workout permissions
                        new SimpleGrantedAuthority("read:workouts"),
                        new SimpleGrantedAuthority("write:workouts"),
                        new SimpleGrantedAuthority("delete:workouts"),
                        // Exercise permissions
                        new SimpleGrantedAuthority("read:exercises"),
                        new SimpleGrantedAuthority("write:exercises"),
                        new SimpleGrantedAuthority("delete:exercises"),
                        // User permissions (admin access)
                        new SimpleGrantedAuthority("read:users"),
                        new SimpleGrantedAuthority("write:users"),
                        new SimpleGrantedAuthority("delete:users")
                );
            } else {
                // Non-admin mode for authorization testing
                authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("read:workouts"),
                        new SimpleGrantedAuthority("write:workouts"),
                        new SimpleGrantedAuthority("delete:workouts"),
                        new SimpleGrantedAuthority("read:exercises"),
                        new SimpleGrantedAuthority("write:exercises"),
                        new SimpleGrantedAuthority("delete:exercises")
                );
            }

            Auth0AuthenticationToken auth = new Auth0AuthenticationToken(
                    principal,
                    jwt,
                    authorities
            );

            // Create and populate security context using Spring Security 6's approach
            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(auth);
            securityContextHolderStrategy.setContext(context);

            // Also save to repository so it's available throughout the request
            securityContextRepository.saveContext(context, request, response);

            log.debug("TestAuthFilter: Set authentication for userId={}, isAuthenticated={}",
                    testUserId, auth.isAuthenticated());
            log.debug("TestAuthFilter: SecurityContext authentication={}",
                    SecurityContextHolder.getContext().getAuthentication());

            filterChain.doFilter(request, response);

            log.debug("TestAuthFilter: After filterChain, authentication={}",
                    SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new RequestAttributeSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository))
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            // Add filter AFTER SecurityContextHolderFilter to ensure context is set up
            .addFilterAfter(new TestAuthFilter(), SecurityContextHolderFilter.class);

        return http.build();
    }
}
