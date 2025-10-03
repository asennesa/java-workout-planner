package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.response.StrengthSetResponse;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.StrengthSetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing strength set operations.
 * Handles business logic for strength sets.
 */
@Service
@Transactional
public class StrengthSetService {

    @Autowired
    private StrengthSetRepository strengthSetRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private WorkoutMapper workoutMapper;

    /**
     * Create a new strength set.
     *
     * @param createStrengthSetRequest the strength set creation request
     * @return StrengthSetResponse the created strength set response
     */
    public StrengthSetResponse createStrengthSet(CreateStrengthSetRequest createStrengthSetRequest) {
        // Validate workout exercise exists
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(createStrengthSetRequest.getWorkoutExerciseId())
                .orElseThrow(() -> new RuntimeException("Workout exercise not found with ID: " + createStrengthSetRequest.getWorkoutExerciseId()));

        // Create strength set entity
        StrengthSet strengthSet = new StrengthSet();
        strengthSet.setWorkoutExercise(workoutExercise);
        strengthSet.setSetNumber(createStrengthSetRequest.getSetNumber());
        strengthSet.setReps(createStrengthSetRequest.getReps());
        strengthSet.setWeight(createStrengthSetRequest.getWeight());
        strengthSet.setRestTimeInSeconds(createStrengthSetRequest.getRestTimeInSeconds());
        strengthSet.setNotes(createStrengthSetRequest.getNotes());
        strengthSet.setCompleted(createStrengthSetRequest.getCompleted());

        StrengthSet savedStrengthSet = strengthSetRepository.save(strengthSet);
        return workoutMapper.toStrengthSetResponse(savedStrengthSet);
    }

    /**
     * Get strength set by ID.
     *
     * @param setId the set ID
     * @return StrengthSetResponse the strength set response
     */
    @Transactional(readOnly = true)
    public StrengthSetResponse getStrengthSetById(Long setId) {
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Strength set not found with ID: " + setId));

        return workoutMapper.toStrengthSetResponse(strengthSet);
    }

    /**
     * Get strength sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of StrengthSetResponse
     */
    @Transactional(readOnly = true)
    public List<StrengthSetResponse> getStrengthSetsByWorkoutExercise(Long workoutExerciseId) {
        List<StrengthSet> strengthSets = strengthSetRepository.findByWorkoutExerciseWorkoutExerciseIdOrderBySetNumber(workoutExerciseId);
        return workoutMapper.toStrengthSetResponseList(strengthSets);
    }

    /**
     * Update strength set.
     *
     * @param setId the set ID
     * @param createStrengthSetRequest the updated strength set information
     * @return StrengthSetResponse the updated strength set response
     */
    public StrengthSetResponse updateStrengthSet(Long setId, CreateStrengthSetRequest createStrengthSetRequest) {
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Strength set not found with ID: " + setId));

        // Update fields
        strengthSet.setSetNumber(createStrengthSetRequest.getSetNumber());
        strengthSet.setReps(createStrengthSetRequest.getReps());
        strengthSet.setWeight(createStrengthSetRequest.getWeight());
        strengthSet.setRestTimeInSeconds(createStrengthSetRequest.getRestTimeInSeconds());
        strengthSet.setNotes(createStrengthSetRequest.getNotes());
        strengthSet.setCompleted(createStrengthSetRequest.getCompleted());

        StrengthSet savedStrengthSet = strengthSetRepository.save(strengthSet);
        return workoutMapper.toStrengthSetResponse(savedStrengthSet);
    }

    /**
     * Delete strength set.
     *
     * @param setId the set ID
     */
    public void deleteStrengthSet(Long setId) {
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Strength set not found with ID: " + setId));

        strengthSetRepository.delete(strengthSet);
    }

    /**
     * Get all strength sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of StrengthSetResponse
     */
    @Transactional(readOnly = true)
    public List<StrengthSetResponse> getStrengthSetsByWorkoutSession(Long sessionId) {
        List<StrengthSet> strengthSets = strengthSetRepository.findByWorkoutExerciseWorkoutSessionSessionId(sessionId);
        return workoutMapper.toStrengthSetResponseList(strengthSets);
    }
}
