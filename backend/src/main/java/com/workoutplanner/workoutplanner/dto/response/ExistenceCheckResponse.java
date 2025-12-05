package com.workoutplanner.workoutplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for username/email existence checks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExistenceCheckResponse {

    private boolean exists;
}
