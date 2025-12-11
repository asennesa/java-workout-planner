package com.workoutplanner.workoutplanner.integration;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.config.TestSecurityConfig;
import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.repository.CardioSetRepository;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
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

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * API Integration tests for Cardio Set endpoints.
 *
 * Tests the complete HTTP request-response cycle for:
 * - Create cardio set
 * - Get sets by workout exercise
 * - Get set by ID
 * - Update set
 * - Delete set
 */
@DisplayName("Cardio Set API Integration Tests")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CardioSetApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private CardioSetRepository cardioSetRepository;

    private User testUser;
    private WorkoutSession workoutSession;
    private WorkoutExercise workoutExercise;
    private Exercise cardioExercise;

    @BeforeEach
    void setUp() {
        // Create user
        testUser = TestDataBuilder.createNewUser();
        testUser.setRole(UserRole.USER);
        testUser = userRepository.saveAndFlush(testUser);
        TestSecurityConfig.TestAuthFilter.setTestUserId(testUser.getUserId());

        // Create cardio exercise
        cardioExercise = TestDataBuilder.createNewCardioExercise();
        cardioExercise = exerciseRepository.saveAndFlush(cardioExercise);

        // Create workout session
        workoutSession = TestDataBuilder.createNewWorkoutSession(testUser);
        workoutSession.setStatus(WorkoutStatus.IN_PROGRESS);
        workoutSession = workoutSessionRepository.saveAndFlush(workoutSession);

        // Create workout exercise
        workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkoutSession(workoutSession);
        workoutExercise.setExercise(cardioExercise);
        workoutExercise.setOrderInWorkout(1);
        workoutExercise.setNotes("Cardio workout");
        workoutExercise = workoutExerciseRepository.saveAndFlush(workoutExercise);
    }

    @AfterEach
    void cleanUp() {
        cardioSetRepository.deleteAll();
        workoutExerciseRepository.deleteAll();
        workoutSessionRepository.deleteAll();
        exerciseRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getBasePath() {
        return "/workout-exercises/" + workoutExercise.getWorkoutExerciseId() + "/cardio-sets";
    }

    @Nested
    @DisplayName("POST - Create Cardio Set")
    class CreateCardioSetTests {

        @Test
        @DisplayName("Should create cardio set and return 201")
        void shouldCreateCardioSetAndReturn201() {
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();

            given()
                .body(request)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(201)
                .body("setId", notNullValue())
                .body("setNumber", equalTo(1))
                .body("durationInSeconds", equalTo(1800))
                .body("distance", equalTo(5.00f))
                .body("distanceUnit", equalTo("km"));
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() {
            CreateCardioSetRequest request = new CreateCardioSetRequest();
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
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();

            given()
                .body(request)
            .when()
                .post("/workout-exercises/99999/cardio-sets")
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 400 for zero duration")
        void shouldReturn400ForZeroDuration() {
            CreateCardioSetRequest request = new CreateCardioSetRequest();
            request.setSetNumber(1);
            request.setDurationInSeconds(0); // Invalid: zero duration
            request.setDistance(new BigDecimal("5.00"));
            request.setDistanceUnit("km");

            given()
                .body(request)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 for negative duration")
        void shouldReturn400ForNegativeDuration() {
            CreateCardioSetRequest request = new CreateCardioSetRequest();
            request.setSetNumber(1);
            request.setDurationInSeconds(-100); // Invalid: negative duration
            request.setDistance(new BigDecimal("5.00"));
            request.setDistanceUnit("km");

            given()
                .body(request)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 for negative distance")
        void shouldReturn400ForNegativeDistance() {
            CreateCardioSetRequest request = new CreateCardioSetRequest();
            request.setSetNumber(1);
            request.setDurationInSeconds(1800);
            request.setDistance(new BigDecimal("-5.00")); // Invalid: negative distance
            request.setDistanceUnit("km");

            given()
                .body(request)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 for negative set number")
        void shouldReturn400ForNegativeSetNumber() {
            CreateCardioSetRequest request = new CreateCardioSetRequest();
            request.setSetNumber(-1); // Invalid: negative set number
            request.setDurationInSeconds(1800);
            request.setDistance(new BigDecimal("5.00"));
            request.setDistanceUnit("km");

            given()
                .body(request)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET - Get Sets By Workout Exercise")
    class GetSetsByWorkoutExerciseTests {

        @BeforeEach
        void createTestSets() {
            for (int i = 1; i <= 3; i++) {
                CardioSet set = new CardioSet();
                set.setWorkoutExercise(workoutExercise);
                set.setSetNumber(i);
                set.setDurationInSeconds(600 * i);
                set.setDistance(new BigDecimal("2.00"));
                set.setDistanceUnit("km");
                cardioSetRepository.save(set);
            }
            cardioSetRepository.flush();
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

        private CardioSet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new CardioSet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setDurationInSeconds(1800);
            testSet.setDistance(new BigDecimal("5.00"));
            testSet.setDistanceUnit("km");
            testSet.setNotes("Morning run");
            testSet = cardioSetRepository.saveAndFlush(testSet);
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
                .body("durationInSeconds", equalTo(1800))
                .body("distance", equalTo(5.00f));
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

        private CardioSet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new CardioSet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setDurationInSeconds(1800);
            testSet.setDistance(new BigDecimal("5.00"));
            testSet.setDistanceUnit("km");
            testSet = cardioSetRepository.saveAndFlush(testSet);
        }

        @Test
        @DisplayName("Should update set and return 200")
        void shouldUpdateSetAndReturn200() {
            CreateCardioSetRequest request = new CreateCardioSetRequest();
            request.setSetNumber(1);
            request.setDurationInSeconds(2400);
            request.setDistance(new BigDecimal("8.00"));
            request.setDistanceUnit("km");
            request.setNotes("Longer run");

            given()
                .body(request)
            .when()
                .put(getBasePath() + "/" + testSet.getSetId())
            .then()
                .statusCode(200)
                .body("durationInSeconds", equalTo(2400))
                .body("distance", equalTo(8.00f));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent set")
        void shouldReturn404WhenUpdatingNonExistentSet() {
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();

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

        private CardioSet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new CardioSet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setDurationInSeconds(1800);
            testSet = cardioSetRepository.saveAndFlush(testSet);
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
