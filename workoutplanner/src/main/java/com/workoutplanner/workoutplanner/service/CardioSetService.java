package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.dto.response.CardioSetResponse;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.CardioSetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for managing cardio set operations.
 * Handles business logic for cardio sets.
 */
@Service
@Transactional
public class CardioSetService implements CardioSetServiceInterface {

    @Autowired
    private CardioSetRepository cardioSetRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private WorkoutMapper workoutMapper;

    /**
     * Create a new cardio set.
     *
     * @param createCardioSetRequest the cardio set creation request
     * @return CardioSetResponse the created cardio set response
     */
    public CardioSetResponse createCardioSet(CreateCardioSetRequest createCardioSetRequest) {
        // Validate workout exercise exists
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(createCardioSetRequest.getWorkoutExerciseId())
                .orElseThrow(() -> new RuntimeException("Workout exercise not found with ID: " + createCardioSetRequest.getWorkoutExerciseId()));

        // Create cardio set entity
        CardioSet cardioSet = new CardioSet();
        cardioSet.setWorkoutExercise(workoutExercise);
        cardioSet.setSetNumber(createCardioSetRequest.getSetNumber());
        cardioSet.setDurationInSeconds(createCardioSetRequest.getDurationInSeconds());
        cardioSet.setDistance(createCardioSetRequest.getDistance());
        cardioSet.setDistanceUnit(createCardioSetRequest.getDistanceUnit());
        cardioSet.setRestTimeInSeconds(createCardioSetRequest.getRestTimeInSeconds());
        cardioSet.setNotes(createCardioSetRequest.getNotes());
        cardioSet.setCompleted(createCardioSetRequest.getCompleted());

        CardioSet savedCardioSet = cardioSetRepository.save(cardioSet);
        return workoutMapper.toCardioSetResponse(savedCardioSet);
    }

    /**
     * Get cardio set by ID.
     *
     * @param setId the set ID
     * @return CardioSetResponse the cardio set response
     */
    @Transactional(readOnly = true)
    public CardioSetResponse getCardioSetById(Long setId) {
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Cardio set not found with ID: " + setId));

        return workoutMapper.toCardioSetResponse(cardioSet);
    }

    /**
     * Get cardio sets by workout exercise ID.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of CardioSetResponse
     */
    @Transactional(readOnly = true)
    public List<CardioSetResponse> getCardioSetsByWorkoutExercise(Long workoutExerciseId) {
        List<CardioSet> cardioSets = cardioSetRepository.findByWorkoutExerciseWorkoutExerciseIdOrderBySetNumber(workoutExerciseId);
        return workoutMapper.toCardioSetResponseList(cardioSets);
    }

    /**
     * Update cardio set.
     *
     * @param setId the set ID
     * @param createCardioSetRequest the updated cardio set information
     * @return CardioSetResponse the updated cardio set response
     */
    public CardioSetResponse updateCardioSet(Long setId, CreateCardioSetRequest createCardioSetRequest) {
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Cardio set not found with ID: " + setId));

        // Update fields
        cardioSet.setSetNumber(createCardioSetRequest.getSetNumber());
        cardioSet.setDurationInSeconds(createCardioSetRequest.getDurationInSeconds());
        cardioSet.setDistance(createCardioSetRequest.getDistance());
        cardioSet.setDistanceUnit(createCardioSetRequest.getDistanceUnit());
        cardioSet.setRestTimeInSeconds(createCardioSetRequest.getRestTimeInSeconds());
        cardioSet.setNotes(createCardioSetRequest.getNotes());
        cardioSet.setCompleted(createCardioSetRequest.getCompleted());

        CardioSet savedCardioSet = cardioSetRepository.save(cardioSet);
        return workoutMapper.toCardioSetResponse(savedCardioSet);
    }

    /**
     * Delete cardio set.
     *
     * @param setId the set ID
     */
    public void deleteCardioSet(Long setId) {
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Cardio set not found with ID: " + setId));

        cardioSetRepository.delete(cardioSet);
    }

    /**
     * Get all cardio sets for a workout session.
     *
     * @param sessionId the workout session ID
     * @return List of CardioSetResponse
     */
    @Transactional(readOnly = true)
    public List<CardioSetResponse> getCardioSetsByWorkoutSession(Long sessionId) {
        List<CardioSet> cardioSets = cardioSetRepository.findByWorkoutExerciseWorkoutSessionSessionId(sessionId);
        return workoutMapper.toCardioSetResponseList(cardioSets);
    }
}
