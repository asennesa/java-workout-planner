package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import jakarta.validation.Valid;
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
 * Concrete controllers only need to provide the service implementation.
 */
public abstract class BaseSetController {

    /**
     * Template method to get the appropriate service implementation.
     * Concrete controllers must implement this to provide their specific service.
     * 
     * @return SetServiceInterface implementation for the specific set type
     */
    protected abstract SetServiceInterface getService();

    /**
     * Create a new set.
     * 
     * @param createSetRequest the set data
     * @return ResponseEntity containing the created set
     */
    @PostMapping
    public ResponseEntity<SetResponse> createSet(@Valid @RequestBody CreateSetRequest createSetRequest) {
        SetResponse setResponse = getService().createSet(createSetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(setResponse);
    }

    /**
     * Get all sets for a specific workout exercise.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return ResponseEntity containing list of sets
     */
    @GetMapping("/workout-exercise/{workoutExerciseId}")
    public ResponseEntity<List<SetResponse>> getSetsByWorkoutExercise(@PathVariable Long workoutExerciseId) {
        List<SetResponse> setResponses = getService().getSetsByWorkoutExercise(workoutExerciseId);
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
        SetResponse setResponse = getService().getSetById(setId);
        return ResponseEntity.ok(setResponse);
    }

    /**
     * Update an existing set.
     * 
     * @param setId the set ID
     * @param createSetRequest the updated set data
     * @return ResponseEntity containing the updated set
     */
    @PutMapping("/{setId}")
    public ResponseEntity<SetResponse> updateSet(@PathVariable Long setId, @Valid @RequestBody CreateSetRequest createSetRequest) {
        SetResponse setResponse = getService().updateSet(setId, createSetRequest);
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
        getService().deleteSet(setId);
        return ResponseEntity.noContent().build();
    }
}
