package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public class CreateCardioSetRequest {

    @NotNull(message = "Workout exercise ID is required")
    private Long workoutExerciseId;

    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    @Max(value = 50, message = "Set number cannot exceed 50")
    private Integer setNumber;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 14400, message = "Duration cannot exceed 4 hours (14400 seconds)")
    private Integer durationInSeconds;

    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    @DecimalMax(value = "1000.0", message = "Distance cannot exceed 1000")
    private BigDecimal distance;

    @Length(max = 10, message = "Distance unit must not exceed 10 characters")
    private String distanceUnit;

    @Min(value = 0, message = "Rest time cannot be negative")
    @Max(value = 3600, message = "Rest time cannot exceed 1 hour (3600 seconds)")
    private Integer restTimeInSeconds;

    @Length(max = 500, message = "Set notes must not exceed 500 characters")
    private String notes;

    private Boolean completed = false;

    // Constructors
    public CreateCardioSetRequest() {
    }

    public CreateCardioSetRequest(Long workoutExerciseId, Integer setNumber, Integer durationInSeconds, 
                                 BigDecimal distance, String distanceUnit, Integer restTimeInSeconds, 
                                 String notes, Boolean completed) {
        this.workoutExerciseId = workoutExerciseId;
        this.setNumber = setNumber;
        this.durationInSeconds = durationInSeconds;
        this.distance = distance;
        this.distanceUnit = distanceUnit;
        this.restTimeInSeconds = restTimeInSeconds;
        this.notes = notes;
        this.completed = completed;
    }

    // Getters and Setters
    public Long getWorkoutExerciseId() {
        return workoutExerciseId;
    }

    public void setWorkoutExerciseId(Long workoutExerciseId) {
        this.workoutExerciseId = workoutExerciseId;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public String getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public Integer getRestTimeInSeconds() {
        return restTimeInSeconds;
    }

    public void setRestTimeInSeconds(Integer restTimeInSeconds) {
        this.restTimeInSeconds = restTimeInSeconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
