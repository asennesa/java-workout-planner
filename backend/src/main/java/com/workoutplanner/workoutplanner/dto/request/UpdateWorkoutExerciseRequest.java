package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for updating workout exercise information.
 * 
 * This DTO allows partial updates of workout exercise data. Only provided fields
 * will be updated, allowing for flexible workout exercise modifications.
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
public class UpdateWorkoutExerciseRequest {

    @Min(value = 1, message = "Order must be at least 1")
    @Max(value = 100, message = "Order cannot exceed 100")
    private Integer orderInWorkout;

    @Length(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    /**
     * Check if any field is being updated.
     * 
     * @return true if at least one field is provided, false otherwise
     */
    public boolean hasUpdates() {
        return orderInWorkout != null || notes != null;
    }

    /**
     * Check if order is being updated.
     * 
     * @return true if orderInWorkout is not null, false otherwise
     */
    public boolean isOrderUpdateRequested() {
        return orderInWorkout != null;
    }

    /**
     * Check if notes are being updated.
     * 
     * @return true if notes is not null and not blank, false otherwise
     */
    public boolean isNotesUpdateRequested() {
        return notes != null && !notes.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "UpdateWorkoutExerciseRequest{" +
                "orderInWorkout=" + orderInWorkout +
                ", notes='" + notes + '\'' +
                '}';
    }
}
