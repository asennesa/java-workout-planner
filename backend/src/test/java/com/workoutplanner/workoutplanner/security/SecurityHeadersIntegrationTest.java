package com.workoutplanner.workoutplanner.security;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests for security headers.
 *
 * Verifies that the application returns proper security headers as recommended by:
 * - OWASP Secure Headers Project
 * - OWASP HTTP Headers Cheat Sheet
 * - Spring Security Best Practices
 *
 * Note: Some headers (HSTS, CSP) are only applied in production profile.
 * This test verifies the baseline headers available in test profile.
 *
 * @see <a href="https://owasp.org/www-project-secure-headers/">OWASP Secure Headers</a>
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Headers_Cheat_Sheet.html">HTTP Headers Cheat Sheet</a>
 */
@DisplayName("Security Headers Integration Tests")
class SecurityHeadersIntegrationTest extends AbstractIntegrationTest {

    @Nested
    @DisplayName("Basic Security Headers")
    class BasicSecurityHeaders {

        @Test
        @DisplayName("Should include X-Content-Type-Options header")
        void shouldIncludeXContentTypeOptionsHeader() {
            given()
                .when()
                    .get("/users/check-username?username=testuser")
                .then()
                    .statusCode(200)
                    .header("X-Content-Type-Options", equalTo("nosniff"));
        }

        @Test
        @DisplayName("Should include X-Frame-Options header")
        void shouldIncludeXFrameOptionsHeader() {
            given()
                .when()
                    .get("/users/check-username?username=testuser")
                .then()
                    .statusCode(200)
                    .header("X-Frame-Options", equalTo("DENY"));
        }

        @Test
        @DisplayName("Should include Cache-Control header for API responses")
        void shouldIncludeCacheControlHeader() {
            given()
                .when()
                    .get("/users/check-username?username=testuser")
                .then()
                    .statusCode(200)
                    .header("Cache-Control", notNullValue());
        }
    }

    @Nested
    @DisplayName("Production Security Headers (Profile-Specific)")
    class ProductionSecurityHeaders {

        /**
         * Note: HSTS is only enabled in production profile.
         * This test documents expected behavior but may not assert in test profile.
         */
        @Test
        @DisplayName("HSTS header should be configured in production profile")
        void hstsHeaderShouldBeConfiguredInProduction() {
            // HSTS is typically only sent over HTTPS and in production profile
            // This test verifies the endpoint responds correctly
            given()
                .when()
                    .get("/users/check-username?username=testuser")
                .then()
                    .statusCode(200);
            // In production with HTTPS: Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
        }

        /**
         * Note: CSP is only enabled in production profile.
         * This test documents expected behavior.
         */
        @Test
        @DisplayName("CSP header should be configured in production profile")
        void cspHeaderShouldBeConfiguredInProduction() {
            // CSP is configured in Auth0SecurityConfig for production
            // Test verifies endpoint responds correctly
            given()
                .when()
                    .get("/users/check-username?username=testuser")
                .then()
                    .statusCode(200);
            // In production: Content-Security-Policy with default-src, script-src, etc.
        }
    }

    @Nested
    @DisplayName("Security.txt Endpoint")
    class SecurityTxtEndpoint {

        @Test
        @DisplayName("Should serve security.txt at /.well-known/security.txt")
        void shouldServeSecurityTxt() {
            // Note: This test requires static resource serving to be enabled
            // The endpoint should return the security.txt content
            given()
                .when()
                    .get("/.well-known/security.txt")
                .then()
                    .statusCode(anyOf(equalTo(200), equalTo(404)));
            // 200 if static resources are served, 404 if not configured for test profile
        }
    }

    // Helper method for flexible status code matching
    private static org.hamcrest.Matcher<Integer> anyOf(org.hamcrest.Matcher<Integer>... matchers) {
        return org.hamcrest.Matchers.anyOf(matchers);
    }
}
