package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import com.workoutplanner.workoutplanner.service.StrengthSetService;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Strength Set operations.
 * 
 * Provides endpoints for managing strength training sets including:
 * - Weight lifted
 * - Number of repetitions
 * - Set completion tracking
 * 
 * All CRUD operations are inherited from BaseSetController.
 * 
 * Endpoint: /api/v1/strength-sets
 * 
 * @see BaseSetController for available operations
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/strength-sets")
public class StrengthSetController extends BaseSetController<CreateStrengthSetRequest> {

    private final StrengthSetService strengthSetService;

    /**
     * Constructor with dependency injection.
     * 
     * @param strengthSetService the strength set service
     */
    public StrengthSetController(StrengthSetService strengthSetService) {
        this.strengthSetService = strengthSetService;
    }

    /**
     * Provides the strength set service implementation.
     * 
     * @return StrengthSetService instance
     */
    @Override
    protected SetServiceInterface<CreateStrengthSetRequest> getService() {
        return strengthSetService;
    }
}

