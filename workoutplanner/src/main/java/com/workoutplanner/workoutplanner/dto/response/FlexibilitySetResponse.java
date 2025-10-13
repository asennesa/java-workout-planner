package com.workoutplanner.workoutplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlexibilitySetResponse {

    private Long setId;
    private Long workoutExerciseId;
    private Integer setNumber;
    private Integer durationInSeconds;
    private String stretchType;
    private Integer intensity;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;
}
