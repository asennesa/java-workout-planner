package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.mapper.ExerciseMapper;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for Exercise entity operations.
 * Handles business logic for exercise management including creation, retrieval, and filtering.
 */
@Service
@Transactional
public class ExerciseService {
    
    @Autowired
    private ExerciseRepository exerciseRepository;
    
    @Autowired
    private ExerciseMapper exerciseMapper;
    
    /**
     * Create a new exercise.
     * 
     * @param createExerciseRequest the exercise creation request
     * @return the created exercise response
     */
    public ExerciseResponse createExercise(CreateExerciseRequest createExerciseRequest) {
        Exercise exercise = exerciseMapper.toEntity(createExerciseRequest);
        Exercise savedExercise = exerciseRepository.save(exercise);
        return exerciseMapper.toResponse(savedExercise);
    }
    
    /**
     * Get exercise by ID.
     * 
     * @param exerciseId the exercise ID
     * @return the exercise response
     * @throws IllegalArgumentException if exercise not found
     */
    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with ID: " + exerciseId));
        
        return exerciseMapper.toResponse(exercise);
    }
    
    /**
     * Get all exercises.
     * 
     * @return list of all exercise responses
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAllExercises() {
        List<Exercise> exercises = exerciseRepository.findAll();
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by type.
     * 
     * @param type the exercise type
     * @return list of exercises of the specified type
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByType(ExerciseType type) {
        List<Exercise> exercises = exerciseRepository.findByType(type);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by target muscle group.
     * 
     * @param targetMuscleGroup the target muscle group
     * @return list of exercises targeting the specified muscle group
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByTargetMuscleGroup(TargetMuscleGroup targetMuscleGroup) {
        List<Exercise> exercises = exerciseRepository.findByTargetMuscleGroup(targetMuscleGroup);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by difficulty level.
     * 
     * @param difficultyLevel the difficulty level
     * @return list of exercises of the specified difficulty
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByDifficultyLevel(DifficultyLevel difficultyLevel) {
        List<Exercise> exercises = exerciseRepository.findByDifficultyLevel(difficultyLevel);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by type and target muscle group.
     * 
     * @param type the exercise type
     * @param targetMuscleGroup the target muscle group
     * @return list of exercises matching both criteria
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByTypeAndTargetMuscleGroup(ExerciseType type, TargetMuscleGroup targetMuscleGroup) {
        List<Exercise> exercises = exerciseRepository.findByTypeAndTargetMuscleGroup(type, targetMuscleGroup);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Search exercises by name.
     * 
     * @param name the name to search for
     * @return list of exercises with names containing the search term
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> searchExercisesByName(String name) {
        List<Exercise> exercises = exerciseRepository.findByNameContainingIgnoreCase(name);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by multiple criteria.
     * 
     * @param type the exercise type (optional)
     * @param targetMuscleGroup the target muscle group (optional)
     * @param difficultyLevel the difficulty level (optional)
     * @return list of exercises matching the criteria
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByCriteria(ExerciseType type, 
                                                         TargetMuscleGroup targetMuscleGroup, 
                                                         DifficultyLevel difficultyLevel) {
        List<Exercise> exercises = exerciseRepository.findByCriteria(type, targetMuscleGroup, difficultyLevel);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Update exercise.
     * 
     * @param exerciseId the exercise ID
     * @param createExerciseRequest the updated exercise information
     * @return the updated exercise response
     * @throws IllegalArgumentException if exercise not found
     */
    public ExerciseResponse updateExercise(Long exerciseId, CreateExerciseRequest createExerciseRequest) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with ID: " + exerciseId));
        
        exerciseMapper.updateEntity(createExerciseRequest, exercise);
        Exercise savedExercise = exerciseRepository.save(exercise);
        
        return exerciseMapper.toResponse(savedExercise);
    }
    
    /**
     * Delete exercise by ID.
     * 
     * @param exerciseId the exercise ID
     * @throws IllegalArgumentException if exercise not found
     */
    public void deleteExercise(Long exerciseId) {
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new IllegalArgumentException("Exercise not found with ID: " + exerciseId);
        }
        
        exerciseRepository.deleteById(exerciseId);
    }
}
