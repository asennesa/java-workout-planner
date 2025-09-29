package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "cardio_sets")
public class CardioSet {

    private Long setId;
    private WorkoutExercise workoutExercise;
    private Integer setNumber;
    private Integer durationInSeconds;
    private Double distance;
    private String distanceUnit;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;

    public CardioSet() {
        // Default constructor for Hibernate
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

    @Column(name = "duration_in_seconds", nullable = false)
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 7200, message = "Duration cannot exceed 7200 seconds (2 hours)")
    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    @Column(name = "distance", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Distance must be non-negative")
    @DecimalMax(value = "999999.99", message = "Distance cannot exceed 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Distance must have at most 6 integer digits and 2 decimal places")
    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Column(name = "distance_unit", length = 10)
    @Length(max = 10, message = "Distance unit must not exceed 10 characters")
    @Pattern(regexp = "^(km|m|miles|yards|feet|meters)?$", message = "Distance unit must be a valid unit (km, m, miles, yards, feet, meters) or empty")
    public String getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;
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

}
