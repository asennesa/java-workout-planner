package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkoutExercise entity with soft delete support.
 * Provides CRUD operations and custom queries for workout exercise management.
 * All query methods automatically filter out soft-deleted workout exercises unless explicitly stated.
 */
@Repository
public interface WorkoutExerciseRepository extends SoftDeleteRepository<WorkoutExercise, Long> {
    
    /**
     * Find active workout exercise by ID with exercise eagerly fetched.
     * Prevents N+1 query problem when accessing exercise details.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return optional workout exercise with exercise eagerly loaded
     */
    @EntityGraph(attributePaths = "exercise")
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutExerciseId = :workoutExerciseId AND we.deleted = false")
    Optional<WorkoutExercise> findWithExerciseByWorkoutExerciseId(@Param("workoutExerciseId") Long workoutExerciseId);
    
    /**
     * Find active workout exercises by session ID with exercise eagerly fetched.
     * Prevents N+1 query problem when accessing exercise details.
     * 
     * @param sessionId the workout session ID
     * @return list of active workout exercises with exercise eagerly loaded
     */
    @EntityGraph(attributePaths = "exercise")
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId AND we.deleted = false ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findByWorkoutSession_SessionIdOrderByOrderInWorkoutAsc(@Param("sessionId") Long sessionId);
    
    /**
     * Find active strength exercises with their sets for a session.
     * Only loads strength sets for strength exercises.
     * 
     * @param sessionId the workout session ID
     * @param type the exercise type
     * @return list of active strength workout exercises with strength sets loaded
     */
    @EntityGraph(attributePaths = {"exercise", "strengthSets"})
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId AND we.exercise.type = :type AND we.deleted = false ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findStrengthExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(@Param("sessionId") Long sessionId, @Param("type") String type);
    
    /**
     * Find active cardio exercises with their sets for a session.
     * Only loads cardio sets for cardio exercises.
     * 
     * @param sessionId the workout session ID
     * @param type the exercise type
     * @return list of active cardio workout exercises with cardio sets loaded
     */
    @EntityGraph(attributePaths = {"exercise", "cardioSets"})
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId AND we.exercise.type = :type AND we.deleted = false ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findCardioExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(@Param("sessionId") Long sessionId, @Param("type") String type);
    
    /**
     * Find active flexibility exercises with their sets for a session.
     * Only loads flexibility sets for flexibility exercises.
     * 
     * @param sessionId the workout session ID
     * @param type the exercise type
     * @return list of active flexibility workout exercises with flexibility sets loaded
     */
    @EntityGraph(attributePaths = {"exercise", "flexibilitySets"})
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId AND we.exercise.type = :type AND we.deleted = false ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findFlexibilityExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(@Param("sessionId") Long sessionId, @Param("type") String type);
}
