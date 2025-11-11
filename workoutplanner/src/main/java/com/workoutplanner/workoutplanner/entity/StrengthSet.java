package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Strength set entity extending BaseSet.
 * Contains strength-specific fields and logic.
 */
@Entity
@Table(name = "strength_sets", indexes = {
    @Index(name = "idx_strength_set_workout_exercise", columnList = "workout_exercise_id"),
    @Index(name = "idx_strength_set_number", columnList = "set_number")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class StrengthSet extends BaseSet {

    @Column(name = "reps", nullable = false)
    private Integer reps;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Override
    public String getExerciseType() {
        return "STRENGTH";
    }

    @Override
    public String getSetSummary() {
        return String.format("%d reps @ %.2f kg", reps, weight != null ? weight : 0.0);
    }

    /**
     * Equals method for StrengthSet.
     * Uses parent class logic but includes strength-specific fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        
        StrengthSet that = (StrengthSet) o;
        
        // Compare strength-specific fields
        return Objects.equals(reps, that.reps) &&
               Objects.equals(weight, that.weight);
    }

    /**
     * HashCode method for StrengthSet.
     * Combines parent hash with strength-specific fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), reps, weight);
    }
}
