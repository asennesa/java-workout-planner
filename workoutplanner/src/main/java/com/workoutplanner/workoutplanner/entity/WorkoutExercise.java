package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.validation.ValidExerciseType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_exercises")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"workoutSession", "exercise", "strengthSets", "cardioSets", "flexibilitySets"})
@ValidExerciseType
public class WorkoutExercise {

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
    private List<StrengthSet> strengthSets = new ArrayList<>();

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private List<CardioSet> cardioSets = new ArrayList<>();

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private List<FlexibilitySet> flexibilitySets = new ArrayList<>();
}
