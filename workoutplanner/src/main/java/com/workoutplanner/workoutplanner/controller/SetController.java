package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.dto.response.StrengthSetResponse;
import com.workoutplanner.workoutplanner.dto.response.CardioSetResponse;
import com.workoutplanner.workoutplanner.dto.response.FlexibilitySetResponse;
import com.workoutplanner.workoutplanner.service.StrengthSetService;
import com.workoutplanner.workoutplanner.service.CardioSetService;
import com.workoutplanner.workoutplanner.service.FlexibilitySetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/sets")
public class SetController {
    
    private static final Logger logger = LoggerFactory.getLogger(SetController.class);
    
    private final StrengthSetService strengthSetService;
    private final CardioSetService cardioSetService;
    private final FlexibilitySetService flexibilitySetService;
    
    /**
     * Constructor injection for dependencies.
     */
    public SetController(StrengthSetService strengthSetService,
                        CardioSetService cardioSetService,
                        FlexibilitySetService flexibilitySetService) {
        this.strengthSetService = strengthSetService;
        this.cardioSetService = cardioSetService;
        this.flexibilitySetService = flexibilitySetService;
    }
    
    // ========== STRENGTH SETS ==========
    
    /**
     * Create a new strength set.
     * 
     * @param createStrengthSetRequest the strength set creation request
     * @return ResponseEntity containing the created strength set response
     */
    @PostMapping("/strength")
    public ResponseEntity<StrengthSetResponse> createStrengthSet(@Valid @RequestBody CreateStrengthSetRequest createStrengthSetRequest) {
        logger.debug("Creating strength set for workoutExerciseId={}, reps={}, weight={}", 
                    createStrengthSetRequest.getWorkoutExerciseId(), 
                    createStrengthSetRequest.getReps(), 
                    createStrengthSetRequest.getWeight());
        
        StrengthSetResponse strengthSetResponse = strengthSetService.createStrengthSet(createStrengthSetRequest);
        
        logger.info("Strength set created successfully. setId={}, workoutExerciseId={}, reps={}, weight={}", 
                   strengthSetResponse.getSetId(), 
                   strengthSetResponse.getWorkoutExerciseId(),
                   strengthSetResponse.getReps(),
                   strengthSetResponse.getWeight());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(strengthSetResponse);
    }
    
    /**
     * Get strength sets by workout exercise ID.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return ResponseEntity containing list of strength set responses
     */
    @GetMapping("/strength/workout-exercise/{workoutExerciseId}")
    public ResponseEntity<List<StrengthSetResponse>> getStrengthSetsByWorkoutExercise(@PathVariable Long workoutExerciseId) {
        List<StrengthSetResponse> strengthSetResponses = strengthSetService.getStrengthSetsByWorkoutExercise(workoutExerciseId);
        return ResponseEntity.ok(strengthSetResponses);
    }
    
    /**
     * Get strength set by ID.
     * 
     * @param setId the set ID
     * @return ResponseEntity containing the strength set response
     */
    @GetMapping("/strength/{setId}")
    public ResponseEntity<StrengthSetResponse> getStrengthSetById(@PathVariable Long setId) {
        StrengthSetResponse strengthSetResponse = strengthSetService.getStrengthSetById(setId);
        return ResponseEntity.ok(strengthSetResponse);
    }
    
    /**
     * Update strength set.
     * 
     * @param setId the set ID
     * @param createStrengthSetRequest the updated strength set information
     * @return ResponseEntity containing the updated strength set response
     */
    @PutMapping("/strength/{setId}")
    public ResponseEntity<StrengthSetResponse> updateStrengthSet(@PathVariable Long setId, 
                                                                @Valid @RequestBody CreateStrengthSetRequest createStrengthSetRequest) {
        StrengthSetResponse strengthSetResponse = strengthSetService.updateStrengthSet(setId, createStrengthSetRequest);
        return ResponseEntity.ok(strengthSetResponse);
    }
    
    /**
     * Delete strength set.
     * 
     * @param setId the set ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/strength/{setId}")
    public ResponseEntity<Void> deleteStrengthSet(@PathVariable Long setId) {
        strengthSetService.deleteStrengthSet(setId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== CARDIO SETS ==========
    
    /**
     * Create a new cardio set.
     * 
     * @param createCardioSetRequest the cardio set creation request
     * @return ResponseEntity containing the created cardio set response
     */
    @PostMapping("/cardio")
    public ResponseEntity<CardioSetResponse> createCardioSet(@Valid @RequestBody CreateCardioSetRequest createCardioSetRequest) {
        logger.debug("Creating cardio set for workoutExerciseId={}, duration={}, distance={}", 
                    createCardioSetRequest.getWorkoutExerciseId(), 
                    createCardioSetRequest.getDurationInSeconds(), 
                    createCardioSetRequest.getDistance());
        
        CardioSetResponse cardioSetResponse = cardioSetService.createCardioSet(createCardioSetRequest);
        
        logger.info("Cardio set created successfully. setId={}, workoutExerciseId={}, duration={}, distance={}", 
                   cardioSetResponse.getSetId(), 
                   cardioSetResponse.getWorkoutExerciseId(),
                   cardioSetResponse.getDurationInSeconds(),
                   cardioSetResponse.getDistance());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(cardioSetResponse);
    }
    
    /**
     * Get cardio sets by workout exercise ID.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return ResponseEntity containing list of cardio set responses
     */
    @GetMapping("/cardio/workout-exercise/{workoutExerciseId}")
    public ResponseEntity<List<CardioSetResponse>> getCardioSetsByWorkoutExercise(@PathVariable Long workoutExerciseId) {
        List<CardioSetResponse> cardioSetResponses = cardioSetService.getCardioSetsByWorkoutExercise(workoutExerciseId);
        return ResponseEntity.ok(cardioSetResponses);
    }
    
    /**
     * Get cardio set by ID.
     * 
     * @param setId the set ID
     * @return ResponseEntity containing the cardio set response
     */
    @GetMapping("/cardio/{setId}")
    public ResponseEntity<CardioSetResponse> getCardioSetById(@PathVariable Long setId) {
        CardioSetResponse cardioSetResponse = cardioSetService.getCardioSetById(setId);
        return ResponseEntity.ok(cardioSetResponse);
    }
    
    /**
     * Update cardio set.
     * 
     * @param setId the set ID
     * @param createCardioSetRequest the updated cardio set information
     * @return ResponseEntity containing the updated cardio set response
     */
    @PutMapping("/cardio/{setId}")
    public ResponseEntity<CardioSetResponse> updateCardioSet(@PathVariable Long setId, 
                                                            @Valid @RequestBody CreateCardioSetRequest createCardioSetRequest) {
        CardioSetResponse cardioSetResponse = cardioSetService.updateCardioSet(setId, createCardioSetRequest);
        return ResponseEntity.ok(cardioSetResponse);
    }
    
    /**
     * Delete cardio set.
     * 
     * @param setId the set ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/cardio/{setId}")
    public ResponseEntity<Void> deleteCardioSet(@PathVariable Long setId) {
        cardioSetService.deleteCardioSet(setId);
        return ResponseEntity.noContent().build();
    }
    
    // ========== FLEXIBILITY SETS ==========
    
    /**
     * Create a new flexibility set.
     * 
     * @param createFlexibilitySetRequest the flexibility set creation request
     * @return ResponseEntity containing the created flexibility set response
     */
    @PostMapping("/flexibility")
    public ResponseEntity<FlexibilitySetResponse> createFlexibilitySet(@Valid @RequestBody CreateFlexibilitySetRequest createFlexibilitySetRequest) {
        logger.debug("Creating flexibility set for workoutExerciseId={}, duration={}, intensity={}", 
                    createFlexibilitySetRequest.getWorkoutExerciseId(), 
                    createFlexibilitySetRequest.getDurationInSeconds(), 
                    createFlexibilitySetRequest.getIntensity());
        
        FlexibilitySetResponse flexibilitySetResponse = flexibilitySetService.createFlexibilitySet(createFlexibilitySetRequest);
        
        logger.info("Flexibility set created successfully. setId={}, workoutExerciseId={}, duration={}, intensity={}", 
                   flexibilitySetResponse.getSetId(), 
                   flexibilitySetResponse.getWorkoutExerciseId(),
                   flexibilitySetResponse.getDurationInSeconds(),
                   flexibilitySetResponse.getIntensity());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(flexibilitySetResponse);
    }
    
    /**
     * Get flexibility sets by workout exercise ID.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return ResponseEntity containing list of flexibility set responses
     */
    @GetMapping("/flexibility/workout-exercise/{workoutExerciseId}")
    public ResponseEntity<List<FlexibilitySetResponse>> getFlexibilitySetsByWorkoutExercise(@PathVariable Long workoutExerciseId) {
        List<FlexibilitySetResponse> flexibilitySetResponses = flexibilitySetService.getFlexibilitySetsByWorkoutExercise(workoutExerciseId);
        return ResponseEntity.ok(flexibilitySetResponses);
    }
    
    /**
     * Get flexibility set by ID.
     * 
     * @param setId the set ID
     * @return ResponseEntity containing the flexibility set response
     */
    @GetMapping("/flexibility/{setId}")
    public ResponseEntity<FlexibilitySetResponse> getFlexibilitySetById(@PathVariable Long setId) {
        FlexibilitySetResponse flexibilitySetResponse = flexibilitySetService.getFlexibilitySetById(setId);
        return ResponseEntity.ok(flexibilitySetResponse);
    }
    
    /**
     * Update flexibility set.
     * 
     * @param setId the set ID
     * @param createFlexibilitySetRequest the updated flexibility set information
     * @return ResponseEntity containing the updated flexibility set response
     */
    @PutMapping("/flexibility/{setId}")
    public ResponseEntity<FlexibilitySetResponse> updateFlexibilitySet(@PathVariable Long setId, 
                                                                     @Valid @RequestBody CreateFlexibilitySetRequest createFlexibilitySetRequest) {
        FlexibilitySetResponse flexibilitySetResponse = flexibilitySetService.updateFlexibilitySet(setId, createFlexibilitySetRequest);
        return ResponseEntity.ok(flexibilitySetResponse);
    }
    
    /**
     * Delete flexibility set.
     * 
     * @param setId the set ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/flexibility/{setId}")
    public ResponseEntity<Void> deleteFlexibilitySet(@PathVariable Long setId) {
        flexibilitySetService.deleteFlexibilitySet(setId);
        return ResponseEntity.noContent().build();
    }
}