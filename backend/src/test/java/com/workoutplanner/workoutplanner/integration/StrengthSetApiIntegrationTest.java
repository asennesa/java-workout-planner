package com.workoutplanner.workoutplanner.integration;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.config.TestSecurityConfig;
import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import com.workoutplanner.workoutplanner.repository.StrengthSetRepository;
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
 * API Integration tests for Strength Set endpoints.
 *
 * Tests the complete HTTP request-response cycle for:
 * - Create strength set
 * - Get sets by workout exercise
 * - Get set by ID
 * - Update set
 * - Delete set
 *
 * Requires full entity hierarchy: User -> WorkoutSession -> WorkoutExercise -> StrengthSet
 */
@DisplayName("Strength Set API Integration Tests")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StrengthSetApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private StrengthSetRepository strengthSetRepository;

    private User testUser;
    private WorkoutSession workoutSession;
    private WorkoutExercise workoutExercise;
    private Exercise strengthExercise;

    @BeforeEach
    void setUp() {
        // Create user
        testUser = TestDataBuilder.createNewUser();
        testUser.setRole(UserRole.USER);
        testUser = userRepository.saveAndFlush(testUser);
        TestSecurityConfig.TestAuthFilter.setTestUserId(testUser.getUserId());

        // Create strength exercise
        strengthExercise = TestDataBuilder.createNewStrengthExercise();
        strengthExercise = exerciseRepository.saveAndFlush(strengthExercise);

        // Create workout session
        workoutSession = TestDataBuilder.createNewWorkoutSession(testUser);
        workoutSession.setStatus(WorkoutStatus.IN_PROGRESS);
        workoutSession = workoutSessionRepository.saveAndFlush(workoutSession);

        // Create workout exercise
        workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkoutSession(workoutSession);
        workoutExercise.setExercise(strengthExercise);
        workoutExercise.setOrderInWorkout(1);
        workoutExercise.setNotes("Test notes");
        workoutExercise = workoutExerciseRepository.saveAndFlush(workoutExercise);
    }

    @AfterEach
    void cleanUp() {
        strengthSetRepository.deleteAll();
        workoutExerciseRepository.deleteAll();
        workoutSessionRepository.deleteAll();
        exerciseRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getBasePath() {
        return "/workout-exercises/" + workoutExercise.getWorkoutExerciseId() + "/strength-sets";
    }

    @Nested
    @DisplayName("POST - Create Strength Set")
    class CreateStrengthSetTests {

        @Test
        @DisplayName("Should create strength set and return 201")
        void shouldCreateStrengthSetAndReturn201() {
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();

            given()
                .body(request)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(201)
                .body("setId", notNullValue())
                .body("setNumber", equalTo(1))
                .body("reps", equalTo(10))
                .body("weight", equalTo(100.00f));
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() {
            CreateStrengthSetRequest request = new CreateStrengthSetRequest();
            // Missing setNumber and reps

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
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();

            given()
                .body(request)
            .when()
                .post("/workout-exercises/99999/strength-sets")
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should create multiple sets with different set numbers")
        void shouldCreateMultipleSetsWithDifferentSetNumbers() {
            // Create first set
            CreateStrengthSetRequest request1 = TestDataBuilder.createStrengthSetRequest();
            request1.setSetNumber(1);
            request1.setReps(10);

            given()
                .body(request1)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(201);

            // Create second set
            CreateStrengthSetRequest request2 = TestDataBuilder.createStrengthSetRequest();
            request2.setSetNumber(2);
            request2.setReps(8);

            given()
                .body(request2)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(201)
                .body("setNumber", equalTo(2))
                .body("reps", equalTo(8));
        }
    }

    @Nested
    @DisplayName("GET - Get Sets By Workout Exercise")
    class GetSetsByWorkoutExerciseTests {

        @BeforeEach
        void createTestSets() {
            for (int i = 1; i <= 3; i++) {
                StrengthSet set = new StrengthSet();
                set.setWorkoutExercise(workoutExercise);
                set.setSetNumber(i);
                set.setReps(10 - i);
                set.setWeight(new BigDecimal("100.00"));
                set.setRestTimeInSeconds(60);
                strengthSetRepository.save(set);
            }
            strengthSetRepository.flush();
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

        @Test
        @DisplayName("Should return empty list when no sets exist")
        void shouldReturnEmptyListWhenNoSetsExist() {
            strengthSetRepository.deleteAll();

            given()
            .when()
                .get(getBasePath())
            .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }

    @Nested
    @DisplayName("GET /{setId} - Get Set By ID")
    class GetSetByIdTests {

        private StrengthSet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new StrengthSet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setReps(10);
            testSet.setWeight(new BigDecimal("100.00"));
            testSet.setRestTimeInSeconds(60);
            testSet.setNotes("Test set");
            testSet = strengthSetRepository.saveAndFlush(testSet);
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
                .body("setNumber", equalTo(1))
                .body("reps", equalTo(10));
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

        private StrengthSet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new StrengthSet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setReps(10);
            testSet.setWeight(new BigDecimal("100.00"));
            testSet.setRestTimeInSeconds(60);
            testSet = strengthSetRepository.saveAndFlush(testSet);
        }

        @Test
        @DisplayName("Should update set and return 200")
        void shouldUpdateSetAndReturn200() {
            CreateStrengthSetRequest request = new CreateStrengthSetRequest();
            request.setSetNumber(1);
            request.setReps(12);
            request.setWeight(new BigDecimal("110.00"));
            request.setRestTimeInSeconds(90);
            request.setNotes("Updated notes");

            given()
                .body(request)
            .when()
                .put(getBasePath() + "/" + testSet.getSetId())
            .then()
                .statusCode(200)
                .body("reps", equalTo(12))
                .body("weight", equalTo(110.00f))
                .body("restTimeInSeconds", equalTo(90));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent set")
        void shouldReturn404WhenUpdatingNonExistentSet() {
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();

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

        private StrengthSet testSet;

        @BeforeEach
        void createTestSet() {
            testSet = new StrengthSet();
            testSet.setWorkoutExercise(workoutExercise);
            testSet.setSetNumber(1);
            testSet.setReps(10);
            testSet.setWeight(new BigDecimal("100.00"));
            testSet = strengthSetRepository.saveAndFlush(testSet);
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

    @Nested
    @DisplayName("Complete Workflow")
    class CompleteWorkflowTests {

        @Test
        @DisplayName("Should complete full CRUD workflow for strength sets")
        void shouldCompleteFullCrudWorkflow() {
            // 1. Create first set
            CreateStrengthSetRequest createRequest = new CreateStrengthSetRequest();
            createRequest.setSetNumber(1);
            createRequest.setReps(10);
            createRequest.setWeight(new BigDecimal("100.00"));
            createRequest.setRestTimeInSeconds(60);

            Integer setId = given()
                .body(createRequest)
            .when()
                .post(getBasePath())
            .then()
                .statusCode(201)
                .extract().path("setId");

            // 2. Get the set
            given()
            .when()
                .get(getBasePath() + "/" + setId)
            .then()
                .statusCode(200)
                .body("reps", equalTo(10));

            // 3. Update the set
            CreateStrengthSetRequest updateRequest = new CreateStrengthSetRequest();
            updateRequest.setSetNumber(1);
            updateRequest.setReps(12);
            updateRequest.setWeight(new BigDecimal("105.00"));
            updateRequest.setRestTimeInSeconds(90);

            given()
                .body(updateRequest)
            .when()
                .put(getBasePath() + "/" + setId)
            .then()
                .statusCode(200)
                .body("reps", equalTo(12));

            // 4. Verify update
            given()
            .when()
                .get(getBasePath() + "/" + setId)
            .then()
                .statusCode(200)
                .body("reps", equalTo(12))
                .body("weight", equalTo(105.00f));

            // 5. Delete the set
            given()
            .when()
                .delete(getBasePath() + "/" + setId)
            .then()
                .statusCode(204);

            // 6. Verify deletion
            given()
            .when()
                .get(getBasePath() + "/" + setId)
            .then()
                .statusCode(404);
        }
    }
}
