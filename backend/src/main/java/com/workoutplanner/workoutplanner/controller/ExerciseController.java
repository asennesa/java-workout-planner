package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.service.ExerciseService;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only controller for browsing the exercise library.
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/exercises")
@Validated
@Tag(name = "Exercise Library", description = "Browse exercises (Strength, Cardio, Flexibility)")
public class ExerciseController {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseController.class);

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @Operation(summary = "Get exercise by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Exercise found",
            content = @Content(schema = @Schema(implementation = ExerciseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content)
    @GetMapping("/{exerciseId}")
    @PreAuthorize("hasAuthority('read:exercises')")
    public ResponseEntity<ExerciseResponse> getExerciseById(
            @Parameter(description = "Exercise ID", example = "1")
            @PathVariable Long exerciseId) {
        logger.debug("Getting exerciseId={}", exerciseId);
        return ResponseEntity.ok(exerciseService.getExerciseById(exerciseId));
    }

    @Operation(summary = "Get all exercises (paginated)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Exercises retrieved",
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @GetMapping
    @PreAuthorize("hasAuthority('read:exercises')")
    public ResponseEntity<PagedResponse<ExerciseResponse>> getAllExercises(
            @PageableDefault(size = 20, sort = "exerciseId", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.debug("Fetching exercises page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        PagedResponse<ExerciseResponse> response = exerciseService.getAllExercises(pageable);

        logger.info("Retrieved {} exercises on page {}/{}",
                response.getContent().size(), response.getPageNumber(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search exercises by name", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Search completed")
    @ApiResponse(responseCode = "400", description = "Invalid search term", content = @Content)
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('read:exercises')")
    public ResponseEntity<List<ExerciseResponse>> searchExercisesByName(
            @Parameter(description = "Exercise name to search", example = "bench press")
            @RequestParam
            @NotBlank(message = "Search term cannot be empty")
            @Size(min = 2, max = 100, message = "Search term must be between 2 and 100 characters")
            String name) {
        logger.debug("Searching exercises by name: {}", name);

        List<ExerciseResponse> results = exerciseService.searchExercisesByName(name);

        logger.info("Found {} exercises matching '{}'", results.size(), name);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Filter exercises by criteria", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Filtered exercises retrieved")
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('read:exercises')")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByCriteria(
            @Parameter(description = "Exercise type", example = "STRENGTH")
            @RequestParam(required = false) ExerciseType type,
            @Parameter(description = "Target muscle group", example = "CHEST")
            @RequestParam(required = false) TargetMuscleGroup targetMuscleGroup,
            @Parameter(description = "Difficulty level", example = "INTERMEDIATE")
            @RequestParam(required = false) DifficultyLevel difficultyLevel) {
        logger.debug("Filtering exercises: type={}, muscle={}, difficulty={}", type, targetMuscleGroup, difficultyLevel);

        List<ExerciseResponse> results = exerciseService.getExercisesByCriteria(type, targetMuscleGroup, difficultyLevel);

        logger.info("Found {} exercises matching filter criteria", results.size());
        return ResponseEntity.ok(results);
    }
}
