package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public class CreateFlexibilitySetRequest {

    @NotNull(message = "Workout exercise ID is required")
    private Long workoutExerciseId;

    @NotNull(message = "Set number is required")
    @Min(value = 1, message = "Set number must be at least 1")
    @Max(value = 50, message = "Set number cannot exceed 50")
    private Integer setNumber;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 3600, message = "Duration cannot exceed 1 hour (3600 seconds)")
    private Integer durationInSeconds;

    @NotBlank(message = "Stretch type is required")
    @Length(min = 2, max = 50, message = "Stretch type must be between 2 and 50 characters")
    private String stretchType;

    @NotNull(message = "Intensity is required")
    @Min(value = 1, message = "Intensity must be at least 1")
    @Max(value = 10, message = "Intensity cannot exceed 10")
    private Integer intensity;

    @Min(value = 0, message = "Rest time cannot be negative")
    @Max(value = 3600, message = "Rest time cannot exceed 1 hour (3600 seconds)")
    private Integer restTimeInSeconds;

    @Length(max = 500, message = "Set notes must not exceed 500 characters")
    private String notes;

    private Boolean completed = false;

    // Constructors
    public CreateFlexibilitySetRequest() {
    }

    public CreateFlexibilitySetRequest(Long workoutExerciseId, Integer setNumber, Integer durationInSeconds, 
                                      String stretchType, Integer intensity, Integer restTimeInSeconds, 
                                      String notes, Boolean completed) {
        this.workoutExerciseId = workoutExerciseId;
        this.setNumber = setNumber;
        this.durationInSeconds = durationInSeconds;
        this.stretchType = stretchType;
        this.intensity = intensity;
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

    public String getStretchType() {
        return stretchType;
    }

    public void setStretchType(String stretchType) {
        this.stretchType = stretchType;
    }

    public Integer getIntensity() {
        return intensity;
    }

    public void setIntensity(Integer intensity) {
        this.intensity = intensity;
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
