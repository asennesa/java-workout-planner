package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.BaseSetMapper;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.FlexibilitySetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing flexibility set operations.
 */
@Service
public class FlexibilitySetService implements SetServiceInterface<CreateFlexibilitySetRequest> {

    private static final Logger logger = LoggerFactory.getLogger(FlexibilitySetService.class);
    private static final String FLEXIBILITY_SET = "Flexibility set";

    private final FlexibilitySetRepository flexibilitySetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutMapper workoutMapper;
    private final BaseSetMapper baseSetMapper;

    public FlexibilitySetService(FlexibilitySetRepository flexibilitySetRepository,
                                WorkoutExerciseRepository workoutExerciseRepository,
                                WorkoutMapper workoutMapper,
                                BaseSetMapper baseSetMapper) {
        this.flexibilitySetRepository = flexibilitySetRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.workoutMapper = workoutMapper;
        this.baseSetMapper = baseSetMapper;
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    public SetResponse createSet(Long workoutExerciseId, CreateFlexibilitySetRequest request) {
        logger.debug("Creating flexibility set for workoutExerciseId={}", workoutExerciseId);

        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", workoutExerciseId));

        if (workoutExercise.getExercise() != null &&
            workoutExercise.getExercise().getType() != null &&
            workoutExercise.getExercise().getType() != ExerciseType.FLEXIBILITY) {
            throw new BusinessLogicException(
                String.format("Cannot add flexibility sets to a %s exercise. Exercise '%s' is of type %s.",
                    workoutExercise.getExercise().getType(),
                    workoutExercise.getExercise().getName(),
                    workoutExercise.getExercise().getType())
            );
        }

        FlexibilitySet flexibilitySet = workoutMapper.toFlexibilitySetEntity(request);
        flexibilitySet.setWorkoutExercise(workoutExercise);
        FlexibilitySet saved = flexibilitySetRepository.save(flexibilitySet);

        logger.info("Flexibility set created: setId={}", saved.getSetId());
        return baseSetMapper.toSetResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SetResponse getSetById(Long setId) {
        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(FLEXIBILITY_SET, "ID", setId));
        return baseSetMapper.toSetResponse(flexibilitySet);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@resourceSecurityService.canAccessWorkoutExercise(#workoutExerciseId)")
    public List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId) {
        List<FlexibilitySet> sets = flexibilitySetRepository.findByWorkoutExerciseIdOrderBySetNumber(workoutExerciseId);
        return baseSetMapper.toFlexibilitySetResponseList(sets);
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessFlexibilitySet(#setId)")
    public SetResponse updateSet(Long setId, CreateFlexibilitySetRequest request) {
        logger.debug("Updating flexibility set: setId={}", setId);

        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(FLEXIBILITY_SET, "ID", setId));

        workoutMapper.updateFlexibilitySetEntity(request, flexibilitySet);
        FlexibilitySet saved = flexibilitySetRepository.save(flexibilitySet);

        logger.info("Flexibility set updated: setId={}", saved.getSetId());
        return baseSetMapper.toSetResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("@resourceSecurityService.canAccessFlexibilitySet(#setId)")
    public void deleteSet(Long setId) {
        logger.debug("Soft deleting flexibility set: setId={}", setId);

        FlexibilitySet flexibilitySet = flexibilitySetRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(FLEXIBILITY_SET, "ID", setId));

        flexibilitySet.softDelete();
        flexibilitySetRepository.save(flexibilitySet);

        logger.info("Flexibility set deleted: setId={}", setId);
    }
}
