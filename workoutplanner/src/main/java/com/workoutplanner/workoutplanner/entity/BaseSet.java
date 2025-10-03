package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

/**
 * Abstract base class for all set types.
 * Contains common fields and validation logic.
 * 
 * This follows the Template Method Pattern and DRY principle.
 */
@MappedSuperclass
public abstract class BaseSet {

    private Long setId;
    private WorkoutExercise workoutExercise;
    private Integer setNumber;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;

    public BaseSet() {
        // Default constructor
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "set_id", nullable = false, updatable = false)
    public Long getSetId() {
        return setId;
    }

    public void setSetId(Long setId) {
        this.setId = setId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    @NotNull(message = "Workout exercise is required")
    public WorkoutExercise getWorkoutExercise() {
        return workoutExercise;
    }

    public void setWorkoutExercise(WorkoutExercise workoutExercise) {
        this.workoutExercise = workoutExercise;
    }

    @Column(name = "set_number", nullable = false)
    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    @Max(value = 50, message = "Set number cannot exceed 50")
    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    @Column(name = "rest_time_in_seconds")
    @Min(value = 0, message = "Rest time must be non-negative")
    @Max(value = 3600, message = "Rest time cannot exceed 3600 seconds (1 hour)")
    public Integer getRestTimeInSeconds() {
        return restTimeInSeconds;
    }

    public void setRestTimeInSeconds(Integer restTimeInSeconds) {
        this.restTimeInSeconds = restTimeInSeconds;
    }

    @Column(name = "notes", length = 500)
    @Length(max = 500, message = "Notes must not exceed 500 characters")
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Column(name = "completed", nullable = false)
    @NotNull(message = "Completed status is required")
    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    /**
     * Abstract method to get the exercise type.
     * Each concrete class must implement this.
     */
    public abstract String getExerciseType();

    /**
     * Template method for calculating set duration.
     * Can be overridden by subclasses if needed.
     */
    public Integer getDurationInSeconds() {
        return null; // Default implementation
    }

    /**
     * Template method for getting set summary.
     * Each subclass can provide its own implementation.
     */
    public abstract String getSetSummary();
}
