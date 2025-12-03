package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExerciseService.
 * Tests read-only business logic for exercise library operations.
 *
 * Note: Exercise library is read-only for users. Create, update, and delete
 * operations are not available.
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
}
