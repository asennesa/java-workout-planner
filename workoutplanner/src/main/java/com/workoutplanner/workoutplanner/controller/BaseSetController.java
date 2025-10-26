package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.service.SetServiceInterface;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseSetController {

    protected abstract SetServiceInterface getService(String setType);

    @PostMapping
    public ResponseEntity<SetResponse> createSet(@PathVariable String setType, @Valid @RequestBody CreateSetRequest createSetRequest) {
        SetResponse setResponse = getService(setType).createSet(createSetRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(setResponse);
    }

    @GetMapping("/workout-exercise/{workoutExerciseId}")
    public ResponseEntity<List<SetResponse>> getSetsByWorkoutExercise(@PathVariable String setType, @PathVariable Long workoutExerciseId) {
        List<SetResponse> setResponses = getService(setType).getSetsByWorkoutExercise(workoutExerciseId);
        return ResponseEntity.ok(setResponses);
    }

    @GetMapping("/{setId}")
    public ResponseEntity<SetResponse> getSetById(@PathVariable String setType, @PathVariable Long setId) {
        SetResponse setResponse = getService(setType).getSetById(setId);
        return ResponseEntity.ok(setResponse);
    }

    @PutMapping("/{setId}")
    public ResponseEntity<SetResponse> updateSet(@PathVariable String setType, @PathVariable Long setId, @Valid @RequestBody CreateSetRequest createSetRequest) {
        SetResponse setResponse = getService(setType).updateSet(setId, createSetRequest);
        return ResponseEntity.ok(setResponse);
    }

    @DeleteMapping("/{setId}")
    public ResponseEntity<Void> deleteSet(@PathVariable String setType, @PathVariable Long setId) {
        getService(setType).deleteSet(setId);
        return ResponseEntity.noContent().build();
    }
}
