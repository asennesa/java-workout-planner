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
import jakarta.validation.ValidationException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final Clock clock;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public WorkoutSessionService(WorkoutSessionRepository workoutSessionRepository,
                                WorkoutExerciseRepository workoutExerciseRepository,
                                UserRepository userRepository,
                                ExerciseRepository exerciseRepository,
                                WorkoutMapper workoutMapper,
                                Clock clock) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutExerciseRepository = workoutExerciseRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutMapper = workoutMapper;
        this.clock = clock;
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
        
        // Validate workout dates
        validateWorkoutDates(createWorkoutRequest.getStartedAt(), createWorkoutRequest.getCompletedAt());
        
        User user = userRepository.findById(createWorkoutRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", createWorkoutRequest.getUserId()));

        WorkoutSession workoutSession = workoutMapper.toEntity(createWorkoutRequest);
        
        workoutSession.setUser(user);

        LocalDateTime now = LocalDateTime.now(clock);
        if (workoutSession.getStartedAt() == null && createWorkoutRequest.getStatus() == WorkoutStatus.IN_PROGRESS) {
            workoutSession.setStartedAt(now);
        }

        WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);

        logger.info("SERVICE: Workout session created successfully. sessionId={}, userId={}, name={}, status={}", 
                   savedWorkoutSession.getSessionId(), savedWorkoutSession.getUser().getUserId(), 
                   savedWorkoutSession.getName(), savedWorkoutSession.getStatus());

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
        WorkoutSession workoutSession = workoutSessionRepository.findWithUserBySessionId(sessionId)
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
        List<WorkoutSession> workoutSessions = workoutSessionRepository.findByUser_UserIdOrderByStartedAtDesc(userId);
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
        WorkoutSession workoutSession = workoutSessionRepository.findWithUserBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", sessionId));
        
        List<WorkoutExercise> exercises = workoutExerciseRepository.findByWorkoutSession_SessionIdOrderByOrderInWorkoutAsc(sessionId);
        
        loadSetsBasedOnExerciseType(sessionId, exercises);
        
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
        
        Map<Long, WorkoutExercise> strengthExercises = workoutExerciseRepository.findStrengthExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(sessionId, "STRENGTH")
                .stream().collect(Collectors.toMap(WorkoutExercise::getWorkoutExerciseId, Function.identity()));
        logger.debug("Loaded {} strength exercises with sets", strengthExercises.size());
        
        Map<Long, WorkoutExercise> cardioExercises = workoutExerciseRepository.findCardioExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(sessionId, "CARDIO")
                .stream().collect(Collectors.toMap(WorkoutExercise::getWorkoutExerciseId, Function.identity()));
        logger.debug("Loaded {} cardio exercises with sets", cardioExercises.size());
        
        Map<Long, WorkoutExercise> flexibilityExercises = workoutExerciseRepository.findFlexibilityExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(sessionId, "FLEXIBILITY")
                .stream().collect(Collectors.toMap(WorkoutExercise::getWorkoutExerciseId, Function.identity()));
        logger.debug("Loaded {} flexibility exercises with sets", flexibilityExercises.size());
        
        for (WorkoutExercise exercise : exercises) {
            WorkoutExercise strengthExercise = strengthExercises.get(exercise.getWorkoutExerciseId());
            if (strengthExercise != null) {
                exercise.setStrengthSets(strengthExercise.getStrengthSets());
            }

            WorkoutExercise cardioExercise = cardioExercises.get(exercise.getWorkoutExerciseId());
            if (cardioExercise != null) {
                exercise.setCardioSets(cardioExercise.getCardioSets());
            }

            WorkoutExercise flexibilityExercise = flexibilityExercises.get(exercise.getWorkoutExerciseId());
            if (flexibilityExercise != null) {
                exercise.setFlexibilitySets(flexibilityExercise.getFlexibilitySets());
            }
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
            // Validate workout dates
            validateWorkoutDates(createWorkoutRequest.getStartedAt(), createWorkoutRequest.getCompletedAt());
            
            WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", sessionId));

            workoutMapper.updateEntity(createWorkoutRequest, workoutSession);

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

    public WorkoutResponse performAction(Long sessionId, String action) {
        WorkoutStatus status = switch (action.toLowerCase()) {
            case "start", "resume" -> WorkoutStatus.IN_PROGRESS;
            case "pause" -> WorkoutStatus.PAUSED;
            case "complete" -> WorkoutStatus.COMPLETED;
            case "cancel" -> WorkoutStatus.CANCELLED;
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        };
        return updateWorkoutSessionStatus(sessionId, status);
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
     * Professional approach: sessionId is passed separately as it comes from URL path,
     * not from the request body. This follows REST best practices.
     *
     * @param sessionId the workout session ID from URL path parameter
     * @param createWorkoutExerciseRequest the workout exercise request from body
     * @return WorkoutExerciseResponse the created workout exercise response
     */
    @Transactional
    public WorkoutExerciseResponse addExerciseToWorkout(Long sessionId, CreateWorkoutExerciseRequest createWorkoutExerciseRequest) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout session", "ID", sessionId));

        Exercise exercise = exerciseRepository.findById(createWorkoutExerciseRequest.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "ID", createWorkoutExerciseRequest.getExerciseId()));

        WorkoutExercise workoutExercise = workoutMapper.toWorkoutExerciseEntity(createWorkoutExerciseRequest);
        
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
        List<WorkoutExercise> workoutExercises = workoutExerciseRepository.findByWorkoutSession_SessionIdOrderByOrderInWorkoutAsc(sessionId);
        return workoutMapper.toWorkoutExerciseResponseList(workoutExercises);
    }


    /**
     * Validates workout dates according to business rules.
     * Service-layer validation following best practices.
     * 
     * Business rules:
     * - startedAt cannot be in the future
     * - completedAt cannot be before startedAt
     * - completedAt cannot be in the future
     *
     * @param startedAt the workout start date/time
     * @param completedAt the workout completion date/time
     * @throws ValidationException if dates violate business rules
     */
    private void validateWorkoutDates(LocalDateTime startedAt, LocalDateTime completedAt) {
        if (startedAt == null && completedAt == null) {
            return; // Both null is valid
        }
        
        LocalDateTime now = LocalDateTime.now(clock);
        
        if (startedAt != null && startedAt.isAfter(now)) {
            throw new ValidationException("Workout session cannot start in the future");
        }
        
        if (startedAt != null && completedAt != null && completedAt.isBefore(startedAt)) {
            throw new ValidationException("Workout session cannot be completed before it starts");
        }
        
        if (completedAt != null && completedAt.isAfter(now)) {
            throw new ValidationException("Workout session cannot be completed in the future");
        }
    }

    /**
     * Handle status transitions for workout sessions.
     *
     * @param workoutSession the workout session
     * @param newStatus the new status
     */
    private void handleStatusTransition(WorkoutSession workoutSession, WorkoutStatus newStatus) {
        LocalDateTime now = LocalDateTime.now(clock);

        switch (newStatus) {
            case IN_PROGRESS:
                if (workoutSession.getStartedAt() == null) {
                    workoutSession.setStartedAt(now);
                }
                break;
            case COMPLETED:
                if (workoutSession.getStartedAt() != null && workoutSession.getCompletedAt() == null) {
                    workoutSession.setCompletedAt(now);
                    if (workoutSession.getActualDurationInMinutes() == null) {
                        long durationMinutes = java.time.Duration.between(workoutSession.getStartedAt(), now).toMinutes();
                        workoutSession.setActualDurationInMinutes((int) durationMinutes);
                    }
                }
                break;
            case CANCELLED:
            case PAUSED:
                break;
            case PLANNED:
                workoutSession.setStartedAt(null);
                workoutSession.setCompletedAt(null);
                workoutSession.setActualDurationInMinutes(null);
                break;
        }
    }
}
