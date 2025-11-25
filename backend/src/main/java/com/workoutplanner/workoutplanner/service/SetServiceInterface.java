package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.response.SetResponse;

import java.util.List;

/**
 * Generic service interface for Set operations.
 * 
 * Following REST best practices: workoutExerciseId is passed separately from the request body.
 * 
 * @param <T> The specific set request type (CreateStrengthSetRequest, CreateCardioSetRequest, etc.)
 */
public interface SetServiceInterface<T> {
    /**
     * Create a new set for a workout exercise.
     * 
     * @param workoutExerciseId the workout exercise ID from URL path parameter
     * @param createSetRequest the set data from request body
     * @return SetResponse the created set
     */
    SetResponse createSet(Long workoutExerciseId, T createSetRequest);
    
    /**
     * Get a set by ID.
     * 
     * @param setId the set ID
     * @return SetResponse the set
     */
    SetResponse getSetById(Long setId);
    
    /**
     * Get all sets for a workout exercise.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return List of SetResponse
     */
    List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId);
    
    /**
     * Update an existing set.
     * 
     * @param setId the set ID
     * @param createSetRequest the updated set data
     * @return SetResponse the updated set
     */
    SetResponse updateSet(Long setId, T createSetRequest);
    
    /**
     * Delete a set.
     * 
     * @param setId the set ID
     */
    void deleteSet(Long setId);
}
