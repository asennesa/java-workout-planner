package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for adding an exercise to a workout session.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkoutExerciseRequest {

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @NotNull(message = "Order in workout is required")
    @Min(value = 1, message = "Order must be at least 1")
    @Max(value = 100, message = "Order cannot exceed 100")
    private Integer orderInWorkout;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
