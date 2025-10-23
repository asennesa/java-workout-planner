package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Cardio set entity extending BaseSet.
 * Contains cardio-specific fields and logic.
 */
@Entity
@Table(name = "cardio_sets", indexes = {
    @Index(name = "idx_cardio_set_workout_exercise", columnList = "workout_exercise_id"),
    @Index(name = "idx_cardio_set_number", columnList = "set_number")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class CardioSet extends BaseSet {

    @Column(name = "duration_in_seconds", nullable = false)
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 7200, message = "Duration cannot exceed 7200 seconds (2 hours)")
    private Integer durationInSeconds;

    @Column(name = "distance", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Distance must be non-negative")
    @DecimalMax(value = "999999.99", message = "Distance cannot exceed 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Distance must have at most 6 integer digits and 2 decimal places")
    private BigDecimal distance;

    @Column(name = "distance_unit", length = 10)
    @Length(max = 10, message = "Distance unit must not exceed 10 characters")
    @Pattern(regexp = "^(km|m|miles|yards|feet|meters)?$", message = "Distance unit must be a valid unit (km, m, miles, yards, feet, meters) or empty")
    private String distanceUnit;

    @Override
    public String getExerciseType() {
        return "CARDIO";
    }

    @Override
    public String getSetSummary() {
        String distanceStr = distance != null ? String.format("%.2f %s", distance, distanceUnit != null ? distanceUnit : "units") : "no distance";
        return String.format("%d seconds, %s", durationInSeconds, distanceStr);
    }

    /**
     * Equals method for CardioSet.
     * Uses parent class logic but includes cardio-specific fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        
        CardioSet cardioSet = (CardioSet) o;
        
        // Compare cardio-specific fields
        return Objects.equals(durationInSeconds, cardioSet.durationInSeconds) &&
               Objects.equals(distance, cardioSet.distance) &&
               Objects.equals(distanceUnit, cardioSet.distanceUnit);
    }

    /**
     * HashCode method for CardioSet.
     * Combines parent hash with cardio-specific fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), durationInSeconds, distance, distanceUnit);
    }
}
