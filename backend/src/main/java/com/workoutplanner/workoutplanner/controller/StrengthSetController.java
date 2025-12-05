package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import com.workoutplanner.workoutplanner.service.StrengthSetService;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for strength set operations. Inherits CRUD from {@link BaseSetController}.
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/workout-exercises/{workoutExerciseId}/strength-sets")
@Tag(name = "Strength Sets", description = "Manage strength training sets (weight, repetitions)")
public class StrengthSetController extends BaseSetController<CreateStrengthSetRequest> {

    private final StrengthSetService strengthSetService;

    public StrengthSetController(StrengthSetService strengthSetService) {
        this.strengthSetService = strengthSetService;
    }

    @Override
    protected SetServiceInterface<CreateStrengthSetRequest> getService() {
        return strengthSetService;
    }
}

