package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for FlexibilitySet entity.
 * Provides CRUD operations and custom queries for flexibility set management.
 * 
 * Uses @EntityGraph to prevent N+1 query problems when accessing workoutExercise.
 */
@Repository
public interface FlexibilitySetRepository extends JpaRepository<FlexibilitySet, Long> {

    /**
     * Find flexibility sets by workout exercise ID, ordered by set number.
     * Uses @EntityGraph to eagerly fetch workoutExercise, preventing N+1 queries.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of flexibility sets ordered by set number
     */
    @EntityGraph(attributePaths = {"workoutExercise"})
    List<FlexibilitySet> findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(Long workoutExerciseId);
    
    /**
     * Find flexibility sets by workout session ID.
     * 
     * @param sessionId the workout session ID
     * @return list of flexibility sets for the workout session
     */
    List<FlexibilitySet> findByWorkoutExercise_WorkoutSession_SessionId(Long sessionId);
}
