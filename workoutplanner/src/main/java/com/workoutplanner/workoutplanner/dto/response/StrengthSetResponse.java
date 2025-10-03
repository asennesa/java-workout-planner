package com.workoutplanner.workoutplanner.dto.response;

import java.math.BigDecimal;

public class StrengthSetResponse {

    private Long setId;
    private Long workoutExerciseId;
    private Integer setNumber;
    private Integer reps;
    private BigDecimal weight;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;

    // Constructors
    public StrengthSetResponse() {
    }

    public StrengthSetResponse(Long setId, Long workoutExerciseId, Integer setNumber, Integer reps, 
                              BigDecimal weight, Integer restTimeInSeconds, String notes, Boolean completed) {
        this.setId = setId;
        this.workoutExerciseId = workoutExerciseId;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.restTimeInSeconds = restTimeInSeconds;
        this.notes = notes;
        this.completed = completed;
    }

    // Getters and Setters
    public Long getSetId() {
        return setId;
    }

    public void setSetId(Long setId) {
        this.setId = setId;
    }

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

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
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
