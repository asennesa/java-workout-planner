package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * Unified DTO for creating all types of sets (Cardio, Strength, Flexibility).
 * 
 * This DTO consolidates the three separate set creation DTOs into a single,
 * flexible request object that handles type-specific validation based on
 * the provided fields.
 * 
 * Common Fields (all set types):
 * - workoutExerciseId, setNumber, restTimeInSeconds, notes, completed
 * 
 * Type-Specific Fields:
 * - Cardio: durationInSeconds, distance, distanceUnit
 * - Strength: reps, weight
 * - Flexibility: durationInSeconds, stretchType, intensity
 * 
 * Validation:
 * - Common fields are always validated
 * - Type-specific fields are validated based on set type detection
 * - Set type is automatically detected from provided fields
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSetRequest {

    // Common fields for all set types
    @NotNull(message = "Workout exercise ID is required")
    private Long workoutExerciseId;

    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    @Max(value = 50, message = "Set number cannot exceed 50")
    private Integer setNumber;

    @Min(value = 0, message = "Rest time cannot be negative")
    @Max(value = 3600, message = "Rest time cannot exceed 1 hour (3600 seconds)")
    private Integer restTimeInSeconds;

    @Length(max = 500, message = "Set notes must not exceed 500 characters")
    private String notes;

    private Boolean completed = false;

    // Cardio-specific fields
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 14400, message = "Duration cannot exceed 4 hours (14400 seconds)")
    private Integer durationInSeconds;

    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    @DecimalMax(value = "1000.0", message = "Distance cannot exceed 1000")
    private BigDecimal distance;

    @Length(max = 10, message = "Distance unit must not exceed 10 characters")
    private String distanceUnit;

    // Strength-specific fields
    @Min(value = 1, message = "Reps must be at least 1")
    @Max(value = 1000, message = "Reps cannot exceed 1000")
    private Integer reps;

    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000")
    private BigDecimal weight;

    // Flexibility-specific fields
    @Length(min = 2, max = 50, message = "Stretch type must be between 2 and 50 characters")
    private String stretchType;

    @Min(value = 1, message = "Intensity must be at least 1")
    @Max(value = 10, message = "Intensity cannot exceed 10")
    private Integer intensity;

    // Helper methods for set type detection
    /**
     * Check if this is a cardio set.
     * Cardio sets are identified by the presence of distance or distanceUnit fields.
     * 
     * @return true if this is a cardio set, false otherwise
     */
    public boolean isCardioSet() {
        return distance != null || distanceUnit != null;
    }

    /**
     * Check if this is a strength set.
     * Strength sets are identified by the presence of both reps and weight fields.
     * 
     * @return true if this is a strength set, false otherwise
     */
    public boolean isStrengthSet() {
        return reps != null && weight != null;
    }

    /**
     * Check if this is a flexibility set.
     * Flexibility sets are identified by the presence of stretchType or intensity fields.
     * 
     * @return true if this is a flexibility set, false otherwise
     */
    public boolean isFlexibilitySet() {
        return stretchType != null || intensity != null;
    }

    /**
     * Get the detected set type based on provided fields.
     * 
     * @return the set type as a string ("CARDIO", "STRENGTH", "FLEXIBILITY", or "UNKNOWN")
     */
    public String getSetType() {
        if (isCardioSet()) return "CARDIO";
        if (isStrengthSet()) return "STRENGTH";
        if (isFlexibilitySet()) return "FLEXIBILITY";
        return "UNKNOWN";
    }

    /**
     * Check if the set type is valid (exactly one type is detected).
     * 
     * @return true if exactly one set type is detected, false otherwise
     */
    public boolean hasValidSetType() {
        int typeCount = 0;
        if (isCardioSet()) typeCount++;
        if (isStrengthSet()) typeCount++;
        if (isFlexibilitySet()) typeCount++;
        return typeCount == 1;
    }

    /**
     * Check if all required fields for the detected set type are provided.
     * 
     * @return true if all required fields are present, false otherwise
     */
    public boolean hasRequiredFields() {
        if (isCardioSet()) {
            return durationInSeconds != null;
        } else if (isStrengthSet()) {
            return reps != null && weight != null;
        } else if (isFlexibilitySet()) {
            return durationInSeconds != null && stretchType != null && intensity != null;
        }
        return false;
    }

    /**
     * Get a summary of the set for logging/debugging.
     * 
     * @return a string summary of the set
     */
    public String getSetSummary() {
        return String.format("SetRequest{type=%s, exerciseId=%d, setNumber=%d, completed=%s}", 
                           getSetType(), workoutExerciseId, setNumber, completed);
    }
}
