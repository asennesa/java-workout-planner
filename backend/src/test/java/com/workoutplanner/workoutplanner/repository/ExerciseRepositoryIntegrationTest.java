package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
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
 * Integration tests for ExerciseRepository.
 * Tests custom queries, filtering, and soft delete behavior.
 */
@DisplayName("ExerciseRepository Integration Tests")
class ExerciseRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private EntityManager entityManager;

    private Exercise createExercise(String name, ExerciseType type,
                                    TargetMuscleGroup muscleGroup, DifficultyLevel difficulty) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setDescription("Test description for " + name);
        exercise.setType(type);
        exercise.setTargetMuscleGroup(muscleGroup);
        exercise.setDifficultyLevel(difficulty);
        return exercise;
    }

    @BeforeEach
    void setUpExercises() {
        // Create a variety of exercises for testing
        exerciseRepository.save(createExercise("Bench Press", ExerciseType.STRENGTH,
                TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exerciseRepository.save(createExercise("Push Up", ExerciseType.STRENGTH,
                TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exerciseRepository.save(createExercise("Running", ExerciseType.CARDIO,
                TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exerciseRepository.save(createExercise("Cycling", ExerciseType.CARDIO,
                TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exerciseRepository.save(createExercise("Yoga Stretch", ExerciseType.FLEXIBILITY,
                TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exerciseRepository.save(createExercise("Squat", ExerciseType.STRENGTH,
                TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exerciseRepository.save(createExercise("Deadlift", ExerciseType.STRENGTH,
                TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));
        entityManager.flush();
        entityManager.clear();
    }

    // ==================== BASIC CRUD TESTS ====================

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save and retrieve exercise")
        void shouldSaveAndRetrieveExercise() {
            // Arrange
            Exercise exercise = createExercise("New Exercise", ExerciseType.STRENGTH,
                    TargetMuscleGroup.ARMS, DifficultyLevel.BEGINNER);

            // Act
            Exercise saved = exerciseRepository.save(exercise);
            Optional<Exercise> retrieved = exerciseRepository.findById(saved.getExerciseId());

            // Assert
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getName()).isEqualTo("New Exercise");
            assertThat(retrieved.get().getType()).isEqualTo(ExerciseType.STRENGTH);
        }

        @Test
        @DisplayName("Should find all exercises")
        void shouldFindAllExercises() {
            // Act
            List<Exercise> all = exerciseRepository.findAll();

            // Assert
            assertThat(all).hasSize(7);
        }
    }

    // ==================== FILTER BY TYPE TESTS ====================

    @Nested
    @DisplayName("Find By Type Tests")
    class FindByTypeTests {

        @Test
        @DisplayName("Should find exercises by type STRENGTH")
        void shouldFindExercisesByTypeStrength() {
            // Act
            List<Exercise> strengthExercises = exerciseRepository.findByType(ExerciseType.STRENGTH);

            // Assert
            assertThat(strengthExercises).hasSize(4);
            assertThat(strengthExercises).extracting(Exercise::getName)
                    .containsExactlyInAnyOrder("Bench Press", "Push Up", "Squat", "Deadlift");
        }

        @Test
        @DisplayName("Should find exercises by type CARDIO")
        void shouldFindExercisesByTypeCardio() {
            // Act
            List<Exercise> cardioExercises = exerciseRepository.findByType(ExerciseType.CARDIO);

            // Assert
            assertThat(cardioExercises).hasSize(2);
            assertThat(cardioExercises).extracting(Exercise::getName)
                    .containsExactlyInAnyOrder("Running", "Cycling");
        }

        @Test
        @DisplayName("Should find exercises by type FLEXIBILITY")
        void shouldFindExercisesByTypeFlexibility() {
            // Act
            List<Exercise> flexibilityExercises = exerciseRepository.findByType(ExerciseType.FLEXIBILITY);

            // Assert
            assertThat(flexibilityExercises).hasSize(1);
            assertThat(flexibilityExercises.get(0).getName()).isEqualTo("Yoga Stretch");
        }
    }

    // ==================== FILTER BY MUSCLE GROUP TESTS ====================

    @Nested
    @DisplayName("Find By Target Muscle Group Tests")
    class FindByTargetMuscleGroupTests {

        @Test
        @DisplayName("Should find exercises by target muscle group CHEST")
        void shouldFindExercisesByTargetMuscleGroupChest() {
            // Act
            List<Exercise> chestExercises = exerciseRepository.findByTargetMuscleGroup(TargetMuscleGroup.CHEST);

            // Assert
            assertThat(chestExercises).hasSize(2);
            assertThat(chestExercises).extracting(Exercise::getName)
                    .containsExactlyInAnyOrder("Bench Press", "Push Up");
        }

        @Test
        @DisplayName("Should find exercises by target muscle group LEGS")
        void shouldFindExercisesByTargetMuscleGroupLegs() {
            // Act
            List<Exercise> legExercises = exerciseRepository.findByTargetMuscleGroup(TargetMuscleGroup.LEGS);

            // Assert
            assertThat(legExercises).hasSize(2);
            assertThat(legExercises).extracting(Exercise::getName)
                    .containsExactlyInAnyOrder("Cycling", "Squat");
        }

        @Test
        @DisplayName("Should find exercises by target muscle group FULL_BODY")
        void shouldFindExercisesByTargetMuscleGroupFullBody() {
            // Act
            List<Exercise> fullBodyExercises = exerciseRepository.findByTargetMuscleGroup(TargetMuscleGroup.FULL_BODY);

            // Assert
            assertThat(fullBodyExercises).hasSize(2);
            assertThat(fullBodyExercises).extracting(Exercise::getName)
                    .containsExactlyInAnyOrder("Running", "Yoga Stretch");
        }
    }

    // ==================== FILTER BY DIFFICULTY TESTS ====================

    @Nested
    @DisplayName("Find By Difficulty Level Tests")
    class FindByDifficultyLevelTests {

        @Test
        @DisplayName("Should find exercises by difficulty BEGINNER")
        void shouldFindExercisesByDifficultyBeginner() {
            // Act
            List<Exercise> beginnerExercises = exerciseRepository.findByDifficultyLevel(DifficultyLevel.BEGINNER);

            // Assert
            assertThat(beginnerExercises).hasSize(3);
            assertThat(beginnerExercises).extracting(Exercise::getName)
                    .containsExactlyInAnyOrder("Push Up", "Running", "Yoga Stretch");
        }

        @Test
        @DisplayName("Should find exercises by difficulty INTERMEDIATE")
        void shouldFindExercisesByDifficultyIntermediate() {
            // Act
            List<Exercise> intermediateExercises = exerciseRepository.findByDifficultyLevel(DifficultyLevel.INTERMEDIATE);

            // Assert
            assertThat(intermediateExercises).hasSize(3);
            assertThat(intermediateExercises).extracting(Exercise::getName)
                    .containsExactlyInAnyOrder("Bench Press", "Cycling", "Squat");
        }

        @Test
        @DisplayName("Should find exercises by difficulty ADVANCED")
        void shouldFindExercisesByDifficultyAdvanced() {
            // Act
            List<Exercise> advancedExercises = exerciseRepository.findByDifficultyLevel(DifficultyLevel.ADVANCED);

            // Assert
            assertThat(advancedExercises).hasSize(1);
            assertThat(advancedExercises.get(0).getName()).isEqualTo("Deadlift");
        }
    }

    // ==================== COMBINED FILTER TESTS ====================

    @Nested
    @DisplayName("Find By Type And Target Muscle Group Tests")
    class FindByTypeAndTargetMuscleGroupTests {

        @Test
        @DisplayName("Should find exercises by type and target muscle group")
        void shouldFindExercisesByTypeAndTargetMuscleGroup() {
            // Act
            List<Exercise> chestStrength = exerciseRepository.findByTypeAndTargetMuscleGroup(
                    ExerciseType.STRENGTH, TargetMuscleGroup.CHEST);

            // Assert
            assertThat(chestStrength).hasSize(2);
            assertThat(chestStrength).extracting(Exercise::getName)
                    .containsExactlyInAnyOrder("Bench Press", "Push Up");
        }

        @Test
        @DisplayName("Should return empty when no exercises match type and muscle group")
        void shouldReturnEmptyWhenNoMatch() {
            // Act
            List<Exercise> cardioChest = exerciseRepository.findByTypeAndTargetMuscleGroup(
                    ExerciseType.CARDIO, TargetMuscleGroup.CHEST);

            // Assert
            assertThat(cardioChest).isEmpty();
        }
    }

    // ==================== SEARCH BY NAME TESTS ====================

    @Nested
    @DisplayName("Find By Name Containing Tests")
    class FindByNameContainingTests {

        @Test
        @DisplayName("Should find exercises by name containing (case insensitive)")
        void shouldFindExercisesByNameContaining() {
            // Act
            List<Exercise> pressExercises = exerciseRepository.findByNameContainingIgnoreCase("press");

            // Assert
            assertThat(pressExercises).hasSize(1);
            assertThat(pressExercises.get(0).getName()).isEqualTo("Bench Press");
        }

        @Test
        @DisplayName("Should find exercises with partial name match")
        void shouldFindExercisesWithPartialNameMatch() {
            // Act
            List<Exercise> exercises = exerciseRepository.findByNameContainingIgnoreCase("u");

            // Assert - Push Up, Running, Squat all contain 'u'
            assertThat(exercises).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty when name not found")
        void shouldReturnEmptyWhenNameNotFound() {
            // Act
            List<Exercise> notFound = exerciseRepository.findByNameContainingIgnoreCase("xyz");

            // Assert
            assertThat(notFound).isEmpty();
        }
    }

    // ==================== DYNAMIC FILTER TESTS ====================

    @Nested
    @DisplayName("Find By Filters Tests")
    class FindByFiltersTests {

        @Test
        @DisplayName("Should find exercises with all filters")
        void shouldFindExercisesWithAllFilters() {
            // Act
            List<Exercise> filtered = exerciseRepository.findByFilters(
                    ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE);

            // Assert
            assertThat(filtered).hasSize(1);
            assertThat(filtered.get(0).getName()).isEqualTo("Bench Press");
        }

        @Test
        @DisplayName("Should find exercises with type filter only")
        void shouldFindExercisesWithTypeFilterOnly() {
            // Act
            List<Exercise> filtered = exerciseRepository.findByFilters(
                    ExerciseType.CARDIO, null, null);

            // Assert
            assertThat(filtered).hasSize(2);
        }

        @Test
        @DisplayName("Should find exercises with muscle group filter only")
        void shouldFindExercisesWithMuscleGroupFilterOnly() {
            // Act
            List<Exercise> filtered = exerciseRepository.findByFilters(
                    null, TargetMuscleGroup.LEGS, null);

            // Assert
            assertThat(filtered).hasSize(2);
        }

        @Test
        @DisplayName("Should find exercises with difficulty filter only")
        void shouldFindExercisesWithDifficultyFilterOnly() {
            // Act
            List<Exercise> filtered = exerciseRepository.findByFilters(
                    null, null, DifficultyLevel.ADVANCED);

            // Assert
            assertThat(filtered).hasSize(1);
            assertThat(filtered.get(0).getName()).isEqualTo("Deadlift");
        }

        @Test
        @DisplayName("Should find all exercises when no filters applied")
        void shouldFindAllExercisesWhenNoFiltersApplied() {
            // Act
            List<Exercise> filtered = exerciseRepository.findByFilters(null, null, null);

            // Assert
            assertThat(filtered).hasSize(7);
        }

        @Test
        @DisplayName("Should find exercises with two filters")
        void shouldFindExercisesWithTwoFilters() {
            // Act
            List<Exercise> filtered = exerciseRepository.findByFilters(
                    ExerciseType.STRENGTH, null, DifficultyLevel.BEGINNER);

            // Assert
            assertThat(filtered).hasSize(1);
            assertThat(filtered.get(0).getName()).isEqualTo("Push Up");
        }
    }

    // ==================== SOFT DELETE TESTS ====================

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should exclude soft deleted exercises from findByType")
        void shouldExcludeSoftDeletedExercisesFromFindByType() {
            // Arrange - Soft delete one strength exercise
            List<Exercise> strengthExercises = exerciseRepository.findByType(ExerciseType.STRENGTH);
            Exercise toDelete = strengthExercises.get(0);
            toDelete.softDelete();
            exerciseRepository.save(toDelete);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<Exercise> remaining = exerciseRepository.findByType(ExerciseType.STRENGTH);

            // Assert
            assertThat(remaining).hasSize(3); // Was 4, now 3
        }

        @Test
        @DisplayName("Should exclude soft deleted exercises from findByFilters")
        void shouldExcludeSoftDeletedExercisesFromFindByFilters() {
            // Arrange - Soft delete Deadlift (only advanced exercise)
            List<Exercise> advancedExercises = exerciseRepository.findByDifficultyLevel(DifficultyLevel.ADVANCED);
            Exercise deadlift = advancedExercises.get(0);
            deadlift.softDelete();
            exerciseRepository.save(deadlift);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<Exercise> filtered = exerciseRepository.findByFilters(
                    null, null, DifficultyLevel.ADVANCED);

            // Assert
            assertThat(filtered).isEmpty();
        }

        @Test
        @DisplayName("Should exclude soft deleted exercises from findByNameContainingIgnoreCase")
        void shouldExcludeSoftDeletedExercisesFromSearch() {
            // Arrange - Soft delete Bench Press
            List<Exercise> pressExercises = exerciseRepository.findByNameContainingIgnoreCase("press");
            Exercise benchPress = pressExercises.get(0);
            benchPress.softDelete();
            exerciseRepository.save(benchPress);
            entityManager.flush();
            entityManager.clear();

            // Act
            List<Exercise> remaining = exerciseRepository.findByNameContainingIgnoreCase("press");

            // Assert
            assertThat(remaining).isEmpty();
        }
    }
}
