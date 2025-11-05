package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.service.CardioSetService;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Cardio Set operations.
 * 
 * Provides endpoints for managing cardiovascular exercise sets including:
 * - Duration
 * - Distance
 * - Calories burned
 * - Heart rate tracking
 * 
 * All CRUD operations are inherited from BaseSetController.
 * 
 * Following REST best practices with fully nested resource hierarchy:
 * Endpoint: /api/v1/workout-exercises/{workoutExerciseId}/cardio-sets
 * 
 * This shows clear parent-child relationship: cardio sets belong to a workout exercise.
 * 
 * @see BaseSetController for available operations
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/workout-exercises/{workoutExerciseId}/cardio-sets")
public class CardioSetController extends BaseSetController<CreateCardioSetRequest> {

    private final CardioSetService cardioSetService;

    /**
     * Constructor with dependency injection.
     * 
     * @param cardioSetService the cardio set service
     */
    public CardioSetController(CardioSetService cardioSetService) {
        this.cardioSetService = cardioSetService;
    }

    /**
     * Provides the cardio set service implementation.
     * 
     * @return CardioSetService instance
     */
    @Override
    protected SetServiceInterface<CreateCardioSetRequest> getService() {
        return cardioSetService;
    }
}

