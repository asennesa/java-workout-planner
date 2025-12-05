package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.ExerciseMapper;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import com.workoutplanner.workoutplanner.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only service for the exercise library.
 * Exercise CRUD is admin-only; users can only browse.
 */
@Service
public class ExerciseService implements ExerciseServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseService.class);

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    public ExerciseService(ExerciseRepository exerciseRepository, ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseMapper = exerciseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "ID", exerciseId));
        return exerciseMapper.toResponse(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ExerciseResponse> getAllExercises(Pageable pageable) {
        logger.debug("Fetching exercises: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Exercise> page = exerciseRepository.findAll(pageable);
        List<ExerciseResponse> responses = exerciseMapper.toResponseList(page.getContent());

        logger.info("Retrieved {} exercises (page {} of {})", responses.size(), page.getNumber(), page.getTotalPages());

        return new PagedResponse<>(
            responses,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> searchExercisesByName(String name) {
        logger.debug("Searching exercises by name: {}", name);

        String sanitizedName = ValidationUtils.sanitizeLikeWildcards(name.trim());
        List<Exercise> exercises = exerciseRepository.findByNameContainingIgnoreCase(sanitizedName);

        logger.info("Found {} exercises matching '{}'", exercises.size(), sanitizedName);
        return exerciseMapper.toResponseList(exercises);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByCriteria(ExerciseType type,
                                                         TargetMuscleGroup targetMuscleGroup,
                                                         DifficultyLevel difficultyLevel) {
        List<Exercise> exercises = exerciseRepository.findByFilters(type, targetMuscleGroup, difficultyLevel);
        return exerciseMapper.toResponseList(exercises);
    }
}
