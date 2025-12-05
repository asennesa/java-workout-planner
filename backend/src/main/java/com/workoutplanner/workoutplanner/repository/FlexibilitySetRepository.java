package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for FlexibilitySet entity with soft delete support.
 */
@Repository
public interface FlexibilitySetRepository extends SoftDeleteRepository<FlexibilitySet, Long> {

    @EntityGraph(attributePaths = {"workoutExercise"})
    @Query("SELECT f FROM FlexibilitySet f WHERE f.workoutExercise.workoutExerciseId = :workoutExerciseId AND f.deleted = false ORDER BY f.setNumber ASC")
    List<FlexibilitySet> findByWorkoutExerciseIdOrderBySetNumber(@Param("workoutExerciseId") Long workoutExerciseId);

    @Query("SELECT f FROM FlexibilitySet f WHERE f.workoutExercise.workoutSession.sessionId = :sessionId AND f.deleted = false")
    List<FlexibilitySet> findBySessionId(@Param("sessionId") Long sessionId);
}
