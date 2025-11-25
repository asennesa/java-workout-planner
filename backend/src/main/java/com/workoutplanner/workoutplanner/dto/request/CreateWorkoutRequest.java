package com.workoutplanner.workoutplanner.dto.request;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateWorkoutRequest {

    @NotBlank(message = "Workout session name is required")
    @Length(min = 2, max = 100, message = "Workout session name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", message = "Workout session name can only contain letters, numbers, spaces, hyphens, and parentheses")
    private String name;

    @Length(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Workout status is required")
    private WorkoutStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours (1440 minutes)")
    private Integer actualDurationInMinutes;

    @Length(max = 1000, message = "Session notes must not exceed 1000 characters")
    private String sessionNotes;

    public CreateWorkoutRequest(String name, String description, Long userId, WorkoutStatus status) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.status = status;
    }
}
