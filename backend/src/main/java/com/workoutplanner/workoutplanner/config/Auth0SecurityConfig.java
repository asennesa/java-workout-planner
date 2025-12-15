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
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.CrossOriginResourcePolicyHeaderWriter;
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
                .requestMatchers("/internal/actuator/health", "/internal/actuator/info").permitAll()
                .requestMatchers("/internal/actuator/**").hasAuthority("read:users")  // Admin-only for other actuator endpoints
                .requestMatchers("/api/v1/users/check-username", "/api/v1/users/check-email").permitAll()
                .requestMatchers("/.well-known/**").permitAll()  // security.txt and other well-known URIs

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

            .headers(headers -> {
                // Existing security headers
                headers.contentTypeOptions(Customizer.withDefaults());  // X-Content-Type-Options: nosniff
                headers.frameOptions(frame -> frame.deny());            // X-Frame-Options: DENY
                headers.referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER));
                headers.cacheControl(Customizer.withDefaults());

                // HSTS - Force HTTPS for 1 year with subdomains and preload
                // @see https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Strict_Transport_Security_Cheat_Sheet.html
                headers.httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)  // 1 year
                    .includeSubDomains(true)
                    .preload(true));

                // Content Security Policy - Prevent XSS attacks
                // @see https://cheatsheetseries.owasp.org/cheatsheets/Content_Security_Policy_Cheat_Sheet.html
                headers.contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self'; " +
                        "connect-src 'self' https://*.auth0.com; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'; " +
                        "base-uri 'self'"
                    ));

                // Permissions Policy - Restrict browser features
                // @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Permissions-Policy
                headers.permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=(), payment=()"));

                // Cross-Origin policies for defense in depth
                headers.crossOriginOpenerPolicy(coop -> coop
                    .policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN));
                headers.crossOriginResourcePolicy(corp -> corp
                    .policy(CrossOriginResourcePolicyHeaderWriter.CrossOriginResourcePolicy.SAME_ORIGIN));
            })

            // Uses Spring Boot auto-configured JwtDecoder with audience validation
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(auth0JwtAuthenticationConverter))
            );

        logger.info("Auth0 Security configured. Issuer: {}", issuerUri);
        return http.build();
    }
}
