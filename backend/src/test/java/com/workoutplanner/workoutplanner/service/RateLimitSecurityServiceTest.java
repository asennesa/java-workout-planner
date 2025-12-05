package com.workoutplanner.workoutplanner.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RateLimitSecurityService.
 * Tests the SpEL expressions used by Bucket4j rate limiting configuration.
 */
@DisplayName("RateLimitSecurityService Unit Tests")
class RateLimitSecurityServiceTest {

    private RateLimitSecurityService rateLimitSecurityService;

    @BeforeEach
    void setUp() {
        rateLimitSecurityService = new RateLimitSecurityService();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== GET USERNAME TESTS ====================

    @Nested
    @DisplayName("getUsername() Tests")
    class GetUsernameTests {

        @Test
        @DisplayName("Should return username when user is authenticated")
        void shouldReturnUsernameWhenAuthenticated() {
            // Arrange
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act
            String result = rateLimitSecurityService.getUsername();

            // Assert
            assertThat(result).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should return 'anonymous' when no authentication")
        void shouldReturnAnonymousWhenNoAuthentication() {
            // Arrange - no authentication set

            // Act
            String result = rateLimitSecurityService.getUsername();

            // Assert
            assertThat(result).isEqualTo("anonymous");
        }

        @Test
        @DisplayName("Should return 'anonymous' when authentication is null")
        void shouldReturnAnonymousWhenAuthenticationNull() {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(null);

            // Act
            String result = rateLimitSecurityService.getUsername();

            // Assert
            assertThat(result).isEqualTo("anonymous");
        }

        @Test
        @DisplayName("Should return 'anonymous' when principal is 'anonymousUser'")
        void shouldReturnAnonymousWhenPrincipalIsAnonymousUser() {
            // Arrange - constructor with authorities list automatically sets authenticated=true
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "anonymousUser",
                    null,
                    Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act
            String result = rateLimitSecurityService.getUsername();

            // Assert
            assertThat(result).isEqualTo("anonymous");
        }

        @Test
        @DisplayName("Should return username for admin user")
        void shouldReturnUsernameForAdminUser() {
            // Arrange
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "adminuser",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act
            String result = rateLimitSecurityService.getUsername();

            // Assert
            assertThat(result).isEqualTo("adminuser");
        }
    }

    // ==================== IS AUTHENTICATED TESTS ====================

    @Nested
    @DisplayName("isAuthenticated() Tests")
    class IsAuthenticatedTests {

        @Test
        @DisplayName("Should return true when user is authenticated")
        void shouldReturnTrueWhenAuthenticated() {
            // Arrange
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act
            boolean result = rateLimitSecurityService.isAuthenticated();

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when no authentication")
        void shouldReturnFalseWhenNoAuthentication() {
            // Arrange - no authentication set

            // Act
            boolean result = rateLimitSecurityService.isAuthenticated();

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when authentication is null")
        void shouldReturnFalseWhenAuthenticationNull() {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(null);

            // Act
            boolean result = rateLimitSecurityService.isAuthenticated();

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when principal is 'anonymousUser'")
        void shouldReturnFalseWhenPrincipalIsAnonymousUser() {
            // Arrange - constructor with authorities list automatically sets authenticated=true
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "anonymousUser",
                    null,
                    Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act
            boolean result = rateLimitSecurityService.isAuthenticated();

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when authentication not marked as authenticated")
        void shouldReturnFalseWhenNotMarkedAuthenticated() {
            // Arrange
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    "password"
            );
            auth.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act
            boolean result = rateLimitSecurityService.isAuthenticated();

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true for admin user")
        void shouldReturnTrueForAdminUser() {
            // Arrange
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "adminuser",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act
            boolean result = rateLimitSecurityService.isAuthenticated();

            // Assert
            assertThat(result).isTrue();
        }
    }

    // ==================== RATE LIMITING SCENARIO TESTS ====================

    @Nested
    @DisplayName("Rate Limiting Scenario Tests")
    class RateLimitingScenarioTests {

        @Test
        @DisplayName("Should provide unique cache key per user for rate limiting")
        void shouldProvideUniqueCacheKeyPerUser() {
            // Arrange & Act & Assert - User 1
            UsernamePasswordAuthenticationToken auth1 = new UsernamePasswordAuthenticationToken(
                    "user1",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth1);
            String user1Key = rateLimitSecurityService.getUsername();

            // Clear and switch to User 2
            SecurityContextHolder.clearContext();
            UsernamePasswordAuthenticationToken auth2 = new UsernamePasswordAuthenticationToken(
                    "user2",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth2);
            String user2Key = rateLimitSecurityService.getUsername();

            // Assert
            assertThat(user1Key).isEqualTo("user1");
            assertThat(user2Key).isEqualTo("user2");
            assertThat(user1Key).isNotEqualTo(user2Key);
        }

        @Test
        @DisplayName("Should apply rate limiting only to authenticated users")
        void shouldApplyRateLimitingOnlyToAuthenticatedUsers() {
            // Test unauthenticated user
            SecurityContextHolder.clearContext();
            assertThat(rateLimitSecurityService.isAuthenticated()).isFalse();
            assertThat(rateLimitSecurityService.getUsername()).isEqualTo("anonymous");

            // Test authenticated user
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "realuser",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            assertThat(rateLimitSecurityService.isAuthenticated()).isTrue();
            assertThat(rateLimitSecurityService.getUsername()).isEqualTo("realuser");
        }

        @Test
        @DisplayName("Should handle multiple consecutive calls consistently")
        void shouldHandleMultipleConsecutiveCallsConsistently() {
            // Arrange
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "testuser",
                    "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Act - simulate multiple rate limit checks
            String username1 = rateLimitSecurityService.getUsername();
            boolean auth1 = rateLimitSecurityService.isAuthenticated();
            String username2 = rateLimitSecurityService.getUsername();
            boolean auth2 = rateLimitSecurityService.isAuthenticated();
            String username3 = rateLimitSecurityService.getUsername();
            boolean auth3 = rateLimitSecurityService.isAuthenticated();

            // Assert - all calls should return consistent results
            assertThat(username1).isEqualTo("testuser");
            assertThat(username2).isEqualTo("testuser");
            assertThat(username3).isEqualTo("testuser");
            assertThat(auth1).isTrue();
            assertThat(auth2).isTrue();
            assertThat(auth3).isTrue();
        }
    }
}
