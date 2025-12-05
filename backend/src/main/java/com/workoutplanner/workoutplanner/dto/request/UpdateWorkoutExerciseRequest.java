package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating workout exercise information.
 * All fields are optional for partial updates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkoutExerciseRequest {

    @Min(value = 1, message = "Order must be at least 1")
    @Max(value = 100, message = "Order cannot exceed 100")
    private Integer orderInWorkout;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    public boolean hasUpdates() {
        return orderInWorkout != null || notes != null;
    }

    public boolean isOrderUpdateRequested() {
        return orderInWorkout != null;
    }

    public boolean isNotesUpdateRequested() {
        return notes != null && !notes.trim().isEmpty();
    }
}
