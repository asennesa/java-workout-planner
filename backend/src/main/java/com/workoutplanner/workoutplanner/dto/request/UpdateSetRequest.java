package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Unified DTO for updating all set types (Cardio, Strength, Flexibility).
 * All fields are optional for partial updates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSetRequest {

    // Common fields
    @Min(value = 1, message = "Set number must be at least 1")
    @Max(value = 50, message = "Set number cannot exceed 50")
    private Integer setNumber;

    @Min(value = 0, message = "Rest time cannot be negative")
    @Max(value = 3600, message = "Rest time cannot exceed 1 hour (3600 seconds)")
    private Integer restTimeInSeconds;

    @Size(max = 500, message = "Set notes must not exceed 500 characters")
    private String notes;

    private Boolean completed;

    // Cardio-specific fields
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 14400, message = "Duration cannot exceed 4 hours (14400 seconds)")
    private Integer durationInSeconds;

    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    @DecimalMax(value = "1000.0", message = "Distance cannot exceed 1000")
    private BigDecimal distance;

    @Size(max = 10, message = "Distance unit must not exceed 10 characters")
    private String distanceUnit;

    // Strength-specific fields
    @Min(value = 1, message = "Reps must be at least 1")
    @Max(value = 1000, message = "Reps cannot exceed 1000")
    private Integer reps;

    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000")
    private BigDecimal weight;

    // Flexibility-specific fields
    @Size(min = 2, max = 50, message = "Stretch type must be between 2 and 50 characters")
    private String stretchType;

    @Min(value = 1, message = "Intensity must be at least 1")
    @Max(value = 10, message = "Intensity cannot exceed 10")
    private Integer intensity;

    public boolean hasUpdates() {
        return setNumber != null || restTimeInSeconds != null || notes != null || completed != null ||
               durationInSeconds != null || distance != null || distanceUnit != null ||
               reps != null || weight != null || stretchType != null || intensity != null;
    }

    public boolean hasCommonFieldUpdates() {
        return setNumber != null || restTimeInSeconds != null || notes != null || completed != null;
    }

    public boolean hasCardioFieldUpdates() {
        return durationInSeconds != null || distance != null || distanceUnit != null;
    }

    public boolean hasStrengthFieldUpdates() {
        return reps != null || weight != null;
    }

    public boolean hasFlexibilityFieldUpdates() {
        return stretchType != null || intensity != null;
    }
}
