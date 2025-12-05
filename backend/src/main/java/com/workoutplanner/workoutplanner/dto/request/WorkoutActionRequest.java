package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for workout status change actions (start, complete, cancel, etc.).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutActionRequest {

    @NotBlank(message = "Action cannot be blank")
    private String action;
}
