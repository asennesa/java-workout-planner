package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public class CreateWorkoutExerciseRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @NotNull(message = "Order in workout is required")
    @Min(value = 1, message = "Order must be at least 1")
    @Max(value = 100, message = "Order cannot exceed 100")
    private Integer orderInWorkout;

    @Length(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Constructors
    public CreateWorkoutExerciseRequest() {
    }

    public CreateWorkoutExerciseRequest(Long sessionId, Long exerciseId, Integer orderInWorkout, String notes) {
        this.sessionId = sessionId;
        this.exerciseId = exerciseId;
        this.orderInWorkout = orderInWorkout;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Integer getOrderInWorkout() {
        return orderInWorkout;
    }

    public void setOrderInWorkout(Integer orderInWorkout) {
        this.orderInWorkout = orderInWorkout;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
