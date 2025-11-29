package com.workoutplanner.workoutplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutplanner.workoutplanner.dto.request.CreateExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.service.ExerciseService;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ExerciseController using MockMvc.
 * 
 * Industry Best Practices:
 * - Use @WebMvcTest for controller layer testing
 * - Mock service layer
 * - Test all CRUD operations
 * - Test query parameters and filtering
 * - Verify authentication requirements
 */
@WebMvcTest(ExerciseController.class)
@ActiveProfiles("test")
@Import(com.workoutplanner.workoutplanner.config.TestSecurityConfig.class)
@DisplayName("ExerciseController Unit Tests")
class ExerciseControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private ExerciseService exerciseService;
    
    // ==================== CREATE EXERCISE TESTS ====================

    @Nested
    @DisplayName("POST /api/v1/exercises - Create Exercise")
    class CreateExerciseTests {

        @Test
        @WithMockUser(authorities = {"write:exercises"})
        @DisplayName("Should create exercise and return 201")
        void shouldCreateExerciseAndReturn201() throws Exception {
            // Arrange
            CreateExerciseRequest request = TestDataBuilder.createExerciseRequest();
            
            ExerciseResponse response = new ExerciseResponse();
            response.setExerciseId(1L);
            response.setName("Test Exercise");
            response.setType(ExerciseType.STRENGTH);
            response.setTargetMuscleGroup(TargetMuscleGroup.CHEST);
            response.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
            
            when(exerciseService.createExercise(any(CreateExerciseRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/exercises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseId").value(1))
                .andExpect(jsonPath("$.name").value("Test Exercise"))
                .andExpect(jsonPath("$.type").value("STRENGTH"));
            
            verify(exerciseService).createExercise(any(CreateExerciseRequest.class));
        }
        
        @Test
        @WithMockUser(authorities = {"write:exercises"})
        @DisplayName("Should return 400 for invalid request with validation errors")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // Arrange - Missing required fields
            CreateExerciseRequest request = new CreateExerciseRequest();

            // Act & Assert
            mockMvc.perform(post("/api/v1/exercises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());

            // BEST PRACTICE: Ensure service was never called when validation fails
            verifyNoInteractions(exerciseService);
        }
    }
    
    // ==================== GET EXERCISE TESTS ====================

    @Nested
    @DisplayName("GET /api/v1/exercises - Get Exercises")
    class GetExerciseTests {

        @Test
        @WithMockUser(authorities = {"read:exercises"})
        @DisplayName("Should get exercise by ID")
        void shouldGetExerciseById() throws Exception {
            // Arrange
            ExerciseResponse response = new ExerciseResponse();
            response.setExerciseId(1L);
            response.setName("Bench Press");
            response.setType(ExerciseType.STRENGTH);
            
            when(exerciseService.getExerciseById(1L)).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseId").value(1))
                .andExpect(jsonPath("$.name").value("Bench Press"));
            
            verify(exerciseService).getExerciseById(1L);
        }
        
        @Test
        @WithMockUser(authorities = {"read:exercises"})
        @DisplayName("Should return 404 when exercise not found")
        void shouldReturn404WhenExerciseNotFound() throws Exception {
            // Arrange
            when(exerciseService.getExerciseById(999L))
                .thenThrow(new ResourceNotFoundException("Exercise", "ID", 999L));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/exercises/999"))
                .andExpect(status().isNotFound());
        }
        
        @Test
        @WithMockUser(authorities = {"read:exercises"})
        @DisplayName("Should get all exercises with pagination")
        void shouldGetAllExercisesWithPagination() throws Exception {
            // Arrange
            ExerciseResponse exercise1 = new ExerciseResponse();
            exercise1.setExerciseId(1L);
            exercise1.setName("Exercise 1");
            
            ExerciseResponse exercise2 = new ExerciseResponse();
            exercise2.setExerciseId(2L);
            exercise2.setName("Exercise 2");
            
            PagedResponse<ExerciseResponse> pagedResponse = new PagedResponse<>(
                List.of(exercise1, exercise2),
                0, 20, 2, 1
            );
            
            when(exerciseService.getAllExercises(any(Pageable.class)))
                .thenReturn(pagedResponse);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/exercises")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
            
            verify(exerciseService).getAllExercises(any(Pageable.class));
        }
        
        @Test
        @WithMockUser(authorities = {"read:exercises"})
        @DisplayName("Should get exercises by type")
        void shouldGetExercisesByType() throws Exception {
            // Arrange
            ExerciseResponse response = new ExerciseResponse();
            response.setExerciseId(1L);
            response.setType(ExerciseType.STRENGTH);
            
            when(exerciseService.getExercisesByCriteria(eq(ExerciseType.STRENGTH), isNull(), isNull()))
                .thenReturn(List.of(response));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/exercises/filter")
                    .param("type", "STRENGTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("STRENGTH"));
            
            verify(exerciseService).getExercisesByCriteria(eq(ExerciseType.STRENGTH), isNull(), isNull());
        }
        
        @Test
        @WithMockUser(authorities = {"read:exercises"})
        @DisplayName("Should search exercises by name")
        void shouldSearchExercisesByName() throws Exception {
            // Arrange
            ExerciseResponse response = new ExerciseResponse();
            response.setExerciseId(1L);
            response.setName("Bench Press");
            
            when(exerciseService.searchExercisesByName("bench"))
                .thenReturn(List.of(response));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/exercises/search")
                    .param("name", "bench"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Bench Press"));
            
            verify(exerciseService).searchExercisesByName("bench");
        }
    }
    
    // ==================== UPDATE EXERCISE TESTS ====================

    @Nested
    @DisplayName("PUT /api/v1/exercises/{id} - Update Exercise")
    class UpdateExerciseTests {

        @Test
        @WithMockUser(authorities = {"write:exercises"})
        @DisplayName("Should update exercise successfully")
        void shouldUpdateExerciseSuccessfully() throws Exception {
            // Arrange
            CreateExerciseRequest request = TestDataBuilder.createExerciseRequest();
            request.setName("Updated Exercise");
            
            ExerciseResponse response = new ExerciseResponse();
            response.setExerciseId(1L);
            response.setName("Updated Exercise");
            
            when(exerciseService.updateExercise(eq(1L), any(CreateExerciseRequest.class)))
                .thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/exercises/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Exercise"));
            
            verify(exerciseService).updateExercise(eq(1L), any(CreateExerciseRequest.class));
        }
        
        @Test
        @WithMockUser(authorities = {"write:exercises"})
        @DisplayName("Should return 404 when updating non-existent exercise")
        void shouldReturn404WhenUpdatingNonExistentExercise() throws Exception {
            // Arrange
            CreateExerciseRequest request = TestDataBuilder.createExerciseRequest();
            
            when(exerciseService.updateExercise(eq(999L), any(CreateExerciseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Exercise", "ID", 999L));
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/exercises/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
        
        @Test
        @WithMockUser(authorities = {"write:exercises"})
        @DisplayName("Should return 400 when updating with invalid data")
        void shouldReturn400WhenUpdatingWithInvalidData() throws Exception {
            // Arrange - Invalid request (missing required fields)
            CreateExerciseRequest request = new CreateExerciseRequest();
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/exercises/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
            
            // BEST PRACTICE: Ensure service was never called when validation fails
            verifyNoInteractions(exerciseService);
        }
    }
    
    // ==================== DELETE EXERCISE TESTS ====================

    @Nested
    @DisplayName("DELETE /api/v1/exercises/{id} - Delete Exercise")
    class DeleteExerciseTests {

        @Test
        @WithMockUser(authorities = {"delete:exercises"})
        @DisplayName("Should delete exercise successfully")
        void shouldDeleteExerciseSuccessfully() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/exercises/1"))
                .andExpect(status().isNoContent());
            
            verify(exerciseService).deleteExercise(1L);
        }
        
        @Test
        @WithMockUser(authorities = {"delete:exercises"})
        @DisplayName("Should return 404 when deleting non-existent exercise")
        void shouldReturn404WhenDeletingNonExistentExercise() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("Exercise", "ID", 999L))
                .when(exerciseService).deleteExercise(999L);
            
            // Act & Assert
            mockMvc.perform(delete("/api/v1/exercises/999"))
                .andExpect(status().isNotFound());
        }
    }
    
    // ==================== FILTERING TESTS ====================

    @Nested
    @DisplayName("Exercise Filtering Tests")
    class FilteringTests {

        @Test
        @WithMockUser(authorities = {"read:exercises"})
        @DisplayName("Should filter by difficulty level")
        void shouldFilterByDifficultyLevel() throws Exception {
            // Arrange
            ExerciseResponse response = new ExerciseResponse();
            response.setExerciseId(1L);
            response.setDifficultyLevel(DifficultyLevel.BEGINNER);
            
            when(exerciseService.getExercisesByCriteria(isNull(), isNull(), eq(DifficultyLevel.BEGINNER)))
                .thenReturn(List.of(response));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/exercises/filter")
                    .param("difficultyLevel", "BEGINNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].difficultyLevel").value("BEGINNER"));
            
            verify(exerciseService).getExercisesByCriteria(isNull(), isNull(), eq(DifficultyLevel.BEGINNER));
        }
        
        @Test
        @WithMockUser(authorities = {"read:exercises"})
        @DisplayName("Should filter by target muscle group")
        void shouldFilterByTargetMuscleGroup() throws Exception {
            // Arrange
            ExerciseResponse response = new ExerciseResponse();
            response.setExerciseId(1L);
            response.setTargetMuscleGroup(TargetMuscleGroup.CHEST);
            
            when(exerciseService.getExercisesByCriteria(isNull(), eq(TargetMuscleGroup.CHEST), isNull()))
                .thenReturn(List.of(response));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/exercises/filter")
                    .param("targetMuscleGroup", "CHEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].targetMuscleGroup").value("CHEST"));
            
            verify(exerciseService).getExercisesByCriteria(isNull(), eq(TargetMuscleGroup.CHEST), isNull());
        }
    }
}

