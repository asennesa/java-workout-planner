package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.BaseSetMapper;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.StrengthSetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for managing strength set operations.
 * Handles business logic for strength sets.
 * 
 * Uses method-level @Transactional for optimal performance:
 * - Read operations use @Transactional(readOnly = true) for better performance
 * - Write operations use @Transactional for data modification
 */
@Service
public class StrengthSetService implements StrengthSetServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(StrengthSetService.class);

    private final StrengthSetRepository strengthSetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutMapper workoutMapper;
    private final BaseSetMapper baseSetMapper;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public StrengthSetService(StrengthSetRepository strengthSetRepository,
                             WorkoutExerciseRepository workoutExerciseRepository,
                             WorkoutMapper workoutMapper,
                             BaseSetMapper baseSetMapper) {
        this.strengthSetRepository = strengthSetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
        this.baseSetMapper = baseSetMapper;
    }

    /**
     * Create a new strength set.
     * 
     * Professional approach: workoutExerciseId is passed separately as it comes from URL path,
     * not from the request body. This follows REST best practices.
     *
     * @param workoutExerciseId the workout exercise ID from URL path parameter
     * @param createStrengthSetRequest the strength set creation request from body
     * @return SetResponse the created strength set response
     */
    @Transactional
    public SetResponse createSet(Long workoutExerciseId, CreateStrengthSetRequest createStrengthSetRequest) {
        logger.debug("SERVICE: Creating strength set. workoutExerciseId={}, setNumber={}, reps={}, weight={}", 
                    workoutExerciseId, createStrengthSetRequest.getSetNumber(),
                    createStrengthSetRequest.getReps(), createStrengthSetRequest.getWeight());
        
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", workoutExerciseId));

        StrengthSet strengthSet = workoutMapper.toStrengthSetEntity(createStrengthSetRequest);
        
        strengthSet.setWorkoutExercise(workoutExercise);

        StrengthSet savedStrengthSet = strengthSetRepository.save(strengthSet);
        
        logger.info("SERVICE: Strength set created successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   savedStrengthSet.getSetId(), savedStrengthSet.getWorkoutExercise().getWorkoutExerciseId(),
                   savedStrengthSet.getSetNumber());
        
        return baseSetMapper.toSetResponse(savedStrengthSet);
    }

    /**
     * Get strength set by ID.
     *
     * @param setId the set ID
     * @return SetResponse the strength set response
     */
    @Transactional(readOnly = true)
    public SetResponse getSetById(Long setId) {
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Strength set", "ID", setId));

        return baseSetMapper.toSetResponse(strengthSet);
    }

    /**
     * Get strength sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    public List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId) {
        List<StrengthSet> strengthSets = strengthSetRepository.findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(workoutExerciseId);
        return baseSetMapper.toSetResponseList(strengthSets);
    }

    /**
     * Update strength set.
     *
     * @param setId the set ID
     * @param createStrengthSetRequest the updated strength set information
     * @return SetResponse the updated strength set response
     */
    @Transactional
    public SetResponse updateSet(Long setId, CreateStrengthSetRequest createStrengthSetRequest) {
        logger.debug("SERVICE: Updating strength set. setId={}, reps={}, weight={}", 
                    setId, createStrengthSetRequest.getReps(), createStrengthSetRequest.getWeight());
        
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Strength set", "ID", setId));

        workoutMapper.updateStrengthSetEntity(createStrengthSetRequest, strengthSet);

        StrengthSet savedStrengthSet = strengthSetRepository.save(strengthSet);
        
        logger.info("SERVICE: Strength set updated successfully. setId={}, setNumber={}", 
                   savedStrengthSet.getSetId(), savedStrengthSet.getSetNumber());
        
        return baseSetMapper.toSetResponse(savedStrengthSet);
    }

    /**
     * Delete strength set.
     *
     * @param setId the set ID
     */
    @Transactional
    public void deleteSet(Long setId) {
        logger.debug("SERVICE: Deleting strength set. setId={}", setId);
        
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Strength set", "ID", setId));

        Integer setNumber = strengthSet.getSetNumber();
        Long workoutExerciseId = strengthSet.getWorkoutExercise().getWorkoutExerciseId();
        
        strengthSetRepository.delete(strengthSet);
        
        logger.info("SERVICE: Strength set deleted successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   setId, workoutExerciseId, setNumber);
    }

    /**
     * Get all strength sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    public List<SetResponse> getSetsByWorkoutSession(Long sessionId) {
        List<StrengthSet> strengthSets = strengthSetRepository.findByWorkoutExercise_WorkoutSession_SessionId(sessionId);
        return baseSetMapper.toSetResponseList(strengthSets);
    }
}
