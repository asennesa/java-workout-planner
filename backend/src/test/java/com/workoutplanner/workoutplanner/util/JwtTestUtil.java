package com.workoutplanner.workoutplanner.util;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.time.Instant;
import java.util.*;

/**
 * Utility class for creating mock JWT tokens in tests.
 * 
 * Provides factory methods to create realistic JWT tokens for testing
 * Auth0 authentication without actually calling Auth0.
 * 
 * Usage in tests:
 * <pre>
 * {@code
 * // Create JWT for regular user
 * Jwt jwt = JwtTestUtil.createMockJwt("auth0|123", "test@example.com", List.of("USER"));
 * 
 * // Create JWT for admin
 * Jwt jwt = JwtTestUtil.createAdminJwt("auth0|456", "admin@example.com");
 * 
 * // Use in MockMvc tests
 * mockMvc.perform(get("/api/v1/workouts")
 *     .with(jwt().jwt(jwt)));
 * }
 * </pre>
 */
public class JwtTestUtil {

    private static final String DEFAULT_ISSUER = "https://test-tenant.auth0.com/";
    private static final String DEFAULT_AUDIENCE = "https://api.workout-planner.com";
    private static final String ROLES_CLAIM = DEFAULT_AUDIENCE + "/roles";
    private static final String PERMISSIONS_CLAIM = DEFAULT_AUDIENCE + "/permissions";

    /**
     * Creates a mock JWT token with standard claims for a regular user.
     * 
     * @param auth0UserId Auth0 user ID (e.g., "auth0|507f1f77bcf86cd799439011")
     * @param email user's email address
     * @return mock JWT token
     */
    public static Jwt createMockJwt(String auth0UserId, String email) {
        return createMockJwt(auth0UserId, email, List.of("USER"), List.of());
    }

    /**
     * Creates a mock JWT token with specified roles.
     * 
     * @param auth0UserId Auth0 user ID
     * @param email user's email address
     * @param roles user roles (e.g., "USER", "ADMIN")
     * @return mock JWT token
     */
    public static Jwt createMockJwt(String auth0UserId, String email, List<String> roles) {
        return createMockJwt(auth0UserId, email, roles, List.of());
    }

    /**
     * Creates a mock JWT token with specified roles and permissions.
     * 
     * @param auth0UserId Auth0 user ID
     * @param email user's email address
     * @param roles user roles (e.g., "USER", "ADMIN")
     * @param permissions user permissions (e.g., "read:workouts", "write:workouts")
     * @return mock JWT token
     */
    public static Jwt createMockJwt(String auth0UserId, String email, 
                                     List<String> roles, List<String> permissions) {
        
        return createJwtBuilder()
            .subject(auth0UserId)
            .claim("email", email)
            .claim("email_verified", true)
            .claim(ROLES_CLAIM, roles)
            .claim(PERMISSIONS_CLAIM, permissions)
            .build();
    }

    /**
     * Creates a mock JWT token for an admin user.
     * 
     * @param auth0UserId Auth0 user ID
     * @param email admin's email address
     * @return mock JWT token with ADMIN role
     */
    public static Jwt createAdminJwt(String auth0UserId, String email) {
        return createMockJwt(auth0UserId, email, List.of("ADMIN"), List.of(
            "read:workouts", "write:workouts", "delete:workouts",
            "read:exercises", "write:exercises", "delete:exercises",
            "admin:users"
        ));
    }

    /**
     * Creates a mock JWT token for a moderator user.
     * 
     * @param auth0UserId Auth0 user ID
     * @param email moderator's email address
     * @return mock JWT token with MODERATOR role
     */
    public static Jwt createModeratorJwt(String auth0UserId, String email) {
        return createMockJwt(auth0UserId, email, List.of("MODERATOR"), List.of(
            "read:exercises", "write:exercises"
        ));
    }

    /**
     * Creates a JWT builder with standard claims pre-populated.
     * 
     * @return JWT builder with issuer, audience, timestamps
     */
    public static JwtBuilder createJwtBuilder() {
        return new JwtBuilder()
            .issuer(DEFAULT_ISSUER)
            .audience(DEFAULT_AUDIENCE)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600));
    }

    /**
     * Builder for creating custom JWT tokens in tests.
     */
    public static class JwtBuilder {
        private final Map<String, Object> claims = new HashMap<>();
        private final Map<String, Object> headers = new HashMap<>();
        private String subject;
        private Instant issuedAt;
        private Instant expiresAt;

        public JwtBuilder() {
            // Default headers
            headers.put("alg", "RS256");
            headers.put("typ", "JWT");
        }

        public JwtBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public JwtBuilder issuer(String issuer) {
            claims.put(JwtClaimNames.ISS, issuer);
            return this;
        }

        public JwtBuilder audience(String audience) {
            claims.put(JwtClaimNames.AUD, List.of(audience));
            return this;
        }

        public JwtBuilder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public JwtBuilder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public JwtBuilder claim(String name, Object value) {
            claims.put(name, value);
            return this;
        }

        public JwtBuilder header(String name, Object value) {
            headers.put(name, value);
            return this;
        }

        public Jwt build() {
            if (subject == null) {
                throw new IllegalStateException("Subject is required");
            }
            if (issuedAt == null) {
                issuedAt = Instant.now();
            }
            if (expiresAt == null) {
                expiresAt = Instant.now().plusSeconds(3600);
            }

            claims.put(JwtClaimNames.SUB, subject);
            claims.put(JwtClaimNames.IAT, issuedAt);
            claims.put(JwtClaimNames.EXP, expiresAt);

            return new Jwt(
                "mock-token-" + UUID.randomUUID(),
                issuedAt,
                expiresAt,
                headers,
                claims
            );
        }
    }

    /**
     * Extracts granted authorities from a JWT token.
     * Useful for verifying role extraction in tests.
     * 
     * @param jwt the JWT token
     * @return collection of granted authorities
     */
    @SuppressWarnings("unchecked")
    public static Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Extract roles
        Object rolesObj = jwt.getClaim(ROLES_CLAIM);
        if (rolesObj instanceof List<?>) {
            List<String> roles = (List<String>) rolesObj;
            roles.forEach(role -> 
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
            );
        }

        // Extract permissions
        Object permissionsObj = jwt.getClaim(PERMISSIONS_CLAIM);
        if (permissionsObj instanceof List<?>) {
            List<String> permissions = (List<String>) permissionsObj;
            permissions.forEach(permission -> 
                authorities.add(new SimpleGrantedAuthority(permission))
            );
        }

        return authorities;
    }
}

