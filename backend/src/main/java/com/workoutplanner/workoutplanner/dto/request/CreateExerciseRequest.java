package com.workoutplanner.workoutplanner.dto.request;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateExerciseRequest {

    @NotBlank(message = "Exercise name is required")
    @Length(min = 2, max = 100, message = "Exercise name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", message = "Exercise name can only contain letters, numbers, spaces, hyphens, and parentheses")
    private String name;

    @Length(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Exercise type is required")
    private ExerciseType type;

    @NotNull(message = "Target muscle group is required")
    private TargetMuscleGroup targetMuscleGroup;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    @Length(max = 500, message = "Image URL must not exceed 500 characters")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;
}
