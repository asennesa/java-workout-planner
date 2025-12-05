package com.workoutplanner.workoutplanner.dto.response;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for workout session data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
