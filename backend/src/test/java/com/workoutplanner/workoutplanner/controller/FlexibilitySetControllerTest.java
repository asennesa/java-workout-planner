package com.workoutplanner.workoutplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.service.FlexibilitySetService;
import com.workoutplanner.workoutplanner.service.ResourceSecurityService;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import com.workoutplanner.workoutplanner.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FlexibilitySetController using MockMvc.
 * 
 * Best Practices:
 * - Use @WebMvcTest for controller layer testing
 * - Mock service layer with @MockitoBean
 * - Verify all service interactions with verify()
 * - Test validation, authentication, and error scenarios
 */
@WebMvcTest(FlexibilitySetController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("FlexibilitySetController Unit Tests")
class FlexibilitySetControllerTest {
    
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
    private FlexibilitySetService flexibilitySetService;

    @MockitoBean(name = "resourceSecurityService")
    private ResourceSecurityService resourceSecurityService;

    @BeforeEach
    void setUp() {
        // Configure ResourceSecurityService to allow all access in tests
        when(resourceSecurityService.canAccessWorkoutExercise(anyLong())).thenReturn(true);
        when(resourceSecurityService.canAccessSet(anyLong())).thenReturn(true);
    }

    @Nested
    @DisplayName("POST /api/v1/workout-exercises/{workoutExerciseId}/flexibility-sets")
    class CreateSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should create flexibility set and return 201")
        void shouldCreateFlexibilitySetAndReturn201() throws Exception {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            SetResponse response = new SetResponse();
            response.setSetId(VALID_SET_ID);
            response.setSetNumber(VALID_SET_NUMBER);
            
            when(flexibilitySetService.createSet(eq(VALID_WORKOUT_EXERCISE_ID), any(CreateFlexibilitySetRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.setId").value(VALID_SET_ID))
                .andExpect(jsonPath("$.setNumber").value(VALID_SET_NUMBER));
            
            verify(flexibilitySetService).createSet(eq(VALID_WORKOUT_EXERCISE_ID), any(CreateFlexibilitySetRequest.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 400 for invalid request with validation errors")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // Arrange - Invalid request (missing required fields)
            CreateFlexibilitySetRequest request = new CreateFlexibilitySetRequest();
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
            
            // BEST PRACTICE: Ensure service was never called when validation fails
            verifyNoInteractions(flexibilitySetService);
        }
    }
    
    @Nested
    @DisplayName("GET /api/v1/workout-exercises/{workoutExerciseId}/flexibility-sets")
    class GetSetsTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should get all flexibility sets for workout exercise")
        void shouldGetAllFlexibilitySetsForWorkoutExercise() throws Exception {
            // Arrange
            SetResponse set1 = new SetResponse();
            set1.setSetId(VALID_SET_ID);
            set1.setSetNumber(VALID_SET_NUMBER);
            
            when(flexibilitySetService.getSetsByWorkoutExercise(VALID_WORKOUT_EXERCISE_ID))
                .thenReturn(List.of(set1));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].setId").value(VALID_SET_ID))
                .andExpect(jsonPath("$[0].setNumber").value(VALID_SET_NUMBER));
            
            verify(flexibilitySetService).getSetsByWorkoutExercise(VALID_WORKOUT_EXERCISE_ID);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should get flexibility set by ID")
        void shouldGetFlexibilitySetById() throws Exception {
            // Arrange
            SetResponse response = new SetResponse();
            response.setSetId(VALID_SET_ID);
            response.setSetNumber(VALID_SET_NUMBER);
            
            when(flexibilitySetService.getSetById(VALID_SET_ID)).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets/" + VALID_SET_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setId").value(VALID_SET_ID));
            
            verify(flexibilitySetService).getSetById(VALID_SET_ID);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when set not found")
        void shouldReturn404WhenSetNotFound() throws Exception {
            // Arrange
            when(flexibilitySetService.getSetById(NON_EXISTENT_ID))
                .thenThrow(new ResourceNotFoundException("Flexibility set", "ID", NON_EXISTENT_ID));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("PUT /api/v1/workout-exercises/{workoutExerciseId}/flexibility-sets/{setId}")
    class UpdateSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should update flexibility set successfully")
        void shouldUpdateFlexibilitySetSuccessfully() throws Exception {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            SetResponse response = new SetResponse();
            response.setSetId(VALID_SET_ID);
            response.setSetNumber(VALID_SET_NUMBER);
            
            when(flexibilitySetService.updateSet(eq(VALID_SET_ID), any(CreateFlexibilitySetRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets/" + VALID_SET_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setId").value(VALID_SET_ID));
            
            verify(flexibilitySetService).updateSet(eq(VALID_SET_ID), any(CreateFlexibilitySetRequest.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 400 when updating with invalid data")
        void shouldReturn400WhenUpdatingWithInvalidData() throws Exception {
            // Arrange - Invalid request (missing required fields)
            CreateFlexibilitySetRequest request = new CreateFlexibilitySetRequest();
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets/" + VALID_SET_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
            
            // BEST PRACTICE: Ensure service was never called when validation fails
            verifyNoInteractions(flexibilitySetService);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when updating non-existent set")
        void shouldReturn404WhenUpdatingNonExistentSet() throws Exception {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            
            when(flexibilitySetService.updateSet(eq(NON_EXISTENT_ID), any(CreateFlexibilitySetRequest.class)))
                .thenThrow(new ResourceNotFoundException("Flexibility set", "ID", NON_EXISTENT_ID));
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets/" + NON_EXISTENT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("DELETE /api/v1/workout-exercises/{workoutExerciseId}/flexibility-sets/{setId}")
    class DeleteSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should delete flexibility set successfully")
        void shouldDeleteFlexibilitySetSuccessfully() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets/" + VALID_SET_ID))
                .andExpect(status().isNoContent());
            
            verify(flexibilitySetService).deleteSet(VALID_SET_ID);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when deleting non-existent set")
        void shouldReturn404WhenDeletingNonExistentSet() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Flexibility set", "ID", NON_EXISTENT_ID))
                .when(flexibilitySetService).deleteSet(NON_EXISTENT_ID);
            
            // Act & Assert
            mockMvc.perform(delete("/api/v1/workout-exercises/" + VALID_WORKOUT_EXERCISE_ID + "/flexibility-sets/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
        }
    }
}
