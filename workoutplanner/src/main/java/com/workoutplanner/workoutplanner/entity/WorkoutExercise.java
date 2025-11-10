package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @NotNull(message = "Workout session is required")
    private WorkoutSession workoutSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @NotNull(message = "Exercise is required")
    private Exercise exercise;

    @Column(name = "order_in_workout", nullable = false)
    @NotNull(message = "Order in workout is required")
    @Min(value = 1, message = "Order must be at least 1")
    @Max(value = 100, message = "Order cannot exceed 100")
    private Integer orderInWorkout;

    @Column(name = "notes", length = 500)
    @Length(max = 500, message = "Notes must not exceed 500 characters")
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

    /**
     * Equals method following Hibernate best practices.
     * Uses database ID for equality when entity is persisted,
     * falls back to field comparison for transient entities.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        WorkoutExercise that = (WorkoutExercise) o;
        
        // If both entities are persisted (have IDs), use ID for equality
        if (workoutExerciseId != null && that.workoutExerciseId != null) {
            return Objects.equals(workoutExerciseId, that.workoutExerciseId);
        }
        
        // For transient entities, compare unique fields (workoutSession, exercise, orderInWorkout)
        return Objects.equals(workoutSession, that.workoutSession) &&
               Objects.equals(exercise, that.exercise) &&
               Objects.equals(orderInWorkout, that.orderInWorkout);
    }

    /**
     * HashCode method following Hibernate best practices.
     * Uses database ID when entity is persisted,
     * falls back to unique fields for transient entities.
     */
    @Override
    public int hashCode() {
        // If entity is persisted, use ID for hash
        if (workoutExerciseId != null) {
            return Objects.hash(workoutExerciseId);
        }
        
        // For transient entities, use unique fields
        return Objects.hash(workoutSession, exercise, orderInWorkout);
    }
}
