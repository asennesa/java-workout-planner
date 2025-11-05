package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateExerciseRequest;
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
 * Service implementation for Exercise entity operations.
 * Handles business logic for exercise management including creation, retrieval, and filtering.
 * 
 * Uses method-level @Transactional for optimal performance:
 * - Read operations use @Transactional(readOnly = true) for better performance
 * - Write operations use @Transactional for data modification
 */
@Service
public class ExerciseService implements ExerciseServiceInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(ExerciseService.class);
    
    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public ExerciseService(ExerciseRepository exerciseRepository, 
                          ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseMapper = exerciseMapper;
    }
    
    /**
     * Create a new exercise.
     * 
     * @param createExerciseRequest the exercise creation request
     * @return the created exercise response
     */
    @Transactional
    public ExerciseResponse createExercise(CreateExerciseRequest createExerciseRequest) {
        logger.debug("SERVICE: Creating new exercise. name={}, type={}, targetMuscle={}, difficulty={}", 
                    createExerciseRequest.getName(), createExerciseRequest.getType(), 
                    createExerciseRequest.getTargetMuscleGroup(), createExerciseRequest.getDifficultyLevel());
        
        Exercise exercise = exerciseMapper.toEntity(createExerciseRequest);
        Exercise savedExercise = exerciseRepository.save(exercise);
        
        logger.info("SERVICE: Exercise created successfully. exerciseId={}, name={}, type={}", 
                   savedExercise.getExerciseId(), savedExercise.getName(), savedExercise.getType());
        
        return exerciseMapper.toResponse(savedExercise);
    }
    
    /**
     * Get exercise by ID.
     * 
     * @param exerciseId the exercise ID
     * @return the exercise response
     * @throws ResourceNotFoundException if exercise not found
     */
    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "ID", exerciseId));
        
        return exerciseMapper.toResponse(exercise);
    }
    
    /**
     * Get all exercises.
     * 
     * @return list of all exercise responses
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAllExercises() {
        List<Exercise> exercises = exerciseRepository.findAll();
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get all exercises with pagination.
     * 
     * @param pageable pagination information (page number, size, sort)
     * @return paginated exercise responses
     */
    @Transactional(readOnly = true)
    public PagedResponse<ExerciseResponse> getAllExercises(Pageable pageable) {
        logger.debug("SERVICE: Fetching exercises with pagination. page={}, size={}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Exercise> exercisePage = exerciseRepository.findAll(pageable);
        List<ExerciseResponse> exerciseResponses = exerciseMapper.toResponseList(exercisePage.getContent());
        
        logger.info("SERVICE: Retrieved {} exercises on page {} of {}", 
                   exerciseResponses.size(), exercisePage.getNumber(), exercisePage.getTotalPages());
        
        return new PagedResponse<>(
            exerciseResponses,
            exercisePage.getNumber(),
            exercisePage.getSize(),
            exercisePage.getTotalElements(),
            exercisePage.getTotalPages()
        );
    }
    
    /**
     * Get exercises by type.
     * 
     * @param type the exercise type
     * @return list of exercises of the specified type
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByType(ExerciseType type) {
        List<Exercise> exercises = exerciseRepository.findByType(type);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by target muscle group.
     * 
     * @param targetMuscleGroup the target muscle group
     * @return list of exercises targeting the specified muscle group
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByTargetMuscleGroup(TargetMuscleGroup targetMuscleGroup) {
        List<Exercise> exercises = exerciseRepository.findByTargetMuscleGroup(targetMuscleGroup);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by difficulty level.
     * 
     * @param difficultyLevel the difficulty level
     * @return list of exercises of the specified difficulty
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByDifficultyLevel(DifficultyLevel difficultyLevel) {
        List<Exercise> exercises = exerciseRepository.findByDifficultyLevel(difficultyLevel);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by type and target muscle group.
     * 
     * @param type the exercise type
     * @param targetMuscleGroup the target muscle group
     * @return list of exercises matching both criteria
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByTypeAndTargetMuscleGroup(ExerciseType type, TargetMuscleGroup targetMuscleGroup) {
        List<Exercise> exercises = exerciseRepository.findByTypeAndTargetMuscleGroup(type, targetMuscleGroup);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Search exercises by name.
     * Sanitizes input to prevent SQL LIKE wildcard abuse.
     * 
     * @param name the name to search for
     * @return list of exercises with names containing the search term
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> searchExercisesByName(String name) {
        logger.debug("SERVICE: Searching exercises by name. searchTerm={}", name);
        
        // Sanitize input to escape LIKE wildcards
        String sanitizedName = ValidationUtils.sanitizeLikeWildcards(name.trim());
        
        List<Exercise> exercises = exerciseRepository.findByNameContainingIgnoreCase(sanitizedName);
        
        logger.info("SERVICE: Found {} exercises matching name search. searchTerm={}", 
                   exercises.size(), sanitizedName);
        
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Get exercises by multiple criteria.
     * 
     * @param type the exercise type (optional)
     * @param targetMuscleGroup the target muscle group (optional)
     * @param difficultyLevel the difficulty level (optional)
     * @return list of exercises matching the criteria
     */
    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercisesByCriteria(ExerciseType type, 
                                                         TargetMuscleGroup targetMuscleGroup, 
                                                         DifficultyLevel difficultyLevel) {
        List<Exercise> exercises = exerciseRepository.findByTypeAndTargetMuscleGroupAndDifficultyLevel(type, targetMuscleGroup, difficultyLevel);
        return exerciseMapper.toResponseList(exercises);
    }
    
    /**
     * Update exercise.
     * 
     * @param exerciseId the exercise ID
     * @param createExerciseRequest the updated exercise information
     * @return the updated exercise response
     * @throws ResourceNotFoundException if exercise not found
     */
    @Transactional
    public ExerciseResponse updateExercise(Long exerciseId, CreateExerciseRequest createExerciseRequest) {
        logger.debug("SERVICE: Updating exercise. exerciseId={}, newName={}", 
                    exerciseId, createExerciseRequest.getName());
        
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "ID", exerciseId));
        
        String oldName = exercise.getName();
        
        exerciseMapper.updateEntity(createExerciseRequest, exercise);
        Exercise savedExercise = exerciseRepository.save(exercise);
        
        logger.info("SERVICE: Exercise updated successfully. exerciseId={}, oldName={}, newName={}", 
                   exerciseId, oldName, savedExercise.getName());
        
        return exerciseMapper.toResponse(savedExercise);
    }
    
    /**
     * Delete exercise by ID.
     * 
     * @param exerciseId the exercise ID
     * @throws ResourceNotFoundException if exercise not found
     */
    @Transactional
    public void deleteExercise(Long exerciseId) {
        logger.debug("SERVICE: Deleting exercise. exerciseId={}", exerciseId);
        
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "ID", exerciseId));
        
        String name = exercise.getName();
        ExerciseType type = exercise.getType();
        
        exerciseRepository.delete(exercise);
        
        logger.info("SERVICE: Exercise deleted successfully. exerciseId={}, name={}, type={}", 
                   exerciseId, name, type);
    }
}
