package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.validation.ValidWorkoutDates;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_sessions")
@ValidWorkoutDates
public class WorkoutSession {

    private Long sessionId;
    private String name;
    private String description;
    private User user;
    private WorkoutStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer actualDurationInMinutes;
    private String sessionNotes;
    private List<WorkoutExercise> workoutExercises = new ArrayList<>();

    public WorkoutSession() {
        // Default constructor for Hibernate
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id", nullable = false, updatable = false)
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Workout session name is required")
    @Length(min = 2, max = 100, message = "Workout session name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", message = "Workout session name can only contain letters, numbers, spaces, hyphens, and parentheses")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description", length = 1000)
    @Length(max = 1000, message = "Description must not exceed 1000 characters")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Workout status is required")
    public WorkoutStatus getStatus() {
        return status;
    }

    public void setStatus(WorkoutStatus status) {
        this.status = status;
    }

    @Column(name = "started_at")
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    @Column(name = "completed_at")
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Column(name = "actual_duration_in_minutes")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours (1440 minutes)")
    public Integer getActualDurationInMinutes() {
        return actualDurationInMinutes;
    }

    public void setActualDurationInMinutes(Integer actualDurationInMinutes) {
        this.actualDurationInMinutes = actualDurationInMinutes;
    }

    @Column(name = "session_notes", length = 1000)
    @Length(max = 1000, message = "Session notes must not exceed 1000 characters")
    public String getSessionNotes() {
        return sessionNotes;
    }

    public void setSessionNotes(String sessionNotes) {
        this.sessionNotes = sessionNotes;
    }

    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderInWorkout ASC")
    public List<WorkoutExercise> getWorkoutExercises() {
        return workoutExercises;
    }

    public void setWorkoutExercises(List<WorkoutExercise> workoutExercises) {
        this.workoutExercises = workoutExercises;
    }
}
