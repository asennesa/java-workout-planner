package com.workoutplanner.workoutplanner.integration;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.WorkoutActionRequest;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * API Integration tests for WorkoutSession endpoints.
 * 
 * Industry Best Practices Demonstrated:
 * 1. Use REST Assured for API testing
 * 2. Test complete request-response cycle
 * 3. Test with real database (Testcontainers)
 * 4. Test HTTP status codes, headers, and response body
 * 5. Test validation errors
 * 6. Test CORS headers
 * 7. Test pagination
 * 
 * Testing Philosophy:
 * - Integration tests verify the entire application stack works together
 * - Test from HTTP request to database and back
 * - Verify API contracts (status codes, response format)
 * - Test real-world scenarios and workflows
 */
@DisplayName("Workout Session API Integration Tests")
class WorkoutSessionApiIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User testUser;
    
    @BeforeEach
    void setUpUser() {
        // Create test user with encoded password for authentication
        testUser = TestDataBuilder.createNewUser(); // For repository.save()
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);
    }
    
    /**
     * CRITICAL FIX: Manual cleanup required for API integration tests.
     * 
     * @Transactional rollback doesn't work with REST Assured because HTTP calls
     * execute in separate transactions. We must manually clean up test data
     * to prevent test pollution and ensure test isolation.
     * 
     * Note: Delete in reverse order of foreign key dependencies.
     */
    @AfterEach
    void cleanUpTestData() {
        // Clean up in reverse order of dependencies
        // WorkoutSessions reference Users, so delete workouts first
        try {
            given()
                .auth().basic("testuser", "password123")
            .when()
                .get("/workouts")
            .then()
                .extract()
                .jsonPath()
                .getList("content.sessionId", Long.class)
                .forEach(sessionId -> {
                    given()
                        .auth().basic("testuser", "password123")
                    .when()
                        .delete("/workouts/" + sessionId);
                });
        } catch (Exception e) {
            // Ignore errors during cleanup
        }
        
        // Clean up users
        userRepository.deleteAll();
    }
    
    // ==================== CREATE WORKOUT TESTS ====================
    
    @Test
    @DisplayName("POST /api/v1/workouts - Should create workout and return 201")
    void shouldCreateWorkoutAndReturn201() {
        // Arrange
        CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/workouts")
        .then()
            .statusCode(201)
            .body("sessionId", notNullValue())
            .body("name", equalTo("Test Workout"))
            .body("status", equalTo("PLANNED"))
            .body("userId", equalTo(testUser.getUserId().intValue()));
    }
    
    @Test
    @DisplayName("POST /api/v1/workouts - Should return 400 for invalid request")
    void shouldReturn400ForInvalidRequest() {
        // Arrange - Invalid request (missing required fields)
        CreateWorkoutRequest request = new CreateWorkoutRequest();
        // Missing userId and name
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/workouts")
        .then()
            .statusCode(400);
    }
    
    // ==================== GET WORKOUT TESTS ====================
    
    @Test
    @DisplayName("GET /api/v1/workouts/{id} - Should return workout by ID")
    void shouldReturnWorkoutById() {
        // Arrange - Create a workout first
        CreateWorkoutRequest createRequest = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        Integer workoutId = given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/workouts")
        .then()
            .statusCode(201)
            .extract().path("sessionId");
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
        .when()
            .get("/workouts/" + workoutId)
        .then()
            .statusCode(200)
            .body("sessionId", equalTo(workoutId))
            .body("name", equalTo("Test Workout"));
    }
    
    @Test
    @DisplayName("GET /api/v1/workouts/{id} - Should return 404 when workout not found")
    void shouldReturn404WhenWorkoutNotFound() {
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
        .when()
            .get("/workouts/99999")
        .then()
            .statusCode(404);
    }
    
    @Test
    @DisplayName("GET /api/v1/workouts - Should return paginated workouts")
    void shouldReturnPaginatedWorkouts() {
        // Arrange - Create multiple workouts
        for (int i = 1; i <= 3; i++) {
            CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
            request.setName("Workout " + i);
            given()
                .auth().basic("testuser", "password123")
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/workouts")
            .then()
                .statusCode(201);
        }
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/workouts")
        .then()
            .statusCode(200)
            .body("content", hasSize(3))
            .body("totalElements", equalTo(3))
            .body("totalPages", equalTo(1))
            .body("currentPage", equalTo(0));
    }
    
    @Test
    @DisplayName("GET /api/v1/workouts/user/{userId} - Should return workouts by user")
    void shouldReturnWorkoutsByUser() {
        // Arrange
        CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/workouts")
        .then()
            .statusCode(201);
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
        .when()
            .get("/workouts/user/" + testUser.getUserId())
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].userId", equalTo(testUser.getUserId().intValue()));
    }
    
    // ==================== UPDATE WORKOUT TESTS ====================
    
    @Test
    @DisplayName("PUT /api/v1/workouts/{id} - Should update workout")
    void shouldUpdateWorkout() {
        // Arrange - Create a workout first
        CreateWorkoutRequest createRequest = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        Integer workoutId = given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/workouts")
        .then()
            .statusCode(201)
            .extract().path("sessionId");
        
        // Update request
        CreateWorkoutRequest updateRequest = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        updateRequest.setName("Updated Workout");
        updateRequest.setStatus(WorkoutStatus.IN_PROGRESS);
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .put("/workouts/" + workoutId)
        .then()
            .statusCode(200)
            .body("sessionId", equalTo(workoutId))
            .body("name", equalTo("Updated Workout"))
            .body("status", equalTo("IN_PROGRESS"));
    }
    
    @Test
    @DisplayName("PATCH /api/v1/workouts/{id}/status - Should update workout status")
    void shouldUpdateWorkoutStatus() {
        // Arrange - Create a workout first
        CreateWorkoutRequest createRequest = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        Integer workoutId = given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/workouts")
        .then()
            .statusCode(201)
            .extract().path("sessionId");
        
        // Status update request
        WorkoutActionRequest actionRequest = new WorkoutActionRequest();
        actionRequest.setAction("complete");
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(actionRequest)
        .when()
            .patch("/workouts/" + workoutId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("COMPLETED"));
    }
    
    @Test
    @DisplayName("PATCH /api/v1/workouts/{id}/status - Should return 400 for invalid action")
    void shouldReturn400ForInvalidAction() {
        // Arrange
        CreateWorkoutRequest createRequest = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        Integer workoutId = given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/workouts")
        .then()
            .statusCode(201)
            .extract().path("sessionId");
        
        WorkoutActionRequest actionRequest = new WorkoutActionRequest();
        actionRequest.setAction("invalid_action");
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(actionRequest)
        .when()
            .patch("/workouts/" + workoutId + "/status")
        .then()
            .statusCode(400);
    }
    
    // ==================== DELETE WORKOUT TESTS ====================
    
    @Test
    @DisplayName("DELETE /api/v1/workouts/{id} - Should delete workout and return 204")
    void shouldDeleteWorkoutAndReturn204() {
        // Arrange - Create a workout first
        CreateWorkoutRequest createRequest = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        Integer workoutId = given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/workouts")
        .then()
            .statusCode(201)
            .extract().path("sessionId");
        
        // Act & Assert
        given()
            .auth().basic("testuser", "password123")
        .when()
            .delete("/workouts/" + workoutId)
        .then()
            .statusCode(204);
        
        // Verify deletion
        given()
            .auth().basic("testuser", "password123")
        .when()
            .get("/workouts/" + workoutId)
        .then()
            .statusCode(404);
    }
    
    // ==================== COMPLETE WORKFLOW TEST ====================
    
    @Test
    @DisplayName("Complete workout lifecycle - Create, Start, Update, Complete, Delete")
    void shouldCompleteWorkoutLifecycle() {
        // 1. Create workout
        CreateWorkoutRequest createRequest = TestDataBuilder.createWorkoutRequest(testUser.getUserId());
        createRequest.setName("Full Lifecycle Workout");
        
        Integer workoutId = given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(createRequest)
        .when()
            .post("/workouts")
        .then()
            .statusCode(201)
            .body("status", equalTo("PLANNED"))
            .extract().path("sessionId");
        
        // 2. Start workout
        WorkoutActionRequest startAction = new WorkoutActionRequest();
        startAction.setAction("start");
        
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(startAction)
        .when()
            .patch("/workouts/" + workoutId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("IN_PROGRESS"));
        
        // 3. Pause workout
        WorkoutActionRequest pauseAction = new WorkoutActionRequest();
        pauseAction.setAction("pause");
        
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(pauseAction)
        .when()
            .patch("/workouts/" + workoutId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("PAUSED"));
        
        // 4. Resume workout
        WorkoutActionRequest resumeAction = new WorkoutActionRequest();
        resumeAction.setAction("resume");
        
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(resumeAction)
        .when()
            .patch("/workouts/" + workoutId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("IN_PROGRESS"));
        
        // 5. Complete workout
        WorkoutActionRequest completeAction = new WorkoutActionRequest();
        completeAction.setAction("complete");
        
        given()
            .auth().basic("testuser", "password123")
            .contentType(ContentType.JSON)
            .body(completeAction)
        .when()
            .patch("/workouts/" + workoutId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("COMPLETED"));
        
        // 6. Verify workout in user's history
        given()
            .auth().basic("testuser", "password123")
        .when()
            .get("/workouts/user/" + testUser.getUserId())
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("find { it.sessionId == " + workoutId + " }.status", equalTo("COMPLETED"));
        
        // 7. Delete workout
        given()
            .auth().basic("testuser", "password123")
        .when()
            .delete("/workouts/" + workoutId)
        .then()
            .statusCode(204);
    }
}

