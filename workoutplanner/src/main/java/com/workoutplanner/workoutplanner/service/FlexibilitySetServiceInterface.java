package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.dto.response.FlexibilitySetResponse;

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
    FlexibilitySetResponse createFlexibilitySet(CreateFlexibilitySetRequest createFlexibilitySetRequest);
    
    /**
     * Get flexibility set by ID.
     *
     * @param setId the set ID
     * @return FlexibilitySetResponse the flexibility set response
     */
    FlexibilitySetResponse getFlexibilitySetById(Long setId);
    
    /**
     * Get flexibility sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of FlexibilitySetResponse
     */
    List<FlexibilitySetResponse> getFlexibilitySetsByWorkoutExercise(Long workoutExerciseId);
    
    /**
     * Update flexibility set.
     *
     * @param setId the set ID
     * @param createFlexibilitySetRequest the updated flexibility set information
     * @return FlexibilitySetResponse the updated flexibility set response
     */
    FlexibilitySetResponse updateFlexibilitySet(Long setId, CreateFlexibilitySetRequest createFlexibilitySetRequest);
    
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
    List<FlexibilitySetResponse> getFlexibilitySetsByWorkoutSession(Long sessionId);
}
