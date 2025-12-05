package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.service.CardioSetService;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for cardio set operations. Inherits CRUD from {@link BaseSetController}.
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/workout-exercises/{workoutExerciseId}/cardio-sets")
@Tag(name = "Cardio Sets", description = "Manage cardio sets (duration, distance, calories, heart rate)")
public class CardioSetController extends BaseSetController<CreateCardioSetRequest> {

    private final CardioSetService cardioSetService;

    public CardioSetController(CardioSetService cardioSetService) {
        this.cardioSetService = cardioSetService;
    }

    @Override
    protected SetServiceInterface<CreateCardioSetRequest> getService() {
        return cardioSetService;
    }
}

