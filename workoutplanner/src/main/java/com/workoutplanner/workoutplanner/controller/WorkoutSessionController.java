package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.WorkoutActionRequest;
import com.workoutplanner.workoutplanner.exception.OptimisticLockConflictException;
import com.workoutplanner.workoutplanner.validation.ValidationGroups;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.workoutplanner.workoutplanner.service.WorkoutSessionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Workout Session operations.
 * Provides endpoints for workout session management following REST API best practices.
 * 
 * CORS is configured globally in CorsConfig.java
 * API Version: v1
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/workouts")
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
    public ResponseEntity<WorkoutResponse> createWorkoutSession(@Validated(ValidationGroups.WorkoutSessionCreation.class) @RequestBody CreateWorkoutRequest createWorkoutRequest) {
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
     * Get a workout session by ID with smart loading.
     * Only loads sets based on exercise type for optimal performance.
     * 
     * @param sessionId the workout session ID
     * @return ResponseEntity containing the workout response with smart-loaded sets
     */
    @GetMapping("/{sessionId}/smart")
    public ResponseEntity<WorkoutResponse> getWorkoutSessionWithSmartLoading(@PathVariable Long sessionId) {
        logger.debug("Getting workout session with smart loading. sessionId={}", sessionId);
        
        WorkoutResponse workoutResponse = workoutSessionService.getWorkoutSessionWithSmartLoading(sessionId);
        
        logger.info("Workout session retrieved with smart loading. sessionId={}, exerciseCount={}", 
                   sessionId, workoutResponse.getWorkoutExercises().size());
        
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Get all workout sessions with pagination.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return ResponseEntity containing paginated workout responses
     */
    @GetMapping
    public ResponseEntity<PagedResponse<WorkoutResponse>> getAllWorkoutSessions(
            @PageableDefault(size = 20, sort = "sessionId", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<WorkoutResponse> pagedResponse = workoutSessionService.getAllWorkoutSessions(pageable);
        return ResponseEntity.ok(pagedResponse);
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
                                                               @Validated(ValidationGroups.WorkoutSessionUpdate.class) @RequestBody CreateWorkoutRequest createWorkoutRequest) {
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSession(sessionId, createWorkoutRequest);
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
     * Perform actions on workout (start, pause, resume, complete, cancel).
     * 
     * RESTful approach: Use PATCH to update workout status.
     * This is more RESTful than POST /action because we're updating a resource attribute.
     * 
     * @param sessionId the session ID
     * @param actionRequest the action to perform (converted to status)
     * @return ResponseEntity containing the updated workout response
     */
    @PatchMapping("/{sessionId}/status")
    public ResponseEntity<WorkoutResponse> updateWorkoutStatus(
            @PathVariable Long sessionId, 
            @Valid @RequestBody WorkoutActionRequest actionRequest) {
        logger.debug("Updating workout status. sessionId={}, action={}", 
                    sessionId, actionRequest.getAction());
        
        WorkoutResponse workoutResponse = workoutSessionService.performAction(sessionId, actionRequest.getAction());
        
        logger.info("Workout status updated successfully. sessionId={}, action={}, newStatus={}", 
                   sessionId, actionRequest.getAction(), workoutResponse.getStatus());
        
        return ResponseEntity.ok(workoutResponse);
    }

    /**
     * Add an exercise to a workout session.
     * 
     * Following REST best practices: sessionId comes from URL path, not request body.
     * This makes the API cleaner and follows the resource hierarchy pattern.
     * 
     * @param sessionId the session ID from path parameter
     * @param createWorkoutExerciseRequest the workout exercise creation request from body
     * @return ResponseEntity containing the created workout exercise response
     */
    @PostMapping("/{sessionId}/exercises")
    public ResponseEntity<WorkoutExerciseResponse> addExerciseToWorkout(
            @PathVariable Long sessionId,
            @Valid @RequestBody CreateWorkoutExerciseRequest createWorkoutExerciseRequest) {
        logger.debug("Adding exercise to workout session. sessionId={}, exerciseId={}", 
                    sessionId, createWorkoutExerciseRequest.getExerciseId());
        
        // Pass sessionId separately - it comes from URL, not request body
        WorkoutExerciseResponse workoutExerciseResponse = workoutSessionService.addExerciseToWorkout(sessionId, createWorkoutExerciseRequest);
        
        logger.info("Exercise added to workout successfully. workoutExerciseId={}, sessionId={}, exerciseId={}", 
                   workoutExerciseResponse.getWorkoutExerciseId(), sessionId, createWorkoutExerciseRequest.getExerciseId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutExerciseResponse);
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
     * Handle optimistic lock conflicts.
     * Returns a 409 Conflict status with a clear error message.
     * 
     * @param ex the optimistic lock conflict exception
     * @return ResponseEntity with conflict status and error details
     */
    @ExceptionHandler(OptimisticLockConflictException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockConflict(OptimisticLockConflictException ex) {
        logger.warn("Optimistic lock conflict: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "CONFLICT");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
