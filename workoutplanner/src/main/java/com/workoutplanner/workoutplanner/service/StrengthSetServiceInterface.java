package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;

import java.util.List;

/**
 * Service interface for StrengthSet entity operations.
 * Defines the contract for strength set management business logic.
 * 
 * This interface follows the Interface Segregation Principle by providing
 * a focused set of methods for strength set operations.
 */
public interface StrengthSetServiceInterface extends SetServiceInterface<CreateStrengthSetRequest> {
    List<SetResponse> getSetsByWorkoutSession(Long sessionId);
}
