package com.workoutplanner.workoutplanner.dto.response;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class WorkoutResponse {

    private Long sessionId;
    private String name;
    private String description;
    private Long userId;
    private String userFullName;
    private WorkoutStatus status;
    private LocalDate scheduledDate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer actualDurationInMinutes;
    private String sessionNotes;
    private List<WorkoutExerciseResponse> workoutExercises;
    private LocalDateTime createdAt;

    public WorkoutResponse(Long sessionId, String name, String description, Long userId, String userFullName,
                          WorkoutStatus status, LocalDate scheduledDate, LocalDateTime startedAt,
                          LocalDateTime completedAt, Integer actualDurationInMinutes, String sessionNotes) {
        this.sessionId = sessionId;
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.userFullName = userFullName;
        this.status = status;
        this.scheduledDate = scheduledDate;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.actualDurationInMinutes = actualDurationInMinutes;
        this.sessionNotes = sessionNotes;
    }
}
