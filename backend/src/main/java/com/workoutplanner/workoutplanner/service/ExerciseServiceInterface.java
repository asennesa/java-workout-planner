package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Read-only service interface for the exercise library.
 */
public interface ExerciseServiceInterface {

    ExerciseResponse getExerciseById(Long exerciseId);

    PagedResponse<ExerciseResponse> getAllExercises(Pageable pageable);

    List<ExerciseResponse> searchExercisesByName(String name);

    List<ExerciseResponse> getExercisesByCriteria(ExerciseType type,
                                                  TargetMuscleGroup targetMuscleGroup,
                                                  DifficultyLevel difficultyLevel);
}
