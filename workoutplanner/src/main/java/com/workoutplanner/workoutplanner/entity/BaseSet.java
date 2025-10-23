package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import com.workoutplanner.workoutplanner.validation.ValidationGroups;
import java.util.Objects;

/**
 * Abstract base class for all set types.
 * Contains common fields and validation logic.
 * 
 * This follows the Template Method Pattern and DRY principle.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "workoutExercise")
public abstract class BaseSet extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "set_id", nullable = false, updatable = false)
    private Long setId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    @NotNull(groups = {ValidationGroups.Create.class}, 
             message = "Workout exercise is required for set creation")
    private WorkoutExercise workoutExercise;

    @Column(name = "set_number", nullable = false)
    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Set number is required")
    @Min(value = 1, 
         groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
         message = "Set number must be at least 1")
    @Max(value = 50, 
         groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
         message = "Set number cannot exceed 50")
    private Integer setNumber;

    @Column(name = "rest_time_in_seconds")
    @Min(value = 0, 
         groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
         message = "Rest time must be non-negative")
    @Max(value = 3600, 
         groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
         message = "Rest time cannot exceed 3600 seconds (1 hour)")
    private Integer restTimeInSeconds;

    @Column(name = "notes", length = 500)
    @Length(max = 500, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
            message = "Notes must not exceed 500 characters")
    private String notes;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Abstract method to get the exercise type.
     * Each concrete class must implement this.
     */
    public abstract String getExerciseType();

    /**
     * Template method for calculating set duration.
     * Can be overridden by subclasses if needed.
     */
    public Integer getDurationInSeconds() {
        return null; // Default implementation
    }

    /**
     * Template method for getting set summary.
     * Each subclass can provide its own implementation.
     */
    public abstract String getSetSummary();

    /**
     * Equals method following Hibernate best practices.
     * Uses database ID for equality when entity is persisted,
     * falls back to field comparison for transient entities.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        BaseSet baseSet = (BaseSet) o;
        
        // If both entities are persisted (have IDs), use ID for equality
        if (setId != null && baseSet.setId != null) {
            return Objects.equals(setId, baseSet.setId);
        }
        
        // For transient entities, compare all relevant fields
        return Objects.equals(setNumber, baseSet.setNumber) &&
               Objects.equals(restTimeInSeconds, baseSet.restTimeInSeconds) &&
               Objects.equals(notes, baseSet.notes) &&
               completed == baseSet.completed &&
               Objects.equals(workoutExercise, baseSet.workoutExercise);
    }

    /**
     * HashCode method following Hibernate best practices.
     * Uses database ID when entity is persisted,
     * falls back to field hash for transient entities.
     */
    @Override
    public int hashCode() {
        // If entity is persisted, use ID for hash
        if (setId != null) {
            return Objects.hash(setId);
        }
        
        // For transient entities, use all relevant fields
        return Objects.hash(setNumber, restTimeInSeconds, notes, completed, workoutExercise);
    }
}
