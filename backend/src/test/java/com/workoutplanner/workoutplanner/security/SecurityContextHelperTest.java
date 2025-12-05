package com.workoutplanner.workoutplanner.security;

import com.workoutplanner.workoutplanner.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for SecurityContextHelper.
 * Tests static helper methods for accessing authenticated user from SecurityContext.
 */
@DisplayName("SecurityContextHelper Unit Tests")
class SecurityContextHelperTest {

    private Auth0Principal testPrincipal;
    private Auth0AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        testPrincipal = new Auth0Principal(
                1L,
                "auth0|123456",
                "test@example.com",
                "testuser",
                "Test",
                "User",
                UserRole.USER
        );

        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", "auth0|123456")
        );

        authToken = new Auth0AuthenticationToken(
                testPrincipal,
                jwt,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== GET CURRENT PRINCIPAL TESTS ====================

    @Nested
    @DisplayName("getCurrentPrincipal() Tests")
    class GetCurrentPrincipalTests {

        @Test
        @DisplayName("Should return principal when authenticated with Auth0AuthenticationToken")
        void shouldReturnPrincipalWhenAuthenticated() {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Act
            Auth0Principal result = SecurityContextHelper.getCurrentPrincipal();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.auth0UserId()).isEqualTo("auth0|123456");
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when no authentication")
        void shouldThrowExceptionWhenNoAuthentication() {
            // Arrange - no authentication set

            // Act & Assert
            assertThatThrownBy(SecurityContextHelper::getCurrentPrincipal)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No authenticated user found");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when authentication is null")
        void shouldThrowExceptionWhenAuthenticationNull() {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(null);

            // Act & Assert
            assertThatThrownBy(SecurityContextHelper::getCurrentPrincipal)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No authenticated user found");
        }
    }

    // ==================== GET CURRENT USER ID TESTS ====================

    @Nested
    @DisplayName("getCurrentUserId() Tests")
    class GetCurrentUserIdTests {

        @Test
        @DisplayName("Should return user ID when authenticated")
        void shouldReturnUserIdWhenAuthenticated() {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Act
            Long result = SecurityContextHelper.getCurrentUserId();

            // Assert
            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when not authenticated")
        void shouldThrowExceptionWhenNotAuthenticated() {
            // Arrange - no authentication set

            // Act & Assert
            assertThatThrownBy(SecurityContextHelper::getCurrentUserId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No authenticated user found");
        }
    }

    // ==================== GET PRINCIPAL OPTIONAL TESTS ====================

    @Nested
    @DisplayName("getPrincipalOptional() Tests")
    class GetPrincipalOptionalTests {

        @Test
        @DisplayName("Should return Optional with principal when authenticated")
        void shouldReturnOptionalWithPrincipalWhenAuthenticated() {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Act
            Optional<Auth0Principal> result = SecurityContextHelper.getPrincipalOptional();

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().userId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty Optional when no authentication")
        void shouldReturnEmptyOptionalWhenNoAuthentication() {
            // Arrange - no authentication set

            // Act
            Optional<Auth0Principal> result = SecurityContextHelper.getPrincipalOptional();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when authentication is null")
        void shouldReturnEmptyOptionalWhenAuthenticationNull() {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(null);

            // Act
            Optional<Auth0Principal> result = SecurityContextHelper.getPrincipalOptional();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when authentication is not Auth0AuthenticationToken")
        void shouldReturnEmptyOptionalWhenNotAuth0Token() {
            // Arrange - use a different authentication type
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken otherToken =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            "user", "password"
                    );
            SecurityContextHolder.getContext().setAuthentication(otherToken);

            // Act
            Optional<Auth0Principal> result = SecurityContextHelper.getPrincipalOptional();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional when not authenticated")
        void shouldReturnEmptyOptionalWhenNotMarkedAuthenticated() {
            // Arrange
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken unauthToken =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            "user", "password"
                    );
            unauthToken.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(unauthToken);

            // Act
            Optional<Auth0Principal> result = SecurityContextHelper.getPrincipalOptional();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle principal with ADMIN role")
        void shouldHandlePrincipalWithAdminRole() {
            // Arrange
            Auth0Principal adminPrincipal = new Auth0Principal(
                    2L,
                    "auth0|admin123",
                    "admin@example.com",
                    "admin",
                    "Admin",
                    "User",
                    UserRole.ADMIN
            );

            Jwt jwt = new Jwt(
                    "admin-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    Map.of("sub", "auth0|admin123")
            );

            Auth0AuthenticationToken adminToken = new Auth0AuthenticationToken(
                    adminPrincipal,
                    jwt,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

            SecurityContextHolder.getContext().setAuthentication(adminToken);

            // Act
            Auth0Principal result = SecurityContextHelper.getCurrentPrincipal();

            // Assert
            assertThat(result.role()).isEqualTo(UserRole.ADMIN);
            assertThat(result.userId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should return consistent results on multiple calls")
        void shouldReturnConsistentResultsOnMultipleCalls() {
            // Arrange
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Act
            Auth0Principal result1 = SecurityContextHelper.getCurrentPrincipal();
            Auth0Principal result2 = SecurityContextHelper.getCurrentPrincipal();
            Long userId1 = SecurityContextHelper.getCurrentUserId();
            Long userId2 = SecurityContextHelper.getCurrentUserId();

            // Assert
            assertThat(result1).isEqualTo(result2);
            assertThat(userId1).isEqualTo(userId2);
        }
    }
}
