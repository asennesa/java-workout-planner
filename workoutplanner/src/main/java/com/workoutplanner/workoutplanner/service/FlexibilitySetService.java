package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.dto.response.FlexibilitySetResponse;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.FlexibilitySetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
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

    private final FlexibilitySetRepository flexibilitySetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutMapper workoutMapper;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public FlexibilitySetService(FlexibilitySetRepository flexibilitySetRepository,
                                WorkoutExerciseRepository workoutExerciseRepository,
                                WorkoutMapper workoutMapper) {
        this.flexibilitySetRepository = flexibilitySetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
    }

    /**
     * Create a new flexibility set.
     *
     * @param createFlexibilitySetRequest the flexibility set creation request
     * @return FlexibilitySetResponse the created flexibility set response
     */
    @Transactional
    public FlexibilitySetResponse createFlexibilitySet(CreateFlexibilitySetRequest createFlexibilitySetRequest) {
        // Validate workout exercise exists
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(createFlexibilitySetRequest.getWorkoutExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", createFlexibilitySetRequest.getWorkoutExerciseId()));

        // Create flexibility set entity
        FlexibilitySet flexibilitySet = new FlexibilitySet();
        flexibilitySet.setWorkoutExercise(workoutExercise);
        flexibilitySet.setSetNumber(createFlexibilitySetRequest.getSetNumber());
        flexibilitySet.setDurationInSeconds(createFlexibilitySetRequest.getDurationInSeconds());
        flexibilitySet.setStretchType(createFlexibilitySetRequest.getStretchType());
        flexibilitySet.setIntensity(createFlexibilitySetRequest.getIntensity());
        flexibilitySet.setRestTimeInSeconds(createFlexibilitySetRequest.getRestTimeInSeconds());
        flexibilitySet.setNotes(createFlexibilitySetRequest.getNotes());
        flexibilitySet.setCompleted(createFlexibilitySetRequest.getCompleted());

        FlexibilitySet savedFlexibilitySet = flexibilitySetRepository.save(flexibilitySet);
        return workoutMapper.toFlexibilitySetResponse(savedFlexibilitySet);
    }

    /**
     * Get flexibility set by ID.
     *
     * @param setId the set ID
     * @return FlexibilitySetResponse the flexibility set response
     */
    @Transactional(readOnly = true)
    public FlexibilitySetResponse getFlexibilitySetById(Long setId) {
        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flexibility set", "ID", setId));

        return workoutMapper.toFlexibilitySetResponse(flexibilitySet);
    }

    /**
     * Get flexibility sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of FlexibilitySetResponse
     */
    @Transactional(readOnly = true)
    public List<FlexibilitySetResponse> getFlexibilitySetsByWorkoutExercise(Long workoutExerciseId) {
        List<FlexibilitySet> flexibilitySets = flexibilitySetRepository.findByWorkoutExerciseWorkoutExerciseIdOrderBySetNumber(workoutExerciseId);
        return workoutMapper.toFlexibilitySetResponseList(flexibilitySets);
    }

    /**
     * Update flexibility set.
     *
     * @param setId the set ID
     * @param createFlexibilitySetRequest the updated flexibility set information
     * @return FlexibilitySetResponse the updated flexibility set response
     */
    @Transactional
    public FlexibilitySetResponse updateFlexibilitySet(Long setId, CreateFlexibilitySetRequest createFlexibilitySetRequest) {
        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flexibility set", "ID", setId));

        // Update fields
        flexibilitySet.setSetNumber(createFlexibilitySetRequest.getSetNumber());
        flexibilitySet.setDurationInSeconds(createFlexibilitySetRequest.getDurationInSeconds());
        flexibilitySet.setStretchType(createFlexibilitySetRequest.getStretchType());
        flexibilitySet.setIntensity(createFlexibilitySetRequest.getIntensity());
        flexibilitySet.setRestTimeInSeconds(createFlexibilitySetRequest.getRestTimeInSeconds());
        flexibilitySet.setNotes(createFlexibilitySetRequest.getNotes());
        flexibilitySet.setCompleted(createFlexibilitySetRequest.getCompleted());

        FlexibilitySet savedFlexibilitySet = flexibilitySetRepository.save(flexibilitySet);
        return workoutMapper.toFlexibilitySetResponse(savedFlexibilitySet);
    }

    /**
     * Delete flexibility set.
     *
     * @param setId the set ID
     */
    @Transactional
    public void deleteFlexibilitySet(Long setId) {
        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flexibility set", "ID", setId));

        flexibilitySetRepository.delete(flexibilitySet);
    }

    /**
     * Get all flexibility sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of FlexibilitySetResponse
     */
    @Transactional(readOnly = true)
    public List<FlexibilitySetResponse> getFlexibilitySetsByWorkoutSession(Long sessionId) {
        List<FlexibilitySet> flexibilitySets = flexibilitySetRepository.findByWorkoutExerciseWorkoutSessionSessionId(sessionId);
        return workoutMapper.toFlexibilitySetResponseList(flexibilitySets);
    }
}
