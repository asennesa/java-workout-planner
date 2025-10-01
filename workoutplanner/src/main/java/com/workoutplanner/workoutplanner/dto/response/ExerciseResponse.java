package com.workoutplanner.workoutplanner.dto.response;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;

public class ExerciseResponse {

    private Long exerciseId;
    private String name;
    private String description;
    private ExerciseType type;
    private TargetMuscleGroup targetMuscleGroup;
    private DifficultyLevel difficultyLevel;
    private String imageUrl;

    // Constructors
    public ExerciseResponse() {
    }

    public ExerciseResponse(Long exerciseId, String name, String description, ExerciseType type, 
                           TargetMuscleGroup targetMuscleGroup, DifficultyLevel difficultyLevel, String imageUrl) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.targetMuscleGroup = targetMuscleGroup;
        this.difficultyLevel = difficultyLevel;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

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
