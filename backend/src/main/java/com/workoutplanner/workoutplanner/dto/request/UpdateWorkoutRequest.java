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
 * DTO for updating workout sessions.
 * All fields are optional for partial updates, but at least one must be provided.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkoutRequest {

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

    @AssertTrue(message = "At least one field must be provided for update")
    private boolean isAtLeastOneFieldProvided() {
        return name != null || description != null || status != null ||
               scheduledDate != null || startedAt != null || completedAt != null ||
               actualDurationInMinutes != null || sessionNotes != null;
    }
}
