package com.workoutplanner.workoutplanner.dto.response;

import com.workoutplanner.workoutplanner.enums.ExerciseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for workout exercise data (exercise within a workout).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutExerciseResponse {

    private Long workoutExerciseId;
    private Long exerciseId;
    private String exerciseName;
    private ExerciseType exerciseType;
    private Integer orderInWorkout;
    private String notes;
}
