package com.workoutplanner.workoutplanner.dto.request;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

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

    // Constructors
    public CreateExerciseRequest() {
    }

    public CreateExerciseRequest(String name, String description, ExerciseType type, 
                                TargetMuscleGroup targetMuscleGroup, DifficultyLevel difficultyLevel, String imageUrl) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.targetMuscleGroup = targetMuscleGroup;
        this.difficultyLevel = difficultyLevel;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ExerciseType getType() {
        return type;
    }

    public void setType(ExerciseType type) {
        this.type = type;
    }

    public TargetMuscleGroup getTargetMuscleGroup() {
        return targetMuscleGroup;
    }

    public void setTargetMuscleGroup(TargetMuscleGroup targetMuscleGroup) {
        this.targetMuscleGroup = targetMuscleGroup;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
