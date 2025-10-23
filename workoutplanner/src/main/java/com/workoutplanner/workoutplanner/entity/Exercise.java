package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.validation.ValidationGroups;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
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
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
              message = "Exercise name is required")
    @Length(min = 2, max = 100, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
            message = "Exercise name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", 
             groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Exercise name can only contain letters, numbers, spaces, hyphens, and parentheses")
    private String name;

    @Column(name = "description", length = 1000)
    @Length(max = 1000, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
            message = "Description must not exceed 1000 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Exercise type is required")
    private ExerciseType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_muscle_group", nullable = false)
    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Target muscle group is required")
    private TargetMuscleGroup targetMuscleGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    @Column(name = "image_url", length = 500)
    @Length(max = 500, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
            message = "Image URL must not exceed 500 characters")
    @URL(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
         message = "Image URL must be a valid URL")
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
