package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * DTO for creating cardiovascular exercise sets.
 * 
 * Note: workoutExerciseId is NOT included here as it comes from the URL path parameter.
 * This follows REST best practices where resource identifiers belong in the URL.
 * 
 * Cardio sets track:
 * - Duration of exercise
 * - Distance covered (optional)
 * - Distance unit (optional)
 * 
 * Duration is required. Distance and unit are optional.
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCardioSetRequest {

    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    @Max(value = 50, message = "Set number cannot exceed 50")
    private Integer setNumber;

    @NotNull(message = "Duration is required for cardio sets")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 14400, message = "Duration cannot exceed 4 hours (14400 seconds)")
    private Integer durationInSeconds;

    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    @DecimalMax(value = "1000.0", message = "Distance cannot exceed 1000")
    private BigDecimal distance;

    @Length(max = 10, message = "Distance unit must not exceed 10 characters")
    private String distanceUnit;

    @Min(value = 0, message = "Rest time cannot be negative")
    @Max(value = 3600, message = "Rest time cannot exceed 1 hour (3600 seconds)")
    private Integer restTimeInSeconds;

    @Length(max = 500, message = "Set notes must not exceed 500 characters")
    private String notes;

    private Boolean completed = false;
}

