package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;

import java.util.List;

public interface SetServiceInterface {
    SetResponse createSet(CreateSetRequest createSetRequest);
    SetResponse getSetById(Long setId);
    List<SetResponse> getSetsByWorkoutExercise(Long workoutExerciseId);
    SetResponse updateSet(Long setId, CreateSetRequest createSetRequest);
    void deleteSet(Long setId);
}
