package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.BaseSetMapper;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.StrengthSetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing strength set operations.
 */
@Service
public class StrengthSetService implements SetServiceInterface<CreateStrengthSetRequest> {

    private static final Logger logger = LoggerFactory.getLogger(StrengthSetService.class);
    private static final String STRENGTH_SET = "Strength set";

    private final StrengthSetRepository strengthSetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutMapper workoutMapper;
    private final BaseSetMapper baseSetMapper;

    public StrengthSetService(StrengthSetRepository strengthSetRepository,
                             WorkoutExerciseRepository workoutExerciseRepository,
                             WorkoutMapper workoutMapper,
                             BaseSetMapper baseSetMapper) {
        this.strengthSetRepository = strengthSetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
        this.baseSetMapper = baseSetMapper;
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    public SetResponse createSet(Long workoutExerciseId, CreateStrengthSetRequest request) {
        logger.debug("Creating strength set for workoutExerciseId={}", workoutExerciseId);

        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", workoutExerciseId));

        if (workoutExercise.getExercise() != null &&
            workoutExercise.getExercise().getType() != null &&
            workoutExercise.getExercise().getType() != ExerciseType.STRENGTH) {
            throw new BusinessLogicException(
                String.format("Cannot add strength sets to a %s exercise. Exercise '%s' is of type %s.",
                    workoutExercise.getExercise().getType(),
                    workoutExercise.getExercise().getName(),
                    workoutExercise.getExercise().getType())
            );
        }

        StrengthSet strengthSet = workoutMapper.toStrengthSetEntity(request);
        strengthSet.setWorkoutExercise(workoutExercise);
        StrengthSet saved = strengthSetRepository.save(strengthSet);

        logger.info("Strength set created: setId={}", saved.getSetId());
        return baseSetMapper.toSetResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SetResponse getSetById(Long setId) {
        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(STRENGTH_SET, "ID", setId));
        return baseSetMapper.toSetResponse(strengthSet);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    public List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId) {
        List<StrengthSet> sets = strengthSetRepository.findByWorkoutExerciseIdOrderBySetNumber(workoutExerciseId);
        return baseSetMapper.toSetResponseList(sets);
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessStrengthSet(#setId)")
    public SetResponse updateSet(Long setId, CreateStrengthSetRequest request) {
        logger.debug("Updating strength set: setId={}", setId);

        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(STRENGTH_SET, "ID", setId));

        workoutMapper.updateStrengthSetEntity(request, strengthSet);
        StrengthSet saved = strengthSetRepository.save(strengthSet);

        logger.info("Strength set updated: setId={}", saved.getSetId());
        return baseSetMapper.toSetResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessStrengthSet(#setId)")
    public void deleteSet(Long setId) {
        logger.debug("Soft deleting strength set: setId={}", setId);

        StrengthSet strengthSet = strengthSetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(STRENGTH_SET, "ID", setId));

        strengthSet.softDelete();
        strengthSetRepository.save(strengthSet);

        logger.info("Strength set deleted: setId={}", setId);
    }
}
