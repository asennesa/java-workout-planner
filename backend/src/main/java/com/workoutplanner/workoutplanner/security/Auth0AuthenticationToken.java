package com.workoutplanner.workoutplanner.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Custom authentication token for Auth0 authenticated users.
 *
 * Industry Best Practice:
 * - Extends AbstractAuthenticationToken for Spring Security integration
 * - Contains Auth0Principal DTO (not JPA entity)
 * - Immutable after construction
 * - Holds JWT for token forwarding to downstream services
 *
 * This token replaces JwtAuthenticationToken in the SecurityContext
 * after Auth0UserSyncFilter processes the request.
 */
public class Auth0AuthenticationToken extends AbstractAuthenticationToken {

    private final Auth0Principal principal;
    private final Jwt jwt;

    public Auth0AuthenticationToken(
            Auth0Principal principal,
            Jwt jwt,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public Auth0Principal getPrincipal() {
        return principal;
    }

    /**
     * Gets the JWT token for forwarding to downstream services.
     */
    public Jwt getJwt() {
        return jwt;
    }

    /**
     * Gets the user's database ID.
     */
    public Long getUserId() {
        return principal.userId();
    }

    /**
     * Gets the Auth0 user ID.
     */
    public String getAuth0UserId() {
        return principal.auth0UserId();
    }

    /**
     * Gets the user's email.
     */
    public String getEmail() {
        return principal.email();
    }

    @Override
    public String getName() {
        return principal.getName();
    }
}
