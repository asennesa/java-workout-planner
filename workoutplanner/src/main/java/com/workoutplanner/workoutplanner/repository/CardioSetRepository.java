package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.CardioSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for CardioSet entity.
 * Provides data access methods for cardio sets.
 */
@Repository
public interface CardioSetRepository extends JpaRepository<CardioSet, Long> {

    /**
     * Find all cardio sets for a specific workout exercise, ordered by set number.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of CardioSet entities
     */
    List<CardioSet> findByWorkoutExerciseWorkoutExerciseIdOrderBySetNumber(Long workoutExerciseId);

    /**
     * Find all cardio sets for a specific workout session.
     *
     * @param sessionId the workout session ID
     * @return List of CardioSet entities
     */
    List<CardioSet> findByWorkoutExerciseWorkoutSessionSessionId(Long sessionId);

    /**
     * Find all completed cardio sets for a specific workout exercise.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return List of completed CardioSet entities
     */
    List<CardioSet> findByWorkoutExerciseWorkoutExerciseIdAndCompletedTrueOrderBySetNumber(Long workoutExerciseId);

    /**
     * Count cardio sets for a specific workout exercise.
     *
     * @param workoutExerciseId the workout exercise ID
     * @return number of cardio sets
     */
    long countByWorkoutExerciseWorkoutExerciseId(Long workoutExerciseId);
}
