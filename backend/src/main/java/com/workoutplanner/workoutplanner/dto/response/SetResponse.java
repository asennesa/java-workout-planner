package com.workoutplanner.workoutplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Unified response for all set types (Cardio, Strength, Flexibility).
 * Type-specific fields are null for other set types.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetResponse {

    // Common fields
    private Long setId;
    private Long workoutExerciseId;
    private Integer setNumber;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;

    // Cardio-specific fields
    private Integer durationInSeconds;
    private BigDecimal distance;
    private String distanceUnit;

    // Strength-specific fields
    private Integer reps;
    private BigDecimal weight;

    // Flexibility-specific fields
    private String stretchType;
    private Integer intensity;
}
