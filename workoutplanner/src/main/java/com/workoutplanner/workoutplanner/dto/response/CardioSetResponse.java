package com.workoutplanner.workoutplanner.dto.response;

import java.math.BigDecimal;

public class CardioSetResponse {

    private Long setId;
    private Long workoutExerciseId;
    private Integer setNumber;
    private Integer durationInSeconds;
    private BigDecimal distance;
    private String distanceUnit;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;

    // Constructors
    public CardioSetResponse() {
    }

    public CardioSetResponse(Long setId, Long workoutExerciseId, Integer setNumber, Integer durationInSeconds, 
                            BigDecimal distance, String distanceUnit, Integer restTimeInSeconds, 
                            String notes, Boolean completed) {
        this.setId = setId;
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
