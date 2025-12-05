package com.workoutplanner.workoutplanner.security;

import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.security.exception.Auth0AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Auth0UserSyncService.
 * Tests user synchronization logic from Auth0 JWT to local database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth0UserSyncService Unit Tests")
class Auth0UserSyncServiceTest {

    private static final String TEST_AUDIENCE = "https://api.workoutplanner.com";
    private static final String AUTH0_USER_ID = "auth0|123456789";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private UserRepository userRepository;

    private Auth0UserSyncService auth0UserSyncService;

    @BeforeEach
    void setUp() {
        auth0UserSyncService = new Auth0UserSyncService(userRepository, TEST_AUDIENCE);
    }

    private Jwt createJwt(Map<String, Object> claims) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );
    }

    private Map<String, Object> createBasicClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", AUTH0_USER_ID);
        claims.put("email", TEST_EMAIL);
        claims.put("nickname", TEST_USERNAME);
        claims.put("given_name", "Test");
        claims.put("family_name", "User");
        return claims;
    }

    private User createExistingUser() {
        User user = new User();
        user.setUserId(1L);
        user.setAuth0UserId(AUTH0_USER_ID);
        user.setEmail(TEST_EMAIL);
        user.setUsername(TEST_USERNAME);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.USER);
        return user;
    }

    // ==================== NEW USER CREATION TESTS ====================

    @Nested
    @DisplayName("New User Creation Tests")
    class NewUserCreationTests {

        @Test
        @DisplayName("Should create new user when Auth0 user does not exist locally")
        void shouldCreateNewUserWhenNotExists() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setUserId(1L);
                return user;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.auth0UserId()).isEqualTo(AUTH0_USER_ID);
            assertThat(result.email()).isEqualTo(TEST_EMAIL);
            assertThat(result.username()).isEqualTo(TEST_USERNAME);
            assertThat(result.firstName()).isEqualTo("Test");
            assertThat(result.lastName()).isEqualTo("User");
            assertThat(result.role()).isEqualTo(UserRole.USER);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should extract username from nickname claim")
        void shouldExtractUsernameFromNickname() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put("nickname", "mynickname");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.username()).isEqualTo("mynickname");
        }

        @Test
        @DisplayName("Should extract username from preferred_username when nickname is missing")
        void shouldExtractUsernameFromPreferredUsername() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.remove("nickname");
            claims.put("preferred_username", "preferred");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.username()).isEqualTo("preferred");
        }

        @Test
        @DisplayName("Should extract username from email when nickname and preferred_username are missing")
        void shouldExtractUsernameFromEmail() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.remove("nickname");
            claims.put("email", "john.doe@example.com");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.username()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("Should extract first name from given_name claim")
        void shouldExtractFirstNameFromGivenName() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put("given_name", "John");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.firstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should extract first name from full name when given_name is missing")
        void shouldExtractFirstNameFromFullName() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.remove("given_name");
            claims.put("name", "John Doe");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.firstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should extract last name from family_name claim")
        void shouldExtractLastNameFromFamilyName() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put("family_name", "Smith");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.lastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should extract last name from full name when family_name is missing")
        void shouldExtractLastNameFromFullName() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.remove("family_name");
            claims.put("name", "John Smith");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.lastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should set default role to USER when role claim is missing")
        void shouldSetDefaultRoleToUser() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.role()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("Should extract ADMIN role from JWT claim")
        void shouldExtractAdminRoleFromClaim() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put(TEST_AUDIENCE + "/role", "ADMIN");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.role()).isEqualTo(UserRole.ADMIN);
        }
    }

    // ==================== EXISTING USER UPDATE TESTS ====================

    @Nested
    @DisplayName("Existing User Update Tests")
    class ExistingUserUpdateTests {

        @Test
        @DisplayName("Should return existing user without saving when no changes")
        void shouldReturnExistingUserWithoutSavingWhenNoChanges() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            Jwt jwt = createJwt(claims);
            User existingUser = createExistingUser();

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.of(existingUser));

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1L);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should update email when changed in Auth0")
        void shouldUpdateEmailWhenChanged() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put("email", "newemail@example.com");
            Jwt jwt = createJwt(claims);
            User existingUser = createExistingUser();

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.email()).isEqualTo("newemail@example.com");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("newemail@example.com");
        }

        @Test
        @DisplayName("Should update first name when changed in Auth0")
        void shouldUpdateFirstNameWhenChanged() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put("given_name", "NewFirstName");
            Jwt jwt = createJwt(claims);
            User existingUser = createExistingUser();

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.firstName()).isEqualTo("NewFirstName");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should update last name when changed in Auth0")
        void shouldUpdateLastNameWhenChanged() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put("family_name", "NewLastName");
            Jwt jwt = createJwt(claims);
            User existingUser = createExistingUser();

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.lastName()).isEqualTo("NewLastName");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should update role when changed in Auth0")
        void shouldUpdateRoleWhenChanged() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put(TEST_AUDIENCE + "/role", "ADMIN");
            Jwt jwt = createJwt(claims);
            User existingUser = createExistingUser();

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.role()).isEqualTo(UserRole.ADMIN);
            verify(userRepository).save(any(User.class));
        }
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw exception when email claim is missing")
        void shouldThrowExceptionWhenEmailMissing() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.remove("email");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> auth0UserSyncService.syncUser(jwt))
                    .isInstanceOf(Auth0AuthenticationException.class)
                    .hasMessageContaining("Email claim missing");
        }

        @Test
        @DisplayName("Should throw exception when email claim is empty")
        void shouldThrowExceptionWhenEmailEmpty() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put("email", "");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> auth0UserSyncService.syncUser(jwt))
                    .isInstanceOf(Auth0AuthenticationException.class)
                    .hasMessageContaining("Email claim missing");
        }

        @Test
        @DisplayName("Should handle invalid role claim gracefully")
        void shouldHandleInvalidRoleClaimGracefully() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            claims.put(TEST_AUDIENCE + "/role", "INVALID_ROLE");
            Jwt jwt = createJwt(claims);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User u = i.getArgument(0);
                u.setUserId(1L);
                return u;
            });

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert - should default to USER role
            assertThat(result.role()).isEqualTo(UserRole.USER);
        }
    }

    // ==================== AUTH0 PRINCIPAL CONVERSION TESTS ====================

    @Nested
    @DisplayName("Auth0Principal Conversion Tests")
    class Auth0PrincipalConversionTests {

        @Test
        @DisplayName("Should convert User entity to Auth0Principal correctly")
        void shouldConvertUserToAuth0Principal() {
            // Arrange
            Map<String, Object> claims = createBasicClaims();
            Jwt jwt = createJwt(claims);
            User existingUser = createExistingUser();
            existingUser.setRole(UserRole.ADMIN);

            when(userRepository.findByAuth0UserId(AUTH0_USER_ID)).thenReturn(Optional.of(existingUser));
            // Mock save() in case updateUserIfNeeded needs to save changes
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Auth0Principal result = auth0UserSyncService.syncUser(jwt);

            // Assert
            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.auth0UserId()).isEqualTo(AUTH0_USER_ID);
            assertThat(result.email()).isEqualTo(TEST_EMAIL);
            assertThat(result.username()).isEqualTo(TEST_USERNAME);
            assertThat(result.firstName()).isEqualTo("Test");
            assertThat(result.lastName()).isEqualTo("User");
            assertThat(result.role()).isEqualTo(UserRole.ADMIN);
        }
    }
}
