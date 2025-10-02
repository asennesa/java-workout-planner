package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.validation.ValidExerciseType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_exercises")
@ValidExerciseType
public class WorkoutExercise {

    private Long workoutExerciseId;
    private WorkoutSession workoutSession;
    private Exercise exercise;
    private Integer orderInWorkout;
    private String notes;
    private List<StrengthSet> strengthSets = new ArrayList<>();
    private List<CardioSet> cardioSets = new ArrayList<>();
    private List<FlexibilitySet> flexibilitySets = new ArrayList<>();

    public WorkoutExercise() {
        // Default constructor for Hibernate
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_exercise_id", nullable = false, updatable = false)
    public Long getWorkoutExerciseId() {
        return workoutExerciseId;
    }

    public void setWorkoutExerciseId(Long workoutExerciseId) {
        this.workoutExerciseId = workoutExerciseId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @NotNull(message = "Workout session is required")
    public WorkoutSession getWorkoutSession() {
        return workoutSession;
    }

    public void setWorkoutSession(WorkoutSession workoutSession) {
        this.workoutSession = workoutSession;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @NotNull(message = "Exercise is required")
    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    @Column(name = "order_in_workout", nullable = false)
    @NotNull(message = "Order in workout is required")
    @Min(value = 1, message = "Order must be at least 1")
    @Max(value = 100, message = "Order cannot exceed 100")
    public Integer getOrderInWorkout() {
        return orderInWorkout;
    }

    public void setOrderInWorkout(Integer orderInWorkout) {
        this.orderInWorkout = orderInWorkout;
    }

    @Column(name = "notes", length = 500)
    @Length(max = 500, message = "Notes must not exceed 500 characters")
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("setNumber ASC")
    public List<StrengthSet> getStrengthSets() {
        return strengthSets;
    }

    public void setStrengthSets(List<StrengthSet> strengthSets) {
        this.strengthSets = strengthSets;
    }

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("setNumber ASC")
    public List<CardioSet> getCardioSets() {
        return cardioSets;
    }

    public void setCardioSets(List<CardioSet> cardioSets) {
        this.cardioSets = cardioSets;
    }

    @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("setNumber ASC")
    public List<FlexibilitySet> getFlexibilitySets() {
        return flexibilitySets;
    }

    public void setFlexibilitySets(List<FlexibilitySet> flexibilitySets) {
        this.flexibilitySets = flexibilitySets;
    }

}
