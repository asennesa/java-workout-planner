package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for WorkoutExercise entity with soft delete support.
 */
@Repository
public interface WorkoutExerciseRepository extends SoftDeleteRepository<WorkoutExercise, Long> {

    @EntityGraph(attributePaths = "exercise")
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutExerciseId = :workoutExerciseId AND we.deleted = false")
    Optional<WorkoutExercise> findWithExerciseById(@Param("workoutExerciseId") Long workoutExerciseId);

    @EntityGraph(attributePaths = "exercise")
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId AND we.deleted = false ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findBySessionIdOrderByOrder(@Param("sessionId") Long sessionId);

    @EntityGraph(attributePaths = {"exercise", "strengthSets"})
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId AND we.exercise.type = :type AND we.deleted = false ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findStrengthExercisesWithSets(@Param("sessionId") Long sessionId, @Param("type") String type);

    @EntityGraph(attributePaths = {"exercise", "cardioSets"})
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId AND we.exercise.type = :type AND we.deleted = false ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findCardioExercisesWithSets(@Param("sessionId") Long sessionId, @Param("type") String type);

    @EntityGraph(attributePaths = {"exercise", "flexibilitySets"})
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId AND we.exercise.type = :type AND we.deleted = false ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findFlexibilityExercisesWithSets(@Param("sessionId") Long sessionId, @Param("type") String type);
}
