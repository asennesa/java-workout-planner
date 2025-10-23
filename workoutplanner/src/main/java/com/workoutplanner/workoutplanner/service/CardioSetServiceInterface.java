package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;

import java.util.List;

/**
 * Service interface for CardioSet entity operations.
 * Defines the contract for cardio set management business logic.
 * 
 * This interface follows the Interface Segregation Principle by providing
 * a focused set of methods for cardio set operations.
 */
public interface CardioSetServiceInterface {
    
    /**
     * Create a new cardio set.
     *
     * @param createCardioSetRequest the cardio set creation request
     * @return CardioSetResponse the created cardio set response
     */
    SetResponse createCardioSet(CreateSetRequest createSetRequest);
    
    /**
     * Get cardio set by ID.
     *
     * @param setId the set ID
     * @return CardioSetResponse the cardio set response
     */
    SetResponse getCardioSetById(Long setId);
    
    /**
     * Get cardio sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of CardioSetResponse
     */
    List<SetResponse> getCardioSetsByWorkoutExercise(Long workoutExerciseId);
    
    /**
     * Update cardio set.
     *
     * @param setId the set ID
     * @param createCardioSetRequest the updated cardio set information
     * @return CardioSetResponse the updated cardio set response
     */
    SetResponse updateCardioSet(Long setId, CreateSetRequest createSetRequest);
    
    /**
     * Delete cardio set.
     *
     * @param setId the set ID
     */
    void deleteCardioSet(Long setId);
    
    /**
     * Get all cardio sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of CardioSetResponse
     */
    List<SetResponse> getCardioSetsByWorkoutSession(Long sessionId);
}
