package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for workout session operations.
 */
public interface WorkoutSessionServiceInterface {

    WorkoutResponse createWorkoutSession(CreateWorkoutRequest request);

    WorkoutResponse getWorkoutSessionById(Long sessionId);

    List<WorkoutResponse> getWorkoutSessionsByUserId(Long userId);

    PagedResponse<WorkoutResponse> getAllWorkoutSessions(Pageable pageable);

    WorkoutResponse updateWorkoutSession(Long sessionId, UpdateWorkoutRequest request);

    WorkoutResponse updateWorkoutSessionStatus(Long sessionId, WorkoutStatus status);

    void deleteWorkoutSession(Long sessionId);

    WorkoutExerciseResponse addExerciseToWorkout(Long sessionId, CreateWorkoutExerciseRequest request);

    void removeExerciseFromWorkout(Long workoutExerciseId);

    WorkoutExerciseResponse updateWorkoutExercise(Long workoutExerciseId, UpdateWorkoutExerciseRequest request);

    List<WorkoutExerciseResponse> getWorkoutExercises(Long sessionId);
}
