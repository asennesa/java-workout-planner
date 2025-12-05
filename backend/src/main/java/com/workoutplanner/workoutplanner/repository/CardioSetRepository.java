package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.CardioSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CardioSet entity with soft delete support.
 */
@Repository
public interface CardioSetRepository extends SoftDeleteRepository<CardioSet, Long> {

    @EntityGraph(attributePaths = {"workoutExercise"})
    @Query("SELECT c FROM CardioSet c WHERE c.workoutExercise.workoutExerciseId = :workoutExerciseId AND c.deleted = false ORDER BY c.setNumber ASC")
    List<CardioSet> findByWorkoutExerciseIdOrderBySetNumber(@Param("workoutExerciseId") Long workoutExerciseId);

    @Query("SELECT c FROM CardioSet c WHERE c.workoutExercise.workoutSession.sessionId = :sessionId AND c.deleted = false")
    List<CardioSet> findBySessionId(@Param("sessionId") Long sessionId);
}
