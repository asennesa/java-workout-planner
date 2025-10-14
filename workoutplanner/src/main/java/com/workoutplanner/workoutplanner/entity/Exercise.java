package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id", nullable = false, updatable = false)
    private Long exerciseId;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Exercise name is required")
    @Length(min = 2, max = 100, message = "Exercise name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", message = "Exercise name can only contain letters, numbers, spaces, hyphens, and parentheses")
    private String name;

    @Column(name = "description", length = 1000)
    @Length(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @NotNull(message = "Exercise type is required")
    private ExerciseType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_muscle_group", nullable = false)
    @NotNull(message = "Target muscle group is required")
    private TargetMuscleGroup targetMuscleGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    @Column(name = "image_url", length = 500)
    @Length(max = 500, message = "Image URL must not exceed 500 characters")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;
}
