package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Exercise operations.
 * Provides endpoints for exercise management following REST API best practices.
 */
@RestController
@RequestMapping("/api/exercises")
@CrossOrigin(origins = "*") // Configure CORS as needed for your frontend
public class ExerciseController {
    
    @Autowired
    private ExerciseService exerciseService;
    
    /**
     * Create a new exercise.
     * 
     * @param createExerciseRequest the exercise creation request
     * @return ResponseEntity containing the created exercise response
     */
    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(@Valid @RequestBody CreateExerciseRequest createExerciseRequest) {
        ExerciseResponse exerciseResponse = exerciseService.createExercise(createExerciseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseResponse);
    }
    
    /**
     * Get exercise by ID.
     * 
     * @param exerciseId the exercise ID
     * @return ResponseEntity containing the exercise response
     */
    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> getExerciseById(@PathVariable Long exerciseId) {
        ExerciseResponse exerciseResponse = exerciseService.getExerciseById(exerciseId);
        return ResponseEntity.ok(exerciseResponse);
    }
    
    /**
     * Get all exercises.
     * 
     * @return ResponseEntity containing list of all exercise responses
     */
    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getAllExercises() {
        List<ExerciseResponse> exerciseResponses = exerciseService.getAllExercises();
        return ResponseEntity.ok(exerciseResponses);
    }
    
    /**
     * Get exercises by type.
     * 
     * @param type the exercise type
     * @return ResponseEntity containing list of exercises of the specified type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByType(@PathVariable ExerciseType type) {
        List<ExerciseResponse> exerciseResponses = exerciseService.getExercisesByType(type);
        return ResponseEntity.ok(exerciseResponses);
    }
    
    /**
     * Get exercises by target muscle group.
     * 
     * @param targetMuscleGroup the target muscle group
     * @return ResponseEntity containing list of exercises targeting the specified muscle group
     */
    @GetMapping("/muscle-group/{targetMuscleGroup}")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByTargetMuscleGroup(@PathVariable TargetMuscleGroup targetMuscleGroup) {
        List<ExerciseResponse> exerciseResponses = exerciseService.getExercisesByTargetMuscleGroup(targetMuscleGroup);
        return ResponseEntity.ok(exerciseResponses);
    }
    
    /**
     * Get exercises by difficulty level.
     * 
     * @param difficultyLevel the difficulty level
     * @return ResponseEntity containing list of exercises of the specified difficulty
     */
    @GetMapping("/difficulty/{difficultyLevel}")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByDifficultyLevel(@PathVariable DifficultyLevel difficultyLevel) {
        List<ExerciseResponse> exerciseResponses = exerciseService.getExercisesByDifficultyLevel(difficultyLevel);
        return ResponseEntity.ok(exerciseResponses);
    }
    
    /**
     * Get exercises by type and target muscle group.
     * 
     * @param type the exercise type
     * @param targetMuscleGroup the target muscle group
     * @return ResponseEntity containing list of exercises matching both criteria
     */
    @GetMapping("/type/{type}/muscle-group/{targetMuscleGroup}")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByTypeAndTargetMuscleGroup(
            @PathVariable ExerciseType type, 
            @PathVariable TargetMuscleGroup targetMuscleGroup) {
        List<ExerciseResponse> exerciseResponses = exerciseService.getExercisesByTypeAndTargetMuscleGroup(type, targetMuscleGroup);
        return ResponseEntity.ok(exerciseResponses);
    }
    
    /**
     * Search exercises by name.
     * 
     * @param name the name to search for
     * @return ResponseEntity containing list of exercises with names containing the search term
     */
    @GetMapping("/search")
    public ResponseEntity<List<ExerciseResponse>> searchExercisesByName(@RequestParam String name) {
        List<ExerciseResponse> exerciseResponses = exerciseService.searchExercisesByName(name);
        return ResponseEntity.ok(exerciseResponses);
    }
    
    /**
     * Get exercises by multiple criteria.
     * 
     * @param type the exercise type (optional)
     * @param targetMuscleGroup the target muscle group (optional)
     * @param difficultyLevel the difficulty level (optional)
     * @return ResponseEntity containing list of exercises matching the criteria
     */
    @GetMapping("/filter")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByCriteria(
            @RequestParam(required = false) ExerciseType type,
            @RequestParam(required = false) TargetMuscleGroup targetMuscleGroup,
            @RequestParam(required = false) DifficultyLevel difficultyLevel) {
        List<ExerciseResponse> exerciseResponses = exerciseService.getExercisesByCriteria(type, targetMuscleGroup, difficultyLevel);
        return ResponseEntity.ok(exerciseResponses);
    }
    
    /**
     * Update exercise.
     * 
     * @param exerciseId the exercise ID
     * @param createExerciseRequest the updated exercise information
     * @return ResponseEntity containing the updated exercise response
     */
    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(@PathVariable Long exerciseId, 
                                                          @Valid @RequestBody CreateExerciseRequest createExerciseRequest) {
        ExerciseResponse exerciseResponse = exerciseService.updateExercise(exerciseId, createExerciseRequest);
        return ResponseEntity.ok(exerciseResponse);
    }
    
    /**
     * Delete exercise by ID.
     * 
     * @param exerciseId the exercise ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long exerciseId) {
        exerciseService.deleteExercise(exerciseId);
        return ResponseEntity.noContent().build();
    }
}
