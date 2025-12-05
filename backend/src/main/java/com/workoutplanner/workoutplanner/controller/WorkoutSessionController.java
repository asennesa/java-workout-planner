package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.WorkoutActionRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.exception.OptimisticLockConflictException;
import com.workoutplanner.workoutplanner.service.WorkoutSessionService;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for workout session and workout exercise operations.
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/workouts")
@Tag(name = "Workout Sessions", description = "Manage workouts, exercises, and progress tracking")
public class WorkoutSessionController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutSessionController.class);

    private final WorkoutSessionService workoutSessionService;

    public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
        this.workoutSessionService = workoutSessionService;
    }

    // ==================== WORKOUT SESSION ENDPOINTS ====================

    @Operation(summary = "Create workout session", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Workout created",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @PostMapping
    @PreAuthorize("hasAuthority('write:workouts')")
    public ResponseEntity<WorkoutResponse> createWorkoutSession(
            @Valid @RequestBody CreateWorkoutRequest request) {
        logger.debug("Creating workout: {}", request.getName());

        WorkoutResponse response = workoutSessionService.createWorkoutSession(request);

        logger.info("Workout created. sessionId={}, status={}", response.getSessionId(), response.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get workout by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Workout found",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class)))
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAuthority('read:workouts') and @resourceSecurityService.canAccessWorkout(#sessionId)")
    public ResponseEntity<WorkoutResponse> getWorkoutSessionById(
            @Parameter(description = "Session ID", example = "1")
            @PathVariable Long sessionId) {
        logger.debug("Getting sessionId={}", sessionId);
        return ResponseEntity.ok(workoutSessionService.getWorkoutSessionById(sessionId));
    }

    @Operation(summary = "Get workout with optimized loading", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Workout retrieved",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class)))
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    @GetMapping("/{sessionId}/smart")
    @PreAuthorize("hasAuthority('read:workouts') and @resourceSecurityService.canAccessWorkout(#sessionId)")
    public ResponseEntity<WorkoutResponse> getWorkoutSessionWithSmartLoading(
            @Parameter(description = "Session ID", example = "1")
            @PathVariable Long sessionId) {
        logger.debug("Getting workout with smart loading. sessionId={}", sessionId);

        WorkoutResponse response = workoutSessionService.getWorkoutSessionWithSmartLoading(sessionId);

        logger.info("Retrieved sessionId={} with {} exercises", sessionId, response.getWorkoutExercises().size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get my workouts", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Workouts retrieved")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('read:workouts')")
    public ResponseEntity<List<WorkoutResponse>> getMyWorkouts() {
        logger.debug("Getting workouts for current user");

        List<WorkoutResponse> responses = workoutSessionService.getMyWorkouts();

        logger.info("Retrieved {} workouts for current user", responses.size());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get all workouts (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Workouts retrieved",
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    @GetMapping
    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<PagedResponse<WorkoutResponse>> getAllWorkoutSessions(
            @PageableDefault(size = 20, sort = "sessionId", direction = Sort.Direction.DESC) Pageable pageable) {
        logger.debug("Getting all workouts page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(workoutSessionService.getAllWorkoutSessions(pageable));
    }

    @Operation(summary = "Get workouts by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Workouts retrieved")
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('read:workouts') and (@userService.isCurrentUser(#userId) or hasAuthority('read:users'))")
    public ResponseEntity<List<WorkoutResponse>> getWorkoutSessionsByUserId(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId) {
        logger.debug("Getting workouts for userId={}", userId);
        return ResponseEntity.ok(workoutSessionService.getWorkoutSessionsByUserId(userId));
    }

    @Operation(summary = "Update workout", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Workout updated",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class)))
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    @PutMapping("/{sessionId}")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkout(#sessionId)")
    public ResponseEntity<WorkoutResponse> updateWorkoutSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateWorkoutRequest request) {
        logger.debug("Updating sessionId={}", sessionId);

        WorkoutResponse response = workoutSessionService.updateWorkoutSession(sessionId, request);

        logger.info("Updated sessionId={}", sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete workout", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Workout deleted")
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasAuthority('delete:workouts') and @resourceSecurityService.canModifyWorkout(#sessionId)")
    public ResponseEntity<Void> deleteWorkoutSession(@PathVariable Long sessionId) {
        logger.debug("Deleting sessionId={}", sessionId);

        workoutSessionService.deleteWorkoutSession(sessionId);

        logger.info("Deleted sessionId={}", sessionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update workout status", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Status updated",
            content = @Content(schema = @Schema(implementation = WorkoutResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid state transition", content = @Content)
    @ApiResponse(responseCode = "409", description = "Optimistic lock conflict", content = @Content)
    @PatchMapping("/{sessionId}/status")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkout(#sessionId)")
    public ResponseEntity<WorkoutResponse> updateWorkoutStatus(
            @Parameter(description = "Session ID", example = "1")
            @PathVariable Long sessionId,
            @Valid @RequestBody WorkoutActionRequest actionRequest) {
        logger.debug("Updating status. sessionId={}, action={}", sessionId, actionRequest.getAction());

        WorkoutResponse response = workoutSessionService.performAction(sessionId, actionRequest.getAction());

        logger.info("Status updated. sessionId={}, newStatus={}", sessionId, response.getStatus());
        return ResponseEntity.ok(response);
    }

    // ==================== WORKOUT EXERCISE ENDPOINTS ====================

    @Operation(summary = "Add exercise to workout", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Exercise added",
            content = @Content(schema = @Schema(implementation = WorkoutExerciseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Workout or exercise not found", content = @Content)
    @PostMapping("/{sessionId}/exercises")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkout(#sessionId)")
    public ResponseEntity<WorkoutExerciseResponse> addExerciseToWorkout(
            @Parameter(description = "Session ID", example = "1")
            @PathVariable Long sessionId,
            @Valid @RequestBody CreateWorkoutExerciseRequest request) {
        logger.debug("Adding exercise to sessionId={}, exerciseId={}", sessionId, request.getExerciseId());

        WorkoutExerciseResponse response = workoutSessionService.addExerciseToWorkout(sessionId, request);

        logger.info("Exercise added. workoutExerciseId={}, sessionId={}", response.getWorkoutExerciseId(), sessionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get exercises in workout", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Exercises retrieved")
    @ApiResponse(responseCode = "404", description = "Workout not found", content = @Content)
    @GetMapping("/{sessionId}/exercises")
    @PreAuthorize("hasAuthority('read:workouts') and @resourceSecurityService.canAccessWorkout(#sessionId)")
    public ResponseEntity<List<WorkoutExerciseResponse>> getWorkoutExercises(
            @Parameter(description = "Session ID", example = "1")
            @PathVariable Long sessionId) {
        logger.debug("Getting exercises for sessionId={}", sessionId);
        return ResponseEntity.ok(workoutSessionService.getWorkoutExercises(sessionId));
    }

    @Operation(summary = "Update workout exercise", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Exercise updated",
            content = @Content(schema = @Schema(implementation = WorkoutExerciseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    @PutMapping("/exercises/{workoutExerciseId}")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkoutExercise(#workoutExerciseId)")
    public ResponseEntity<WorkoutExerciseResponse> updateWorkoutExercise(
            @Parameter(description = "Workout Exercise ID", example = "1")
            @PathVariable Long workoutExerciseId,
            @Valid @RequestBody UpdateWorkoutExerciseRequest request) {
        logger.debug("Updating workoutExerciseId={}", workoutExerciseId);

        WorkoutExerciseResponse response = workoutSessionService.updateWorkoutExercise(workoutExerciseId, request);

        logger.info("Updated workoutExerciseId={}", workoutExerciseId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove exercise from workout", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Exercise removed")
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    @DeleteMapping("/exercises/{workoutExerciseId}")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canModifyWorkoutExercise(#workoutExerciseId)")
    public ResponseEntity<Void> removeExerciseFromWorkout(
            @Parameter(description = "Workout Exercise ID", example = "1")
            @PathVariable Long workoutExerciseId) {
        logger.debug("Removing workoutExerciseId={}", workoutExerciseId);

        workoutSessionService.removeExerciseFromWorkout(workoutExerciseId);

        logger.info("Removed workoutExerciseId={}", workoutExerciseId);
        return ResponseEntity.noContent().build();
    }

    // ==================== EXCEPTION HANDLER ====================

    @ExceptionHandler(OptimisticLockConflictException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockConflict(OptimisticLockConflictException ex) {
        logger.warn("Optimistic lock conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "CONFLICT",
                "message", ex.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.CONFLICT.value()
        ));
    }
}
