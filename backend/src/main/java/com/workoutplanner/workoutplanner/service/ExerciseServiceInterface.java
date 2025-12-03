package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Exercise entity operations.
 * Provides read-only access to the exercise library.
 *
 * Note: Exercise creation, modification, and deletion are not available to users.
 * The exercise library is pre-populated by administrators.
 */
public interface ExerciseServiceInterface {

    /**
     * Get exercise by ID.
     *
     * @param exerciseId the exercise ID
     * @return the exercise response
     * @throws IllegalArgumentException if exercise not found
     */
    ExerciseResponse getExerciseById(Long exerciseId);

    /**
     * Get all exercises.
     *
     * @return list of all exercise responses
     */
    List<ExerciseResponse> getAllExercises();

    /**
     * Get all exercises with pagination.
     *
     * @param pageable pagination information
     * @return paginated exercise responses
     */
    PagedResponse<ExerciseResponse> getAllExercises(Pageable pageable);

    /**
     * Get exercises by type.
     *
     * @param type the exercise type
     * @return list of exercises of the specified type
     */
    List<ExerciseResponse> getExercisesByType(ExerciseType type);

    /**
     * Get exercises by target muscle group.
     *
     * @param targetMuscleGroup the target muscle group
     * @return list of exercises targeting the specified muscle group
     */
    List<ExerciseResponse> getExercisesByTargetMuscleGroup(TargetMuscleGroup targetMuscleGroup);

    /**
     * Get exercises by difficulty level.
     *
     * @param difficultyLevel the difficulty level
     * @return list of exercises of the specified difficulty
     */
    List<ExerciseResponse> getExercisesByDifficultyLevel(DifficultyLevel difficultyLevel);

    /**
     * Get exercises by type and target muscle group.
     *
     * @param type the exercise type
     * @param targetMuscleGroup the target muscle group
     * @return list of exercises matching both criteria
     */
    List<ExerciseResponse> getExercisesByTypeAndTargetMuscleGroup(ExerciseType type, TargetMuscleGroup targetMuscleGroup);

    /**
     * Search exercises by name.
     *
     * @param name the name to search for
     * @return list of exercises with names containing the search term
     */
    List<ExerciseResponse> searchExercisesByName(String name);

    /**
     * Get exercises by multiple criteria.
     *
     * @param type the exercise type (optional)
     * @param targetMuscleGroup the target muscle group (optional)
     * @param difficultyLevel the difficulty level (optional)
     * @return list of exercises matching the criteria
     */
    List<ExerciseResponse> getExercisesByCriteria(ExerciseType type,
                                                  TargetMuscleGroup targetMuscleGroup,
                                                  DifficultyLevel difficultyLevel);
}
