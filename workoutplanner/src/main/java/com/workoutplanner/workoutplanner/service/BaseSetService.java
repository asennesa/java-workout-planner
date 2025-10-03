package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.entity.BaseSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Generic service for handling common set operations.
 * Uses generics to work with any BaseSet subclass.
 * 
 * This follows the Template Method Pattern and DRY principle.
 */
@Service
@Transactional
public abstract class BaseSetService<T extends BaseSet> {

    @Autowired
    protected WorkoutExerciseRepository workoutExerciseRepository;

    /**
     * Validate that a workout exercise exists.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return WorkoutExercise the workout exercise
     * @throws RuntimeException if workout exercise not found
     */
    protected WorkoutExercise validateWorkoutExercise(Long workoutExerciseId) {
        return workoutExerciseRepository.findById(workoutExerciseId)
                .orElseThrow(() -> new RuntimeException("Workout exercise not found with ID: " + workoutExerciseId));
    }

    /**
     * Get all sets for a workout session.
     * 
     * @param sessionId the workout session ID
     * @return List of sets for the session
     */
    @Transactional(readOnly = true)
    public abstract List<T> getSetsByWorkoutSession(Long sessionId);

    /**
     * Get set summary for display purposes.
     * 
     * @param set the set entity
     * @return String summary of the set
     */
    public String getSetSummary(T set) {
        return set.getSetSummary();
    }

    /**
     * Check if a set is completed.
     * 
     * @param set the set entity
     * @return boolean indicating if set is completed
     */
    public boolean isSetCompleted(T set) {
        return set.getCompleted() != null && set.getCompleted();
    }

    /**
     * Mark a set as completed.
     * 
     * @param set the set entity
     */
    public void markSetAsCompleted(T set) {
        set.setCompleted(true);
    }

    /**
     * Mark a set as not completed.
     * 
     * @param set the set entity
     */
    public void markSetAsNotCompleted(T set) {
        set.setCompleted(false);
    }
}
