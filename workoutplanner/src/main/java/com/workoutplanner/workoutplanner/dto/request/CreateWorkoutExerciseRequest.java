package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkoutExerciseRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @NotNull(message = "Order in workout is required")
    @Min(value = 1, message = "Order must be at least 1")
    @Max(value = 100, message = "Order cannot exceed 100")
    private Integer orderInWorkout;

    @Length(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
