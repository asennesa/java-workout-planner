package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Base controller for Set operations using Template Method pattern.
 * Provides consistent CRUD operations across set types (Strength, Cardio, Flexibility).
 *
 * @param <T> The specific set request type
 */
public abstract class BaseSetController<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseSetController.class);

    /**
     * Template method - concrete controllers provide their service implementation.
     */
    protected abstract SetServiceInterface<T> getService();

    @Operation(summary = "Create a new set", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Set created",
            content = @Content(schema = @Schema(implementation = SetResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Workout exercise not found", content = @Content)
    @PostMapping
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    @SuppressWarnings("java:S6856") // workoutExerciseId is bound via child controller's @RequestMapping
    public ResponseEntity<SetResponse> createSet(
            @Parameter(description = "Workout exercise ID", example = "1")
            @PathVariable Long workoutExerciseId,
            @Valid @RequestBody T request) {
        logger.debug("Creating set for workoutExerciseId={}", workoutExerciseId);

        SetResponse response = getService().createSet(workoutExerciseId, request);

        logger.info("Set created. setId={}, workoutExerciseId={}", response.getSetId(), workoutExerciseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all sets for a workout exercise", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Sets retrieved")
    @ApiResponse(responseCode = "404", description = "Workout exercise not found", content = @Content)
    @GetMapping
    @PreAuthorize("hasAuthority('read:workouts') and @resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    @SuppressWarnings("java:S6856") // workoutExerciseId is bound via child controller's @RequestMapping
    public ResponseEntity<List<SetResponse>> getSetsByWorkoutExercise(
            @Parameter(description = "Workout exercise ID", example = "1")
            @PathVariable Long workoutExerciseId) {
        logger.debug("Getting sets for workoutExerciseId={}", workoutExerciseId);

        List<SetResponse> responses = getService().getSetsByWorkoutExercise(workoutExerciseId);

        logger.info("Retrieved {} sets for workoutExerciseId={}", responses.size(), workoutExerciseId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get set by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Set found",
            content = @Content(schema = @Schema(implementation = SetResponse.class)))
    @ApiResponse(responseCode = "404", description = "Set not found", content = @Content)
    @GetMapping("/{setId}")
    @PreAuthorize("hasAuthority('read:workouts') and @resourceSecurityService.canAccessSet(#setId)")
    public ResponseEntity<SetResponse> getSetById(
            @Parameter(description = "Set ID", example = "1")
            @PathVariable Long setId) {
        logger.debug("Getting setId={}", setId);

        SetResponse response = getService().getSetById(setId);

        logger.info("Retrieved setId={}", setId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a set", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Set updated",
            content = @Content(schema = @Schema(implementation = SetResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "Set not found", content = @Content)
    @PutMapping("/{setId}")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canAccessSet(#setId)")
    public ResponseEntity<SetResponse> updateSet(
            @Parameter(description = "Set ID", example = "1")
            @PathVariable Long setId,
            @Valid @RequestBody T request) {
        logger.debug("Updating setId={}", setId);

        SetResponse response = getService().updateSet(setId, request);

        logger.info("Updated setId={}", setId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a set", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "Set deleted")
    @ApiResponse(responseCode = "404", description = "Set not found", content = @Content)
    @DeleteMapping("/{setId}")
    @PreAuthorize("hasAuthority('write:workouts') and @resourceSecurityService.canAccessSet(#setId)")
    public ResponseEntity<Void> deleteSet(
            @Parameter(description = "Set ID", example = "1")
            @PathVariable Long setId) {
        logger.debug("Deleting setId={}", setId);

        getService().deleteSet(setId);

        logger.info("Deleted setId={}", setId);
        return ResponseEntity.noContent().build();
    }
}
