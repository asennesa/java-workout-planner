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
public interface FlexibilitySetServiceInterface extends SetServiceInterface {
    List<SetResponse> getSetsByWorkoutSession(Long sessionId);
}
