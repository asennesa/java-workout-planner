package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.CreateExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.service.ExerciseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Exercise operations.
 * Provides endpoints for exercise management following REST API best practices.
 * 
 * CORS is configured globally in CorsConfig.java
 * API Version: v1
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/exercises")
@Validated
public class ExerciseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExerciseController.class);
    
    private final ExerciseService exerciseService;
    
    /**
     * Constructor injection for dependencies.
     */
    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }
    
    /**
     * Create a new exercise.
     * 
     * @param createExerciseRequest the exercise creation request
     * @return ResponseEntity containing the created exercise response
     */
    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(@Valid @RequestBody CreateExerciseRequest createExerciseRequest) {
        logger.debug("Creating exercise: name={}, type={}", createExerciseRequest.getName(), createExerciseRequest.getType());
        
        ExerciseResponse exerciseResponse = exerciseService.createExercise(createExerciseRequest);
        
        logger.info("Exercise created successfully. exerciseId={}, name={}, type={}", 
                   exerciseResponse.getExerciseId(), exerciseResponse.getName(), exerciseResponse.getType());
        
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
     * Get all exercises with pagination.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return ResponseEntity containing paginated exercise responses
     */
    @GetMapping
    public ResponseEntity<PagedResponse<ExerciseResponse>> getAllExercises(
            @PageableDefault(size = 20, sort = "exerciseId", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.debug("Fetching exercises with pagination. page={}, size={}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        
        PagedResponse<ExerciseResponse> pagedResponse = exerciseService.getAllExercises(pageable);
        
        logger.info("Retrieved {} exercises on page {} of {}", 
                   pagedResponse.getContent().size(), 
                   pagedResponse.getPageNumber(), 
                   pagedResponse.getTotalPages());
        
        return ResponseEntity.ok(pagedResponse);
    }
    
    /**
     * Search exercises by name.
     * Input is validated and sanitized to prevent SQL injection and wildcard abuse.
     * 
     * @param name the name to search for (min 2, max 100 characters)
     * @return ResponseEntity containing list of exercises with names containing the search term
     */
    @GetMapping("/search")
    public ResponseEntity<List<ExerciseResponse>> searchExercisesByName(
            @RequestParam 
            @NotBlank(message = "Search term cannot be empty")
            @Size(min = 2, max = 100, message = "Search term must be between 2 and 100 characters") 
            String name) {
        logger.debug("Searching exercises by name: {}", name);
        
        List<ExerciseResponse> exerciseResponses = exerciseService.searchExercisesByName(name);
        
        logger.info("Found {} exercises matching name search", exerciseResponses.size());
        
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
        logger.warn("Deleting exercise: exerciseId={}", exerciseId);
        
        exerciseService.deleteExercise(exerciseId);
        
        logger.info("Exercise deleted successfully. exerciseId={}", exerciseId);
        
        return ResponseEntity.noContent().build();
    }
}
