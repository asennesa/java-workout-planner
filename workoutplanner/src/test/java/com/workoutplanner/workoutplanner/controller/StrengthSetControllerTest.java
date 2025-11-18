package com.workoutplanner.workoutplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.service.StrengthSetService;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for StrengthSetController using MockMvc.
 * 
 * Best Practices:
 * - Use @WebMvcTest for controller layer testing
 * - Mock service layer with @MockitoBean
 * - Verify all service interactions with verify()
 * - Test validation, authentication, and error scenarios
 */
@WebMvcTest(StrengthSetController.class)
@ActiveProfiles("test")
@DisplayName("StrengthSetController Unit Tests")
class StrengthSetControllerTest {
    
    // Test constants
    private static final Long VALID_WORKOUT_EXERCISE_ID = 1L;
    private static final Long VALID_SET_ID = 1L;
    private static final Long NON_EXISTENT_ID = 999L;
    private static final Integer VALID_SET_NUMBER = 1;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private StrengthSetService strengthSetService;
    
    @Nested
    @DisplayName("POST /api/v1/workout-exercises/{workoutExerciseId}/strength-sets")
    class CreateSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should create strength set and return 201")
        void shouldCreateStrengthSetAndReturn201() throws Exception {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            SetResponse response = new SetResponse();
            response.setSetId(VALID_SET_ID);
            response.setSetNumber(VALID_SET_NUMBER);
            
            when(strengthSetService.createSet(eq(VALID_WORKOUT_EXERCISE_ID), any(CreateStrengthSetRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.setId").value(VALID_SET_ID))
                .andExpect(jsonPath("$.setNumber").value(VALID_SET_NUMBER));
            
            verify(strengthSetService).createSet(eq(VALID_WORKOUT_EXERCISE_ID), any(CreateStrengthSetRequest.class));
        }
    }
    
    @Nested
    @DisplayName("GET /api/v1/workout-exercises/{workoutExerciseId}/strength-sets")
    class GetSetsTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should get all strength sets for workout exercise")
        void shouldGetAllStrengthSetsForWorkoutExercise() throws Exception {
            // Arrange
            SetResponse set1 = new SetResponse();
            set1.setSetId(VALID_SET_ID);
            set1.setSetNumber(VALID_SET_NUMBER);
            
            when(strengthSetService.getSetsByWorkoutExercise(VALID_WORKOUT_EXERCISE_ID))
                .thenReturn(List.of(set1));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].setId").value(VALID_SET_ID))
                .andExpect(jsonPath("$[0].setNumber").value(VALID_SET_NUMBER));
            
            verify(strengthSetService).getSetsByWorkoutExercise(VALID_WORKOUT_EXERCISE_ID);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should get strength set by ID")
        void shouldGetStrengthSetById() throws Exception {
            // Arrange
            SetResponse response = new SetResponse();
            response.setSetId(VALID_SET_ID);
            response.setSetNumber(VALID_SET_NUMBER);
            
            when(strengthSetService.getSetById(VALID_SET_ID)).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets/" + VALID_SET_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setId").value(VALID_SET_ID));
            
            verify(strengthSetService).getSetById(VALID_SET_ID);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when set not found")
        void shouldReturn404WhenSetNotFound() throws Exception {
            // Arrange
            when(strengthSetService.getSetById(NON_EXISTENT_ID))
                .thenThrow(new ResourceNotFoundException("Strength set", "ID", NON_EXISTENT_ID));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("PUT /api/v1/workout-exercises/{workoutExerciseId}/strength-sets/{setId}")
    class UpdateSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should update strength set successfully")
        void shouldUpdateStrengthSetSuccessfully() throws Exception {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            SetResponse response = new SetResponse();
            response.setSetId(VALID_SET_ID);
            response.setSetNumber(VALID_SET_NUMBER);
            
            when(strengthSetService.updateSet(eq(VALID_SET_ID), any(CreateStrengthSetRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets/" + VALID_SET_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setId").value(VALID_SET_ID));
            
            verify(strengthSetService).updateSet(eq(VALID_SET_ID), any(CreateStrengthSetRequest.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // Arrange - Invalid request (missing required fields)
            CreateStrengthSetRequest request = new CreateStrengthSetRequest();
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets/" + VALID_SET_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when updating non-existent set")
        void shouldReturn404WhenUpdatingNonExistentSet() throws Exception {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            
            when(strengthSetService.updateSet(eq(NON_EXISTENT_ID), any(CreateStrengthSetRequest.class)))
                .thenThrow(new ResourceNotFoundException("Strength set", "ID", NON_EXISTENT_ID));
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets/" + NON_EXISTENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("DELETE /api/v1/workout-exercises/{workoutExerciseId}/strength-sets/{setId}")
    class DeleteSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should delete strength set successfully")
        void shouldDeleteStrengthSetSuccessfully() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets/" + VALID_SET_ID))
                .andExpect(status().isNoContent());
            
            verify(strengthSetService).deleteSet(VALID_SET_ID);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when deleting non-existent set")
        void shouldReturn404WhenDeletingNonExistentSet() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Strength set", "ID", NON_EXISTENT_ID))
                .when(strengthSetService).deleteSet(NON_EXISTENT_ID);
            
            // Act & Assert
            mockMvc.perform(delete("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/strength-sets/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
        }
    }
}

