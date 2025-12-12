package com.workoutplanner.workoutplanner.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Additional security headers not provided by Spring Security out of the box.
 *
 * This filter adds:
 * - X-Permitted-Cross-Domain-Policies: Prevents Adobe products from loading data
 * - X-XSS-Protection: Legacy XSS protection for older browsers
 *
 * These headers complement the security headers configured in Auth0SecurityConfig:
 * - Content-Security-Policy (CSP)
 * - Strict-Transport-Security (HSTS)
 * - X-Frame-Options
 * - X-Content-Type-Options
 * - Referrer-Policy
 * - Permissions-Policy
 * - Cross-Origin-Opener-Policy
 * - Cross-Origin-Resource-Policy
 *
 * @see <a href="https://owasp.org/www-project-secure-headers/">OWASP Secure Headers Project</a>
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Headers_Cheat_Sheet.html">OWASP HTTP Headers Cheat Sheet</a>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Profile("!test")
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Prevent Adobe Flash and PDF from loading data from this domain
        // Mitigates cross-domain data theft via Adobe products
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");

        // Legacy XSS protection for older browsers (IE, older Safari)
        // Modern browsers use CSP instead, but this provides defense in depth
        // mode=block prevents page rendering if XSS is detected
        response.setHeader("X-XSS-Protection", "1; mode=block");

        filterChain.doFilter(request, response);
    }
}
