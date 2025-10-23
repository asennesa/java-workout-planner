package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
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
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public StrengthSetService(StrengthSetRepository strengthSetRepository,
                             WorkoutExerciseRepository workoutExerciseRepository,
                             WorkoutMapper workoutMapper) {
        this.strengthSetRepository = strengthSetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
    }

    /**
     * Create a new strength set.
     *
     * @param createStrengthSetRequest the strength set creation request
     * @return SetResponse the created strength set response
     */
    @Transactional
    public SetResponse createStrengthSet(CreateSetRequest createSetRequest) {
        logger.debug("SERVICE: Creating strength set. workoutExerciseId={}, setNumber={}, reps={}, weight={}", 
                    createSetRequest.getWorkoutExerciseId(), createSetRequest.getSetNumber(),
                    createSetRequest.getReps(), createSetRequest.getWeight());
        
        // Validate workout exercise exists
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(createSetRequest.getWorkoutExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", createSetRequest.getWorkoutExerciseId()));

        // Map request to entity using mapper
        StrengthSet strengthSet = workoutMapper.toStrengthSetEntity(createSetRequest);
        
        // Set the workout exercise (not handled by mapper)
        strengthSet.setWorkoutExercise(workoutExercise);

        StrengthSet savedStrengthSet = strengthSetRepository.save(strengthSet);
        
        logger.info("SERVICE: Strength set created successfully. setId={}, workoutExerciseId={}, setNumber={}", 
                   savedStrengthSet.getSetId(), savedStrengthSet.getWorkoutExercise().getWorkoutExerciseId(),
                   savedStrengthSet.getSetNumber());
        
        return workoutMapper.toSetResponse(savedStrengthSet);
    }

    /**
     * Get strength set by ID.
     *
     * @param setId the set ID
     * @return SetResponse the strength set response
     */
    @Transactional(readOnly = true)
    public SetResponse getStrengthSetById(Long setId) {
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Strength set", "ID", setId));

        return workoutMapper.toSetResponse(strengthSet);
    }

    /**
     * Get strength sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of SetResponse
     */
    @Transactional(readOnly = true)
    public List<SetResponse> getStrengthSetsByWorkoutExercise(Long workoutExerciseId) {
        List<StrengthSet> strengthSets = strengthSetRepository.findByWorkoutExerciseWorkoutExerciseIdOrderBySetNumber(workoutExerciseId);
        return workoutMapper.toSetResponseList(strengthSets);
    }

    /**
     * Update strength set.
     *
     * @param setId the set ID
     * @param createStrengthSetRequest the updated strength set information
     * @return SetResponse the updated strength set response
     */
    @Transactional
    public SetResponse updateStrengthSet(Long setId, CreateSetRequest createStrengthSetRequest) {
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Strength set", "ID", setId));

        // Update fields using mapper
        workoutMapper.updateStrengthSetEntity(createStrengthSetRequest, strengthSet);

        StrengthSet savedStrengthSet = strengthSetRepository.save(strengthSet);
        return workoutMapper.toSetResponse(savedStrengthSet);
    }

    /**
     * Delete strength set.
     *
     * @param setId the set ID
     */
    @Transactional
    public void deleteStrengthSet(Long setId) {
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
    public List<SetResponse> getStrengthSetsByWorkoutSession(Long sessionId) {
        List<StrengthSet> strengthSets = strengthSetRepository.findByWorkoutExerciseWorkoutSessionSessionId(sessionId);
        return workoutMapper.toSetResponseList(strengthSets);
    }
}
