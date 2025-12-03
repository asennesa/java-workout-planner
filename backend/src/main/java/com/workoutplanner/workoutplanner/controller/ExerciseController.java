package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
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
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.service.ExerciseService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Exercise operations.
 * Provides read-only endpoints for browsing the exercise library.
 *
 * Note: Exercise creation, modification, and deletion are restricted to administrators.
 * Users can only browse and use the preloaded exercises in their workouts.
 *
 * CORS is configured globally in CorsConfig.java
 * API Version: v1
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/exercises")
@Validated
@Tag(name = "Exercise Library", description = "Read-only endpoints for browsing the exercise library (Strength, Cardio, Flexibility)")
public class ExerciseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExerciseController.class);
    
    private final ExerciseService exerciseService;
    
    /**
     * Constructor injection for dependencies.
     */
    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }
    
    /**
     * Get exercise by ID.
     * 
     * @param exerciseId the exercise ID
     * @return ResponseEntity containing the exercise response
     */
    @Operation(
        summary = "Get exercise by ID",
        description = "Retrieves detailed information about a specific exercise.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise found",
            content = @Content(schema = @Schema(implementation = ExerciseResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/{exerciseId}")
    @PreAuthorize("hasAuthority('read:exercises')")
    public ResponseEntity<ExerciseResponse> getExerciseById(
            @Parameter(description = "Exercise ID", required = true, example = "1")
            @PathVariable Long exerciseId) {
        ExerciseResponse exerciseResponse = exerciseService.getExerciseById(exerciseId);
        return ResponseEntity.ok(exerciseResponse);
    }
    
    /**
     * Get all exercises with pagination.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return ResponseEntity containing paginated exercise responses
     */
    @Operation(
        summary = "Get all exercises (paginated)",
        description = "Retrieves a paginated list of all exercises in the library. Supports sorting and pagination.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercises retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('read:exercises')")
    public ResponseEntity<PagedResponse<ExerciseResponse>> getAllExercises(
            @Parameter(description = "Pagination parameters", example = "page=0&size=20&sort=name,asc")
            @PageableDefault(size = 20, sort = "exerciseId", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.debug("Fetching exercises with pagination. page={}, size={}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        
        PagedResponse<ExerciseResponse> pagedResponse = exerciseService.getAllExercises(pageable);
        
        logger.info("Retrieved {} exercises on page {} of {}", 
                   pagedResponse.getContent().size(), 
                   pagedResponse.getPageNumber(), 
                   pagedResponse.getTotalPages());
        
        return ResponseEntity.ok(pagedResponse);
    }
    
    /**
     * Search exercises by name.
     * Input is validated and sanitized to prevent SQL injection and wildcard abuse.
     * 
     * @param name the name to search for (min 2, max 100 characters)
     * @return ResponseEntity containing list of exercises with names containing the search term
     */
    @Operation(
        summary = "Search exercises by name",
        description = "Searches for exercises by name (case-insensitive, partial match). Minimum 2 characters required.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid search term - must be 2-100 characters",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('read:exercises')")
    public ResponseEntity<List<ExerciseResponse>> searchExercisesByName(
            @Parameter(description = "Exercise name to search for", required = true, example = "bench press")
            @RequestParam 
            @NotBlank(message = "Search term cannot be empty")
            @Size(min = 2, max = 100, message = "Search term must be between 2 and 100 characters") 
            String name) {
        logger.debug("Searching exercises by name: {}", name);
        
        List<ExerciseResponse> exerciseResponses = exerciseService.searchExercisesByName(name);
        
        logger.info("Found {} exercises matching name search", exerciseResponses.size());
        
        return ResponseEntity.ok(exerciseResponses);
    }
    
    /**
     * Get exercises by multiple criteria.
     * 
     * @param type the exercise type (optional)
     * @param targetMuscleGroup the target muscle group (optional)
     * @param difficultyLevel the difficulty level (optional)
     * @return ResponseEntity containing list of exercises matching the criteria
     */
    @Operation(
        summary = "Filter exercises by criteria",
        description = "Filters exercises by type, target muscle group, and/or difficulty level. All parameters are optional and can be combined.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Filtered exercises retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('read:exercises')")
    public ResponseEntity<List<ExerciseResponse>> getExercisesByCriteria(
            @Parameter(description = "Exercise type (STRENGTH, CARDIO, FLEXIBILITY)", example = "STRENGTH")
            @RequestParam(required = false) ExerciseType type,
            @Parameter(description = "Target muscle group", example = "CHEST")
            @RequestParam(required = false) TargetMuscleGroup targetMuscleGroup,
            @Parameter(description = "Difficulty level", example = "INTERMEDIATE")
            @RequestParam(required = false) DifficultyLevel difficultyLevel) {
        List<ExerciseResponse> exerciseResponses = exerciseService.getExercisesByCriteria(type, targetMuscleGroup, difficultyLevel);
        return ResponseEntity.ok(exerciseResponses);
    }
}
