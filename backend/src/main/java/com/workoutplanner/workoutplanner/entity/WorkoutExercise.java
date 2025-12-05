package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Workout exercise entity linking exercises to workout sessions.
 */
@Entity
@Table(name = "workout_exercises", indexes = {
    @Index(name = "idx_workout_exercise_session_id", columnList = "session_id"),
    @Index(name = "idx_workout_exercise_exercise_id", columnList = "exercise_id"),
    @Index(name = "idx_workout_exercise_order", columnList = "order_in_workout")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"workoutSession", "exercise", "strengthSets", "cardioSets", "flexibilitySets"})
public class WorkoutExercise extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_exercise_id", nullable = false, updatable = false)
    private Long workoutExerciseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession workoutSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "order_in_workout", nullable = false)
    private Integer orderInWorkout;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    @BatchSize(size = 20)
    @Fetch(FetchMode.SUBSELECT)
    private List<StrengthSet> strengthSets = new ArrayList<>();

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    @BatchSize(size = 20)
    @Fetch(FetchMode.SUBSELECT)
    private List<CardioSet> cardioSets = new ArrayList<>();

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    @BatchSize(size = 20)
    @Fetch(FetchMode.SUBSELECT)
    private List<FlexibilitySet> flexibilitySets = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkoutExercise that = (WorkoutExercise) o;
        if (workoutExerciseId != null && that.workoutExerciseId != null) {
            return Objects.equals(workoutExerciseId, that.workoutExerciseId);
        }
        return Objects.equals(workoutSession, that.workoutSession) &&
               Objects.equals(exercise, that.exercise) &&
               Objects.equals(orderInWorkout, that.orderInWorkout);
    }

    @Override
    public int hashCode() {
        if (workoutExerciseId != null) {
            return Objects.hash(workoutExerciseId);
        }
        return Objects.hash(workoutSession, exercise, orderInWorkout);
    }
}
