package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for FlexibilitySet entity.
 * Provides CRUD operations and custom queries for flexibility set management.
 */
@Repository
public interface FlexibilitySetRepository extends JpaRepository<FlexibilitySet, Long> {

    /**
     * Find flexibility sets by workout exercise ID, ordered by set number.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of flexibility sets ordered by set number
     */
    List<FlexibilitySet> findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(Long workoutExerciseId);
    
    /**
     * Find flexibility sets by workout session ID.
     * 
     * @param sessionId the workout session ID
     * @return list of flexibility sets for the workout session
     */
    List<FlexibilitySet> findByWorkoutExercise_WorkoutSession_SessionId(Long sessionId);
}
