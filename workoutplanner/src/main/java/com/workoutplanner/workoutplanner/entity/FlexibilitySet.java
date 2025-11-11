package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.Objects;

/**
 * Flexibility set entity extending BaseSet.
 * Contains flexibility-specific fields and logic.
 */
@Entity
@Table(name = "flexibility_sets", indexes = {
    @Index(name = "idx_flexibility_set_workout_exercise", columnList = "workout_exercise_id"),
    @Index(name = "idx_flexibility_set_number", columnList = "set_number")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class FlexibilitySet extends BaseSet {

    @Column(name = "duration_in_seconds", nullable = false)
    private Integer durationInSeconds;

    @Column(name = "stretch_type", length = 50)
    private String stretchType;

    @Column(name = "intensity")
    private Integer intensity; // 1-10 scale

    @Override
    public String getExerciseType() {
        return "FLEXIBILITY";
    }

    @Override
    public String getSetSummary() {
        return String.format("%d seconds of %s (intensity: %d/10)", 
                           durationInSeconds, stretchType, intensity);
    }

    /**
     * Equals method for FlexibilitySet.
     * Uses parent class logic but includes flexibility-specific fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        
        FlexibilitySet that = (FlexibilitySet) o;
        
        // Compare flexibility-specific fields
        return Objects.equals(durationInSeconds, that.durationInSeconds) &&
               Objects.equals(stretchType, that.stretchType) &&
               Objects.equals(intensity, that.intensity);
    }

    /**
     * HashCode method for FlexibilitySet.
     * Combines parent hash with flexibility-specific fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), durationInSeconds, stretchType, intensity);
    }
}
