package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Base controller for Set operations providing common CRUD functionality.
 * 
 * This abstract class implements the Template Method pattern to provide
 * consistent CRUD operations across different set types (Strength, Cardio, Flexibility).
 * 
 * Following REST best practices: workoutExerciseId comes from URL path, not request body.
 * This makes the API cleaner and follows the resource hierarchy pattern.
 * 
 * The generic type T allows each concrete controller to use its specific request DTO.
 * 
 * @param <T> The specific set request type (CreateStrengthSetRequest, CreateCardioSetRequest, etc.)
 */
public abstract class BaseSetController<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseSetController.class);

    /**
     * Template method to get the appropriate service implementation.
     * Concrete controllers must implement this to provide their specific service.
     * 
     * @return SetServiceInterface implementation for the specific set type
     */
    protected abstract SetServiceInterface<T> getService();

    /**
     * Create a new set for a workout exercise.
     * 
     * Following REST best practices: workoutExerciseId comes from parent path in @RequestMapping.
     * Full nested resource pattern: /workout-exercises/{workoutExerciseId}/strength-sets
     * 
     * @param workoutExerciseId the workout exercise ID from path parameter
     * @param createSetRequest the set data (type-specific) from request body
     * @return ResponseEntity containing the created set
     */
    @PostMapping
    public ResponseEntity<SetResponse> createSet(
            @PathVariable Long workoutExerciseId,
            @Valid @RequestBody T createSetRequest) {
        logger.debug("Creating set for workout exercise. workoutExerciseId={}", workoutExerciseId);
        
        // Pass workoutExerciseId separately - it comes from URL, not request body
        SetResponse setResponse = getService().createSet(workoutExerciseId, createSetRequest);
        
        logger.info("Set created successfully. setId={}, setType={}, workoutExerciseId={}", 
                   setResponse.getSetId(), setResponse.getSetType(), workoutExerciseId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(setResponse);
    }

    /**
     * Get all sets for a specific workout exercise.
     * 
      * RESTful approach: GET on the collection itself.
     * URL: /api/v1/workout-exercises/{workoutExerciseId}/strength-sets
     * 
     * @param workoutExerciseId the workout exercise ID from parent path
     * @return ResponseEntity containing list of sets
     */
    @GetMapping
    public ResponseEntity<List<SetResponse>> getSetsByWorkoutExercise(@PathVariable Long workoutExerciseId) {
        logger.debug("Getting all sets for workout exercise. workoutExerciseId={}", workoutExerciseId);
        
        List<SetResponse> setResponses = getService().getSetsByWorkoutExercise(workoutExerciseId);
        
        logger.info("Retrieved {} sets for workoutExerciseId={}", setResponses.size(), workoutExerciseId);
        
        return ResponseEntity.ok(setResponses);
    }

    /**
     * Get a specific set by ID.
     * 
     * @param setId the set ID
     * @return ResponseEntity containing the set
     */
    @GetMapping("/{setId}")
    public ResponseEntity<SetResponse> getSetById(@PathVariable Long setId) {
        logger.debug("Getting set by ID: setId={}", setId);
        
        SetResponse setResponse = getService().getSetById(setId);
        
        logger.info("Set retrieved successfully. setId={}, setType={}", 
                   setResponse.getSetId(), setResponse.getSetType());
        
        return ResponseEntity.ok(setResponse);
    }

    /**
     * Update an existing set.
     * 
     * Note: Update uses setId in path, not workoutExerciseId, since we're modifying
     * a specific set that already exists.
     * 
     * @param setId the set ID from path parameter
     * @param createSetRequest the updated set data (type-specific)
     * @return ResponseEntity containing the updated set
     */
    @PutMapping("/{setId}")
    public ResponseEntity<SetResponse> updateSet(
            @PathVariable Long setId, 
            @Valid @RequestBody T createSetRequest) {
        logger.debug("Updating set. setId={}", setId);
        
        SetResponse setResponse = getService().updateSet(setId, createSetRequest);
        
        logger.info("Set updated successfully. setId={}, setType={}", 
                   setResponse.getSetId(), setResponse.getSetType());
        
        return ResponseEntity.ok(setResponse);
    }

    /**
     * Delete a set.
     * 
     * @param setId the set ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{setId}")
    public ResponseEntity<Void> deleteSet(@PathVariable Long setId) {
        logger.warn("Deleting set: setId={}", setId);
        
        getService().deleteSet(setId);
        
        logger.info("Set deleted successfully. setId={}", setId);
        
        return ResponseEntity.noContent().build();
    }
}
