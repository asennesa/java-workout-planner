package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.ExerciseMapper;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExerciseService.
 * Tests read-only business logic for exercise library operations.
 *
 * Note: Exercise library is read-only for users. Create, update, and delete
 * operations are not available.
 *
 * @see <a href="https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html">Spring Boot Testing</a>
 * @see <a href="https://site.mockito.org/">Mockito Documentation</a>
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
    private Exercise cardioExercise;
    private ExerciseResponse testResponse;
    private ExerciseResponse cardioResponse;

    @BeforeEach
    void setUp() {
        // Strength exercise
        testExercise = TestDataBuilder.createStrengthExercise();
        testResponse = new ExerciseResponse();
        testResponse.setExerciseId(1L);
        testResponse.setName("Bench Press");
        testResponse.setType(ExerciseType.STRENGTH);
        testResponse.setTargetMuscleGroup(TargetMuscleGroup.CHEST);
        testResponse.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);

        // Cardio exercise
        cardioExercise = TestDataBuilder.createCardioExercise();
        cardioResponse = new ExerciseResponse();
        cardioResponse.setExerciseId(2L);
        cardioResponse.setName("Running");
        cardioResponse.setType(ExerciseType.CARDIO);
        cardioResponse.setTargetMuscleGroup(TargetMuscleGroup.FULL_BODY);
        cardioResponse.setDifficultyLevel(DifficultyLevel.BEGINNER);
    }

    // ==================== GET BY ID TESTS ====================

    @Nested
    @DisplayName("Get Exercise By ID Tests")
    class GetExerciseByIdTests {

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
            assertThat(result.getName()).isEqualTo("Bench Press");
            assertThat(result.getType()).isEqualTo(ExerciseType.STRENGTH);
            verify(exerciseRepository).findById(1L);
            verify(exerciseMapper).toResponse(testExercise);
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

            verify(exerciseRepository).findById(999L);
            verify(exerciseMapper, never()).toResponse(any());
        }
    }

    // ==================== GET ALL EXERCISES (PAGINATED) TESTS ====================

    @Nested
    @DisplayName("Get All Exercises (Paginated) Tests")
    class GetAllExercisesTests {

        @Test
        @DisplayName("Should get all exercises with pagination")
        void shouldGetAllExercisesWithPagination() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<Exercise> exercises = List.of(testExercise, cardioExercise);
            Page<Exercise> page = new PageImpl<>(exercises, pageable, 2);
            List<ExerciseResponse> responses = List.of(testResponse, cardioResponse);

            when(exerciseRepository.findAll(pageable)).thenReturn(page);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);

            // Act
            PagedResponse<ExerciseResponse> result = exerciseService.getAllExercises(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPageNumber()).isZero();
            assertThat(result.getTotalPages()).isEqualTo(1);
            verify(exerciseRepository).findAll(pageable);
            verify(exerciseMapper).toResponseList(exercises);
        }

        @Test
        @DisplayName("Should return empty page when no exercises exist")
        void shouldReturnEmptyPageWhenNoExercisesExist() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Exercise> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(exerciseRepository.findAll(pageable)).thenReturn(emptyPage);
            when(exerciseMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            // Act
            PagedResponse<ExerciseResponse> result = exerciseService.getAllExercises(pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
        }

        @Test
        @DisplayName("Should respect page size parameter")
        void shouldRespectPageSizeParameter() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 5);
            List<Exercise> exercises = List.of(testExercise);
            Page<Exercise> page = new PageImpl<>(exercises, pageable, 10); // 10 total, page size 5

            when(exerciseRepository.findAll(pageable)).thenReturn(page);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(List.of(testResponse));

            // Act
            PagedResponse<ExerciseResponse> result = exerciseService.getAllExercises(pageable);

            // Assert
            assertThat(result.getPageSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return correct page number")
        void shouldReturnCorrectPageNumber() {
            // Arrange
            Pageable pageable = PageRequest.of(2, 5); // Page 2 (0-indexed)
            List<Exercise> exercises = List.of(testExercise);
            Page<Exercise> page = new PageImpl<>(exercises, pageable, 15);

            when(exerciseRepository.findAll(pageable)).thenReturn(page);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(List.of(testResponse));

            // Act
            PagedResponse<ExerciseResponse> result = exerciseService.getAllExercises(pageable);

            // Assert
            assertThat(result.getPageNumber()).isEqualTo(2);
        }
    }

    // ==================== SEARCH EXERCISES BY NAME TESTS ====================

    @Nested
    @DisplayName("Search Exercises By Name Tests")
    class SearchExercisesByNameTests {

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
            assertThat(result.get(0).getName()).isEqualTo("Bench Press");
            verify(exerciseRepository).findByNameContainingIgnoreCase("bench");
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void shouldReturnEmptyListWhenNoMatchesFound() {
            // Arrange
            when(exerciseRepository.findByNameContainingIgnoreCase("nonexistent"))
                .thenReturn(Collections.emptyList());
            when(exerciseMapper.toResponseList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

            // Act
            List<ExerciseResponse> result = exerciseService.searchExercisesByName("nonexistent");

            // Assert
            assertThat(result).isEmpty();
            verify(exerciseRepository).findByNameContainingIgnoreCase("nonexistent");
        }

        @Test
        @DisplayName("Should search case insensitively")
        void shouldSearchCaseInsensitively() {
            // Arrange
            List<Exercise> exercises = List.of(testExercise);
            List<ExerciseResponse> responses = List.of(testResponse);

            when(exerciseRepository.findByNameContainingIgnoreCase("BENCH")).thenReturn(exercises);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);

            // Act
            List<ExerciseResponse> result = exerciseService.searchExercisesByName("BENCH");

            // Assert
            assertThat(result).hasSize(1);
            verify(exerciseRepository).findByNameContainingIgnoreCase("BENCH");
        }

        @Test
        @DisplayName("Should trim whitespace from search term")
        void shouldTrimWhitespaceFromSearchTerm() {
            // Arrange
            List<Exercise> exercises = List.of(testExercise);
            List<ExerciseResponse> responses = List.of(testResponse);

            when(exerciseRepository.findByNameContainingIgnoreCase("bench")).thenReturn(exercises);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);

            // Act
            List<ExerciseResponse> result = exerciseService.searchExercisesByName("  bench  ");

            // Assert
            assertThat(result).hasSize(1);
            verify(exerciseRepository).findByNameContainingIgnoreCase("bench");
        }
    }

    // ==================== FILTER EXERCISES BY CRITERIA TESTS ====================

    @Nested
    @DisplayName("Filter Exercises By Criteria Tests")
    class FilterExercisesByCriteriaTests {

        @Test
        @DisplayName("Should filter by exercise type")
        void shouldFilterByExerciseType() {
            // Arrange
            List<Exercise> exercises = List.of(testExercise);
            List<ExerciseResponse> responses = List.of(testResponse);

            when(exerciseRepository.findByFilters(ExerciseType.STRENGTH, null, null))
                .thenReturn(exercises);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);

            // Act
            List<ExerciseResponse> result = exerciseService.getExercisesByCriteria(
                ExerciseType.STRENGTH, null, null);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(ExerciseType.STRENGTH);
            verify(exerciseRepository).findByFilters(ExerciseType.STRENGTH, null, null);
        }

        @Test
        @DisplayName("Should filter by target muscle group")
        void shouldFilterByTargetMuscleGroup() {
            // Arrange
            List<Exercise> exercises = List.of(testExercise);
            List<ExerciseResponse> responses = List.of(testResponse);

            when(exerciseRepository.findByFilters(null, TargetMuscleGroup.CHEST, null))
                .thenReturn(exercises);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);

            // Act
            List<ExerciseResponse> result = exerciseService.getExercisesByCriteria(
                null, TargetMuscleGroup.CHEST, null);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTargetMuscleGroup()).isEqualTo(TargetMuscleGroup.CHEST);
        }

        @Test
        @DisplayName("Should filter by difficulty level")
        void shouldFilterByDifficultyLevel() {
            // Arrange
            List<Exercise> exercises = List.of(cardioExercise);
            List<ExerciseResponse> responses = List.of(cardioResponse);

            when(exerciseRepository.findByFilters(null, null, DifficultyLevel.BEGINNER))
                .thenReturn(exercises);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);

            // Act
            List<ExerciseResponse> result = exerciseService.getExercisesByCriteria(
                null, null, DifficultyLevel.BEGINNER);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDifficultyLevel()).isEqualTo(DifficultyLevel.BEGINNER);
        }

        @Test
        @DisplayName("Should filter by multiple criteria")
        void shouldFilterByMultipleCriteria() {
            // Arrange
            List<Exercise> exercises = List.of(testExercise);
            List<ExerciseResponse> responses = List.of(testResponse);

            when(exerciseRepository.findByFilters(
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE))
                .thenReturn(exercises);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);

            // Act
            List<ExerciseResponse> result = exerciseService.getExercisesByCriteria(
                ExerciseType.STRENGTH, TargetMuscleGroup.CHEST, DifficultyLevel.INTERMEDIATE);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Bench Press");
        }

        @Test
        @DisplayName("Should return all exercises when no filters provided")
        void shouldReturnAllExercisesWhenNoFiltersProvided() {
            // Arrange
            List<Exercise> exercises = List.of(testExercise, cardioExercise);
            List<ExerciseResponse> responses = List.of(testResponse, cardioResponse);

            when(exerciseRepository.findByFilters(null, null, null)).thenReturn(exercises);
            when(exerciseMapper.toResponseList(exercises)).thenReturn(responses);

            // Act
            List<ExerciseResponse> result = exerciseService.getExercisesByCriteria(null, null, null);

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no exercises match criteria")
        void shouldReturnEmptyListWhenNoExercisesMatchCriteria() {
            // Arrange
            when(exerciseRepository.findByFilters(ExerciseType.CARDIO, TargetMuscleGroup.CHEST, null))
                .thenReturn(Collections.emptyList());
            when(exerciseMapper.toResponseList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

            // Act
            List<ExerciseResponse> result = exerciseService.getExercisesByCriteria(
                ExerciseType.CARDIO, TargetMuscleGroup.CHEST, null);

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
