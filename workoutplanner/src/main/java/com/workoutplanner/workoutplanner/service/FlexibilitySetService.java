package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.BaseSetMapper;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.FlexibilitySetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for managing flexibility set operations.
 * Handles business logic for flexibility sets.
 * 
 * Uses method-level @Transactional for optimal performance:
 * - Read operations use @Transactional(readOnly = true) for better performance
 * - Write operations use @Transactional for data modification
 */
@Service
public class FlexibilitySetService implements FlexibilitySetServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(FlexibilitySetService.class);

    private final FlexibilitySetRepository flexibilitySetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutMapper workoutMapper;
    private final BaseSetMapper baseSetMapper;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public FlexibilitySetService(FlexibilitySetRepository flexibilitySetRepository,
                                WorkoutExerciseRepository workoutExerciseRepository,
                                WorkoutMapper workoutMapper,
                                BaseSetMapper baseSetMapper) {
        this.flexibilitySetRepository = flexibilitySetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
        this.baseSetMapper = baseSetMapper;
    }

    /**
     * Create a new flexibility set.
     *
     * @param createFlexibilitySetRequest the flexibility set creation request
     * @return SetResponse the created flexibility set response
     */
    @Transactional
    public SetResponse createSet(CreateSetRequest createFlexibilitySetRequest) {
        logger.debug("SERVICE: Creating flexibility set. workoutExerciseId={}, setNumber={}, duration={}s, intensity={}", 
                    createFlexibilitySetRequest.getWorkoutExerciseId(), createFlexibilitySetRequest.getSetNumber(),
                    createFlexibilitySetRequest.getDurationInSeconds(), createFlexibilitySetRequest.getIntensity());
        
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(createFlexibilitySetRequest.getWorkoutExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", createFlexibilitySetRequest.getWorkoutExerciseId()));

        FlexibilitySet flexibilitySet = workoutMapper.toFlexibilitySetEntity(createFlexibilitySetRequest);
        
        flexibilitySet.setWorkoutExercise(workoutExercise);

        FlexibilitySet savedFlexibilitySet = flexibilitySetRepository.save(flexibilitySet);
        
        logger.info("SERVICE: Flexibility set created successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   savedFlexibilitySet.getSetId(), savedFlexibilitySet.getWorkoutExercise().getWorkoutExerciseId(),
                   savedFlexibilitySet.getSetNumber());
        
        return baseSetMapper.toSetResponse(savedFlexibilitySet);
    }

    /**
     * Get flexibility set by ID.
     *
     * @param setId the set ID
     * @return SetResponse the flexibility set response
     */
    @Transactional(readOnly = true)
    public SetResponse getSetById(Long setId) {
        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flexibility set", "ID", setId));

        return baseSetMapper.toSetResponse(flexibilitySet);
    }

    /**
     * Get flexibility sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    public List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId) {
        List<FlexibilitySet> flexibilitySets = flexibilitySetRepository.findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(workoutExerciseId);
        return baseSetMapper.toFlexibilitySetResponseList(flexibilitySets);
    }

    /**
     * Update flexibility set.
     *
     * @param setId the set ID
     * @param createFlexibilitySetRequest the updated flexibility set information
     * @return SetResponse the updated flexibility set response
     */
    @Transactional
    public SetResponse updateSet(Long setId, CreateSetRequest createFlexibilitySetRequest) {
        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flexibility set", "ID", setId));

        workoutMapper.updateFlexibilitySetEntity(createFlexibilitySetRequest, flexibilitySet);

        FlexibilitySet savedFlexibilitySet = flexibilitySetRepository.save(flexibilitySet);
        return baseSetMapper.toSetResponse(savedFlexibilitySet);
    }

    /**
     * Delete flexibility set.
     *
     * @param setId the set ID
     */
    @Transactional
    public void deleteSet(Long setId) {
        logger.debug("SERVICE: Deleting flexibility set. setId={}", setId);
        
        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flexibility set", "ID", setId));

        Integer setNumber = flexibilitySet.getSetNumber();
        Long workoutExerciseId = flexibilitySet.getWorkoutExercise().getWorkoutExerciseId();
        
        flexibilitySetRepository.delete(flexibilitySet);
        
        logger.info("SERVICE: Flexibility set deleted successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   setId, workoutExerciseId, setNumber);
    }

    /**
     * Get all flexibility sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    public List<SetResponse> getSetsByWorkoutSession(Long sessionId) {
        List<FlexibilitySet> flexibilitySets = flexibilitySetRepository.findByWorkoutExercise_WorkoutSession_SessionId(sessionId);
        return baseSetMapper.toFlexibilitySetResponseList(flexibilitySets);
    }
}
