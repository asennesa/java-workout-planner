package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Flexibility set entity with stretch type and intensity (1-10 scale).
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
    private Integer intensity;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
