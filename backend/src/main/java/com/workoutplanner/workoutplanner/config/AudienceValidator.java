package com.workoutplanner.workoutplanner.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Validates the audience (aud) claim in JWT tokens.
 * 
 * This validator ensures that JWT tokens issued by Auth0 are intended for this API
 * by checking if the configured audience is present in the token's audience claims.
 * 
 * Security Rationale:
 * - Prevents token misuse across different APIs
 * - Ensures tokens are intended for this specific resource server
 * - Part of OAuth2 best practices for multi-API environments
 * 
 * @see <a href="https://auth0.com/docs/secure/tokens/json-web-tokens/validate-json-web-tokens">Auth0 JWT Validation</a>
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    
    private final String audience;
    
    /**
     * Creates an audience validator for the specified audience.
     * 
     * @param audience the expected audience value (typically your API identifier)
     */
    public AudienceValidator(String audience) {
        this.audience = audience;
    }
    
    /**
     * Validates that the JWT contains the expected audience claim.
     * 
     * @param jwt the JWT token to validate
     * @return validation result indicating success or failure
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audiences = jwt.getAudience();
        
        if (audiences != null && audiences.contains(this.audience)) {
            return OAuth2TokenValidatorResult.success();
        }
        
        OAuth2Error error = new OAuth2Error(
            "invalid_token",
            String.format("The required audience '%s' is missing. Token audiences: %s", 
                this.audience, audiences),
            null
        );
        
        return OAuth2TokenValidatorResult.failure(error);
    }
}

