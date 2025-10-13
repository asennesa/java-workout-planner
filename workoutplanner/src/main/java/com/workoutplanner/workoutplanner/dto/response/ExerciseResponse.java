package com.workoutplanner.workoutplanner.dto.response;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResponse {

    private Long exerciseId;
    private String name;
    private String description;
    private ExerciseType type;
    private TargetMuscleGroup targetMuscleGroup;
    private DifficultyLevel difficultyLevel;
    private String imageUrl;
}
