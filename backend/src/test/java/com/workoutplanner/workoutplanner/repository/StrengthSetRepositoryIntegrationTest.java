package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
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
 * Integration tests for StrengthSetRepository.
 *
 * Tests custom queries including:
 * - findByWorkoutExerciseIdOrderBySetNumber
 * - findBySessionId
 * - Soft delete behavior
 */
@DisplayName("StrengthSetRepository Integration Tests")
class StrengthSetRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private StrengthSetRepository strengthSetRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private WorkoutSession workoutSession;
    private WorkoutExercise workoutExercise;
    private Exercise strengthExercise;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createNewUser();
        testUser = userRepository.save(testUser);

        workoutSession = TestDataBuilder.createNewWorkoutSession(testUser);
        workoutSession = workoutSessionRepository.save(workoutSession);

        strengthExercise = TestDataBuilder.createNewStrengthExercise();
        strengthExercise = exerciseRepository.save(strengthExercise);

        workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkoutSession(workoutSession);
        workoutExercise.setExercise(strengthExercise);
        workoutExercise.setOrderInWorkout(1);
        workoutExercise = workoutExerciseRepository.save(workoutExercise);

        entityManager.flush();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save and retrieve strength set")
        void shouldSaveAndRetrieveStrengthSet() {
            // Arrange
            StrengthSet set = createStrengthSet(1, 10, "100.00");

            // Act
            StrengthSet saved = strengthSetRepository.save(set);
            Optional<StrengthSet> retrieved = strengthSetRepository.findById(saved.getSetId());

            // Assert
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getSetNumber()).isEqualTo(1);
            assertThat(retrieved.get().getReps()).isEqualTo(10);
            assertThat(retrieved.get().getWeight()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Should update strength set")
        void shouldUpdateStrengthSet() {
            // Arrange
            StrengthSet set = createStrengthSet(1, 10, "100.00");
            set = strengthSetRepository.save(set);

            // Act
            set.setReps(12);
            set.setWeight(new BigDecimal("110.00"));
            StrengthSet updated = strengthSetRepository.save(set);

            // Assert
            assertThat(updated.getReps()).isEqualTo(12);
            assertThat(updated.getWeight()).isEqualByComparingTo(new BigDecimal("110.00"));
        }

        @Test
        @DisplayName("Should delete strength set")
        void shouldDeleteStrengthSet() {
            // Arrange
            StrengthSet set = createStrengthSet(1, 10, "100.00");
            set = strengthSetRepository.save(set);
            Long id = set.getSetId();

            // Act
            strengthSetRepository.deleteById(id);

            // Assert
            assertThat(strengthSetRepository.findById(id)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByWorkoutExerciseIdOrderBySetNumber Tests")
    class FindByWorkoutExerciseIdOrderBySetNumberTests {

        @Test
        @DisplayName("Should find sets ordered by set number")
        void shouldFindSetsOrderedBySetNumber() {
            // Arrange - Create in reverse order
            strengthSetRepository.save(createStrengthSet(3, 8, "100.00"));
            strengthSetRepository.save(createStrengthSet(1, 10, "100.00"));
            strengthSetRepository.save(createStrengthSet(2, 9, "100.00"));
            entityManager.flush();
            entityManager.clear();

            // Act
            List<StrengthSet> result = strengthSetRepository.findByWorkoutExerciseIdOrderBySetNumber(
                    workoutExercise.getWorkoutExerciseId());

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getSetNumber()).isEqualTo(1);
            assertThat(result.get(1).getSetNumber()).isEqualTo(2);
            assertThat(result.get(2).getSetNumber()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return empty list when no sets exist")
        void shouldReturnEmptyListWhenNoSetsExist() {
            // Act
            List<StrengthSet> result = strengthSetRepository.findByWorkoutExerciseIdOrderBySetNumber(
                    workoutExercise.getWorkoutExerciseId());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted sets")
        void shouldExcludeSoftDeletedSets() {
            // Arrange
            StrengthSet set1 = strengthSetRepository.save(createStrengthSet(1, 10, "100.00"));
            StrengthSet set2 = strengthSetRepository.save(createStrengthSet(2, 10, "100.00"));
            entityManager.flush();

            // Soft delete one
            set1.softDelete();
            strengthSetRepository.save(set1);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<StrengthSet> result = strengthSetRepository.findByWorkoutExerciseIdOrderBySetNumber(
                    workoutExercise.getWorkoutExerciseId());

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSetNumber()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findBySessionId Tests")
    class FindBySessionIdTests {

        @Test
        @DisplayName("Should find all sets for a session")
        void shouldFindAllSetsForSession() {
            // Arrange
            strengthSetRepository.save(createStrengthSet(1, 10, "100.00"));
            strengthSetRepository.save(createStrengthSet(2, 10, "100.00"));
            strengthSetRepository.save(createStrengthSet(3, 10, "100.00"));
            entityManager.flush();
            entityManager.clear();

            // Act
            List<StrengthSet> result = strengthSetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should find sets from multiple exercises in session")
        void shouldFindSetsFromMultipleExercisesInSession() {
            // Arrange - Create second workout exercise
            WorkoutExercise workoutExercise2 = new WorkoutExercise();
            workoutExercise2.setWorkoutSession(workoutSession);
            workoutExercise2.setExercise(strengthExercise);
            workoutExercise2.setOrderInWorkout(2);
            workoutExercise2 = workoutExerciseRepository.save(workoutExercise2);

            // Add sets to both exercises
            strengthSetRepository.save(createStrengthSet(1, 10, "100.00"));
            strengthSetRepository.save(createStrengthSet(2, 10, "100.00"));

            StrengthSet set3 = new StrengthSet();
            set3.setWorkoutExercise(workoutExercise2);
            set3.setSetNumber(1);
            set3.setReps(10);
            set3.setWeight(new BigDecimal("100.00"));
            strengthSetRepository.save(set3);

            entityManager.flush();
            entityManager.clear();

            // Act
            List<StrengthSet> result = strengthSetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty for session with no sets")
        void shouldReturnEmptyForSessionWithNoSets() {
            // Act
            List<StrengthSet> result = strengthSetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted sets")
        void shouldExcludeSoftDeletedSets() {
            // Arrange
            StrengthSet set1 = strengthSetRepository.save(createStrengthSet(1, 10, "100.00"));
            strengthSetRepository.save(createStrengthSet(2, 10, "100.00"));
            entityManager.flush();

            // Soft delete one
            set1.softDelete();
            strengthSetRepository.save(set1);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<StrengthSet> result = strengthSetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should soft delete strength set")
        void shouldSoftDeleteStrengthSet() {
            // Arrange
            StrengthSet set = strengthSetRepository.save(createStrengthSet(1, 10, "100.00"));
            Long id = set.getSetId();
            entityManager.flush();
            entityManager.clear();

            // Act
            StrengthSet toDelete = strengthSetRepository.findById(id).orElseThrow();
            toDelete.softDelete();
            strengthSetRepository.save(toDelete);
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(strengthSetRepository.findById(id)).isEmpty();
            assertThat(strengthSetRepository.findByIdIncludingDeleted(id)).isPresent();
        }

        @Test
        @DisplayName("Should restore soft deleted strength set")
        void shouldRestoreSoftDeletedStrengthSet() {
            // Arrange
            StrengthSet set = strengthSetRepository.save(createStrengthSet(1, 10, "100.00"));
            Long id = set.getSetId();

            set.softDelete();
            strengthSetRepository.save(set);
            entityManager.flush();
            entityManager.clear();

            // Act
            StrengthSet deleted = strengthSetRepository.findByIdIncludingDeleted(id).orElseThrow();
            deleted.restore();
            strengthSetRepository.save(deleted);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Optional<StrengthSet> restored = strengthSetRepository.findById(id);
            assertThat(restored).isPresent();
            assertThat(restored.get().isActive()).isTrue();
        }
    }

    private StrengthSet createStrengthSet(int setNumber, int reps, String weight) {
        StrengthSet set = new StrengthSet();
        set.setWorkoutExercise(workoutExercise);
        set.setSetNumber(setNumber);
        set.setReps(reps);
        set.setWeight(new BigDecimal(weight));
        set.setRestTimeInSeconds(60);
        return set;
    }
}
