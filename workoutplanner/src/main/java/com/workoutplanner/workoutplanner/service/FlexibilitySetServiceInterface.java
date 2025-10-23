package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;

import java.util.List;

/**
 * Service interface for FlexibilitySet entity operations.
 * Defines the contract for flexibility set management business logic.
 * 
 * This interface follows the Interface Segregation Principle by providing
 * a focused set of methods for flexibility set operations.
 */
public interface FlexibilitySetServiceInterface {
    
    /**
     * Create a new flexibility set.
     *
     * @param createFlexibilitySetRequest the flexibility set creation request
     * @return FlexibilitySetResponse the created flexibility set response
     */
    SetResponse createFlexibilitySet(CreateSetRequest createSetRequest);
    
    /**
     * Get flexibility set by ID.
     *
     * @param setId the set ID
     * @return FlexibilitySetResponse the flexibility set response
     */
    SetResponse getFlexibilitySetById(Long setId);
    
    /**
     * Get flexibility sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of FlexibilitySetResponse
     */
    List<SetResponse> getFlexibilitySetsByWorkoutExercise(Long workoutExerciseId);
    
    /**
     * Update flexibility set.
     *
     * @param setId the set ID
     * @param createFlexibilitySetRequest the updated flexibility set information
     * @return FlexibilitySetResponse the updated flexibility set response
     */
    SetResponse updateFlexibilitySet(Long setId, CreateSetRequest createSetRequest);
    
    /**
     * Delete flexibility set.
     *
     * @param setId the set ID
     */
    void deleteFlexibilitySet(Long setId);
    
    /**
     * Get all flexibility sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of FlexibilitySetResponse
     */
    List<SetResponse> getFlexibilitySetsByWorkoutSession(Long sessionId);
}
