package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Cardio set entity with duration and distance tracking.
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
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
