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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Auth0 OAuth2 + JWT Security Configuration.
 *
 * Uses Spring Boot's auto-configured JwtDecoder with:
 * - Issuer validation (spring.security.oauth2.resourceserver.jwt.issuer-uri)
 * - Audience validation (spring.security.oauth2.resourceserver.jwt.audiences)
 * - RS256 signature verification via Auth0's JWKS endpoint
 *
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html">Spring Security JWT</a>
 * @see <a href="https://auth0.com/docs/quickstart/backend/java-spring-security5">Auth0 Spring Security</a>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!test & !dev")
public class Auth0SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(Auth0SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

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
     *
     * JWT decoding and validation (issuer + audience) is auto-configured by Spring Boot
     * based on application.properties settings.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(authz -> authz
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                // Public endpoints
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/users/check-username", "/api/v1/users/check-email").permitAll()

                // Protected endpoints
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

            // Uses Spring Boot auto-configured JwtDecoder with audience validation
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(auth0JwtAuthenticationConverter))
            );

        logger.info("Auth0 Security configured. Issuer: {}", issuerUri);
        return http.build();
    }
}
