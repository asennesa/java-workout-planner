package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.CreateExerciseRequest;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Exercise operations.
 * Provides endpoints for exercise management following REST API best practices.
 * 
 * CORS is configured globally in CorsConfig.java
 * API Version: v1
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/exercises")
@Validated
@Tag(name = "Exercise Management", description = "Endpoints for managing the exercise library (Strength, Cardio, Flexibility)")
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
     * Create a new exercise.
     * 
     * @param createExerciseRequest the exercise creation request
     * @return ResponseEntity containing the created exercise response
     */
    @Operation(
        summary = "Create a new exercise (Admin/Moderator only)",
        description = "Creates a new exercise in the library. Only accessible by ADMIN and MODERATOR roles.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Exercise created successfully",
            content = @Content(schema = @Schema(implementation = ExerciseResponse.class))
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
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - requires ADMIN or MODERATOR role",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @Parameter(description = "Exercise details", required = true)
            @Valid @RequestBody CreateExerciseRequest createExerciseRequest) {
        logger.debug("Creating exercise: name={}, type={}", createExerciseRequest.getName(), createExerciseRequest.getType());
        
        ExerciseResponse exerciseResponse = exerciseService.createExercise(createExerciseRequest);
        
        logger.info("Exercise created successfully. exerciseId={}, name={}, type={}", 
                   exerciseResponse.getExerciseId(), exerciseResponse.getName(), exerciseResponse.getType());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseResponse);
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
    
    /**
     * Update exercise.
     * 
     * @param exerciseId the exercise ID
     * @param createExerciseRequest the updated exercise information
     * @return ResponseEntity containing the updated exercise response
     */
    @Operation(
        summary = "Update exercise (Admin/Moderator only)",
        description = "Updates an existing exercise. Only accessible by ADMIN and MODERATOR roles.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exercise updated successfully",
            content = @Content(schema = @Schema(implementation = ExerciseResponse.class))
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
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - requires ADMIN or MODERATOR role",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found",
            content = @Content
        )
    })
    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @Parameter(description = "Exercise ID", required = true, example = "1")
            @PathVariable Long exerciseId, 
            @Parameter(description = "Updated exercise details", required = true)
            @Valid @RequestBody CreateExerciseRequest createExerciseRequest) {
        ExerciseResponse exerciseResponse = exerciseService.updateExercise(exerciseId, createExerciseRequest);
        return ResponseEntity.ok(exerciseResponse);
    }
    
    /**
     * Delete exercise by ID.
     * 
     * @param exerciseId the exercise ID
     * @return ResponseEntity with no content
     */
    @Operation(
        summary = "Delete exercise (Admin only)",
        description = "Permanently deletes an exercise from the library. Only accessible by ADMIN role.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Exercise deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - requires ADMIN role",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Exercise not found",
            content = @Content
        )
    })
    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @Parameter(description = "Exercise ID to delete", required = true, example = "1")
            @PathVariable Long exerciseId) {
        logger.warn("Deleting exercise: exerciseId={}", exerciseId);
        
        exerciseService.deleteExercise(exerciseId);
        
        logger.info("Exercise deleted successfully. exerciseId={}", exerciseId);
        
        return ResponseEntity.noContent().build();
    }
}
