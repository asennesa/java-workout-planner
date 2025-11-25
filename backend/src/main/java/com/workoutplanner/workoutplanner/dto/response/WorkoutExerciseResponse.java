package com.workoutplanner.workoutplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutExerciseResponse {

    private Long workoutExerciseId;
    private Long exerciseId;
    private String exerciseName;
    private Integer orderInWorkout;
    private String notes;
}
