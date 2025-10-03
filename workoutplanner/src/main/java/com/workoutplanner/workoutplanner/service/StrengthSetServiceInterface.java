package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.response.StrengthSetResponse;

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
     * @param createStrengthSetRequest the strength set creation request
     * @return StrengthSetResponse the created strength set response
     */
    StrengthSetResponse createStrengthSet(CreateStrengthSetRequest createStrengthSetRequest);
    
    /**
     * Get strength set by ID.
     *
     * @param setId the set ID
     * @return StrengthSetResponse the strength set response
     */
    StrengthSetResponse getStrengthSetById(Long setId);
    
    /**
     * Get strength sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of StrengthSetResponse
     */
    List<StrengthSetResponse> getStrengthSetsByWorkoutExercise(Long workoutExerciseId);
    
    /**
     * Update strength set.
     *
     * @param setId the set ID
     * @param createStrengthSetRequest the updated strength set information
     * @return StrengthSetResponse the updated strength set response
     */
    StrengthSetResponse updateStrengthSet(Long setId, CreateStrengthSetRequest createStrengthSetRequest);
    
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
     * @return List of StrengthSetResponse
     */
    List<StrengthSetResponse> getStrengthSetsByWorkoutSession(Long sessionId);
}
