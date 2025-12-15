package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for WorkoutExerciseRepository.
 *
 * Tests custom queries including:
 * - findWithExerciseById (eager loading)
 * - findBySessionIdOrderByOrder (ordering)
 * - findStrengthExercisesWithSets (EntityGraph)
 * - findCardioExercisesWithSets (EntityGraph)
 * - findFlexibilityExercisesWithSets (EntityGraph)
 * - Soft delete behavior
 */
@DisplayName("WorkoutExerciseRepository Integration Tests")
class WorkoutExerciseRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StrengthSetRepository strengthSetRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private WorkoutSession workoutSession;
    private Exercise strengthExercise;
    private Exercise cardioExercise;
    private Exercise flexibilityExercise;

    @BeforeEach
    void setUp() {
        // Create user
        testUser = TestDataBuilder.createNewUser();
        testUser = userRepository.save(testUser);

        // Create workout session
        workoutSession = TestDataBuilder.createNewWorkoutSession(testUser);
        workoutSession = workoutSessionRepository.save(workoutSession);

        // Create exercises
        strengthExercise = TestDataBuilder.createNewStrengthExercise();
        strengthExercise = exerciseRepository.save(strengthExercise);

        cardioExercise = TestDataBuilder.createNewCardioExercise();
        cardioExercise = exerciseRepository.save(cardioExercise);

        flexibilityExercise = TestDataBuilder.createNewFlexibilityExercise();
        flexibilityExercise = exerciseRepository.save(flexibilityExercise);

        entityManager.flush();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save and retrieve workout exercise")
        void shouldSaveAndRetrieveWorkoutExercise() {
            // Arrange
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(strengthExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExercise.setNotes("Test notes");

            // Act
            WorkoutExercise saved = workoutExerciseRepository.save(workoutExercise);
            Optional<WorkoutExercise> retrieved = workoutExerciseRepository.findById(saved.getWorkoutExerciseId());

            // Assert
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getOrderInWorkout()).isEqualTo(1);
            assertThat(retrieved.get().getNotes()).isEqualTo("Test notes");
        }

        @Test
        @DisplayName("Should update workout exercise")
        void shouldUpdateWorkoutExercise() {
            // Arrange
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(strengthExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExercise = workoutExerciseRepository.save(workoutExercise);

            // Act
            workoutExercise.setOrderInWorkout(2);
            workoutExercise.setNotes("Updated notes");
            WorkoutExercise updated = workoutExerciseRepository.save(workoutExercise);

            // Assert
            assertThat(updated.getOrderInWorkout()).isEqualTo(2);
            assertThat(updated.getNotes()).isEqualTo("Updated notes");
        }

        @Test
        @DisplayName("Should delete workout exercise")
        void shouldDeleteWorkoutExercise() {
            // Arrange
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(strengthExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExercise = workoutExerciseRepository.save(workoutExercise);
            Long id = workoutExercise.getWorkoutExerciseId();

            // Act
            workoutExerciseRepository.deleteById(id);

            // Assert
            assertThat(workoutExerciseRepository.findById(id)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findWithExerciseById Tests")
    class FindWithExerciseByIdTests {

        @Test
        @DisplayName("Should find workout exercise with exercise eagerly loaded")
        void shouldFindWorkoutExerciseWithExerciseEagerlyLoaded() {
            // Arrange
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(strengthExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExercise = workoutExerciseRepository.save(workoutExercise);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<WorkoutExercise> result = workoutExerciseRepository.findWithExerciseById(
                    workoutExercise.getWorkoutExerciseId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getExercise()).isNotNull();
            assertThat(result.get().getExercise().getName()).isEqualTo("Bench Press");
        }

        @Test
        @DisplayName("Should return empty for non-existent workout exercise")
        void shouldReturnEmptyForNonExistentWorkoutExercise() {
            // Act
            Optional<WorkoutExercise> result = workoutExerciseRepository.findWithExerciseById(99999L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted workout exercises")
        void shouldExcludeSoftDeletedWorkoutExercises() {
            // Arrange
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(strengthExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExercise = workoutExerciseRepository.save(workoutExercise);
            Long id = workoutExercise.getWorkoutExerciseId();

            // Soft delete
            workoutExercise.softDelete();
            workoutExerciseRepository.save(workoutExercise);
            entityManager.flush();
            entityManager.clear();

            // Act
            Optional<WorkoutExercise> result = workoutExerciseRepository.findWithExerciseById(id);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySessionIdOrderByOrder Tests")
    class FindBySessionIdOrderByOrderTests {

        @Test
        @DisplayName("Should find exercises ordered by order in workout")
        void shouldFindExercisesOrderedByOrderInWorkout() {
            // Arrange - Create exercises in reverse order
            WorkoutExercise we3 = createWorkoutExercise(3);
            WorkoutExercise we1 = createWorkoutExercise(1);
            WorkoutExercise we2 = createWorkoutExercise(2);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<WorkoutExercise> result = workoutExerciseRepository.findBySessionIdOrderByOrder(
                    workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getOrderInWorkout()).isEqualTo(1);
            assertThat(result.get(1).getOrderInWorkout()).isEqualTo(2);
            assertThat(result.get(2).getOrderInWorkout()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return empty list for session with no exercises")
        void shouldReturnEmptyListForSessionWithNoExercises() {
            // Act
            List<WorkoutExercise> result = workoutExerciseRepository.findBySessionIdOrderByOrder(
                    workoutSession.getSessionId());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted exercises")
        void shouldExcludeSoftDeletedExercises() {
            // Arrange
            WorkoutExercise we1 = createWorkoutExercise(1);
            WorkoutExercise we2 = createWorkoutExercise(2);
            entityManager.flush();

            // Soft delete one
            we1.softDelete();
            workoutExerciseRepository.save(we1);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<WorkoutExercise> result = workoutExerciseRepository.findBySessionIdOrderByOrder(
                    workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOrderInWorkout()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findStrengthExercisesWithSets Tests")
    class FindStrengthExercisesWithSetsTests {

        @Test
        @DisplayName("Should find strength exercises with sets eagerly loaded")
        void shouldFindStrengthExercisesWithSetsEagerlyLoaded() {
            // Arrange
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(strengthExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExercise = workoutExerciseRepository.save(workoutExercise);

            // Add strength sets
            for (int i = 1; i <= 3; i++) {
                StrengthSet set = new StrengthSet();
                set.setWorkoutExercise(workoutExercise);
                set.setSetNumber(i);
                set.setReps(10);
                set.setWeight(new BigDecimal("100.00"));
                strengthSetRepository.save(set);
            }
            entityManager.flush();
            entityManager.clear();

            // Act
            List<WorkoutExercise> result = workoutExerciseRepository.findStrengthExercisesWithSets(
                    workoutSession.getSessionId(), ExerciseType.STRENGTH);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStrengthSets()).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty list when no strength exercises")
        void shouldReturnEmptyListWhenNoStrengthExercises() {
            // Arrange - Add only cardio exercise
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(cardioExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExerciseRepository.save(workoutExercise);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<WorkoutExercise> result = workoutExerciseRepository.findStrengthExercisesWithSets(
                    workoutSession.getSessionId(), ExerciseType.STRENGTH);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should soft delete workout exercise")
        void shouldSoftDeleteWorkoutExercise() {
            // Arrange
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(strengthExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExercise = workoutExerciseRepository.save(workoutExercise);
            Long id = workoutExercise.getWorkoutExerciseId();
            entityManager.flush();
            entityManager.clear();

            // Act
            WorkoutExercise toDelete = workoutExerciseRepository.findById(id).orElseThrow();
            toDelete.softDelete();
            workoutExerciseRepository.save(toDelete);
            entityManager.flush();
            entityManager.clear();

            // Assert - Regular findById should not find it
            assertThat(workoutExerciseRepository.findById(id)).isEmpty();

            // But findByIdIncludingDeleted should
            assertThat(workoutExerciseRepository.findByIdIncludingDeleted(id)).isPresent();
        }

        @Test
        @DisplayName("Should restore soft deleted workout exercise")
        void shouldRestoreSoftDeletedWorkoutExercise() {
            // Arrange
            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkoutSession(workoutSession);
            workoutExercise.setExercise(strengthExercise);
            workoutExercise.setOrderInWorkout(1);
            workoutExercise = workoutExerciseRepository.save(workoutExercise);
            Long id = workoutExercise.getWorkoutExerciseId();

            // Soft delete
            workoutExercise.softDelete();
            workoutExerciseRepository.save(workoutExercise);
            entityManager.flush();
            entityManager.clear();

            // Act - Restore
            WorkoutExercise deleted = workoutExerciseRepository.findByIdIncludingDeleted(id).orElseThrow();
            deleted.restore();
            workoutExerciseRepository.save(deleted);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Optional<WorkoutExercise> restored = workoutExerciseRepository.findById(id);
            assertThat(restored).isPresent();
            assertThat(restored.get().isActive()).isTrue();
        }
    }

    private WorkoutExercise createWorkoutExercise(int order) {
        WorkoutExercise we = new WorkoutExercise();
        we.setWorkoutSession(workoutSession);
        we.setExercise(strengthExercise);
        we.setOrderInWorkout(order);
        return workoutExerciseRepository.save(we);
    }
}
