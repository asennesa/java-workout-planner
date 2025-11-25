package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.CardioSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for CardioSet entity with soft delete support.
 * Provides CRUD operations and custom queries for cardio set management.
 * All query methods automatically filter out soft-deleted cardio sets unless explicitly stated.
 * 
 * Uses @EntityGraph to prevent N+1 query problems when accessing workoutExercise.
 */
@Repository
public interface CardioSetRepository extends SoftDeleteRepository<CardioSet, Long> {

    /**
     * Find active cardio sets by workout exercise ID, ordered by set number.
     * Uses @EntityGraph to eagerly fetch workoutExercise, preventing N+1 queries.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of active cardio sets ordered by set number
     */
    @EntityGraph(attributePaths = {"workoutExercise"})
    @Query("SELECT c FROM CardioSet c WHERE c.workoutExercise.workoutExerciseId = :workoutExerciseId AND c.deleted = false ORDER BY c.setNumber ASC")
    List<CardioSet> findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(@Param("workoutExerciseId") Long workoutExerciseId);
    
    /**
     * Find active cardio sets by workout session ID.
     * 
     * @param sessionId the workout session ID
     * @return list of active cardio sets for the workout session
     */
    @Query("SELECT c FROM CardioSet c WHERE c.workoutExercise.workoutSession.sessionId = :sessionId AND c.deleted = false")
    List<CardioSet> findByWorkoutExercise_WorkoutSession_SessionId(@Param("sessionId") Long sessionId);
}
