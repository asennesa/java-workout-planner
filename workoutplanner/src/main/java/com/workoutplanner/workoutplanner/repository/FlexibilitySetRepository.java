package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for FlexibilitySet entity with soft delete support.
 * Provides CRUD operations and custom queries for flexibility set management.
 * All query methods automatically filter out soft-deleted flexibility sets unless explicitly stated.
 * 
 * Uses @EntityGraph to prevent N+1 query problems when accessing workoutExercise.
 */
@Repository
public interface FlexibilitySetRepository extends SoftDeleteRepository<FlexibilitySet, Long> {

    /**
     * Find active flexibility sets by workout exercise ID, ordered by set number.
     * Uses @EntityGraph to eagerly fetch workoutExercise, preventing N+1 queries.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of active flexibility sets ordered by set number
     */
    @EntityGraph(attributePaths = {"workoutExercise"})
    @Query("SELECT f FROM FlexibilitySet f WHERE f.workoutExercise.workoutExerciseId = :workoutExerciseId AND f.deleted = false ORDER BY f.setNumber ASC")
    List<FlexibilitySet> findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(@Param("workoutExerciseId") Long workoutExerciseId);
    
    /**
     * Find active flexibility sets by workout session ID.
     * 
     * @param sessionId the workout session ID
     * @return list of active flexibility sets for the workout session
     */
    @Query("SELECT f FROM FlexibilitySet f WHERE f.workoutExercise.workoutSession.sessionId = :sessionId AND f.deleted = false")
    List<FlexibilitySet> findByWorkoutExercise_WorkoutSession_SessionId(@Param("sessionId") Long sessionId);
}
