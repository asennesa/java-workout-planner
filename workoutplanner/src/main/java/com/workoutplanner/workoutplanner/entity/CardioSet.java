package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
    private Integer durationInSeconds;

    @Column(name = "distance", precision = 8, scale = 2)
    private BigDecimal distance;

    @Column(name = "distance_unit", length = 10)
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
