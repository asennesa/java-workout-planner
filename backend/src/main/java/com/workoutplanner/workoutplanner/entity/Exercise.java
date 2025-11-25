package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

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

    /**
     * Equals method following Hibernate best practices.
     * Uses database ID for equality when entity is persisted,
     * falls back to field comparison for transient entities.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Exercise exercise = (Exercise) o;
        
        // If both entities are persisted (have IDs), use ID for equality
        if (exerciseId != null && exercise.exerciseId != null) {
            return Objects.equals(exerciseId, exercise.exerciseId);
        }
        
        // For transient entities, compare unique fields (name and type)
        return Objects.equals(name, exercise.name) &&
               Objects.equals(type, exercise.type);
    }

    /**
     * HashCode method following Hibernate best practices.
     * Uses database ID when entity is persisted,
     * falls back to unique fields for transient entities.
     */
    @Override
    public int hashCode() {
        // If entity is persisted, use ID for hash
        if (exerciseId != null) {
            return Objects.hash(exerciseId);
        }
        
        // For transient entities, use unique fields
        return Objects.hash(name, type);
    }
}
