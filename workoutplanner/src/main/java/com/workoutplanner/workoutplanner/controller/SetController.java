package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import com.workoutplanner.workoutplanner.service.StrengthSetService;
import com.workoutplanner.workoutplanner.service.CardioSetService;
import com.workoutplanner.workoutplanner.service.FlexibilitySetService;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Set operations.
 * Provides endpoints for managing strength, cardio, and flexibility sets.
 * 
 * This controller follows proper separation of concerns:
 * - Controllers handle HTTP requests/responses
 * - Services handle business logic
 * - Repositories handle data access
 * 
 * CORS is configured globally in CorsConfig.java
 * API Version: v1
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/sets/{setType}")
public class SetController extends BaseSetController {
    
    private final Map<String, SetServiceInterface> services;

    public SetController(StrengthSetService strengthSetService, CardioSetService cardioSetService, FlexibilitySetService flexibilitySetService) {
        this.services = Map.of(
                "strength", strengthSetService,
                "cardio", cardioSetService,
                "flexibility", flexibilitySetService
        );
    }

    @Override
    protected SetServiceInterface getService(String setType) {
        return Optional.ofNullable(services.get(setType.toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid set type: " + setType));
    }
}