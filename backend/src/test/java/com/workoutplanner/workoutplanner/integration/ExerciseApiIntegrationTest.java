package com.workoutplanner.workoutplanner.integration;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.config.TestSecurityConfig;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * API Integration tests for Exercise endpoints.
 *
 * Tests the complete HTTP request-response cycle for:
 * - Get exercise by ID
 * - Get all exercises (paginated)
 * - Search exercises by name
 * - Filter exercises by criteria (type, muscle group, difficulty)
 *
 * Uses REST Assured for HTTP testing with real PostgreSQL (Testcontainers).
 */
@DisplayName("Exercise API Integration Tests")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ExerciseApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user for authentication
        testUser = TestDataBuilder.createNewUser();
        testUser.setRole(UserRole.USER);
        testUser = userRepository.saveAndFlush(testUser);
        TestSecurityConfig.TestAuthFilter.setTestUserId(testUser.getUserId());

        // Create test exercises
        createTestExercises();
    }

    @AfterEach
    void cleanUp() {
        exerciseRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createTestExercises() {
        // Strength exercises
        exerciseRepository.save(createExercise("Bench Press", ExerciseType.STRENGTH,
                TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE));
        exerciseRepository.save(createExercise("Push Up", ExerciseType.STRENGTH,
                TargetMuscleGroup.CHEST, DifficultyLevel.BEGINNER));
        exerciseRepository.save(createExercise("Squat", ExerciseType.STRENGTH,
                TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));
        exerciseRepository.save(createExercise("Deadlift", ExerciseType.STRENGTH,
                TargetMuscleGroup.BACK, DifficultyLevel.ADVANCED));

        // Cardio exercises
        exerciseRepository.save(createExercise("Running", ExerciseType.CARDIO,
                TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));
        exerciseRepository.save(createExercise("Cycling", ExerciseType.CARDIO,
                TargetMuscleGroup.LEGS, DifficultyLevel.INTERMEDIATE));

        // Flexibility exercises
        exerciseRepository.save(createExercise("Yoga Stretch", ExerciseType.FLEXIBILITY,
                TargetMuscleGroup.FULL_BODY, DifficultyLevel.BEGINNER));

        exerciseRepository.flush();
    }

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

    @Nested
    @DisplayName("GET /api/v1/exercises/{exerciseId} - Get Exercise By ID")
    class GetExerciseByIdTests {

        @Test
        @DisplayName("Should return exercise by ID")
        void shouldReturnExerciseById() {
            Exercise exercise = exerciseRepository.findAll().get(0);

            given()
            .when()
                .get("/exercises/" + exercise.getExerciseId())
            .then()
                .statusCode(200)
                .body("exerciseId", equalTo(exercise.getExerciseId().intValue()))
                .body("name", equalTo(exercise.getName()))
                .body("type", equalTo(exercise.getType().name()));
        }

        @Test
        @DisplayName("Should return 404 when exercise not found")
        void shouldReturn404WhenExerciseNotFound() {
            given()
            .when()
                .get("/exercises/99999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/exercises - Get All Exercises (Paginated)")
    class GetAllExercisesTests {

        @Test
        @DisplayName("Should return paginated exercises")
        void shouldReturnPaginatedExercises() {
            given()
                .queryParam("page", 0)
                .queryParam("size", 10)
            .when()
                .get("/exercises")
            .then()
                .statusCode(200)
                .body("content", hasSize(7))
                .body("totalElements", equalTo(7))
                .body("pageNumber", equalTo(0));
        }

        @Test
        @DisplayName("Should respect page size")
        void shouldRespectPageSize() {
            given()
                .queryParam("page", 0)
                .queryParam("size", 3)
            .when()
                .get("/exercises")
            .then()
                .statusCode(200)
                .body("content", hasSize(3))
                .body("totalPages", equalTo(3));
        }

        @Test
        @DisplayName("Should return correct page")
        void shouldReturnCorrectPage() {
            given()
                .queryParam("page", 1)
                .queryParam("size", 3)
            .when()
                .get("/exercises")
            .then()
                .statusCode(200)
                .body("content", hasSize(3))
                .body("pageNumber", equalTo(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/exercises/search - Search Exercises")
    class SearchExercisesTests {

        @Test
        @DisplayName("Should find exercises by name containing")
        void shouldFindExercisesByNameContaining() {
            given()
                .queryParam("name", "Press")
            .when()
                .get("/exercises/search")
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Bench Press"));
        }

        @Test
        @DisplayName("Should find exercises case insensitively")
        void shouldFindExercisesCaseInsensitively() {
            given()
                .queryParam("name", "push")
            .when()
                .get("/exercises/search")
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Push Up"));
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            given()
                .queryParam("name", "NonExistentExercise")
            .when()
                .get("/exercises/search")
            .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }

        @Test
        @DisplayName("Should return 400 for empty search term")
        void shouldReturn400ForEmptySearchTerm() {
            given()
                .queryParam("name", "")
            .when()
                .get("/exercises/search")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 for search term too short")
        void shouldReturn400ForSearchTermTooShort() {
            given()
                .queryParam("name", "a")
            .when()
                .get("/exercises/search")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 for whitespace-only search term")
        void shouldReturn400ForWhitespaceOnlySearchTerm() {
            given()
                .queryParam("name", "   ")
            .when()
                .get("/exercises/search")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should handle special characters in search safely")
        void shouldHandleSpecialCharactersInSearchSafely() {
            // Test SQL injection prevention - should return empty list, not error
            given()
                .queryParam("name", "'; DROP TABLE exercises; --")
            .when()
                .get("/exercises/search")
            .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }

        @Test
        @DisplayName("Should handle percent and underscore wildcards safely")
        void shouldHandleWildcardCharactersSafely() {
            // % and _ are SQL LIKE wildcards - should be escaped
            given()
                .queryParam("name", "%Press%")
            .when()
                .get("/exercises/search")
            .then()
                .statusCode(200)
                .body("$", hasSize(0)); // Should NOT match "Bench Press" since % is escaped
        }
    }

    @Nested
    @DisplayName("GET /api/v1/exercises/filter - Filter Exercises")
    class FilterExercisesTests {

        @Test
        @DisplayName("Should filter by type")
        void shouldFilterByType() {
            given()
                .queryParam("type", "STRENGTH")
            .when()
                .get("/exercises/filter")
            .then()
                .statusCode(200)
                .body("$", hasSize(4))
                .body("type", everyItem(equalTo("STRENGTH")));
        }

        @Test
        @DisplayName("Should filter by target muscle group")
        void shouldFilterByTargetMuscleGroup() {
            given()
                .queryParam("targetMuscleGroup", "CHEST")
            .when()
                .get("/exercises/filter")
            .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("targetMuscleGroup", everyItem(equalTo("CHEST")));
        }

        @Test
        @DisplayName("Should filter by difficulty level")
        void shouldFilterByDifficultyLevel() {
            given()
                .queryParam("difficultyLevel", "BEGINNER")
            .when()
                .get("/exercises/filter")
            .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("difficultyLevel", everyItem(equalTo("BEGINNER")));
        }

        @Test
        @DisplayName("Should filter by multiple criteria")
        void shouldFilterByMultipleCriteria() {
            given()
                .queryParam("type", "STRENGTH")
                .queryParam("targetMuscleGroup", "CHEST")
            .when()
                .get("/exercises/filter")
            .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("type", everyItem(equalTo("STRENGTH")))
                .body("targetMuscleGroup", everyItem(equalTo("CHEST")));
        }

        @Test
        @DisplayName("Should filter by all criteria")
        void shouldFilterByAllCriteria() {
            given()
                .queryParam("type", "STRENGTH")
                .queryParam("targetMuscleGroup", "CHEST")
                .queryParam("difficultyLevel", "INTERMEDIATE")
            .when()
                .get("/exercises/filter")
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Bench Press"));
        }

        @Test
        @DisplayName("Should return all exercises when no filters")
        void shouldReturnAllExercisesWhenNoFilters() {
            given()
            .when()
                .get("/exercises/filter")
            .then()
                .statusCode(200)
                .body("$", hasSize(7));
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            given()
                .queryParam("type", "CARDIO")
                .queryParam("targetMuscleGroup", "CHEST")
            .when()
                .get("/exercises/filter")
            .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }
}
