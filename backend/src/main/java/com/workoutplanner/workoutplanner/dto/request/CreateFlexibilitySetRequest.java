package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating flexibility/stretching exercise sets.
 * Tracks duration, stretch type, and intensity (1-10 scale).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlexibilitySetRequest {

    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    @Max(value = 50, message = "Set number cannot exceed 50")
    private Integer setNumber;

    @NotNull(message = "Duration is required for flexibility sets")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 14400, message = "Duration cannot exceed 4 hours (14400 seconds)")
    private Integer durationInSeconds;

    @NotNull(message = "Stretch type is required for flexibility sets")
    @Size(min = 2, max = 50, message = "Stretch type must be between 2 and 50 characters")
    private String stretchType;

    @NotNull(message = "Intensity is required for flexibility sets")
    @Min(value = 1, message = "Intensity must be at least 1")
    @Max(value = 10, message = "Intensity cannot exceed 10")
    private Integer intensity;

    @Min(value = 0, message = "Rest time cannot be negative")
    @Max(value = 3600, message = "Rest time cannot exceed 1 hour (3600 seconds)")
    private Integer restTimeInSeconds;

    @Size(max = 500, message = "Set notes must not exceed 500 characters")
    private String notes;

    private Boolean completed = false;
}

