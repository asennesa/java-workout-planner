package com.workoutplanner.workoutplanner.dto.request;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * DTO for updating workout sessions (Separate from Create - Industry Best Practice).
 * All fields are optional for partial updates, but at least one must be provided.
 * Note: userId cannot be changed - workouts belong to the original creator.
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateWorkoutRequest {

    @Length(min = 2, max = 100, message = "Workout session name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", message = "Workout session name can only contain letters, numbers, spaces, hyphens, and parentheses")
    private String name;

    @Length(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private WorkoutStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours (1440 minutes)")
    private Integer actualDurationInMinutes;

    @Length(max = 1000, message = "Session notes must not exceed 1000 characters")
    private String sessionNotes;

    /**
     * Validation method using built-in @AssertTrue annotation.
     * Ensures at least one field is provided for the update request.
     * 
     * @return true if at least one field is non-null, false otherwise
     */
    @AssertTrue(message = "At least one field must be provided for update")
    private boolean isAtLeastOneFieldProvided() {
        return name != null || 
               description != null || 
               status != null || 
               startedAt != null || 
               completedAt != null || 
               actualDurationInMinutes != null || 
               sessionNotes != null;
    }
}
