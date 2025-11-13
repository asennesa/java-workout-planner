package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.StrengthSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StrengthSet entity with soft delete support.
 * Provides CRUD operations and custom queries for strength set management.
 * All query methods automatically filter out soft-deleted strength sets unless explicitly stated.
 * 
 * Uses @EntityGraph to prevent N+1 query problems when accessing workoutExercise.
 */
@Repository
public interface StrengthSetRepository extends SoftDeleteRepository<StrengthSet, Long> {
    
    /**
     * Find active strength sets by workout exercise ID, ordered by set number.
     * Uses @EntityGraph to eagerly fetch workoutExercise, preventing N+1 queries.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of active strength sets ordered by set number
     */
    @EntityGraph(attributePaths = {"workoutExercise"})
    @Query("SELECT s FROM StrengthSet s WHERE s.workoutExercise.workoutExerciseId = :workoutExerciseId AND s.deleted = false ORDER BY s.setNumber ASC")
    List<StrengthSet> findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(@Param("workoutExerciseId") Long workoutExerciseId);
    
    /**
     * Find active strength sets by workout session ID.
     * 
     * @param sessionId the workout session ID
     * @return list of active strength sets for the workout session
     */
    @Query("SELECT s FROM StrengthSet s WHERE s.workoutExercise.workoutSession.sessionId = :sessionId AND s.deleted = false")
    List<StrengthSet> findByWorkoutExercise_WorkoutSession_SessionId(@Param("sessionId") Long sessionId);
}
