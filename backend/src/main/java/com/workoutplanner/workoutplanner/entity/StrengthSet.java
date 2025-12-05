package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Strength set entity with reps and weight tracking.
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
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
