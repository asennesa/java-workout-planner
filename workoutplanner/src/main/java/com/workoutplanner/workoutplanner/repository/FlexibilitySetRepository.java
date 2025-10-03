package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for FlexibilitySet entity.
 * Provides data access methods for flexibility sets.
 */
@Repository
public interface FlexibilitySetRepository extends JpaRepository<FlexibilitySet, Long> {

    /**
     * Find all flexibility sets for a specific workout exercise, ordered by set number.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of FlexibilitySet entities
     */
    List<FlexibilitySet> findByWorkoutExerciseWorkoutExerciseIdOrderBySetNumber(Long workoutExerciseId);

    /**
     * Find all flexibility sets for a specific workout session.
     *
     * @param sessionId the workout session ID
     * @return List of FlexibilitySet entities
     */
    List<FlexibilitySet> findByWorkoutExerciseWorkoutSessionSessionId(Long sessionId);

    /**
     * Find all completed flexibility sets for a specific workout exercise.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of completed FlexibilitySet entities
     */
    List<FlexibilitySet> findByWorkoutExerciseWorkoutExerciseIdAndCompletedTrueOrderBySetNumber(Long workoutExerciseId);

    /**
     * Count flexibility sets for a specific workout exercise.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return number of flexibility sets
     */
    long countByWorkoutExerciseWorkoutExerciseId(Long workoutExerciseId);
}
