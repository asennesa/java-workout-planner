package com.workoutplanner.workoutplanner.config;

import com.workoutplanner.workoutplanner.security.Auth0JwtAuthenticationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Auth0 OAuth2 + JWT Security Configuration.
 *
 * Industry Best Practice Implementation:
 * - OAuth2 Resource Server with JWT validation
 * - JwtAuthenticationConverter extracts authorities only (stateless, fast)
 * - Auth0UserSyncFilter handles user sync with database (transactional, cached)
 * - Stateless authentication (no server-side sessions)
 * - RS256 signature verification using Auth0's JWKS
 * - Audience validation
 * - Permission-based access control (OAuth2 standard)
 *
 * Architecture:
 * <pre>
 * Request → JwtDecoder → Auth0JwtAuthenticationConverter → JwtAuthenticationToken
 *                                                                    ↓
 *                                                        Auth0UserSyncFilter
 *                                                        (user sync, transactional)
 *                                                                    ↓
 *                                                        Auth0AuthenticationToken
 *                                                        (Auth0Principal DTO + Authorities)
 *                                                                    ↓
 *                                                             SecurityContext
 * </pre>
 *
 * @see <a href="https://auth0.com/docs/quickstart/backend/java-spring-security5">Auth0 Spring Security</a>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!test & !dev")  // Only active in production (Auth0 mode)
public class Auth0SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(Auth0SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${auth0.audience}")
    private String audience;

    private final CorsConfigurationSource corsConfigurationSource;
    private final Auth0JwtAuthenticationConverter auth0JwtAuthenticationConverter;

    public Auth0SecurityConfig(
            CorsConfigurationSource corsConfigurationSource,
            Auth0JwtAuthenticationConverter auth0JwtAuthenticationConverter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.auth0JwtAuthenticationConverter = auth0JwtAuthenticationConverter;
    }

    /**
     * Security filter chain with JWT authentication.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/users/check-username", "/api/v1/users/check-email").permitAll()

                // Protected endpoints (fine-grained checks at method level)
                .requestMatchers("/api/v1/workouts/**")
                    .hasAnyAuthority("read:workouts", "write:workouts", "delete:workouts")
                .requestMatchers("/api/v1/exercises/**")
                    .hasAnyAuthority("read:exercises", "write:exercises", "delete:exercises")
                .requestMatchers("/api/v1/workout-exercises/**")
                    .hasAnyAuthority("read:workouts", "write:workouts", "delete:workouts")
                .requestMatchers("/api/v1/users/**").authenticated()

                .anyRequest().denyAll()
            )

            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .cacheControl(Customizer.withDefaults())
            )

            // OAuth2 Resource Server with custom converter
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(auth0JwtAuthenticationConverter)
                )
            );

        logger.info("Auth0 Security configured. Issuer: {}", issuerUri);
        return http.build();
    }

    /**
     * JWT Decoder with issuer and audience validation.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
            .withIssuerLocation(issuerUri)
            .build();

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> combined =
            new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(combined);

        logger.info("JWT Decoder configured. Issuer: {}, Audience: {}", issuerUri, audience);
        return jwtDecoder;
    }
}
