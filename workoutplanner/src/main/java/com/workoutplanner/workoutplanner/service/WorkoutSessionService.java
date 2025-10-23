package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.exception.OptimisticLockConflictException;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.WorkoutSessionRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for managing workout session operations.
 * Handles business logic for workout sessions, exercises, and sets.
 * 
 * Uses method-level @Transactional for optimal performance:
 * - Read operations use @Transactional(readOnly = true) for better performance
 * - Write operations use @Transactional for data modification
 */
@Service
public class WorkoutSessionService implements WorkoutSessionServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutSessionService.class);

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutMapper workoutMapper;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public WorkoutSessionService(WorkoutSessionRepository workoutSessionRepository,
                                WorkoutExerciseRepository workoutExerciseRepository,
                                UserRepository userRepository,
                                ExerciseRepository exerciseRepository,
                                WorkoutMapper workoutMapper) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutMapper = workoutMapper;
    }

    /**
     * Create a new workout session.
     *
     * @param createWorkoutRequest the workout creation request
     * @return WorkoutResponse the created workout response
     */
    @Transactional
    public WorkoutResponse createWorkoutSession(CreateWorkoutRequest createWorkoutRequest) {
        logger.debug("SERVICE: Creating workout session. userId={}, name={}, status={}", 
                    createWorkoutRequest.getUserId(), createWorkoutRequest.getName(), createWorkoutRequest.getStatus());
        
        // Validate user exists
        User user = userRepository.findById(createWorkoutRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", createWorkoutRequest.getUserId()));

        // Map request to entity using mapper
        WorkoutSession workoutSession = workoutMapper.toEntity(createWorkoutRequest);
        
        // Set the user (not handled by mapper)
        workoutSession.setUser(user);

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        if (workoutSession.getStartedAt() == null && createWorkoutRequest.getStatus() == WorkoutStatus.IN_PROGRESS) {
            workoutSession.setStartedAt(now);
        }

        // Save workout session
        WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);

        logger.info("SERVICE: Workout session created successfully. sessionId={}, userId={}, name={}, status={}", 
                   savedWorkoutSession.getSessionId(), savedWorkoutSession.getUser().getUserId(), 
                   savedWorkoutSession.getName(), savedWorkoutSession.getStatus());

        // Convert to response DTO
        return workoutMapper.toWorkoutResponse(savedWorkoutSession);
    }

    /**
     * Get workout session by ID.
     * Uses JOIN FETCH to prevent N+1 query problem when accessing user details.
     *
     * @param sessionId the session ID
     * @return WorkoutResponse the workout response
     */
    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutSessionById(Long sessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findByIdWithUser(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", sessionId));

        return workoutMapper.toWorkoutResponse(workoutSession);
    }

    /**
     * Get all workout sessions for a user.
     * Uses JOIN FETCH to prevent N+1 query problem when accessing user details.
     *
     * @param userId the user ID
     * @return List of WorkoutResponse
     */
    @Transactional(readOnly = true)
    public List<WorkoutResponse> getWorkoutSessionsByUserId(Long userId) {
        List<WorkoutSession> workoutSessions = workoutSessionRepository.findByUserIdWithUser(userId);
        return workoutMapper.toWorkoutResponseList(workoutSessions);
    }

    /**
     * Get all workout sessions.
     *
     * @return List of WorkoutResponse
     */
    @Transactional(readOnly = true)
    public List<WorkoutResponse> getAllWorkoutSessions() {
        List<WorkoutSession> workoutSessions = workoutSessionRepository.findAll();
        return workoutMapper.toWorkoutResponseList(workoutSessions);
    }

    /**
     * Get workout session with smart loading of sets.
     * Only loads sets based on exercise type to optimize performance.
     * 
     * @param sessionId the workout session ID
     * @return WorkoutResponse with smart-loaded sets
     */
    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutSessionWithSmartLoading(Long sessionId) {
        logger.info("Loading workout session {} with smart loading", sessionId);
        
        // Load workout session with user
        WorkoutSession workoutSession = workoutSessionRepository.findByIdWithUser(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", sessionId));
        
        // Load exercises with their exercise details
        List<WorkoutExercise> exercises = workoutExerciseRepository.findBySessionIdWithExerciseDetails(sessionId);
        
        // Smart loading: Load sets based on exercise type
        loadSetsBasedOnExerciseType(sessionId, exercises);
        
        // Set the exercises on the workout session
        workoutSession.setWorkoutExercises(exercises);
        
        logger.info("Successfully loaded workout session {} with {} exercises using smart loading", 
                   sessionId, exercises.size());
        
        return workoutMapper.toWorkoutResponse(workoutSession);
    }
    
    /**
     * Smart loading method that loads sets based on exercise type.
     * This prevents loading unnecessary set types and optimizes performance.
     * 
     * @param sessionId the workout session ID
     * @param exercises the list of exercises to load sets for
     */
    private void loadSetsBasedOnExerciseType(Long sessionId, List<WorkoutExercise> exercises) {
        logger.debug("Starting smart loading for session {} with {} exercises", sessionId, exercises.size());
        
        // Load strength exercises with their sets
        List<WorkoutExercise> strengthExercises = workoutExerciseRepository.findStrengthExercisesWithSets(sessionId);
        logger.debug("Loaded {} strength exercises with sets", strengthExercises.size());
        
        // Load cardio exercises with their sets
        List<WorkoutExercise> cardioExercises = workoutExerciseRepository.findCardioExercisesWithSets(sessionId);
        logger.debug("Loaded {} cardio exercises with sets", cardioExercises.size());
        
        // Load flexibility exercises with their sets
        List<WorkoutExercise> flexibilityExercises = workoutExerciseRepository.findFlexibilityExercisesWithSets(sessionId);
        logger.debug("Loaded {} flexibility exercises with sets", flexibilityExercises.size());
        
        // Merge the loaded exercises back into the main list
        // This ensures the exercises have their sets loaded
        for (WorkoutExercise exercise : exercises) {
            // Find the corresponding exercise with loaded sets
            strengthExercises.stream()
                .filter(se -> se.getWorkoutExerciseId().equals(exercise.getWorkoutExerciseId()))
                .findFirst()
                .ifPresent(loadedExercise -> {
                    exercise.setStrengthSets(loadedExercise.getStrengthSets());
                });
            
            cardioExercises.stream()
                .filter(ce -> ce.getWorkoutExerciseId().equals(exercise.getWorkoutExerciseId()))
                .findFirst()
                .ifPresent(loadedExercise -> {
                    exercise.setCardioSets(loadedExercise.getCardioSets());
                });
            
            flexibilityExercises.stream()
                .filter(fe -> fe.getWorkoutExerciseId().equals(exercise.getWorkoutExerciseId()))
                .findFirst()
                .ifPresent(loadedExercise -> {
                    exercise.setFlexibilitySets(loadedExercise.getFlexibilitySets());
                });
        }
        
        logger.debug("Completed smart loading for session {}", sessionId);
    }
    
    /**
     * Get all workout sessions with pagination.
     *
     * @param pageable pagination information (page number, size, sort)
     * @return Paginated WorkoutResponse
     */
    @Transactional(readOnly = true)
    public PagedResponse<WorkoutResponse> getAllWorkoutSessions(Pageable pageable) {
        Page<WorkoutSession> workoutPage = workoutSessionRepository.findAll(pageable);
        List<WorkoutResponse> workoutResponses = workoutMapper.toWorkoutResponseList(workoutPage.getContent());
        
        return new PagedResponse<>(
            workoutResponses,
            workoutPage.getNumber(),
            workoutPage.getSize(),
            workoutPage.getTotalElements(),
            workoutPage.getTotalPages()
        );
    }

    /**
     * Update workout session.
     *
     * @param sessionId the session ID
     * @param createWorkoutRequest the updated workout request
     * @return WorkoutResponse the updated workout response
     */
    @Transactional
    public WorkoutResponse updateWorkoutSession(Long sessionId, CreateWorkoutRequest createWorkoutRequest) {
        try {
            WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", sessionId));

            // Update fields using mapper
            workoutMapper.updateEntity(createWorkoutRequest, workoutSession);

            // Handle status transitions
            handleStatusTransition(workoutSession, createWorkoutRequest.getStatus());

            WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);
            return workoutMapper.toWorkoutResponse(savedWorkoutSession);
            
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.warn("Optimistic lock conflict when updating workout session {}: {}", sessionId, e.getMessage());
            throw new OptimisticLockConflictException(
                "The workout session was modified by another user. Please refresh and try again."
            );
        }
    }

    /**
     * Update workout session status.
     *
     * @param sessionId the session ID
     * @param status the new status
     * @return WorkoutResponse the updated workout response
     */
    @Transactional
    public WorkoutResponse updateWorkoutSessionStatus(Long sessionId, WorkoutStatus status) {
        try {
            logger.debug("SERVICE: Updating workout session status. sessionId={}, newStatus={}", sessionId, status);
            
            WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", sessionId));

            WorkoutStatus oldStatus = workoutSession.getStatus();
            handleStatusTransition(workoutSession, status);
            workoutSession.setStatus(status);

            WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);
            
            logger.info("SERVICE: Workout session status updated. sessionId={}, oldStatus={}, newStatus={}", 
                       sessionId, oldStatus, status);
            
            return workoutMapper.toWorkoutResponse(savedWorkoutSession);
            
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.warn("Optimistic lock conflict when updating workout session status {}: {}", sessionId, e.getMessage());
            throw new OptimisticLockConflictException(
                "The workout session was modified by another user. Please refresh and try again."
            );
        }
    }

    /**
     * Delete workout session.
     *
     * @param sessionId the session ID
     */
    @Transactional
    public void deleteWorkoutSession(Long sessionId) {
        logger.debug("SERVICE: Deleting workout session. sessionId={}", sessionId);
        
        WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", sessionId));

        String name = workoutSession.getName();
        WorkoutStatus status = workoutSession.getStatus();
        Long userId = workoutSession.getUser().getUserId();
        
        workoutSessionRepository.delete(workoutSession);
        
        logger.info("SERVICE: Workout session deleted successfully. sessionId={}, name={}, status={}, userId={}", 
                   sessionId, name, status, userId);
    }

    /**
     * Add exercise to workout session.
     *
     * @param createWorkoutExerciseRequest the workout exercise request
     * @return WorkoutExerciseResponse the created workout exercise response
     */
    @Transactional
    public WorkoutExerciseResponse addExerciseToWorkout(CreateWorkoutExerciseRequest createWorkoutExerciseRequest) {
        // Validate workout session exists
        WorkoutSession workoutSession = workoutSessionRepository.findById(createWorkoutExerciseRequest.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", createWorkoutExerciseRequest.getSessionId()));

        // Validate exercise exists
        Exercise exercise = exerciseRepository.findById(createWorkoutExerciseRequest.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "ID", createWorkoutExerciseRequest.getExerciseId()));

        // Map request to entity using mapper
        WorkoutExercise workoutExercise = workoutMapper.toWorkoutExerciseEntity(createWorkoutExerciseRequest);
        
        // Set relationships (not handled by mapper)
        workoutExercise.setWorkoutSession(workoutSession);
        workoutExercise.setExercise(exercise);

        WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(workoutExercise);
        return workoutMapper.toWorkoutExerciseResponse(savedWorkoutExercise);
    }

    /**
     * Remove exercise from workout session.
     *
     * @param workoutExerciseId the workout exercise ID
     */
    @Transactional
    public void removeExerciseFromWorkout(Long workoutExerciseId) {
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout exercise", "ID", workoutExerciseId));

        workoutExerciseRepository.delete(workoutExercise);
    }

    /**
     * Get exercises for a workout session.
     * Uses JOIN FETCH to prevent N+1 query problem when accessing exercise details.
     *
     * @param sessionId the session ID
     * @return List of WorkoutExerciseResponse
     */
    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> getWorkoutExercises(Long sessionId) {
        List<WorkoutExercise> workoutExercises = workoutExerciseRepository.findBySessionIdWithExercise(sessionId);
        return workoutMapper.toWorkoutExerciseResponseList(workoutExercises);
    }

    /**
     * Handle status transitions for workout sessions.
     *
     * @param workoutSession the workout session
     * @param newStatus the new status
     */
    private void handleStatusTransition(WorkoutSession workoutSession, WorkoutStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case IN_PROGRESS:
                if (workoutSession.getStartedAt() == null) {
                    workoutSession.setStartedAt(now);
                }
                break;
            case COMPLETED:
                if (workoutSession.getStartedAt() != null && workoutSession.getCompletedAt() == null) {
                    workoutSession.setCompletedAt(now);
                    // Calculate duration if not set
                    if (workoutSession.getActualDurationInMinutes() == null) {
                        long durationMinutes = java.time.Duration.between(workoutSession.getStartedAt(), now).toMinutes();
                        workoutSession.setActualDurationInMinutes((int) durationMinutes);
                    }
                }
                break;
            case CANCELLED:
            case PAUSED:
                // No automatic timestamp changes for these statuses
                break;
            case PLANNED:
                // Reset timestamps when going back to planned
                workoutSession.setStartedAt(null);
                workoutSession.setCompletedAt(null);
                workoutSession.setActualDurationInMinutes(null);
                break;
        }
    }
}
