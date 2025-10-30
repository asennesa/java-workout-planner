package com.workoutplanner.workoutplanner.controller;

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
 * Endpoint: /api/v1/flexibility-sets
 * 
 * @see BaseSetController for available operations
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/flexibility-sets")
public class FlexibilitySetController extends BaseSetController {

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
    protected SetServiceInterface getService() {
        return flexibilitySetService;
    }
}

