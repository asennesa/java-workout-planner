package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

/**
 * Flexibility set entity extending BaseSet.
 * Contains flexibility-specific fields and logic.
 */
@Entity
@Table(name = "flexibility_sets")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class FlexibilitySet extends BaseSet {

    @Column(name = "duration_in_seconds", nullable = false)
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 3600, message = "Duration cannot exceed 3600 seconds (1 hour)")
    private Integer durationInSeconds;

    @Column(name = "stretch_type", length = 50)
    @Length(max = 50, message = "Stretch type must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s\\-()]*$", message = "Stretch type can only contain letters, spaces, hyphens, and parentheses")
    private String stretchType;

    @Column(name = "intensity")
    @Min(value = 1, message = "Intensity must be at least 1")
    @Max(value = 10, message = "Intensity cannot exceed 10")
    private Integer intensity; // 1-10 scale

    @Override
    public String getExerciseType() {
        return "FLEXIBILITY";
    }

    @Override
    public String getSetSummary() {
        return String.format("%d seconds of %s (intensity: %d/10)", 
                           durationInSeconds, stretchType, intensity);
    }
}
