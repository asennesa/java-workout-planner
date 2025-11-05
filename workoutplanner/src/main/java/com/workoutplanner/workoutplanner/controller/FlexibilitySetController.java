package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.service.FlexibilitySetService;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Flexibility Set operations.
 * 
 * Provides endpoints for managing flexibility and stretching sets including:
 * - Hold duration
 * - Stretch intensity
 * - Range of motion tracking
 * 
 * All CRUD operations are inherited from BaseSetController.
 * 
 * Following REST best practices with fully nested resource hierarchy:
 * Endpoint: /api/v1/workout-exercises/{workoutExerciseId}/flexibility-sets
 * 
 * This shows clear parent-child relationship: flexibility sets belong to a workout exercise.
 * 
 * @see BaseSetController for available operations
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/workout-exercises/{workoutExerciseId}/flexibility-sets")
public class FlexibilitySetController extends BaseSetController<CreateFlexibilitySetRequest> {

    private final FlexibilitySetService flexibilitySetService;

    /**
     * Constructor with dependency injection.
     * 
     * @param flexibilitySetService the flexibility set service
     */
    public FlexibilitySetController(FlexibilitySetService flexibilitySetService) {
        this.flexibilitySetService = flexibilitySetService;
    }

    /**
     * Provides the flexibility set service implementation.
     * 
     * @return FlexibilitySetService instance
     */
    @Override
    protected SetServiceInterface<CreateFlexibilitySetRequest> getService() {
        return flexibilitySetService;
    }
}

