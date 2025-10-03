package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import java.math.BigDecimal;

/**
 * Cardio set entity extending BaseSet.
 * Contains cardio-specific fields and logic.
 */
@Entity
@Table(name = "cardio_sets")
public class CardioSet extends BaseSet {

    private Integer durationInSeconds;
    private BigDecimal distance;
    private String distanceUnit;

    public CardioSet() {
        super();
    }


    @Column(name = "duration_in_seconds", nullable = false)
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 7200, message = "Duration cannot exceed 7200 seconds (2 hours)")
    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    @Column(name = "distance", precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Distance must be non-negative")
    @DecimalMax(value = "999999.99", message = "Distance cannot exceed 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Distance must have at most 6 integer digits and 2 decimal places")
    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    @Column(name = "distance_unit", length = 10)
    @Length(max = 10, message = "Distance unit must not exceed 10 characters")
    @Pattern(regexp = "^(km|m|miles|yards|feet|meters)?$", message = "Distance unit must be a valid unit (km, m, miles, yards, feet, meters) or empty")
    public String getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    @Override
    public String getExerciseType() {
        return "CARDIO";
    }

    @Override
    public String getSetSummary() {
        String distanceStr = distance != null ? String.format("%.2f %s", distance, distanceUnit != null ? distanceUnit : "units") : "no distance";
        return String.format("%d seconds, %s", durationInSeconds, distanceStr);
    }
}
