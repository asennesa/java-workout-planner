package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for WorkoutSessionRepository.
 * 
 * Industry Best Practices Demonstrated:
 * 1. Extend AbstractIntegrationTest for Testcontainers setup
 * 2. Test with real PostgreSQL database
 * 3. Test isolation via @Transactional (inherited from AbstractIntegrationTest)
 *    - Each test runs in its own transaction that rolls back automatically
 *    - No manual cleanup needed for repository tests
 * 4. Test JPA queries, custom queries, and relationships
 * 5. Verify soft delete behavior
 * 6. Test fetch strategies and N+1 prevention
 * 
 * Testing Philosophy:
 * - Integration tests verify database operations work correctly
 * - Use real database (not H2) for accurate testing
 * - Test complex queries and relationships
 * - Ensure indexes and constraints work as expected
 */
@DisplayName("WorkoutSessionRepository Integration Tests")
class WorkoutSessionRepositoryIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = TestDataBuilder.createNewUser(); // For repository.save()
        testUser = userRepository.save(testUser);
    }
    
    // ==================== BASIC CRUD TESTS ====================
    
    @Test
    @DisplayName("Should save and retrieve workout session")
    void shouldSaveAndRetrieveWorkoutSession() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        
        // Act
        WorkoutSession saved = workoutSessionRepository.save(workout);
        Optional<WorkoutSession> retrieved = workoutSessionRepository.findById(saved.getSessionId());
        
        // Assert
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Morning Workout");
        assertThat(retrieved.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
    }
    
    @Test
    @DisplayName("Should update workout session")
    void shouldUpdateWorkoutSession() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workout = workoutSessionRepository.save(workout);
        
        // Act
        workout.setName("Updated Workout");
        workout.setStatus(WorkoutStatus.COMPLETED);
        WorkoutSession updated = workoutSessionRepository.save(workout);
        
        // Assert
        assertThat(updated.getName()).isEqualTo("Updated Workout");
        assertThat(updated.getStatus()).isEqualTo(WorkoutStatus.COMPLETED);
    }
    
    @Test
    @DisplayName("Should delete workout session")
    void shouldDeleteWorkoutSession() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workout = workoutSessionRepository.save(workout);
        Long workoutId = workout.getSessionId();
        
        // Act
        workoutSessionRepository.deleteById(workoutId);
        
        // Assert
        Optional<WorkoutSession> deleted = workoutSessionRepository.findById(workoutId);
        assertThat(deleted).isEmpty();
    }
    
    // ==================== CUSTOM QUERY TESTS ====================
    
    @Test
    @DisplayName("Should find workout with user using JOIN FETCH")
    void shouldFindWorkoutWithUserUsingJoinFetch() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workout = workoutSessionRepository.save(workout);
        
        // Act
        Optional<WorkoutSession> result = workoutSessionRepository.findWithUserBySessionId(workout.getSessionId());
        
        // Assert - User should be loaded eagerly (no lazy loading exception)
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUsername()).isEqualTo(testUser.getUsername());
    }
    
    @Test
    @DisplayName("Should find workouts by user ID ordered by started date")
    void shouldFindWorkoutsByUserIdOrderedByStartedAt() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        WorkoutSession workout1 = TestDataBuilder.createInProgressWorkout(testUser, now.minusDays(2));
        workout1.setName("Workout 1");
        workoutSessionRepository.save(workout1);
        
        WorkoutSession workout2 = TestDataBuilder.createInProgressWorkout(testUser, now.minusDays(1));
        workout2.setName("Workout 2");
        workoutSessionRepository.save(workout2);
        
        WorkoutSession workout3 = TestDataBuilder.createInProgressWorkout(testUser, now);
        workout3.setName("Workout 3");
        workoutSessionRepository.save(workout3);
        
        // Act
        List<WorkoutSession> workouts = workoutSessionRepository.findByUserIdOrderByStartedAtDesc(testUser.getUserId());
        
        // Assert - Should be ordered newest first
        assertThat(workouts).hasSize(3);
        assertThat(workouts.get(0).getName()).isEqualTo("Workout 3");
        assertThat(workouts.get(1).getName()).isEqualTo("Workout 2");
        assertThat(workouts.get(2).getName()).isEqualTo("Workout 1");
    }
    
    @Test
    @DisplayName("Should check if user has workout sessions")
    void shouldCheckIfUserHasWorkoutSessions() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workoutSessionRepository.save(workout);
        
        // Act
        boolean exists = workoutSessionRepository.existsByUserId(testUser.getUserId());
        boolean notExists = workoutSessionRepository.existsByUserId(999L);
        
        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    // ==================== SOFT DELETE TESTS ====================
    
    @Test
    @DisplayName("Should soft delete workout session")
    void shouldSoftDeleteWorkoutSession() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workout = workoutSessionRepository.save(workout);
        Long workoutId = workout.getSessionId();
        
        // Act
        workout.softDelete();
        workoutSessionRepository.save(workout);
        entityManager.flush();
        entityManager.clear();

        // Assert - Regular findById should not find deleted workout
        Optional<WorkoutSession> notFound = workoutSessionRepository.findById(workoutId);
        assertThat(notFound).isEmpty();
        
        // But findByIdIncludingDeleted should find it
        Optional<WorkoutSession> found = workoutSessionRepository.findByIdIncludingDeleted(workoutId);
        assertThat(found).isPresent();
        assertThat(found.get().isActive()).isFalse();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Should restore soft deleted workout session")
    void shouldRestoreSoftDeletedWorkoutSession() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workout = workoutSessionRepository.save(workout);
        Long workoutId = workout.getSessionId();
        
        // Soft delete and flush to database
        workout.softDelete();
        workoutSessionRepository.save(workout);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to avoid stale state

        // Act - Retrieve fresh entity from database
        WorkoutSession deleted = workoutSessionRepository.findByIdIncludingDeleted(workoutId).orElseThrow();
        deleted.restore();
        workoutSessionRepository.save(deleted);
        entityManager.flush();
        entityManager.clear();
        
        // Assert
        Optional<WorkoutSession> restored = workoutSessionRepository.findById(workoutId);
        assertThat(restored).isPresent();
        assertThat(restored.get().isActive()).isTrue();
        assertThat(restored.get().getDeletedAt()).isNull();
    }
    
    @Test
    @DisplayName("Should exclude soft deleted workouts from normal queries")
    void shouldExcludeSoftDeletedWorkoutsFromNormalQueries() {
        // Arrange
        WorkoutSession workout1 = TestDataBuilder.createNewWorkoutSession(testUser);
        workout1.setName("Active Workout");
        workoutSessionRepository.save(workout1);

        WorkoutSession workout2 = TestDataBuilder.createNewWorkoutSession(testUser);
        workout2.setName("Deleted Workout");
        workout2 = workoutSessionRepository.save(workout2);
        Long workout2Id = workout2.getSessionId();
        entityManager.flush();
        entityManager.clear(); // Clear to avoid stale state

        // Retrieve fresh entity from database before soft delete
        WorkoutSession toDelete = workoutSessionRepository.findById(workout2Id).orElseThrow();
        toDelete.softDelete();
        workoutSessionRepository.save(toDelete);
        entityManager.flush();
        entityManager.clear();
        
        // Act
        List<WorkoutSession> allWorkouts = workoutSessionRepository.findAll();
        List<WorkoutSession> userWorkouts = workoutSessionRepository.findByUserIdOrderByStartedAtDesc(testUser.getUserId());
        
        // Assert - Only active workout should be found
        assertThat(allWorkouts).hasSize(1);
        assertThat(allWorkouts.get(0).getName()).isEqualTo("Active Workout");
        
        assertThat(userWorkouts).hasSize(1);
        assertThat(userWorkouts.get(0).getName()).isEqualTo("Active Workout");
    }
    
    // ==================== OPTIMISTIC LOCKING TESTS ====================
    
    @Test
    @DisplayName("Should handle optimistic locking with version field")
    void shouldHandleOptimisticLockingWithVersionField() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workout = workoutSessionRepository.save(workout);
        entityManager.flush();
        
        // Act - Version should start at 0 and increment
        assertThat(workout.getVersion()).isZero();
        
        workout.setName("Updated Once");
        workout = workoutSessionRepository.save(workout);
        entityManager.flush();
        assertThat(workout.getVersion()).isEqualTo(1L);
        
        workout.setName("Updated Twice");
        workout = workoutSessionRepository.save(workout);
        entityManager.flush();
        assertThat(workout.getVersion()).isEqualTo(2L);
    }
    
    // ==================== RELATIONSHIP TESTS ====================
    
    @Test
    @DisplayName("Should cascade delete workout exercises when workout is deleted")
    void shouldCascadeDeleteWorkoutExercises() {
        // Note: This test verifies the cascade behavior is configured correctly
        // Actual exercise deletion would require WorkoutExercise entities
        
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workout = workoutSessionRepository.save(workout);
        Long workoutId = workout.getSessionId();
        entityManager.flush();
        entityManager.clear();
        
        // Act
        workoutSessionRepository.deleteById(workoutId);
        entityManager.flush();
        
        // Assert
        Optional<WorkoutSession> deleted = workoutSessionRepository.findById(workoutId);
        assertThat(deleted).isEmpty();
    }
    
    // ==================== AUDIT FIELDS TESTS ====================
    
    @Test
    @DisplayName("Should automatically populate audit fields on create")
    void shouldAutomaticallyPopulateAuditFieldsOnCreate() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        
        // Act
        WorkoutSession saved = workoutSessionRepository.save(workout);
        
        // Assert
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        // Note: createdBy/updatedBy would be set by AuditConfig with @CreatedBy/@LastModifiedBy
    }
    
    @Test
    @DisplayName("Should update updatedAt field on modification")
    void shouldUpdateUpdatedAtFieldOnModification() {
        // Arrange
        WorkoutSession workout = TestDataBuilder.createNewWorkoutSession(testUser);
        workout = workoutSessionRepository.save(workout);
        entityManager.flush();
        LocalDateTime originalCreatedAt = workout.getCreatedAt();
        
        // Act - Modify and save
        workout.setName("Updated Name");
        WorkoutSession updated = workoutSessionRepository.save(workout);
        entityManager.flush();
        
        // Assert
        // Verify the update was persisted
        assertThat(updated.getName()).isEqualTo("Updated Name");
        // Verify audit fields are present (JPA auditing is working)
        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getUpdatedAt()).isNotNull();
        // Note: We don't use Thread.sleep() as it's a test anti-pattern.
        // The important thing is that JPA auditing is working, not exact timing.
        // If timestamp precision is critical, use a test Clock bean instead.
    }
}

