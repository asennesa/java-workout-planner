package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.service.FlexibilitySetService;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for flexibility set operations. Inherits CRUD from {@link BaseSetController}.
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/workout-exercises/{workoutExerciseId}/flexibility-sets")
@Tag(name = "Flexibility Sets", description = "Manage flexibility/stretching sets (hold duration, intensity)")
public class FlexibilitySetController extends BaseSetController<CreateFlexibilitySetRequest> {

    private final FlexibilitySetService flexibilitySetService;

    public FlexibilitySetController(FlexibilitySetService flexibilitySetService) {
        this.flexibilitySetService = flexibilitySetService;
    }

    @Override
    protected SetServiceInterface<CreateFlexibilitySetRequest> getService() {
        return flexibilitySetService;
    }
}

