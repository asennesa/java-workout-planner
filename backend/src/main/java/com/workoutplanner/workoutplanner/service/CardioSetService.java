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
 * Service for managing cardio set operations.
 */
@Service
public class CardioSetService implements SetServiceInterface<CreateCardioSetRequest> {

    private static final Logger logger = LoggerFactory.getLogger(CardioSetService.class);
    private static final String CARDIO_SET = "Cardio set";

    private final CardioSetRepository cardioSetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutMapper workoutMapper;
    private final BaseSetMapper baseSetMapper;

    public CardioSetService(CardioSetRepository cardioSetRepository,
                           WorkoutExerciseRepository workoutExerciseRepository,
                           WorkoutMapper workoutMapper,
                           BaseSetMapper baseSetMapper) {
        this.cardioSetRepository = cardioSetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
        this.baseSetMapper = baseSetMapper;
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    public SetResponse createSet(Long workoutExerciseId, CreateCardioSetRequest request) {
        logger.debug("Creating cardio set for workoutExerciseId={}", workoutExerciseId);

        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", workoutExerciseId));

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

        CardioSet cardioSet = workoutMapper.toCardioSetEntity(request);
        cardioSet.setWorkoutExercise(workoutExercise);
        CardioSet saved = cardioSetRepository.save(cardioSet);

        logger.info("Cardio set created: setId={}", saved.getSetId());
        return baseSetMapper.toSetResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SetResponse getSetById(Long setId) {
        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(CARDIO_SET, "ID", setId));
        return baseSetMapper.toSetResponse(cardioSet);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    public List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId) {
        List<CardioSet> sets = cardioSetRepository.findByWorkoutExerciseIdOrderBySetNumber(workoutExerciseId);
        return baseSetMapper.toCardioSetResponseList(sets);
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessCardioSet(#setId)")
    public SetResponse updateSet(Long setId, CreateCardioSetRequest request) {
        logger.debug("Updating cardio set: setId={}", setId);

        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(CARDIO_SET, "ID", setId));

        workoutMapper.updateCardioSetEntity(request, cardioSet);
        CardioSet saved = cardioSetRepository.save(cardioSet);

        logger.info("Cardio set updated: setId={}", saved.getSetId());
        return baseSetMapper.toSetResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessCardioSet(#setId)")
    public void deleteSet(Long setId) {
        logger.debug("Soft deleting cardio set: setId={}", setId);

        CardioSet cardioSet = cardioSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(CARDIO_SET, "ID", setId));

        cardioSet.softDelete();
        cardioSetRepository.save(cardioSet);

        logger.info("Cardio set deleted: setId={}", setId);
    }
}
