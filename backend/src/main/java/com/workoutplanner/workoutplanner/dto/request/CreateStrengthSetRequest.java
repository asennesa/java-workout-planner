package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO for creating strength training sets.
 * Tracks reps, weight, and optional rest time.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStrengthSetRequest {

    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    @Max(value = 50, message = "Set number cannot exceed 50")
    private Integer setNumber;

    @NotNull(message = "Reps are required for strength sets")
    @Min(value = 1, message = "Reps must be at least 1")
    @Max(value = 1000, message = "Reps cannot exceed 1000")
    private Integer reps;

    @NotNull(message = "Weight is required for strength sets")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000")
    private BigDecimal weight;

    @Min(value = 0, message = "Rest time cannot be negative")
    @Max(value = 3600, message = "Rest time cannot exceed 1 hour (3600 seconds)")
    private Integer restTimeInSeconds;

    @Size(max = 500, message = "Set notes must not exceed 500 characters")
    private String notes;

    private Boolean completed = false;
}

