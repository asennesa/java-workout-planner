package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.config.ApiVersionConfig;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.service.WorkoutSessionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Workout Session operations.
 * Provides endpoints for workout session management following REST API best practices.
 * 
 * CORS is configured globally in CorsConfig.java
 * API Version: v1
 */
@RestController
@RequestMapping(ApiVersionConfig.V1_BASE_PATH + "/workouts")
public class WorkoutSessionController {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkoutSessionController.class);
    
    private final WorkoutSessionService workoutSessionService;
    
    /**
     * Constructor injection for dependencies.
     */
    public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
        this.workoutSessionService = workoutSessionService;
    }
    
    /**
     * Create a new workout session.
     * 
     * @param createWorkoutRequest the workout creation request
     * @return ResponseEntity containing the created workout response
     */
    @PostMapping
    public ResponseEntity<WorkoutResponse> createWorkoutSession(@Valid @RequestBody CreateWorkoutRequest createWorkoutRequest) {
        logger.debug("Creating workout session for userId={}", 
                    createWorkoutRequest.getUserId());
        
        WorkoutResponse workoutResponse = workoutSessionService.createWorkoutSession(createWorkoutRequest);
        
        logger.info("Workout session created successfully. sessionId={}, userId={}, status={}", 
                   workoutResponse.getSessionId(), workoutResponse.getUserId(), workoutResponse.getStatus());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutResponse);
    }
    
    /**
     * Get workout session by ID.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity containing the workout response
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<WorkoutResponse> getWorkoutSessionById(@PathVariable Long sessionId) {
        WorkoutResponse workoutResponse = workoutSessionService.getWorkoutSessionById(sessionId);
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Get all workout sessions.
     * 
     * @return ResponseEntity containing list of all workout responses
     */
    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> getAllWorkoutSessions() {
        List<WorkoutResponse> workoutResponses = workoutSessionService.getAllWorkoutSessions();
        return ResponseEntity.ok(workoutResponses);
    }
    
    /**
     * Get workout sessions by user ID.
     * 
     * @param userId the user ID
     * @return ResponseEntity containing list of workout responses for the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WorkoutResponse>> getWorkoutSessionsByUserId(@PathVariable Long userId) {
        List<WorkoutResponse> workoutResponses = workoutSessionService.getWorkoutSessionsByUserId(userId);
        return ResponseEntity.ok(workoutResponses);
    }
    
    /**
     * Update workout session.
     * 
     * @param sessionId the session ID
     * @param createWorkoutRequest the updated workout information
     * @return ResponseEntity containing the updated workout response
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<WorkoutResponse> updateWorkoutSession(@PathVariable Long sessionId, 
                                                               @Valid @RequestBody CreateWorkoutRequest createWorkoutRequest) {
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSession(sessionId, createWorkoutRequest);
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Update workout session status.
     * 
     * @param sessionId the session ID
     * @param status the new status
     * @return ResponseEntity containing the updated workout response
     */
    @PutMapping("/{sessionId}/status")
    public ResponseEntity<WorkoutResponse> updateWorkoutSessionStatus(@PathVariable Long sessionId, 
                                                                      @RequestParam WorkoutStatus status) {
        logger.info("Updating workout session status: sessionId={}, newStatus={}", sessionId, status);
        
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSessionStatus(sessionId, status);
        
        logger.info("Workout session status updated successfully. sessionId={}, status={}", sessionId, status);
        
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Delete workout session.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteWorkoutSession(@PathVariable Long sessionId) {
        workoutSessionService.deleteWorkoutSession(sessionId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Add exercise to workout session.
     * 
     * @param createWorkoutExerciseRequest the workout exercise request
     * @return ResponseEntity containing the created workout exercise response
     */
    @PostMapping("/exercises")
    public ResponseEntity<WorkoutExerciseResponse> addExerciseToWorkout(@Valid @RequestBody CreateWorkoutExerciseRequest createWorkoutExerciseRequest) {
        WorkoutExerciseResponse workoutExerciseResponse = workoutSessionService.addExerciseToWorkout(createWorkoutExerciseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutExerciseResponse);
    }
    
    /**
     * Remove exercise from workout session.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/exercises/{workoutExerciseId}")
    public ResponseEntity<Void> removeExerciseFromWorkout(@PathVariable Long workoutExerciseId) {
        workoutSessionService.removeExerciseFromWorkout(workoutExerciseId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get exercises for a workout session.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity containing list of workout exercise responses
     */
    @GetMapping("/{sessionId}/exercises")
    public ResponseEntity<List<WorkoutExerciseResponse>> getWorkoutExercises(@PathVariable Long sessionId) {
        List<WorkoutExerciseResponse> workoutExerciseResponses = workoutSessionService.getWorkoutExercises(sessionId);
        return ResponseEntity.ok(workoutExerciseResponses);
    }
    
    /**
     * Start a workout session.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity containing the updated workout response
     */
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<WorkoutResponse> startWorkoutSession(@PathVariable Long sessionId) {
        logger.info("Starting workout session: sessionId={}", sessionId);
        
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSessionStatus(sessionId, WorkoutStatus.IN_PROGRESS);
        
        logger.info("Workout session started. sessionId={}", sessionId);
        
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Pause a workout session.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity containing the updated workout response
     */
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<WorkoutResponse> pauseWorkoutSession(@PathVariable Long sessionId) {
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSessionStatus(sessionId, WorkoutStatus.PAUSED);
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Resume a workout session.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity containing the updated workout response
     */
    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<WorkoutResponse> resumeWorkoutSession(@PathVariable Long sessionId) {
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSessionStatus(sessionId, WorkoutStatus.IN_PROGRESS);
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Complete a workout session.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity containing the updated workout response
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<WorkoutResponse> completeWorkoutSession(@PathVariable Long sessionId) {
        logger.info("Completing workout session: sessionId={}", sessionId);
        
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSessionStatus(sessionId, WorkoutStatus.COMPLETED);
        
        logger.info("Workout session completed successfully. sessionId={}", sessionId);
        
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Cancel a workout session.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity containing the updated workout response
     */
    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<WorkoutResponse> cancelWorkoutSession(@PathVariable Long sessionId) {
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSessionStatus(sessionId, WorkoutStatus.CANCELLED);
        return ResponseEntity.ok(workoutResponse);
    }
}
