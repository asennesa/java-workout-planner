package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

/**
 * Strength set entity extending BaseSet.
 * Contains strength-specific fields and logic.
 */
@Entity
@Table(name = "strength_sets")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class StrengthSet extends BaseSet {

    @Column(name = "reps", nullable = false)
    @NotNull(message = "Reps is required")
    @Min(value = 1, message = "Reps must be at least 1")
    @Max(value = 1000, message = "Reps cannot exceed 1000")
    private Integer reps;

    @Column(name = "weight", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    @DecimalMax(value = "999.99", message = "Weight cannot exceed 999.99")
    @Digits(integer = 3, fraction = 2, message = "Weight must have at most 3 integer digits and 2 decimal places")
    private BigDecimal weight;

    @Override
    public String getExerciseType() {
        return "STRENGTH";
    }

    @Override
    public String getSetSummary() {
        return String.format("%d reps @ %.2f kg", reps, weight != null ? weight : 0.0);
    }
}
