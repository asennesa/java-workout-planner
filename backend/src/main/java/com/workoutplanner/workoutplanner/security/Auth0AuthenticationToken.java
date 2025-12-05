package com.workoutplanner.workoutplanner.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Objects;

/**
 * Custom authentication token for Auth0 authenticated users.
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

    @Override
    public String getName() {
        return principal.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Auth0AuthenticationToken that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(principal, that.principal) && Objects.equals(jwt, that.jwt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), principal, jwt);
    }
}
