package com.workoutplanner.workoutplanner.dto.response;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;

import java.time.LocalDateTime;
import java.util.List;

public class WorkoutResponse {

    private Long sessionId;
    private String name;
    private String description;
    private Long userId;
    private String userFullName;
    private WorkoutStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer actualDurationInMinutes;
    private String sessionNotes;
    private List<WorkoutExerciseResponse> workoutExercises;

    // Constructors
    public WorkoutResponse() {
    }

    public WorkoutResponse(Long sessionId, String name, String description, Long userId, String userFullName, 
                          WorkoutStatus status, LocalDateTime startedAt, LocalDateTime completedAt, 
                          Integer actualDurationInMinutes, String sessionNotes) {
        this.sessionId = sessionId;
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.userFullName = userFullName;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.actualDurationInMinutes = actualDurationInMinutes;
        this.sessionNotes = sessionNotes;
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public WorkoutStatus getStatus() {
        return status;
    }

    public void setStatus(WorkoutStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getActualDurationInMinutes() {
        return actualDurationInMinutes;
    }

    public void setActualDurationInMinutes(Integer actualDurationInMinutes) {
        this.actualDurationInMinutes = actualDurationInMinutes;
    }

    public String getSessionNotes() {
        return sessionNotes;
    }

    public void setSessionNotes(String sessionNotes) {
        this.sessionNotes = sessionNotes;
    }

    public List<WorkoutExerciseResponse> getWorkoutExercises() {
        return workoutExercises;
    }

    public void setWorkoutExercises(List<WorkoutExerciseResponse> workoutExercises) {
        this.workoutExercises = workoutExercises;
    }
}
