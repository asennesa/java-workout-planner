package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

/**
 * Flexibility set entity extending BaseSet.
 * Contains flexibility-specific fields and logic.
 */
@Entity
@Table(name = "flexibility_sets")
public class FlexibilitySet extends BaseSet {

    private Integer durationInSeconds;
    private String stretchType;
    private Integer intensity; // 1-10 scale

    public FlexibilitySet() {
        super();
    }


    @Column(name = "duration_in_seconds", nullable = false)
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 3600, message = "Duration cannot exceed 3600 seconds (1 hour)")
    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    @Column(name = "stretch_type", length = 50)
    @Length(max = 50, message = "Stretch type must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s\\-()]*$", message = "Stretch type can only contain letters, spaces, hyphens, and parentheses")
    public String getStretchType() {
        return stretchType;
    }

    public void setStretchType(String stretchType) {
        this.stretchType = stretchType;
    }

    @Column(name = "intensity")
    @Min(value = 1, message = "Intensity must be at least 1")
    @Max(value = 10, message = "Intensity cannot exceed 10")
    public Integer getIntensity() {
        return intensity;
    }

    public void setIntensity(Integer intensity) {
        this.intensity = intensity;
    }

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
