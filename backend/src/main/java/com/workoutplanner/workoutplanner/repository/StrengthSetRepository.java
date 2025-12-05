package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.StrengthSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for StrengthSet entity with soft delete support.
 */
@Repository
public interface StrengthSetRepository extends SoftDeleteRepository<StrengthSet, Long> {

    @EntityGraph(attributePaths = {"workoutExercise"})
    @Query("SELECT s FROM StrengthSet s WHERE s.workoutExercise.workoutExerciseId = :workoutExerciseId AND s.deleted = false ORDER BY s.setNumber ASC")
    List<StrengthSet> findByWorkoutExerciseIdOrderBySetNumber(@Param("workoutExerciseId") Long workoutExerciseId);

    @Query("SELECT s FROM StrengthSet s WHERE s.workoutExercise.workoutSession.sessionId = :sessionId AND s.deleted = false")
    List<StrengthSet> findBySessionId(@Param("sessionId") Long sessionId);
}
