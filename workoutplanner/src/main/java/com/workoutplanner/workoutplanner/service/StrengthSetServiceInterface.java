package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;

import java.util.List;

/**
 * Service interface for StrengthSet entity operations.
 * Defines the contract for strength set management business logic.
 * 
 * This interface follows the Interface Segregation Principle by providing
 * a focused set of methods for strength set operations.
 */
public interface StrengthSetServiceInterface {
    
    /**
     * Create a new strength set.
     *
     * @param createSetRequest the set creation request
     * @return SetResponse the created set response
     */
    SetResponse createStrengthSet(CreateSetRequest createSetRequest);
    
    /**
     * Get strength set by ID.
     *
     * @param setId the set ID
     * @return SetResponse the set response
     */
    SetResponse getStrengthSetById(Long setId);
    
    /**
     * Get strength sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of SetResponse
     */
    List<SetResponse> getStrengthSetsByWorkoutExercise(Long workoutExerciseId);
    
    /**
     * Update strength set.
     *
     * @param setId the set ID
     * @param createSetRequest the updated set information
     * @return SetResponse the updated set response
     */
    SetResponse updateStrengthSet(Long setId, CreateSetRequest createSetRequest);
    
    /**
     * Delete strength set.
     *
     * @param setId the set ID
     */
    void deleteStrengthSet(Long setId);
    
    /**
     * Get all strength sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of SetResponse
     */
    List<SetResponse> getStrengthSetsByWorkoutSession(Long sessionId);
}
