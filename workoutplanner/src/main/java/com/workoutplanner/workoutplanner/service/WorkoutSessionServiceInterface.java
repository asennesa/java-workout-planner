package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for WorkoutSession entity operations.
 * Defines the contract for workout session management business logic.
 * 
 * This interface follows the Interface Segregation Principle by providing
 * a focused set of methods for workout session operations.
 */
public interface WorkoutSessionServiceInterface {
    
    /**
     * Create a new workout session.
     *
     * @param createWorkoutRequest the workout creation request
     * @return WorkoutResponse the created workout response
     */
    WorkoutResponse createWorkoutSession(CreateWorkoutRequest createWorkoutRequest);
    
    /**
     * Get workout session by ID.
     *
     * @param sessionId the session ID
     * @return WorkoutResponse the workout response
     */
    WorkoutResponse getWorkoutSessionById(Long sessionId);
    
    /**
     * Get all workout sessions for a user.
     *
     * @param userId the user ID
     * @return List of WorkoutResponse
     */
    List<WorkoutResponse> getWorkoutSessionsByUserId(Long userId);
    
    /**
     * Get all workout sessions.
     *
     * @return List of WorkoutResponse
     */
    List<WorkoutResponse> getAllWorkoutSessions();
    
    /**
     * Get all workout sessions with pagination.
     *
     * @param pageable pagination information
     * @return Paginated WorkoutResponse
     */
    PagedResponse<WorkoutResponse> getAllWorkoutSessions(Pageable pageable);
    
    /**
     * Update workout session.
     *
     * @param sessionId the session ID
     * @param createWorkoutRequest the updated workout request
     * @return WorkoutResponse the updated workout response
     */
    WorkoutResponse updateWorkoutSession(Long sessionId, CreateWorkoutRequest createWorkoutRequest);
    
    /**
     * Update workout session status.
     *
     * @param sessionId the session ID
     * @param status the new status
     * @return WorkoutResponse the updated workout response
     */
    WorkoutResponse updateWorkoutSessionStatus(Long sessionId, WorkoutStatus status);
    
    /**
     * Delete workout session.
     *
     * @param sessionId the session ID
     */
    void deleteWorkoutSession(Long sessionId);
    
    /**
     * Add exercise to workout session.
     *
     * @param createWorkoutExerciseRequest the workout exercise request
     * @return WorkoutExerciseResponse the created workout exercise response
     */
    WorkoutExerciseResponse addExerciseToWorkout(CreateWorkoutExerciseRequest createWorkoutExerciseRequest);
    
    /**
     * Remove exercise from workout session.
     *
     * @param workoutExerciseId the workout exercise ID
     */
    void removeExerciseFromWorkout(Long workoutExerciseId);
    
    /**
     * Get exercises for a workout session.
     *
     * @param sessionId the session ID
     * @return List of WorkoutExerciseResponse
     */
    List<WorkoutExerciseResponse> getWorkoutExercises(Long sessionId);
}
