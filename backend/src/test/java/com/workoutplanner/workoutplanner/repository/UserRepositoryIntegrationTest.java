package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 * Tests custom queries, soft delete behavior, and Auth0 integration.
 */
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    // ==================== BASIC CRUD TESTS ====================

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save and retrieve user")
        void shouldSaveAndRetrieveUser() {
            // Arrange
            User user = TestDataBuilder.createNewUser();

            // Act
            User saved = userRepository.save(user);
            Optional<User> retrieved = userRepository.findById(saved.getUserId());

            // Assert
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getUsername()).isEqualTo(user.getUsername());
            assertThat(retrieved.get().getEmail()).isEqualTo(user.getEmail());
        }

        @Test
        @DisplayName("Should update user")
        void shouldUpdateUser() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user = userRepository.save(user);

            // Act
            user.setFirstName("UpdatedFirst");
            user.setLastName("UpdatedLast");
            User updated = userRepository.save(user);

            // Assert
            assertThat(updated.getFirstName()).isEqualTo("UpdatedFirst");
            assertThat(updated.getLastName()).isEqualTo("UpdatedLast");
        }

        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user = userRepository.save(user);
            Long userId = user.getUserId();

            // Act
            userRepository.deleteById(userId);

            // Assert
            Optional<User> deleted = userRepository.findById(userId);
            assertThat(deleted).isEmpty();
        }
    }

    // ==================== CUSTOM QUERY TESTS ====================

    @Nested
    @DisplayName("Custom Query Tests")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setEmail("unique@example.com");
            userRepository.save(user);

            // Act
            Optional<User> found = userRepository.findByEmail("unique@example.com");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("unique@example.com");
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // Act
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            // Assert
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should check if username exists")
        void shouldCheckIfUsernameExists() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setUsername("existinguser");
            userRepository.save(user);

            // Act & Assert
            assertThat(userRepository.existsByUsername("existinguser")).isTrue();
            assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setEmail("exists@example.com");
            userRepository.save(user);

            // Act & Assert
            assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should find users by first name containing (case insensitive)")
        void shouldFindUsersByFirstNameContaining() {
            // Arrange
            User user1 = TestDataBuilder.createNewUser();
            user1.setUsername("john1");
            user1.setEmail("john1@example.com");
            user1.setFirstName("John");
            userRepository.save(user1);

            User user2 = TestDataBuilder.createNewUser();
            user2.setUsername("johnny");
            user2.setEmail("johnny@example.com");
            user2.setFirstName("Johnny");
            userRepository.save(user2);

            User user3 = TestDataBuilder.createNewUser();
            user3.setUsername("jane");
            user3.setEmail("jane@example.com");
            user3.setFirstName("Jane");
            userRepository.save(user3);

            // Act
            List<User> usersWithJohn = userRepository.findByFirstNameContainingIgnoreCase("john");

            // Assert
            assertThat(usersWithJohn).hasSize(2);
            assertThat(usersWithJohn).extracting(User::getFirstName)
                    .containsExactlyInAnyOrder("John", "Johnny");
        }

        @Test
        @DisplayName("Should find user by Auth0 user ID")
        void shouldFindUserByAuth0UserId() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setAuth0UserId("auth0|123456789");
            userRepository.save(user);

            // Act
            Optional<User> found = userRepository.findByAuth0UserId("auth0|123456789");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getAuth0UserId()).isEqualTo("auth0|123456789");
        }

        @Test
        @DisplayName("Should return empty when Auth0 user ID not found")
        void shouldReturnEmptyWhenAuth0UserIdNotFound() {
            // Act
            Optional<User> found = userRepository.findByAuth0UserId("auth0|nonexistent");

            // Assert
            assertThat(found).isEmpty();
        }
    }

    // ==================== SOFT DELETE TESTS ====================

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should exclude soft deleted users from findByEmail")
        void shouldExcludeSoftDeletedUsersFromFindByEmail() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setEmail("deleted@example.com");
            user = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Soft delete
            user = userRepository.findById(user.getUserId()).get();
            user.softDelete();
            userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<User> found = userRepository.findByEmail("deleted@example.com");

            // Assert
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted users from existsByUsername")
        void shouldExcludeSoftDeletedUsersFromExistsByUsername() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setUsername("deleteduser");
            user = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Soft delete
            user = userRepository.findById(user.getUserId()).get();
            user.softDelete();
            userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Act & Assert
            assertThat(userRepository.existsByUsername("deleteduser")).isFalse();
        }

        @Test
        @DisplayName("Should exclude soft deleted users from existsByEmail")
        void shouldExcludeSoftDeletedUsersFromExistsByEmail() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setEmail("softdeleted@example.com");
            user = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Soft delete
            user = userRepository.findById(user.getUserId()).get();
            user.softDelete();
            userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Act & Assert
            assertThat(userRepository.existsByEmail("softdeleted@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should exclude soft deleted users from findByAuth0UserId")
        void shouldExcludeSoftDeletedUsersFromFindByAuth0UserId() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setAuth0UserId("auth0|tobedeleted");
            user = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Soft delete
            user = userRepository.findById(user.getUserId()).get();
            user.softDelete();
            userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<User> found = userRepository.findByAuth0UserId("auth0|tobedeleted");

            // Assert
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should restore soft deleted user and make them findable again")
        void shouldRestoreSoftDeletedUser() {
            // Arrange - Create and soft delete a user
            User user = TestDataBuilder.createNewUser();
            user.setEmail("restore@example.com");
            user = userRepository.save(user);
            Long userId = user.getUserId();
            entityManager.flush();
            entityManager.clear();

            // Soft delete
            user = userRepository.findById(userId).get();
            user.softDelete();
            userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Verify user is not findable
            assertThat(userRepository.findByEmail("restore@example.com")).isEmpty();

            // Act - Restore the user
            userRepository.restoreById(userId);
            entityManager.flush();
            entityManager.clear();

            // Assert - User should be findable again
            Optional<User> restored = userRepository.findByEmail("restore@example.com");
            assertThat(restored).isPresent();
            assertThat(restored.get().isActive()).isTrue();
            assertThat(restored.get().getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Should restore soft deleted user via entity restore() method")
        void shouldRestoreViaSoftDeletableRestore() {
            // Arrange - Create and soft delete a user
            User user = TestDataBuilder.createNewUser();
            user.setUsername("restoreuser");
            user = userRepository.save(user);
            Long userId = user.getUserId();
            entityManager.flush();
            entityManager.clear();

            // Soft delete
            user = userRepository.findById(userId).get();
            user.softDelete();
            userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Verify user is not findable via normal query
            assertThat(userRepository.existsByUsername("restoreuser")).isFalse();

            // Act - Restore using entity method (need to use findByIdIncludingDeleted)
            user = userRepository.findByIdIncludingDeleted(userId).get();
            user.restore();
            userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(userRepository.existsByUsername("restoreuser")).isTrue();
        }
    }

    // ==================== ROLE TESTS ====================

    @Nested
    @DisplayName("User Role Tests")
    class UserRoleTests {

        @Test
        @DisplayName("Should save user with USER role by default")
        void shouldSaveUserWithUserRoleByDefault() {
            // Arrange
            User user = TestDataBuilder.createNewUser();

            // Act
            User saved = userRepository.save(user);

            // Assert
            assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("Should save user with ADMIN role")
        void shouldSaveUserWithAdminRole() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user.setRole(UserRole.ADMIN);

            // Act
            User saved = userRepository.save(user);

            // Assert
            assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        }
    }

    // ==================== OPTIMISTIC LOCKING TESTS ====================

    @Nested
    @DisplayName("Optimistic Locking Tests")
    class OptimisticLockingTests {

        @Test
        @DisplayName("Should increment version on update")
        void shouldIncrementVersionOnUpdate() {
            // Arrange
            User user = TestDataBuilder.createNewUser();
            user = userRepository.save(user);
            entityManager.flush();
            assertThat(user.getVersion()).isZero();

            // Act
            user.setFirstName("Updated");
            user = userRepository.save(user);
            entityManager.flush();

            // Assert
            assertThat(user.getVersion()).isEqualTo(1L);
        }
    }

    // ==================== AUDIT FIELD TESTS ====================

    @Nested
    @DisplayName("Audit Field Tests")
    class AuditFieldTests {

        @Test
        @DisplayName("Should populate audit fields on create")
        void shouldPopulateAuditFieldsOnCreate() {
            // Arrange
            User user = TestDataBuilder.createNewUser();

            // Act
            User saved = userRepository.save(user);

            // Assert
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }
}
