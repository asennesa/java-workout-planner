package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.CardioSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for CardioSet entity.
 * Provides CRUD operations and custom queries for cardio set management.
 * 
 * Uses @EntityGraph to prevent N+1 query problems when accessing workoutExercise.
 */
@Repository
public interface CardioSetRepository extends JpaRepository<CardioSet, Long> {

    /**
     * Find cardio sets by workout exercise ID, ordered by set number.
     * Uses @EntityGraph to eagerly fetch workoutExercise, preventing N+1 queries.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of cardio sets ordered by set number
     */
    @EntityGraph(attributePaths = {"workoutExercise"})
    List<CardioSet> findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(Long workoutExerciseId);
    
    /**
     * Find cardio sets by workout session ID.
     * 
     * @param sessionId the workout session ID
     * @return list of cardio sets for the workout session
     */
    List<CardioSet> findByWorkoutExercise_WorkoutSession_SessionId(Long sessionId);
}
