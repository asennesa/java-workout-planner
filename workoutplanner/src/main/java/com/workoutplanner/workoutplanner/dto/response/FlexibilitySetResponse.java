package com.workoutplanner.workoutplanner.dto.response;

public class FlexibilitySetResponse {

    private Long setId;
    private Long workoutExerciseId;
    private Integer setNumber;
    private Integer durationInSeconds;
    private String stretchType;
    private Integer intensity;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;

    // Constructors
    public FlexibilitySetResponse() {
    }

    public FlexibilitySetResponse(Long setId, Long workoutExerciseId, Integer setNumber, Integer durationInSeconds, 
                                 String stretchType, Integer intensity, Integer restTimeInSeconds, 
                                 String notes, Boolean completed) {
        this.setId = setId;
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
