package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.ExerciseMapper;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExerciseService.
 * Tests business logic for exercise management operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseService Unit Tests")
class ExerciseServiceTest {
    
    @Mock
    private ExerciseRepository exerciseRepository;
    
    @Mock
    private ExerciseMapper exerciseMapper;
    
    @InjectMocks
    private ExerciseService exerciseService;
    
    private Exercise testExercise;
    private ExerciseResponse testResponse;
    
    @BeforeEach
    void setUp() {
        testExercise = TestDataBuilder.createStrengthExercise(); // Has ID
        testResponse = new ExerciseResponse();
        testResponse.setExerciseId(1L);
        testResponse.setName("Bench Press");
        testResponse.setType(ExerciseType.STRENGTH);
    }
    
    @Test
    @DisplayName("Should create exercise successfully")
    void shouldCreateExerciseSuccessfully() {
        // Arrange
        CreateExerciseRequest request = TestDataBuilder.createExerciseRequest();
        when(exerciseMapper.toEntity(request)).thenReturn(testExercise);
        when(exerciseRepository.save(testExercise)).thenReturn(testExercise);
        when(exerciseMapper.toResponse(testExercise)).thenReturn(testResponse);
        
        // Act
        ExerciseResponse result = exerciseService.createExercise(request);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Bench Press");
        verify(exerciseRepository).save(testExercise);
    }
    
    @Test
    @DisplayName("Should get exercise by ID successfully")
    void shouldGetExerciseByIdSuccessfully() {
        // Arrange
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(testExercise));
        when(exerciseMapper.toResponse(testExercise)).thenReturn(testResponse);
        
        // Act
        ExerciseResponse result = exerciseService.getExerciseById(1L);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getExerciseId()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when exercise not found")
    void shouldThrowExceptionWhenExerciseNotFound() {
        // Arrange
        when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> exerciseService.getExerciseById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Exercise");
    }
    
    @Test
    @DisplayName("Should get exercises by type successfully")
    void shouldGetExercisesByTypeSuccessfully() {
        // Arrange
        List<Exercise> exercises = List.of(testExercise);
        List<ExerciseResponse> responses = List.of(testResponse);
        
        when(exerciseRepository.findByType(ExerciseType.STRENGTH)).thenReturn(exercises);
        when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);
        
        // Act
        List<ExerciseResponse> result = exerciseService.getExercisesByType(ExerciseType.STRENGTH);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(ExerciseType.STRENGTH);
    }
    
    @Test
    @DisplayName("Should search exercises by name successfully")
    void shouldSearchExercisesByNameSuccessfully() {
        // Arrange
        List<Exercise> exercises = List.of(testExercise);
        List<ExerciseResponse> responses = List.of(testResponse);
        
        when(exerciseRepository.findByNameContainingIgnoreCase("bench")).thenReturn(exercises);
        when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);
        
        // Act
        List<ExerciseResponse> result = exerciseService.searchExercisesByName("bench");
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Bench");
    }
    
    @Test
    @DisplayName("Should update exercise successfully")
    void shouldUpdateExerciseSuccessfully() {
        // Arrange
        CreateExerciseRequest updateRequest = TestDataBuilder.createExerciseRequest();
        updateRequest.setName("Updated Exercise");
        
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(testExercise));
        when(exerciseRepository.save(testExercise)).thenReturn(testExercise);
        when(exerciseMapper.toResponse(testExercise)).thenReturn(testResponse);
        
        // Act
        ExerciseResponse result = exerciseService.updateExercise(1L, updateRequest);
        
        // Assert
        assertThat(result).isNotNull();
        verify(exerciseMapper).updateEntity(eq(updateRequest), eq(testExercise));
        verify(exerciseRepository).save(testExercise);
    }
    
    @Test
    @DisplayName("Should soft delete exercise successfully")
    void shouldSoftDeleteExerciseSuccessfully() {
        // Arrange
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(testExercise));
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(testExercise);
        
        // Act
        exerciseService.deleteExercise(1L);
        
        // Assert
        verify(exerciseRepository).save(argThat(ex -> !ex.isActive()));
    }
    
    @Test
    @DisplayName("Should restore soft deleted exercise successfully")
    void shouldRestoreSoftDeletedExerciseSuccessfully() {
        // Arrange
        testExercise.softDelete();
        when(exerciseRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testExercise));
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(testExercise);
        
        // Act
        exerciseService.restoreExercise(1L);
        
        // Assert
        verify(exerciseRepository).save(argThat(Exercise::isActive));
    }
    
    @Test
    @DisplayName("Should throw exception when restoring active exercise")
    void shouldThrowExceptionWhenRestoringActiveExercise() {
        // Arrange
        when(exerciseRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testExercise));
        
        // Act & Assert
        assertThatThrownBy(() -> exerciseService.restoreExercise(1L))
            .isInstanceOf(BusinessLogicException.class)
            .hasMessageContaining("not deleted");
    }
}

