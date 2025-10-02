package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "exercises")
public class Exercise {

    private Long exerciseId;
    private String name;
    private String description;
    private ExerciseType type;
    private TargetMuscleGroup targetMuscleGroup;
    private DifficultyLevel difficultyLevel;
    private String imageUrl;

    public Exercise() {
        // Default constructor
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id", nullable = false, updatable = false)
    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Exercise name is required")
    @Length(min = 2, max = 100, message = "Exercise name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", message = "Exercise name can only contain letters, numbers, spaces, hyphens, and parentheses")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description", length = 1000)
    @Length(max = 1000, message = "Description must not exceed 1000 characters")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @NotNull(message = "Exercise type is required")
    public ExerciseType getType() {
        return type;
    }

    public void setType(ExerciseType type) {
        this.type = type;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "target_muscle_group", nullable = false)
    @NotNull(message = "Target muscle group is required")
    public TargetMuscleGroup getTargetMuscleGroup() {
        return targetMuscleGroup;
    }

    public void setTargetMuscleGroup(TargetMuscleGroup targetMuscleGroup) {
        this.targetMuscleGroup = targetMuscleGroup;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    @NotNull(message = "Difficulty level is required")
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    @Column(name = "image_url", length = 500)
    @Length(max = 500, message = "Image URL must not exceed 500 characters")
    @URL(message = "Image URL must be a valid URL")
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
