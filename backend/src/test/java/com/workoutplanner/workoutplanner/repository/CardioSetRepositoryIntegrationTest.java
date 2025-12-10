package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.Exercise;
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
 * Integration tests for CardioSetRepository.
 *
 * Tests custom queries including:
 * - findByWorkoutExerciseIdOrderBySetNumber
 * - findBySessionId
 * - Soft delete behavior
 */
@DisplayName("CardioSetRepository Integration Tests")
class CardioSetRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CardioSetRepository cardioSetRepository;

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
    private Exercise cardioExercise;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createNewUser();
        testUser = userRepository.save(testUser);

        workoutSession = TestDataBuilder.createNewWorkoutSession(testUser);
        workoutSession = workoutSessionRepository.save(workoutSession);

        cardioExercise = TestDataBuilder.createNewCardioExercise();
        cardioExercise = exerciseRepository.save(cardioExercise);

        workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkoutSession(workoutSession);
        workoutExercise.setExercise(cardioExercise);
        workoutExercise.setOrderInWorkout(1);
        workoutExercise = workoutExerciseRepository.save(workoutExercise);

        entityManager.flush();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save and retrieve cardio set")
        void shouldSaveAndRetrieveCardioSet() {
            // Arrange
            CardioSet set = createCardioSet(1, 1800, "5.00", "km");

            // Act
            CardioSet saved = cardioSetRepository.save(set);
            Optional<CardioSet> retrieved = cardioSetRepository.findById(saved.getSetId());

            // Assert
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getSetNumber()).isEqualTo(1);
            assertThat(retrieved.get().getDurationInSeconds()).isEqualTo(1800);
            assertThat(retrieved.get().getDistance()).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(retrieved.get().getDistanceUnit()).isEqualTo("km");
        }

        @Test
        @DisplayName("Should update cardio set")
        void shouldUpdateCardioSet() {
            // Arrange
            CardioSet set = createCardioSet(1, 1800, "5.00", "km");
            set = cardioSetRepository.save(set);

            // Act
            set.setDurationInSeconds(2400);
            set.setDistance(new BigDecimal("8.00"));
            CardioSet updated = cardioSetRepository.save(set);

            // Assert
            assertThat(updated.getDurationInSeconds()).isEqualTo(2400);
            assertThat(updated.getDistance()).isEqualByComparingTo(new BigDecimal("8.00"));
        }

        @Test
        @DisplayName("Should delete cardio set")
        void shouldDeleteCardioSet() {
            // Arrange
            CardioSet set = createCardioSet(1, 1800, "5.00", "km");
            set = cardioSetRepository.save(set);
            Long id = set.getSetId();

            // Act
            cardioSetRepository.deleteById(id);

            // Assert
            assertThat(cardioSetRepository.findById(id)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByWorkoutExerciseIdOrderBySetNumber Tests")
    class FindByWorkoutExerciseIdOrderBySetNumberTests {

        @Test
        @DisplayName("Should find sets ordered by set number")
        void shouldFindSetsOrderedBySetNumber() {
            // Arrange - Create in reverse order
            cardioSetRepository.save(createCardioSet(3, 600, "2.00", "km"));
            cardioSetRepository.save(createCardioSet(1, 1800, "5.00", "km"));
            cardioSetRepository.save(createCardioSet(2, 1200, "3.00", "km"));
            entityManager.flush();
            entityManager.clear();

            // Act
            List<CardioSet> result = cardioSetRepository.findByWorkoutExerciseIdOrderBySetNumber(
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
            List<CardioSet> result = cardioSetRepository.findByWorkoutExerciseIdOrderBySetNumber(
                    workoutExercise.getWorkoutExerciseId());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted sets")
        void shouldExcludeSoftDeletedSets() {
            // Arrange
            CardioSet set1 = cardioSetRepository.save(createCardioSet(1, 1800, "5.00", "km"));
            cardioSetRepository.save(createCardioSet(2, 1800, "5.00", "km"));
            entityManager.flush();

            // Soft delete one
            set1.softDelete();
            cardioSetRepository.save(set1);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<CardioSet> result = cardioSetRepository.findByWorkoutExerciseIdOrderBySetNumber(
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
            cardioSetRepository.save(createCardioSet(1, 1800, "5.00", "km"));
            cardioSetRepository.save(createCardioSet(2, 1800, "5.00", "km"));
            cardioSetRepository.save(createCardioSet(3, 1800, "5.00", "km"));
            entityManager.flush();
            entityManager.clear();

            // Act
            List<CardioSet> result = cardioSetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty for session with no sets")
        void shouldReturnEmptyForSessionWithNoSets() {
            // Act
            List<CardioSet> result = cardioSetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted sets")
        void shouldExcludeSoftDeletedSets() {
            // Arrange
            CardioSet set1 = cardioSetRepository.save(createCardioSet(1, 1800, "5.00", "km"));
            cardioSetRepository.save(createCardioSet(2, 1800, "5.00", "km"));
            entityManager.flush();

            // Soft delete one
            set1.softDelete();
            cardioSetRepository.save(set1);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<CardioSet> result = cardioSetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should soft delete cardio set")
        void shouldSoftDeleteCardioSet() {
            // Arrange
            CardioSet set = cardioSetRepository.save(createCardioSet(1, 1800, "5.00", "km"));
            Long id = set.getSetId();
            entityManager.flush();
            entityManager.clear();

            // Act
            CardioSet toDelete = cardioSetRepository.findById(id).orElseThrow();
            toDelete.softDelete();
            cardioSetRepository.save(toDelete);
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(cardioSetRepository.findById(id)).isEmpty();
            assertThat(cardioSetRepository.findByIdIncludingDeleted(id)).isPresent();
        }

        @Test
        @DisplayName("Should restore soft deleted cardio set")
        void shouldRestoreSoftDeletedCardioSet() {
            // Arrange
            CardioSet set = cardioSetRepository.save(createCardioSet(1, 1800, "5.00", "km"));
            Long id = set.getSetId();

            set.softDelete();
            cardioSetRepository.save(set);
            entityManager.flush();
            entityManager.clear();

            // Act
            CardioSet deleted = cardioSetRepository.findByIdIncludingDeleted(id).orElseThrow();
            deleted.restore();
            cardioSetRepository.save(deleted);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Optional<CardioSet> restored = cardioSetRepository.findById(id);
            assertThat(restored).isPresent();
            assertThat(restored.get().isActive()).isTrue();
        }
    }

    private CardioSet createCardioSet(int setNumber, int duration, String distance, String unit) {
        CardioSet set = new CardioSet();
        set.setWorkoutExercise(workoutExercise);
        set.setSetNumber(setNumber);
        set.setDurationInSeconds(duration);
        set.setDistance(new BigDecimal(distance));
        set.setDistanceUnit(unit);
        return set;
    }
}
