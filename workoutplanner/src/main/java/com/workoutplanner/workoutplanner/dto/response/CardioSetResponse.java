package com.workoutplanner.workoutplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardioSetResponse {

    private Long setId;
    private Long workoutExerciseId;
    private Integer setNumber;
    private Integer durationInSeconds;
    private BigDecimal distance;
    private String distanceUnit;
    private Integer restTimeInSeconds;
    private String notes;
    private Boolean completed;
}
