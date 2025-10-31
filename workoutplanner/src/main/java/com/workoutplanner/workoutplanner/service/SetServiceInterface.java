package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.response.SetResponse;

import java.util.List;

/**
 * Generic service interface for Set operations.
 * 
 * @param <T> The specific set request type (CreateStrengthSetRequest, CreateCardioSetRequest, etc.)
 */
public interface SetServiceInterface<T> {
    SetResponse createSet(T createSetRequest);
    SetResponse getSetById(Long setId);
    List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId);
    SetResponse updateSet(Long setId, T createSetRequest);
    void deleteSet(Long setId);
}
