package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Exercise entity representing exercises in the library.
 */
@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
public class Exercise extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id", nullable = false, updatable = false)
    private Long exerciseId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ExerciseType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_muscle_group", nullable = false)
    private TargetMuscleGroup targetMuscleGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Exercise exercise = (Exercise) o;
        if (exerciseId != null && exercise.exerciseId != null) {
            return Objects.equals(exerciseId, exercise.exerciseId);
        }
        return Objects.equals(name, exercise.name) && Objects.equals(type, exercise.type);
    }

    @Override
    public int hashCode() {
        if (exerciseId != null) {
            return Objects.hash(exerciseId);
        }
        return Objects.hash(name, type);
    }
}
