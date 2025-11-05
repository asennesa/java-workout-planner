package com.workoutplanner.workoutplanner.dto.request;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * DTO for updating workout session information.
 * 
 * This DTO allows partial updates of workout session data. Only provided fields
 * will be updated, allowing for flexible workout modifications.
 * 
 * All fields are optional - only the fields that need to be changed
 * should be provided in the request.
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
     * Check if any field is being updated.
     * 
     * @return true if at least one field is provided, false otherwise
     */
    public boolean hasUpdates() {
        return name != null || 
               description != null || 
               status != null || 
               startedAt != null || 
               completedAt != null || 
               actualDurationInMinutes != null || 
               sessionNotes != null;
    }

    /**
     * Check if name is being updated.
     * 
     * @return true if name is not null and not blank, false otherwise
     */
    public boolean isNameUpdateRequested() {
        return name != null && !name.trim().isEmpty();
    }

    /**
     * Check if description is being updated.
     * 
     * @return true if description is not null and not blank, false otherwise
     */
    public boolean isDescriptionUpdateRequested() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * Check if status is being updated.
     * 
     * @return true if status is not null, false otherwise
     */
    public boolean isStatusUpdateRequested() {
        return status != null;
    }

    /**
     * Check if session notes are being updated.
     * 
     * @return true if sessionNotes is not null and not blank, false otherwise
     */
    public boolean isSessionNotesUpdateRequested() {
        return sessionNotes != null && !sessionNotes.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "UpdateWorkoutRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", actualDurationInMinutes=" + actualDurationInMinutes +
                ", sessionNotes='" + sessionNotes + '\'' +
                '}';
    }
}
