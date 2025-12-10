package com.workoutplanner.workoutplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.service.CardioSetService;
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
 * Unit tests for CardioSetController using MockMvc.
 * 
 * Industry Best Practices:
 * - Test controller layer in isolation
 * - Mock service layer
 * - Verify HTTP status codes and JSON responses
 * - Test validation and security
 */
@WebMvcTest(CardioSetController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("CardioSetController Unit Tests")
class CardioSetControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private CardioSetService cardioSetService;

    @MockitoBean(name = "resourceSecurityService")
    private ResourceSecurityService resourceSecurityService;

    @BeforeEach
    void setUp() {
        // Configure ResourceSecurityService to allow all access in tests
        when(resourceSecurityService.canAccessWorkoutExercise(anyLong())).thenReturn(true);
        when(resourceSecurityService.canAccessSet(anyLong())).thenReturn(true);
    }

    // ==================== CREATE SET TESTS ====================
    
    @Nested
    @DisplayName("POST /api/v1/workout-exercises/{workoutExerciseId}/cardio-sets")
    class CreateSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should create cardio set and return 201")
        void shouldCreateCardioSetAndReturn201() throws Exception {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            SetResponse response = new SetResponse();
            response.setSetId(1L);
            response.setSetNumber(1);
            
            when(cardioSetService.createSet(eq(1L), any(CreateCardioSetRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/workout-exercises/1/cardio-sets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.setId").value(1))
                .andExpect(jsonPath("$.setNumber").value(1));
            
            verify(cardioSetService).createSet(eq(1L), any(CreateCardioSetRequest.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 400 for invalid request with validation errors")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // Arrange - Invalid request (missing required fields)
            CreateCardioSetRequest request = new CreateCardioSetRequest();
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/workout-exercises/1/cardio-sets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
            
            // BEST PRACTICE: Ensure service was never called when validation fails
            verifyNoInteractions(cardioSetService);
        }
    }
    
    // ==================== GET SET TESTS ====================
    
    @Nested
    @DisplayName("GET /api/v1/workout-exercises/{workoutExerciseId}/cardio-sets")
    class GetSetsTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should get all cardio sets for workout exercise")
        void shouldGetAllCardioSetsForWorkoutExercise() throws Exception {
            // Arrange
            SetResponse set1 = new SetResponse();
            set1.setSetId(1L);
            set1.setSetNumber(1);
            
            SetResponse set2 = new SetResponse();
            set2.setSetId(2L);
            set2.setSetNumber(2);
            
            when(cardioSetService.getSetsByWorkoutExercise(1L))
                .thenReturn(List.of(set1, set2));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/1/cardio-sets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].setId").value(1))
                .andExpect(jsonPath("$[1].setId").value(2));
            
            verify(cardioSetService).getSetsByWorkoutExercise(1L);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should get cardio set by ID")
        void shouldGetCardioSetById() throws Exception {
            // Arrange
            SetResponse response = new SetResponse();
            response.setSetId(1L);
            response.setSetNumber(1);
            
            when(cardioSetService.getSetById(1L)).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/1/cardio-sets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setId").value(1))
                .andExpect(jsonPath("$.setNumber").value(1));
            
            verify(cardioSetService).getSetById(1L);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when set not found")
        void shouldReturn404WhenSetNotFound() throws Exception {
            // Arrange
            when(cardioSetService.getSetById(999L))
                .thenThrow(new ResourceNotFoundException("Cardio set", "ID", 999L));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/workout-exercises/1/cardio-sets/999"))
                .andExpect(status().isNotFound());
        }
    }
    
    // ==================== UPDATE SET TESTS ====================
    
    @Nested
    @DisplayName("PUT /api/v1/workout-exercises/{workoutExerciseId}/cardio-sets/{setId}")
    class UpdateSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should update cardio set successfully")
        void shouldUpdateCardioSetSuccessfully() throws Exception {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            SetResponse response = new SetResponse();
            response.setSetId(1L);
            response.setSetNumber(1);
            
            when(cardioSetService.updateSet(eq(1L), any(CreateCardioSetRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/1/cardio-sets/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setId").value(1));
            
            verify(cardioSetService).updateSet(eq(1L), any(CreateCardioSetRequest.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when updating non-existent set")
        void shouldReturn404WhenUpdatingNonExistentSet() throws Exception {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            
            when(cardioSetService.updateSet(eq(999L), any(CreateCardioSetRequest.class)))
                .thenThrow(new ResourceNotFoundException("Cardio set", "ID", 999L));
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/1/cardio-sets/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 400 when updating with invalid data")
        void shouldReturn400WhenUpdatingWithInvalidData() throws Exception {
            // Arrange - Invalid request (missing required fields)
            CreateCardioSetRequest request = new CreateCardioSetRequest();
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/workout-exercises/1/cardio-sets/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
            
            // BEST PRACTICE: Ensure service was never called when validation fails
            verifyNoInteractions(cardioSetService);
        }
    }
    
    // ==================== DELETE SET TESTS ====================
    
    @Nested
    @DisplayName("DELETE /api/v1/workout-exercises/{workoutExerciseId}/cardio-sets/{setId}")
    class DeleteSetTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should delete cardio set successfully")
        void shouldDeleteCardioSetSuccessfully() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/workout-exercises/1/cardio-sets/1"))
                .andExpect(status().isNoContent());
            
            verify(cardioSetService).deleteSet(1L);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when deleting non-existent set")
        void shouldReturn404WhenDeletingNonExistentSet() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Cardio set", "ID", 999L))
                .when(cardioSetService).deleteSet(999L);
            
            // Act & Assert
            mockMvc.perform(delete("/api/v1/workout-exercises/1/cardio-sets/999"))
                .andExpect(status().isNotFound());
        }
    }
}

