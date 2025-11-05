package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;

import java.util.List;

/**
 * Service interface for CardioSet entity operations.
 * Defines the contract for cardio set management business logic.
 * 
 * This interface follows the Interface Segregation Principle by providing
 * a focused set of methods for cardio set operations.
 */
public interface CardioSetServiceInterface extends SetServiceInterface<CreateCardioSetRequest> {
    List<SetResponse> getSetsByWorkoutSession(Long sessionId);
}
