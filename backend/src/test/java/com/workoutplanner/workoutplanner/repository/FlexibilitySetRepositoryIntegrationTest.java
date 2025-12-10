package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for FlexibilitySetRepository.
 *
 * Tests custom queries including:
 * - findByWorkoutExerciseIdOrderBySetNumber
 * - findBySessionId
 * - Soft delete behavior
 */
@DisplayName("FlexibilitySetRepository Integration Tests")
class FlexibilitySetRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FlexibilitySetRepository flexibilitySetRepository;

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
    private Exercise flexibilityExercise;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createNewUser();
        testUser = userRepository.save(testUser);

        workoutSession = TestDataBuilder.createNewWorkoutSession(testUser);
        workoutSession = workoutSessionRepository.save(workoutSession);

        flexibilityExercise = TestDataBuilder.createNewFlexibilityExercise();
        flexibilityExercise = exerciseRepository.save(flexibilityExercise);

        workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkoutSession(workoutSession);
        workoutExercise.setExercise(flexibilityExercise);
        workoutExercise.setOrderInWorkout(1);
        workoutExercise = workoutExerciseRepository.save(workoutExercise);

        entityManager.flush();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save and retrieve flexibility set")
        void shouldSaveAndRetrieveFlexibilitySet() {
            // Arrange
            FlexibilitySet set = createFlexibilitySet(1, 60, "Static", 3);

            // Act
            FlexibilitySet saved = flexibilitySetRepository.save(set);
            Optional<FlexibilitySet> retrieved = flexibilitySetRepository.findById(saved.getSetId());

            // Assert
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getSetNumber()).isEqualTo(1);
            assertThat(retrieved.get().getDurationInSeconds()).isEqualTo(60);
            assertThat(retrieved.get().getStretchType()).isEqualTo("Static");
            assertThat(retrieved.get().getIntensity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should update flexibility set")
        void shouldUpdateFlexibilitySet() {
            // Arrange
            FlexibilitySet set = createFlexibilitySet(1, 60, "Static", 3);
            set = flexibilitySetRepository.save(set);

            // Act
            set.setDurationInSeconds(90);
            set.setStretchType("Dynamic");
            set.setIntensity(5);
            FlexibilitySet updated = flexibilitySetRepository.save(set);

            // Assert
            assertThat(updated.getDurationInSeconds()).isEqualTo(90);
            assertThat(updated.getStretchType()).isEqualTo("Dynamic");
            assertThat(updated.getIntensity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should delete flexibility set")
        void shouldDeleteFlexibilitySet() {
            // Arrange
            FlexibilitySet set = createFlexibilitySet(1, 60, "Static", 3);
            set = flexibilitySetRepository.save(set);
            Long id = set.getSetId();

            // Act
            flexibilitySetRepository.deleteById(id);

            // Assert
            assertThat(flexibilitySetRepository.findById(id)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByWorkoutExerciseIdOrderBySetNumber Tests")
    class FindByWorkoutExerciseIdOrderBySetNumberTests {

        @Test
        @DisplayName("Should find sets ordered by set number")
        void shouldFindSetsOrderedBySetNumber() {
            // Arrange - Create in reverse order
            flexibilitySetRepository.save(createFlexibilitySet(3, 30, "Static", 2));
            flexibilitySetRepository.save(createFlexibilitySet(1, 60, "Static", 3));
            flexibilitySetRepository.save(createFlexibilitySet(2, 45, "Dynamic", 4));
            entityManager.flush();
            entityManager.clear();

            // Act
            List<FlexibilitySet> result = flexibilitySetRepository.findByWorkoutExerciseIdOrderBySetNumber(
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
            List<FlexibilitySet> result = flexibilitySetRepository.findByWorkoutExerciseIdOrderBySetNumber(
                    workoutExercise.getWorkoutExerciseId());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted sets")
        void shouldExcludeSoftDeletedSets() {
            // Arrange
            FlexibilitySet set1 = flexibilitySetRepository.save(createFlexibilitySet(1, 60, "Static", 3));
            flexibilitySetRepository.save(createFlexibilitySet(2, 60, "Static", 3));
            entityManager.flush();

            // Soft delete one
            set1.softDelete();
            flexibilitySetRepository.save(set1);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<FlexibilitySet> result = flexibilitySetRepository.findByWorkoutExerciseIdOrderBySetNumber(
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
            flexibilitySetRepository.save(createFlexibilitySet(1, 60, "Static", 3));
            flexibilitySetRepository.save(createFlexibilitySet(2, 60, "Dynamic", 4));
            flexibilitySetRepository.save(createFlexibilitySet(3, 60, "PNF", 5));
            entityManager.flush();
            entityManager.clear();

            // Act
            List<FlexibilitySet> result = flexibilitySetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty for session with no sets")
        void shouldReturnEmptyForSessionWithNoSets() {
            // Act
            List<FlexibilitySet> result = flexibilitySetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted sets")
        void shouldExcludeSoftDeletedSets() {
            // Arrange
            FlexibilitySet set1 = flexibilitySetRepository.save(createFlexibilitySet(1, 60, "Static", 3));
            flexibilitySetRepository.save(createFlexibilitySet(2, 60, "Dynamic", 4));
            entityManager.flush();

            // Soft delete one
            set1.softDelete();
            flexibilitySetRepository.save(set1);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<FlexibilitySet> result = flexibilitySetRepository.findBySessionId(workoutSession.getSessionId());

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should soft delete flexibility set")
        void shouldSoftDeleteFlexibilitySet() {
            // Arrange
            FlexibilitySet set = flexibilitySetRepository.save(createFlexibilitySet(1, 60, "Static", 3));
            Long id = set.getSetId();
            entityManager.flush();
            entityManager.clear();

            // Act
            FlexibilitySet toDelete = flexibilitySetRepository.findById(id).orElseThrow();
            toDelete.softDelete();
            flexibilitySetRepository.save(toDelete);
            entityManager.flush();
            entityManager.clear();

            // Assert
            assertThat(flexibilitySetRepository.findById(id)).isEmpty();
            assertThat(flexibilitySetRepository.findByIdIncludingDeleted(id)).isPresent();
        }

        @Test
        @DisplayName("Should restore soft deleted flexibility set")
        void shouldRestoreSoftDeletedFlexibilitySet() {
            // Arrange
            FlexibilitySet set = flexibilitySetRepository.save(createFlexibilitySet(1, 60, "Static", 3));
            Long id = set.getSetId();

            set.softDelete();
            flexibilitySetRepository.save(set);
            entityManager.flush();
            entityManager.clear();

            // Act
            FlexibilitySet deleted = flexibilitySetRepository.findByIdIncludingDeleted(id).orElseThrow();
            deleted.restore();
            flexibilitySetRepository.save(deleted);
            entityManager.flush();
            entityManager.clear();

            // Assert
            Optional<FlexibilitySet> restored = flexibilitySetRepository.findById(id);
            assertThat(restored).isPresent();
            assertThat(restored.get().isActive()).isTrue();
        }
    }

    private FlexibilitySet createFlexibilitySet(int setNumber, int duration, String stretchType, int intensity) {
        FlexibilitySet set = new FlexibilitySet();
        set.setWorkoutExercise(workoutExercise);
        set.setSetNumber(setNumber);
        set.setDurationInSeconds(duration);
        set.setStretchType(stretchType);
        set.setIntensity(intensity);
        return set;
    }
}
