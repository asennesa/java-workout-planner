package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.annotation.RateLimited;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.WorkoutActionRequest;
import com.workoutplanner.workoutplanner.exception.OptimisticLockConflictException;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.workoutplanner.workoutplanner.service.WorkoutSessionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

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
@Tag(name = "Workout Sessions", description = "Endpoints for managing workout sessions, exercises, and workout progress tracking")
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
    @Operation(
        summary = "Create a new workout session",
        description = "Creates a new workout session for a user. Initial status is PLANNED.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Workout session created successfully",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input - validation errors",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('write:workouts')")
    @RateLimited(capacity = 100, refillTokens = 100, refillPeriod = 1,
                 timeUnit = TimeUnit.HOURS, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<WorkoutResponse> createWorkoutSession(
            @Parameter(description ="Workout session details", required = true)
            @Valid @RequestBody CreateWorkoutRequest createWorkoutRequest) {

        logger.debug("Creating workout session for name={}", createWorkoutRequest.getName());

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
    @Operation(
        summary = "Get workout session by ID",
        description = "Retrieves detailed information about a specific workout session including all exercises and sets.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Workout session found",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Workout session not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAuthority('read:workouts') and @resourceSecurityService.canAccessWorkout(#sessionId)")
    public ResponseEntity<WorkoutResponse> getWorkoutSessionById(
            @Parameter(description = "Workout session ID", required = true, example = "1")
            @PathVariable Long sessionId) {
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
    @Operation(
        summary = "Get workout session with optimized loading",
        description = "Retrieves workout session with smart loading - only loads sets that match each exercise type for better performance.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Workout session retrieved successfully",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Workout session not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/{sessionId}/smart")
    @PreAuthorize("hasAuthority('read:workouts') and @resourceSecurityService.canAccessWorkout(#sessionId)")
    public ResponseEntity<WorkoutResponse> getWorkoutSessionWithSmartLoading(
            @Parameter(description = "Workout session ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        logger.debug("Getting workout session with smart loading. sessionId={}", sessionId);
        
        WorkoutResponse workoutResponse = workoutSessionService.getWorkoutSessionWithSmartLoading(sessionId);
        
        logger.info("Workout session retrieved with smart loading. sessionId={}, exerciseCount={}", 
                   sessionId, workoutResponse.getWorkoutExercises().size());
        
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Get the current user's workout sessions.
     * Uses JWT token to identify the user - no user ID needed in request.
     *
     * @return ResponseEntity containing list of workout responses for the current user
     */
    @Operation(
        summary = "Get my workout sessions",
        description = "Retrieves all workout sessions for the currently authenticated user. Uses JWT token to identify the user.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User's workout sessions retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('read:workouts')")
    public ResponseEntity<List<WorkoutResponse>> getMyWorkouts() {
        logger.debug("Getting workouts for current authenticated user");

        List<WorkoutResponse> workoutResponses = workoutSessionService.getMyWorkouts();

        logger.info("Retrieved {} workouts for current user", workoutResponses.size());

        return ResponseEntity.ok(workoutResponses);
    }

    /**
     * Get all workout sessions with pagination (ADMIN ONLY).
     * Returns ALL workouts across ALL users - restricted to administrators.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return ResponseEntity containing paginated workout responses
     */
    @Operation(
        summary = "Get all workout sessions - Admin only (paginated)",
        description = "Admin endpoint: Retrieves a paginated list of ALL workout sessions from ALL users. Requires admin permissions.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Workout sessions retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - admin permissions required",
            content = @Content
        )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<PagedResponse<WorkoutResponse>> getAllWorkoutSessions(
            @Parameter(description = "Pagination parameters", example = "page=0&size=20&sort=sessionId,desc")
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
    @Operation(
        summary = "Get workout sessions by user",
        description = "Retrieves all workout sessions for a specific user.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User's workout sessions retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('read:workouts') and (@userService.isCurrentUser(#userId) or hasAuthority('read:users'))")
    public ResponseEntity<List<WorkoutResponse>> getWorkoutSessionsByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        List<WorkoutResponse> workoutResponses = workoutSessionService.getWorkoutSessionsByUserId(userId);
        return ResponseEntity.ok(workoutResponses);
    }
    
    /**
     * Update workout session.
     * 
     * @param sessionId the session ID
     * @param updateWorkoutRequest the updated workout information
     * @return ResponseEntity containing the updated workout response
     */
    @PutMapping("/{sessionId}")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkout(#sessionId)")
    public ResponseEntity<WorkoutResponse> updateWorkoutSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateWorkoutRequest updateWorkoutRequest) {
        WorkoutResponse workoutResponse = workoutSessionService.updateWorkoutSession(sessionId, updateWorkoutRequest);
        return ResponseEntity.ok(workoutResponse);
    }
    
    /**
     * Delete workout session.
     * 
     * @param sessionId the session ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasAuthority('delete:workouts') and @resourceSecurityService.canModifyWorkout(#sessionId)")
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
    @Operation(
        summary = "Update workout status",
        description = "Updates the workout status by performing an action (START, PAUSE, RESUME, COMPLETE, CANCEL). Validates state transitions.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Workout status updated successfully",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid state transition",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Workout session not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Optimistic lock conflict - workout was modified by another request",
            content = @Content
        )
    })
    @PatchMapping("/{sessionId}/status")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkout(#sessionId)")
    public ResponseEntity<WorkoutResponse> updateWorkoutStatus(
            @Parameter(description = "Workout session ID", required = true, example = "1")
            @PathVariable Long sessionId,
            @Parameter(description = "Action to perform on workout", required = true)
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
    @Operation(
        summary = "Add exercise to workout session",
        description = "Adds an exercise from the library to a workout session. The exercise can then have sets added to it.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Exercise added to workout successfully",
            content = @Content(schema = @Schema(implementation = WorkoutExerciseResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input - validation errors",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Workout session or exercise not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @PostMapping("/{sessionId}/exercises")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkout(#sessionId)")
    public ResponseEntity<WorkoutExerciseResponse> addExerciseToWorkout(
            @Parameter(description = "Workout session ID", required = true, example = "1")
            @PathVariable Long sessionId,
            @Parameter(description = "Exercise to add to workout", required = true)
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
    @Operation(
        summary = "Get exercises in workout session",
        description = "Retrieves all exercises included in a workout session.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Workout exercises retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Workout session not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/{sessionId}/exercises")
    @PreAuthorize("hasAuthority('read:workouts') and @resourceSecurityService.canAccessWorkout(#sessionId)")
    public ResponseEntity<List<WorkoutExerciseResponse>> getWorkoutExercises(
            @Parameter(description = "Workout session ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        List<WorkoutExerciseResponse> workoutExerciseResponses = workoutSessionService.getWorkoutExercises(sessionId);
        return ResponseEntity.ok(workoutExerciseResponses);
    }

    /**
     * Remove an exercise from a workout session.
     *
     * @param workoutExerciseId the workout exercise ID to remove
     * @return ResponseEntity with no content
     */
    @Operation(
        summary = "Remove exercise from workout session",
        description = "Removes an exercise from a workout session. This also deletes all associated sets.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Exercise removed from workout successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Workout exercise not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @DeleteMapping("/exercises/{workoutExerciseId}")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkoutExercise(#workoutExerciseId)")
    public ResponseEntity<Void> removeExerciseFromWorkout(
            @Parameter(description = "Workout Exercise ID", required = true, example = "1")
            @PathVariable Long workoutExerciseId) {
        logger.debug("Removing exercise from workout. workoutExerciseId={}", workoutExerciseId);

        workoutSessionService.removeExerciseFromWorkout(workoutExerciseId);

        logger.info("Exercise removed from workout successfully. workoutExerciseId={}", workoutExerciseId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Update a workout exercise (e.g., change order or notes).
     *
     * @param workoutExerciseId the workout exercise ID to update
     * @param updateRequest the update request containing new values
     * @return ResponseEntity containing the updated workout exercise response
     */
    @Operation(
        summary = "Update workout exercise",
        description = "Updates a workout exercise's order or notes.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Workout exercise updated successfully",
            content = @Content(schema = @Schema(implementation = WorkoutExerciseResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Workout exercise not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @PutMapping("/exercises/{workoutExerciseId}")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkoutExercise(#workoutExerciseId)")
    public ResponseEntity<WorkoutExerciseResponse> updateWorkoutExercise(
            @Parameter(description = "Workout Exercise ID", required = true, example = "1")
            @PathVariable Long workoutExerciseId,
            @Parameter(description = "Update request with new order or notes")
            @Valid @RequestBody UpdateWorkoutExerciseRequest updateRequest) {
        logger.debug("Updating workout exercise. workoutExerciseId={}", workoutExerciseId);

        WorkoutExerciseResponse response = workoutSessionService.updateWorkoutExercise(workoutExerciseId, updateRequest);

        logger.info("Workout exercise updated successfully. workoutExerciseId={}", workoutExerciseId);

        return ResponseEntity.ok(response);
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
