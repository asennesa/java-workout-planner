package com.workoutplanner.workoutplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.WorkoutActionRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.exception.OptimisticLockConflictException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.service.WorkoutSessionService;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for WorkoutSessionController using MockMvc.
 * 
 * Industry Best Practices:
 * - Use @WebMvcTest for controller layer testing
 * - Mock service layer with @MockitoBean
 * - Use MockMvc for simulating HTTP requests
 * - Test HTTP status codes, response bodies, and headers
 * - Use @WithMockUser for security context
 * - Test validation errors (400 Bad Request)
 * - Test exception handling (404, 409, etc.)
 * - Organize tests with @Nested classes
 * 
 * Testing Philosophy:
 * - Test controller behavior, not business logic
 * - Verify HTTP contracts (status codes, headers, body structure)
 * - Ensure proper request/response serialization
 * - Test security constraints
 */
@WebMvcTest(WorkoutSessionController.class)
@ActiveProfiles("test")
@DisplayName("WorkoutSessionController Unit Tests")
class WorkoutSessionControllerTest {
    
    // Test constants
    private static final Long VALID_WORKOUT_ID = 1L;
    private static final Long VALID_USER_ID = 1L;
    private static final Long NON_EXISTENT_ID = 999L;
    private static final String WORKOUT_NAME = "Test Workout";
    private static final String UPDATED_WORKOUT_NAME = "Updated Workout";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private WorkoutSessionService workoutSessionService;
    
    // ==================== CREATE WORKOUT TESTS ====================
    
    @Nested
    @DisplayName("POST /api/v1/workouts - Create Workout")
    class CreateWorkoutTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should create workout and return 201")
        void shouldCreateWorkoutAndReturn201() throws Exception {
            // Arrange
            CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest(VALID_USER_ID);
            WorkoutResponse response = new WorkoutResponse();
            response.setSessionId(VALID_WORKOUT_ID);
            response.setName(WORKOUT_NAME);
            response.setStatus(WorkoutStatus.PLANNED);
            
            when(workoutSessionService.createWorkoutSession(any(CreateWorkoutRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/workouts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(VALID_WORKOUT_ID))
                .andExpect(jsonPath("$.name").value(WORKOUT_NAME))
                .andExpect(jsonPath("$.status").value("PLANNED"));
            
            verify(workoutSessionService).createWorkoutSession(any(CreateWorkoutRequest.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // Arrange - Create invalid request (missing required fields)
            CreateWorkoutRequest request = new CreateWorkoutRequest();
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/workouts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
            
            // BEST PRACTICE: Ensure service was never called when validation fails
            verifyNoInteractions(workoutSessionService);
        }
    }
    
    // ==================== GET WORKOUT TESTS ====================
    
    @Nested
    @DisplayName("GET /api/v1/workouts - Get Workouts")
    class GetWorkoutTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should return workout by ID")
        void shouldReturnWorkoutById() throws Exception {
            // Arrange
            WorkoutResponse response = new WorkoutResponse();
            response.setSessionId(VALID_WORKOUT_ID);
            response.setName(WORKOUT_NAME);
            
            when(workoutSessionService.getWorkoutSessionById(VALID_WORKOUT_ID)).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workouts/" + VALID_WORKOUT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(VALID_WORKOUT_ID))
                .andExpect(jsonPath("$.name").value(WORKOUT_NAME));
            
            verify(workoutSessionService).getWorkoutSessionById(VALID_WORKOUT_ID);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when workout not found")
        void shouldReturn404WhenWorkoutNotFound() throws Exception {
            // Arrange
            when(workoutSessionService.getWorkoutSessionById(NON_EXISTENT_ID))
                .thenThrow(new ResourceNotFoundException("Workout session", "ID", NON_EXISTENT_ID));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workouts/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return paginated workouts")
        void shouldReturnPaginatedWorkouts() throws Exception {
            // Arrange
            WorkoutResponse workout1 = new WorkoutResponse();
            workout1.setSessionId(1L);
            workout1.setName("Workout 1");
            
            WorkoutResponse workout2 = new WorkoutResponse();
            workout2.setSessionId(2L);
            workout2.setName("Workout 2");
            
            PagedResponse<WorkoutResponse> pagedResponse = new PagedResponse<>(
                List.of(workout1, workout2),
                0, 20, 2, 1
            );
            
            when(workoutSessionService.getAllWorkoutSessions(any(Pageable.class)))
                .thenReturn(pagedResponse);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workouts")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Workout 1"))
                .andExpect(jsonPath("$.content[1].name").value("Workout 2"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
            
            verify(workoutSessionService).getAllWorkoutSessions(any(Pageable.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return workouts by user")
        void shouldReturnWorkoutsByUser() throws Exception {
            // Arrange
            WorkoutResponse response = new WorkoutResponse();
            response.setSessionId(VALID_WORKOUT_ID);
            response.setUserId(VALID_USER_ID);
            
            when(workoutSessionService.getWorkoutSessionsByUserId(VALID_USER_ID))
                .thenReturn(List.of(response));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workouts/user/" + VALID_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(VALID_USER_ID));
            
            verify(workoutSessionService).getWorkoutSessionsByUserId(VALID_USER_ID);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return workout with smart loading")
        void shouldReturnWorkoutWithSmartLoading() throws Exception {
            // Arrange
            WorkoutResponse response = new WorkoutResponse();
            response.setSessionId(VALID_WORKOUT_ID);
            response.setName("Smart Loaded Workout");
            response.setWorkoutExercises(List.of()); // Initialize to prevent NPE in controller logging
            
            when(workoutSessionService.getWorkoutSessionWithSmartLoading(VALID_WORKOUT_ID))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workouts/" + VALID_WORKOUT_ID + "/smart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(VALID_WORKOUT_ID))
                .andExpect(jsonPath("$.name").value("Smart Loaded Workout"));
            
            verify(workoutSessionService).getWorkoutSessionWithSmartLoading(VALID_WORKOUT_ID);
        }
    }
    
    // ==================== UPDATE WORKOUT TESTS ====================
    
    @Nested
    @DisplayName("PUT /api/v1/workouts/{id} - Update Workout")
    class UpdateWorkoutTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should update workout")
        void shouldUpdateWorkout() throws Exception {
            // Arrange
            UpdateWorkoutRequest request = new UpdateWorkoutRequest();
            request.setName(UPDATED_WORKOUT_NAME);
            
            WorkoutResponse response = new WorkoutResponse();
            response.setSessionId(VALID_WORKOUT_ID);
            response.setName(UPDATED_WORKOUT_NAME);
            
            when(workoutSessionService.updateWorkoutSession(eq(VALID_WORKOUT_ID), any(UpdateWorkoutRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workouts/" + VALID_WORKOUT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(UPDATED_WORKOUT_NAME));
            
            verify(workoutSessionService).updateWorkoutSession(eq(VALID_WORKOUT_ID), any(UpdateWorkoutRequest.class));
        }
    }
    
    // ==================== WORKOUT STATUS TESTS ====================
    
    @Nested
    @DisplayName("PATCH /api/v1/workouts/{id}/status - Workout Actions")
    class WorkoutActionTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should update workout status")
        void shouldUpdateWorkoutStatus() throws Exception {
            // Arrange
            WorkoutActionRequest actionRequest = new WorkoutActionRequest();
            actionRequest.setAction("complete");
            
            WorkoutResponse response = new WorkoutResponse();
            response.setSessionId(VALID_WORKOUT_ID);
            response.setStatus(WorkoutStatus.COMPLETED);
            
            when(workoutSessionService.performAction(VALID_WORKOUT_ID, "complete")).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(patch("/api/v1/workouts/" + VALID_WORKOUT_ID + "/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(actionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
            
            verify(workoutSessionService).performAction(VALID_WORKOUT_ID, "complete");
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 409 on optimistic lock conflict")
        void shouldReturn409OnOptimisticLockConflict() throws Exception {
            // Arrange
            WorkoutActionRequest actionRequest = new WorkoutActionRequest();
            actionRequest.setAction("complete");
            
            when(workoutSessionService.performAction(VALID_WORKOUT_ID, "complete"))
                .thenThrow(new OptimisticLockConflictException("Version conflict"));
            
            // Act & Assert
            mockMvc.perform(patch("/api/v1/workouts/" + VALID_WORKOUT_ID + "/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(actionRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
        }
    }
    
    // ==================== DELETE WORKOUT TESTS ====================
    
    @Nested
    @DisplayName("DELETE /api/v1/workouts/{id} - Delete Workout")
    class DeleteWorkoutTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should delete workout")
        void shouldDeleteWorkout() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/workouts/" + VALID_WORKOUT_ID))
                .andExpect(status().isNoContent());
            
            verify(workoutSessionService).deleteWorkoutSession(VALID_WORKOUT_ID);
        }
    }
    
    // ==================== WORKOUT EXERCISE TESTS ====================
    
    @Nested
    @DisplayName("Workout Exercise Management")
    class WorkoutExerciseTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should add exercise to workout")
        void shouldAddExerciseToWorkout() throws Exception {
            // Arrange
            CreateWorkoutExerciseRequest request = TestDataBuilder.createWorkoutExerciseRequest(1L);
            WorkoutExerciseResponse response = new WorkoutExerciseResponse();
            response.setWorkoutExerciseId(1L);
            response.setExerciseId(1L);
            
            when(workoutSessionService.addExerciseToWorkout(eq(VALID_WORKOUT_ID), any(CreateWorkoutExerciseRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/workouts/" + VALID_WORKOUT_ID + "/exercises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workoutExerciseId").value(1))
                .andExpect(jsonPath("$.exerciseId").value(1));
            
            verify(workoutSessionService).addExerciseToWorkout(eq(VALID_WORKOUT_ID), any(CreateWorkoutExerciseRequest.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should get workout exercises")
        void shouldGetWorkoutExercises() throws Exception {
            // Arrange
            WorkoutExerciseResponse exercise1 = new WorkoutExerciseResponse();
            exercise1.setWorkoutExerciseId(1L);
            exercise1.setExerciseName("Bench Press");
            
            when(workoutSessionService.getWorkoutExercises(VALID_WORKOUT_ID))
                .thenReturn(List.of(exercise1));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workouts/" + VALID_WORKOUT_ID + "/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].exerciseName").value("Bench Press"));
            
            verify(workoutSessionService).getWorkoutExercises(VALID_WORKOUT_ID);
        }
    }
}
