package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * Abstract base class for all set types (Strength, Cardio, Flexibility).
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "workoutExercise")
public abstract class BaseSet extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "set_id", nullable = false, updatable = false)
    private Long setId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Column(name = "rest_time_in_seconds")
    private Integer restTimeInSeconds;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseSet baseSet = (BaseSet) o;
        if (setId != null && baseSet.setId != null) {
            return Objects.equals(setId, baseSet.setId);
        }
        return Objects.equals(setNumber, baseSet.setNumber) &&
               Objects.equals(workoutExercise, baseSet.workoutExercise);
    }

    @Override
    public int hashCode() {
        if (setId != null) {
            return Objects.hash(setId);
        }
        return Objects.hash(setNumber, workoutExercise);
    }
}
