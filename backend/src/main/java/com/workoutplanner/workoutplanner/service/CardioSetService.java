package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.BaseSetMapper;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.CardioSetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final BaseSetMapper baseSetMapper;

    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public CardioSetService(CardioSetRepository cardioSetRepository,
                           WorkoutExerciseRepository workoutExerciseRepository,
                           WorkoutMapper workoutMapper,
                           BaseSetMapper baseSetMapper) {
        this.cardioSetRepository = cardioSetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
        this.baseSetMapper = baseSetMapper;
    }

    /**
     * Create a new cardio set.
     * 
     * Professional approach: workoutExerciseId is passed separately as it comes from URL path,
     * not from the request body. This follows REST best practices.
     *
     * @param workoutExerciseId the workout exercise ID from URL path parameter
     * @param createCardioSetRequest the cardio set creation request from body
     * @return SetResponse the created cardio set response
     */
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    public SetResponse createSet(Long workoutExerciseId, CreateCardioSetRequest createCardioSetRequest) {
        logger.debug("SERVICE: Creating cardio set. workoutExerciseId={}, setNumber={}, duration={}s, distance={}", 
                    workoutExerciseId, createCardioSetRequest.getSetNumber(),
                    createCardioSetRequest.getDurationInSeconds(), createCardioSetRequest.getDistance());
        
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", workoutExerciseId));

        // Validate exercise type matches set type - business rule enforcement
        if (workoutExercise.getExercise() != null && 
            workoutExercise.getExercise().getType() != null && 
            workoutExercise.getExercise().getType() != ExerciseType.CARDIO) {
            throw new BusinessLogicException(
                String.format("Cannot add cardio sets to a %s exercise. Exercise '%s' is of type %s.",
                    workoutExercise.getExercise().getType(),
                    workoutExercise.getExercise().getName(),
                    workoutExercise.getExercise().getType())
            );
        }

        CardioSet cardioSet = workoutMapper.toCardioSetEntity(createCardioSetRequest);
        
        cardioSet.setWorkoutExercise(workoutExercise);

        CardioSet savedCardioSet = cardioSetRepository.save(cardioSet);
        
        logger.info("SERVICE: Cardio set created successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   savedCardioSet.getSetId(), savedCardioSet.getWorkoutExercise().getWorkoutExerciseId(),
                   savedCardioSet.getSetNumber());
        
        return baseSetMapper.toSetResponse(savedCardioSet);
    }

    /**
     * Get cardio set by ID.
     *
     * @param setId the set ID
     * @return SetResponse the cardio set response
     */
    @Transactional(readOnly = true)
    public SetResponse getSetById(Long setId) {
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Cardio set", "ID", setId));

        return baseSetMapper.toSetResponse(cardioSet);
    }

    /**
     * Get cardio sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    @PreAuthorize("@resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    public List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId) {
        List<CardioSet> cardioSets = cardioSetRepository.findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(workoutExerciseId);
        return baseSetMapper.toCardioSetResponseList(cardioSets);
    }

    /**
     * Update cardio set.
     *
     * @param setId the set ID
     * @param createCardioSetRequest the updated cardio set information
     * @return SetResponse the updated cardio set response
     */
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessCardioSet(#setId)")
    public SetResponse updateSet(Long setId, CreateCardioSetRequest createCardioSetRequest) {
        logger.debug("SERVICE: Updating cardio set. setId={}, duration={}s, distance={}", 
                    setId, createCardioSetRequest.getDurationInSeconds(), createCardioSetRequest.getDistance());
        
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Cardio set", "ID", setId));

        workoutMapper.updateCardioSetEntity(createCardioSetRequest, cardioSet);

        CardioSet savedCardioSet = cardioSetRepository.save(cardioSet);
        
        logger.info("SERVICE: Cardio set updated successfully. setId={}, setNumber={}", 
                   savedCardioSet.getSetId(), savedCardioSet.getSetNumber());
        
        return baseSetMapper.toSetResponse(savedCardioSet);
    }

    /**
     * Delete cardio set.
     *
     * @param setId the set ID
     */
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessCardioSet(#setId)")
    public void deleteSet(Long setId) {
        logger.debug("SERVICE: Soft deleting cardio set. setId={}", setId);
        
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Cardio set", "ID", setId));

        Integer setNumber = cardioSet.getSetNumber();
        Long workoutExerciseId = cardioSet.getWorkoutExercise().getWorkoutExerciseId();
        
        cardioSet.softDelete();
        cardioSetRepository.save(cardioSet);
        
        logger.info("SERVICE: Cardio set soft deleted successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   setId, workoutExerciseId, setNumber);
    }
    
    /**
     * Restore a soft deleted cardio set.
     * 
     * @param setId the set ID
     * @throws ResourceNotFoundException if cardio set not found
     */
    @Transactional
    public void restoreSet(Long setId) {
        logger.debug("SERVICE: Restoring soft deleted cardio set. setId={}", setId);
        
        // Use findByIdIncludingDeleted because we need to access the deleted set to restore it
        CardioSet cardioSet = cardioSetRepository.findByIdIncludingDeleted(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Cardio set", "ID", setId));
        
        if (cardioSet.isActive()) {
            logger.warn("SERVICE: Cardio set is already active. setId={}", setId);
            throw new BusinessLogicException("Cardio set is not deleted and cannot be restored");
        }
        
        cardioSet.restore();
        cardioSetRepository.save(cardioSet);
        
        logger.info("SERVICE: Cardio set restored successfully. setId={}", setId);
    }

    /**
     * Get all cardio sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    public List<SetResponse> getSetsByWorkoutSession(Long sessionId) {
        List<CardioSet> cardioSets = cardioSetRepository.findByWorkoutExercise_WorkoutSession_SessionId(sessionId);
        return baseSetMapper.toCardioSetResponseList(cardioSets);
    }
}
