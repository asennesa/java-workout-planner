package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.WorkoutSessionRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for managing workout session operations.
 * Handles business logic for workout sessions, exercises, and sets.
 */
@Service
@Transactional
public class WorkoutSessionService implements WorkoutSessionServiceInterface {

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutMapper workoutMapper;

    /**
     * Create a new workout session.
     *
     * @param createWorkoutRequest the workout creation request
     * @return WorkoutResponse the created workout response
     */
    public WorkoutResponse createWorkoutSession(CreateWorkoutRequest createWorkoutRequest) {
        // Validate user exists
        User user = userRepository.findById(createWorkoutRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + createWorkoutRequest.getUserId()));

        // Create workout session entity
        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setName(createWorkoutRequest.getName());
        workoutSession.setDescription(createWorkoutRequest.getDescription());
        workoutSession.setUser(user);
        workoutSession.setStatus(createWorkoutRequest.getStatus());
        workoutSession.setStartedAt(createWorkoutRequest.getStartedAt());
        workoutSession.setCompletedAt(createWorkoutRequest.getCompletedAt());
        workoutSession.setActualDurationInMinutes(createWorkoutRequest.getActualDurationInMinutes());
        workoutSession.setSessionNotes(createWorkoutRequest.getSessionNotes());

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        if (workoutSession.getStartedAt() == null && createWorkoutRequest.getStatus() == WorkoutStatus.IN_PROGRESS) {
            workoutSession.setStartedAt(now);
        }

        // Save workout session
        WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);

        // Convert to response DTO
        return workoutMapper.toWorkoutResponse(savedWorkoutSession);
    }

    /**
     * Get workout session by ID.
     *
     * @param sessionId the session ID
     * @return WorkoutResponse the workout response
     */
    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutSessionById(Long sessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found with ID: " + sessionId));

        return workoutMapper.toWorkoutResponse(workoutSession);
    }

    /**
     * Get all workout sessions for a user.
     *
     * @param userId the user ID
     * @return List of WorkoutResponse
     */
    @Transactional(readOnly = true)
    public List<WorkoutResponse> getWorkoutSessionsByUserId(Long userId) {
        List<WorkoutSession> workoutSessions = workoutSessionRepository.findByUserUserIdOrderByStartedAtDesc(userId);
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
     * Update workout session.
     *
     * @param sessionId the session ID
     * @param createWorkoutRequest the updated workout request
     * @return WorkoutResponse the updated workout response
     */
    public WorkoutResponse updateWorkoutSession(Long sessionId, CreateWorkoutRequest createWorkoutRequest) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found with ID: " + sessionId));

        // Update fields
        workoutSession.setName(createWorkoutRequest.getName());
        workoutSession.setDescription(createWorkoutRequest.getDescription());
        workoutSession.setStatus(createWorkoutRequest.getStatus());
        workoutSession.setStartedAt(createWorkoutRequest.getStartedAt());
        workoutSession.setCompletedAt(createWorkoutRequest.getCompletedAt());
        workoutSession.setActualDurationInMinutes(createWorkoutRequest.getActualDurationInMinutes());
        workoutSession.setSessionNotes(createWorkoutRequest.getSessionNotes());

        // Handle status transitions
        handleStatusTransition(workoutSession, createWorkoutRequest.getStatus());

        WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);
        return workoutMapper.toWorkoutResponse(savedWorkoutSession);
    }

    /**
     * Update workout session status.
     *
     * @param sessionId the session ID
     * @param status the new status
     * @return WorkoutResponse the updated workout response
     */
    public WorkoutResponse updateWorkoutSessionStatus(Long sessionId, WorkoutStatus status) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found with ID: " + sessionId));

        handleStatusTransition(workoutSession, status);
        workoutSession.setStatus(status);

        WorkoutSession savedWorkoutSession = workoutSessionRepository.save(workoutSession);
        return workoutMapper.toWorkoutResponse(savedWorkoutSession);
    }

    /**
     * Delete workout session.
     *
     * @param sessionId the session ID
     */
    public void deleteWorkoutSession(Long sessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found with ID: " + sessionId));

        workoutSessionRepository.delete(workoutSession);
    }

    /**
     * Add exercise to workout session.
     *
     * @param createWorkoutExerciseRequest the workout exercise request
     * @return WorkoutExerciseResponse the created workout exercise response
     */
    public WorkoutExerciseResponse addExerciseToWorkout(CreateWorkoutExerciseRequest createWorkoutExerciseRequest) {
        // Validate workout session exists
        WorkoutSession workoutSession = workoutSessionRepository.findById(createWorkoutExerciseRequest.getSessionId())
                .orElseThrow(() -> new RuntimeException("Workout session not found with ID: " + createWorkoutExerciseRequest.getSessionId()));

        // Validate exercise exists
        Exercise exercise = exerciseRepository.findById(createWorkoutExerciseRequest.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercise not found with ID: " + createWorkoutExerciseRequest.getExerciseId()));

        // Create workout exercise
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setWorkoutSession(workoutSession);
        workoutExercise.setExercise(exercise);
        workoutExercise.setOrderInWorkout(createWorkoutExerciseRequest.getOrderInWorkout());
        workoutExercise.setNotes(createWorkoutExerciseRequest.getNotes());

        WorkoutExercise savedWorkoutExercise = workoutExerciseRepository.save(workoutExercise);
        return workoutMapper.toWorkoutExerciseResponse(savedWorkoutExercise);
    }

    /**
     * Remove exercise from workout session.
     *
     * @param workoutExerciseId the workout exercise ID
     */
    public void removeExerciseFromWorkout(Long workoutExerciseId) {
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new RuntimeException("Workout exercise not found with ID: " + workoutExerciseId));

        workoutExerciseRepository.delete(workoutExercise);
    }

    /**
     * Get exercises for a workout session.
     *
     * @param sessionId the session ID
     * @return List of WorkoutExerciseResponse
     */
    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> getWorkoutExercises(Long sessionId) {
        List<WorkoutExercise> workoutExercises = workoutExerciseRepository.findByWorkoutSessionSessionIdOrderByOrderInWorkout(sessionId);
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
