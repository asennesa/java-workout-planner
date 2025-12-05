package com.workoutplanner.workoutplanner.dto.request;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for creating workout sessions. Status defaults to PLANNED if not provided.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkoutRequest {

    @NotBlank(message = "Workout session name is required")
    @Size(min = 2, max = 100, message = "Workout session name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", message = "Workout session name can only contain letters, numbers, spaces, hyphens, and parentheses")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private WorkoutStatus status;

    private LocalDate scheduledDate;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours (1440 minutes)")
    private Integer actualDurationInMinutes;

    @Size(max = 1000, message = "Session notes must not exceed 1000 characters")
    private String sessionNotes;
}
