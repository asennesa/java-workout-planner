package com.workoutplanner.workoutplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Auth0 OAuth2 + JWT Security Configuration.
 * 
 * This configuration replaces the previous session-based authentication with
 * stateless JWT authentication using Auth0 as the identity provider.
 * 
 * Security Features:
 * - OAuth2 Resource Server with JWT validation
 * - Auth0 as authorization server (issues tokens)
 * - Stateless authentication (no server-side sessions)
 * - RS256 signature verification using Auth0's public keys (JWKS)
 * - Audience validation (ensures tokens are for this API)
 * - Role-based access control (RBAC) extracted from JWT claims
 * - CORS protection with configurable origins
 * - CSRF disabled (not needed for stateless JWT authentication)
 * 
 * Token Flow:
 * 1. User authenticates with Auth0 (frontend handles this)
 * 2. Auth0 issues JWT access token
 * 3. Client sends token in Authorization: Bearer <token> header
 * 4. This config validates token signature, issuer, audience, expiry
 * 5. Extracts user identity and roles from token claims
 * 6. Grants access based on roles and permissions
 * 
 * @see <a href="https://auth0.com/docs/quickstart/backend/java-spring-security5">Auth0 Spring Security Quickstart</a>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!dev") // Active in all profiles EXCEPT 'dev'
public class Auth0SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(Auth0SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${auth0.audience}")
    private String audience;

    private final CorsConfigurationSource corsConfigurationSource;

    public Auth0SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Security filter chain with JWT authentication and role-based authorization.
     * 
     * Authentication:
     * - OAuth2 Resource Server validates JWT tokens from Auth0
     * - No session management (stateless)
     * - Bearer token authentication via Authorization header
     * 
     * Authorization:
     * - Role-based access control using @PreAuthorize annotations
     * - Roles extracted from JWT custom claims
     * - Same authorization rules as previous session-based config
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // CSRF not needed for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            
            // Stateless session (no server-side sessions)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization rules - Defense-in-Depth Dual-Layer Approach
            // Layer 1 (Filter Chain): Coarse-grained permission checks (safety net)
            // Layer 2 (Method Level): Fine-grained permissions + ownership via @PreAuthorize
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - API Documentation (Swagger/OpenAPI)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // Public endpoints - Health checks
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // Public endpoints - User checks (for registration)
                .requestMatchers("/api/v1/users/check-username", "/api/v1/users/check-email").permitAll()

                // Coarse-grained permission checks (defense in depth)
                // These provide a safety net - fine-grained checks happen at method level

                // Workout Sessions - require ANY workout-related permission
                .requestMatchers("/api/v1/workouts/**")
                    .hasAnyAuthority("read:workouts", "write:workouts", "delete:workouts")

                // Exercise Library - require ANY exercise-related permission
                .requestMatchers("/api/v1/exercises/**")
                    .hasAnyAuthority("read:exercises", "write:exercises", "delete:exercises")

                // Workout Exercise Sets - require ANY workout permission (sets belong to workouts)
                .requestMatchers("/api/v1/workout-exercises/**")
                    .hasAnyAuthority("read:workouts", "write:workouts", "delete:workouts")

                // Users - authenticated users can access (specific permissions at method level)
                .requestMatchers("/api/v1/users/**").authenticated()

                // Deny everything else
                .anyRequest().denyAll()
            )

            // Security Headers Configuration (API-focused)
            // Following OWASP REST Security Cheat Sheet and Spring Security best practices
            .headers(headers -> headers
                // Prevent MIME-sniffing attacks
                // Forces browsers to respect Content-Type header
                // Critical for APIs to prevent content type confusion
                .contentTypeOptions(Customizer.withDefaults())

                // Prevent clickjacking attacks (defense in depth)
                // Even though this is a JSON API, good security practice
                .frameOptions(frame -> frame.deny())

                // Control referrer information sent to third parties
                // Prevents leaking API URLs and sensitive path information
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                )

                // Cache control for API responses
                // Prevents browsers/proxies from caching sensitive data
                .cacheControl(Customizer.withDefaults())

                // NOTE: HSTS (Strict-Transport-Security) should be enabled in PRODUCTION
                // Only enable when HTTPS is configured:
                // .httpStrictTransportSecurity(hsts -> hsts
                //     .maxAgeInSeconds(31536000)  // 1 year
                //     .includeSubDomains(true)
                // )
            )

            // OAuth2 Resource Server with JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        logger.info("Security filter chain configured with API-focused headers");

        return http.build();
    }

    /**
     * JWT Decoder bean - validates JWT tokens from Auth0.
     *
     * Validation includes:
     * - Signature verification using Auth0's public keys (RS256)
     * - Issuer validation (must match Auth0 tenant)
     * - Audience validation (must match this API)
     * - Expiration validation (token must not be expired)
     * - Not-before validation (token must be valid now)
     *
     * JWKS Caching Strategy:
     * - Auth0's public keys (JWKS) are cached for 10 minutes
     * - Reduces Auth0 API calls and improves performance
     * - Provides resilience if Auth0 JWKS endpoint is temporarily slow
     * - 10 minutes balances freshness vs performance (Auth0 rotates keys infrequently)
     *
     * The decoder automatically fetches Auth0's public keys from the JWKS endpoint:
     * https://{tenant}.auth0.com/.well-known/jwks.json
     *
     * @return configured JwtDecoder with explicit JWKS caching
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Create decoder - JWKS caching is handled automatically by Spring Security
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri)
            .build();

        logger.info("JWT Decoder configured for issuer: {}", issuerUri);

        // Add custom validators
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience =
            new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    /**
     * JWT Authentication Converter - extracts user details and authorities from JWT.
     * 
     * Converts JWT claims into Spring Security's Authentication object.
     * Extracts PERMISSIONS (OAuth2 scopes) from Auth0 custom claims.
     * 
     * OAuth2 Best Practice: Use permissions for authorization, not roles.
     * Permissions represent what actions a user can perform (OAuth2 standard).
     * 
     * Auth0 JWT structure (example):
     * {
     *   "sub": "auth0|507f1f77bcf86cd799439011",
     *   "email": "user@example.com",
     *   "https://api.workout-planner.com/role": "USER",
     *   "https://api.workout-planner.com/permissions": [
     *     "read:workouts", "write:workouts", "read:exercises"
     *   ]
     * }
     * 
     * @return configured JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        // Custom converter to extract permissions from Auth0 custom claims
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        
        // Use 'sub' claim as principal (Auth0 user ID)
        converter.setPrincipalClaimName("sub");
        
        return converter;
    }

    /**
     * Extracts permissions from Auth0 JWT custom claims (OAuth2 best practice).
     * 
     * Auth0 stores custom claims with namespaced keys to avoid conflicts:
     * - Primary: https://api.workout-planner.com/permissions (OAuth2 scopes)
     * - Optional: https://api.workout-planner.com/role (for informational purposes)
     * 
     * These are configured in Auth0 Actions (you'll add this in Auth0 dashboard).
     * The Action maps user roles to permissions based on your authorization model.
     * 
     * Permissions are used as-is for authorization checks (e.g., "read:workouts").
     * Roles are optionally included with "ROLE_" prefix for backward compatibility.
     * 
     * @return converter that extracts authorities from JWT
     */
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            
            // Primary: Extract permissions from custom claim (OAuth2 standard)
            Collection<String> permissions = extractPermissionsFromJwt(jwt);
            permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
            
            // Optional: Extract role for informational purposes
            // This allows hasRole() checks if needed, but permissions are preferred
            String role = extractRoleFromJwt(jwt);
            if (role != null && !role.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
            
            return authorities;
        };
    }

    /**
     * Extracts role from JWT custom claims (optional, for informational purposes).
     * 
     * OAuth2 Best Practice: Roles are not part of OAuth2 standard.
     * This is included for backward compatibility and informational purposes only.
     * Authorization should be based on permissions, not roles.
     * 
     * You'll configure this in Auth0 Actions (Post-Login):
     * const role = event.user.app_metadata?.role || 'USER';
     * api.accessToken.setCustomClaim('https://api.workout-planner.com/role', role);
     * 
     * @param jwt the JWT token
     * @return role name (e.g., "USER", "ADMIN")
     */
    private String extractRoleFromJwt(Jwt jwt) {
        String roleClaim = audience + "/role";
        
        Object roleObj = jwt.getClaim(roleClaim);
        
        if (roleObj instanceof String) {
            return (String) roleObj;
        } else if (roleObj instanceof Collection<?>) {
            // Support legacy format where role might be an array
            Collection<?> roles = (Collection<?>) roleObj;
            if (!roles.isEmpty() && roles.iterator().next() instanceof String) {
                return (String) roles.iterator().next();
            }
        }
        
        return null;
    }

    /**
     * Extracts permissions from JWT custom claims (PRIMARY authorization mechanism).
     * 
     * OAuth2 Best Practice: Permissions (scopes) are the standard OAuth2 authorization mechanism.
     * This is what your API should check to determine if a user can perform an action.
     * 
     * You'll configure this in Auth0 Actions (Post-Login):
     * 
     * // Map user role to permissions
     * const role = event.user.app_metadata?.role || 'USER';
     * let permissions = [];
     * 
     * if (role === 'ADMIN') {
     *   permissions = ['read:workouts', 'write:workouts', 'delete:workouts', ...];
     * } else if (role === 'USER') {
     *   permissions = ['read:workouts', 'write:workouts', 'read:exercises', ...];
     * }
     * 
     * api.accessToken.setCustomClaim('https://api.workout-planner.com/permissions', permissions);
     * 
     * @param jwt the JWT token
     * @return collection of permission strings (e.g., "read:workouts", "write:exercises")
     */
    private Collection<String> extractPermissionsFromJwt(Jwt jwt) {
        String permissionsClaim = audience + "/permissions";

        Object permissionsObj = jwt.getClaim(permissionsClaim);

        if (permissionsObj instanceof Collection<?>) {
            Collection<String> permissions = ((Collection<?>) permissionsObj).stream()
                .filter(obj -> obj instanceof String)
                .map(obj -> (String) obj)
                .collect(Collectors.toList());

            // SECURITY LOGGING: Warn if permissions list is empty
            if (permissions.isEmpty()) {
                logger.warn("SECURITY: JWT contains no permissions. claim={}, subject={}, issuer={}",
                    permissionsClaim, jwt.getSubject(), jwt.getIssuer());
            } else {
                logger.debug("Extracted {} permissions for user {}: {}",
                    permissions.size(), jwt.getSubject(), permissions);
            }

            return permissions;
        }

        // SECURITY LOGGING: Log when permissions claim is completely missing
        logger.warn("SECURITY: Permissions claim not found in JWT. Expected claim: {}, Subject: {}, Issuer: {}, Available claims: {}",
            permissionsClaim, jwt.getSubject(), jwt.getIssuer(), jwt.getClaims().keySet());

        // Return empty list if no permissions (user can't do anything)
        return Collections.emptyList();
    }
}

