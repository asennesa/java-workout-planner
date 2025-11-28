package com.workoutplanner.workoutplanner.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Converts JWT tokens to Spring Security Authentication objects.
 *
 * Industry Best Practice:
 * - ONLY extracts authorities from JWT claims
 * - NO database operations (those happen in Auth0UserSyncFilter)
 * - Stateless and fast
 * - Called once per request by Spring Security
 *
 * Architecture:
 * <pre>
 * JwtDecoder → THIS CONVERTER → JwtAuthenticationToken → Auth0UserSyncFilter → Auth0AuthenticationToken
 * </pre>
 *
 * The converter extracts permissions from Auth0 custom claims and converts them
 * to Spring Security GrantedAuthority objects for @PreAuthorize checks.
 *
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html">Spring Security JWT</a>
 */
@Component
@Profile("!test & !dev")  // Only active in production (Auth0 mode)
public class Auth0JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(Auth0JwtAuthenticationConverter.class);

    private final String audience;

    public Auth0JwtAuthenticationConverter(@Value("${auth0.audience}") String audience) {
        this.audience = audience;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        logger.debug("Converted JWT for user: {}, authorities: {}",
            jwt.getSubject(), authorities.size());

        // Return standard JwtAuthenticationToken
        // Auth0UserSyncFilter will later wrap this with Auth0AuthenticationToken
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    /**
     * Extracts granted authorities from JWT claims.
     *
     * OAuth2 Best Practice:
     * - Use permissions (scopes) as primary authorization mechanism
     * - Permissions are added via Auth0 Actions
     * - Role is included for backward compatibility with hasRole()
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract permissions from custom claim (OAuth2 standard)
        String permissionsClaim = audience + "/permissions";
        Object permissionsObj = jwt.getClaim(permissionsClaim);

        if (permissionsObj instanceof Collection<?> permissions) {
            permissions.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

            logger.debug("Extracted permissions: {}", authorities);
        } else {
            logger.warn("No permissions found in JWT claim: {}", permissionsClaim);
        }

        // Extract role for backward compatibility with hasRole()
        String roleClaim = audience + "/role";
        Object roleObj = jwt.getClaim(roleClaim);

        if (roleObj instanceof String role) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            logger.debug("Extracted role: ROLE_{}", role);
        }

        return authorities;
    }
}
