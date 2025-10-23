package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.CardioSetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for managing cardio set operations.
 * Handles business logic for cardio sets.
 * 
 * Uses method-level @Transactional for optimal performance:
 * - Read operations use @Transactional(readOnly = true) for better performance
 * - Write operations use @Transactional for data modification
 */
@Service
public class CardioSetService implements CardioSetServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(CardioSetService.class);

    private final CardioSetRepository cardioSetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutMapper workoutMapper;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public CardioSetService(CardioSetRepository cardioSetRepository,
                           WorkoutExerciseRepository workoutExerciseRepository,
                           WorkoutMapper workoutMapper) {
        this.cardioSetRepository = cardioSetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
    }

    /**
     * Create a new cardio set.
     *
     * @param createCardioSetRequest the cardio set creation request
     * @return SetResponse the created cardio set response
     */
    @Transactional
    public SetResponse createCardioSet(CreateSetRequest createCardioSetRequest) {
        logger.debug("SERVICE: Creating cardio set. workoutExerciseId={}, setNumber={}, duration={}s, distance={}", 
                    createCardioSetRequest.getWorkoutExerciseId(), createCardioSetRequest.getSetNumber(),
                    createCardioSetRequest.getDurationInSeconds(), createCardioSetRequest.getDistance());
        
        // Validate workout exercise exists
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(createCardioSetRequest.getWorkoutExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", createCardioSetRequest.getWorkoutExerciseId()));

        // Map request to entity using mapper
        CardioSet cardioSet = workoutMapper.toCardioSetEntity(createCardioSetRequest);
        
        // Set the workout exercise (not handled by mapper)
        cardioSet.setWorkoutExercise(workoutExercise);

        CardioSet savedCardioSet = cardioSetRepository.save(cardioSet);
        
        logger.info("SERVICE: Cardio set created successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   savedCardioSet.getSetId(), savedCardioSet.getWorkoutExercise().getWorkoutExerciseId(),
                   savedCardioSet.getSetNumber());
        
        return workoutMapper.toSetResponse(savedCardioSet);
    }

    /**
     * Get cardio set by ID.
     *
     * @param setId the set ID
     * @return SetResponse the cardio set response
     */
    @Transactional(readOnly = true)
    public SetResponse getCardioSetById(Long setId) {
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Cardio set", "ID", setId));

        return workoutMapper.toSetResponse(cardioSet);
    }

    /**
     * Get cardio sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    public List<SetResponse> getCardioSetsByWorkoutExercise(Long workoutExerciseId) {
        List<CardioSet> cardioSets = cardioSetRepository.findByWorkoutExerciseWorkoutExerciseIdOrderBySetNumber(workoutExerciseId);
        return workoutMapper.toCardioSetResponseList(cardioSets);
    }

    /**
     * Update cardio set.
     *
     * @param setId the set ID
     * @param createCardioSetRequest the updated cardio set information
     * @return SetResponse the updated cardio set response
     */
    @Transactional
    public SetResponse updateCardioSet(Long setId, CreateSetRequest createCardioSetRequest) {
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Cardio set", "ID", setId));

        // Update fields using mapper
        workoutMapper.updateCardioSetEntity(createCardioSetRequest, cardioSet);

        CardioSet savedCardioSet = cardioSetRepository.save(cardioSet);
        return workoutMapper.toSetResponse(savedCardioSet);
    }

    /**
     * Delete cardio set.
     *
     * @param setId the set ID
     */
    @Transactional
    public void deleteCardioSet(Long setId) {
        logger.debug("SERVICE: Deleting cardio set. setId={}", setId);
        
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Cardio set", "ID", setId));

        Integer setNumber = cardioSet.getSetNumber();
        Long workoutExerciseId = cardioSet.getWorkoutExercise().getWorkoutExerciseId();
        
        cardioSetRepository.delete(cardioSet);
        
        logger.info("SERVICE: Cardio set deleted successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   setId, workoutExerciseId, setNumber);
    }

    /**
     * Get all cardio sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    public List<SetResponse> getCardioSetsByWorkoutSession(Long sessionId) {
        List<CardioSet> cardioSets = cardioSetRepository.findByWorkoutExerciseWorkoutSessionSessionId(sessionId);
        return workoutMapper.toCardioSetResponseList(cardioSets);
    }
}
