package com.workoutplanner.workoutplanner.integration;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.config.TestSecurityConfig;
import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import com.workoutplanner.workoutplanner.repository.FlexibilitySetRepository;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutSessionRepository;
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
 * API Integration tests for Flexibility Set endpoints.
 *
 * Tests the complete HTTP request-response cycle for:
 * - Create flexibility set
 * - Get sets by workout exercise
 * - Get set by ID
 * - Update set
 * - Delete set
 */
@DisplayName("Flexibility Set API Integration Tests")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class FlexibilitySetApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private FlexibilitySetRepository flexibilitySetRepository;

    private User testUser;
    private WorkoutSession workoutSession;
    private WorkoutExercise workoutExercise;
    private Exercise flexibilityExercise;

    @BeforeEach
    void setUp() {
        // Create user
        testUser = TestDataBuilder.createNewUser();
        testUser.setRole(UserRole.USER);
        testUser = userRepository.saveAndFlush(testUser);
        TestSecurityConfig.TestAuthFilter.setTestUserId(testUser.getUserId());

        // Create flexibility exercise
        flexibilityExercise = TestDataBuilder.createNewFlexibilityExercise();
        flexibilityExercise = exerciseRepository.saveAndFlush(flexibilityExercise);

        // Create workout session
        workoutSession = TestDataBuilder.createNewWorkoutSession(testUser);
        workoutSession.setStatus(WorkoutStatus.IN_PROGRESS);
        workoutSession = workoutSessionRepository.saveAndFlush(workoutSession);

        // Create workout exercise
        workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkoutSession(workoutSession);
        workoutExercise.setExercise(flexibilityExercise);
        workoutExercise.setOrderInWorkout(1);
        workoutExercise.setNotes("Stretching session");
        workoutExercise = workoutExerciseRepository.saveAndFlush(workoutExercise);
    }

    @AfterEach
    void cleanUp() {
        flexibilitySetRepository.deleteAll();
        workoutExerciseRepository.deleteAll();
        workoutSessionRepository.deleteAll();
        exerciseRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getBasePath() {
        return "/workout-exercises/" + workoutExercise.getWorkoutExerciseId() + "/flexibility-sets";
    }

    @Nested
    @DisplayName("POST - Create Flexibility Set")
    class CreateFlexibilitySetTests {

        @Test
        @DisplayName("Should create flexibility set and return 201")
        void shouldCreateFlexibilitySetAndReturn201() {
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();

            given()
                .body(request)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(201)
                .body("setId", notNullValue())
                .body("setNumber", equalTo(1))
                .body("durationInSeconds", equalTo(60))
                .body("stretchType", equalTo("Static"))
                .body("intensity", equalTo(3));
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() {
            CreateFlexibilitySetRequest request = new CreateFlexibilitySetRequest();
            // Missing setNumber and durationInSeconds

            given()
                .body(request)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 404 for non-existent workout exercise")
        void shouldReturn404ForNonExistentWorkoutExercise() {
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();

            given()
                .body(request)
            .when()
                .post("/workout-exercises/99999/flexibility-sets")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET - Get Sets By Workout Exercise")
    class GetSetsByWorkoutExerciseTests {

        @BeforeEach
        void createTestSets() {
            for (int i = 1; i <= 3; i++) {
                FlexibilitySet set = new FlexibilitySet();
                set.setWorkoutExercise(workoutExercise);
                set.setSetNumber(i);
                set.setDurationInSeconds(30 * i);
                set.setStretchType("Static");
                set.setIntensity(i);
                flexibilitySetRepository.save(set);
            }
            flexibilitySetRepository.flush();
        }

        @Test
        @DisplayName("Should return all sets for workout exercise")
        void shouldReturnAllSetsForWorkoutExercise() {
            given()
            .when()
                .get(getBasePath())
            .then()
                .statusCode(200)
                .body("$", hasSize(3));
        }

        @Test
        @DisplayName("Should return sets ordered by set number")
        void shouldReturnSetsOrderedBySetNumber() {
            given()
            .when()
                .get(getBasePath())
            .then()
                .statusCode(200)
                .body("[0].setNumber", equalTo(1))
                .body("[1].setNumber", equalTo(2))
                .body("[2].setNumber", equalTo(3));
        }
    }

    @Nested
    @DisplayName("GET /{setId} - Get Set By ID")
    class GetSetByIdTests {

        private FlexibilitySet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new FlexibilitySet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setDurationInSeconds(60);
            testSet.setStretchType("Dynamic");
            testSet.setIntensity(4);
            testSet.setNotes("Deep stretch");
            testSet = flexibilitySetRepository.saveAndFlush(testSet);
        }

        @Test
        @DisplayName("Should return set by ID")
        void shouldReturnSetById() {
            given()
            .when()
                .get(getBasePath() + "/" + testSet.getSetId())
            .then()
                .statusCode(200)
                .body("setId", equalTo(testSet.getSetId().intValue()))
                .body("durationInSeconds", equalTo(60))
                .body("stretchType", equalTo("Dynamic"))
                .body("intensity", equalTo(4));
        }

        @Test
        @DisplayName("Should return 404 for non-existent set")
        void shouldReturn404ForNonExistentSet() {
            given()
            .when()
                .get(getBasePath() + "/99999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PUT /{setId} - Update Set")
    class UpdateSetTests {

        private FlexibilitySet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new FlexibilitySet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setDurationInSeconds(60);
            testSet.setStretchType("Static");
            testSet.setIntensity(3);
            testSet = flexibilitySetRepository.saveAndFlush(testSet);
        }

        @Test
        @DisplayName("Should update set and return 200")
        void shouldUpdateSetAndReturn200() {
            CreateFlexibilitySetRequest request = new CreateFlexibilitySetRequest();
            request.setSetNumber(1);
            request.setDurationInSeconds(90);
            request.setStretchType("Dynamic");
            request.setIntensity(5);
            request.setNotes("Increased intensity");

            given()
                .body(request)
            .when()
                .put(getBasePath() + "/" + testSet.getSetId())
            .then()
                .statusCode(200)
                .body("durationInSeconds", equalTo(90))
                .body("stretchType", equalTo("Dynamic"))
                .body("intensity", equalTo(5));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent set")
        void shouldReturn404WhenUpdatingNonExistentSet() {
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();

            given()
                .body(request)
            .when()
                .put(getBasePath() + "/99999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("DELETE /{setId} - Delete Set")
    class DeleteSetTests {

        private FlexibilitySet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new FlexibilitySet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setDurationInSeconds(60);
            testSet = flexibilitySetRepository.saveAndFlush(testSet);
        }

        @Test
        @DisplayName("Should delete set and return 204")
        void shouldDeleteSetAndReturn204() {
            given()
            .when()
                .delete(getBasePath() + "/" + testSet.getSetId())
            .then()
                .statusCode(204);

            // Verify deletion
            given()
            .when()
                .get(getBasePath() + "/" + testSet.getSetId())
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent set")
        void shouldReturn404WhenDeletingNonExistentSet() {
            given()
            .when()
                .delete(getBasePath() + "/99999")
            .then()
                .statusCode(404);
        }
    }
}
