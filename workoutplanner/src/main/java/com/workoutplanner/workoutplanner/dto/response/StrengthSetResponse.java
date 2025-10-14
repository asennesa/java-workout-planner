package com.workoutplanner.workoutplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrengthSetResponse {

    private Long setId;
    private Long workoutExerciseId;
    private Integer setNumber;
    private Integer reps;
    private BigDecimal weight;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;
}
