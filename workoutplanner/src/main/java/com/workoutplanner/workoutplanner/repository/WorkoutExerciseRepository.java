package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for WorkoutExercise entity.
 * Provides CRUD operations and custom queries for workout exercise management.
 */
@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
    
    /**
     * Find workout exercises by workout session.
     * 
     * @param workoutSession the workout session
     * @return list of workout exercises for the session
     */
    List<WorkoutExercise> findByWorkoutSession(WorkoutSession workoutSession);
    
    /**
     * Find workout exercises by workout session ID.
     * 
     * @param sessionId the workout session ID
     * @return list of workout exercises for the session
     */
    List<WorkoutExercise> findByWorkoutSessionSessionId(Long sessionId);
    
    /**
     * Find workout exercises by exercise.
     * 
     * @param exercise the exercise
     * @return list of workout exercises using the specified exercise
     */
    List<WorkoutExercise> findByExercise(Exercise exercise);
    
    /**
     * Find workout exercises by exercise ID.
     * 
     * @param exerciseId the exercise ID
     * @return list of workout exercises using the specified exercise
     */
    List<WorkoutExercise> findByExerciseExerciseId(Long exerciseId);
    
    /**
     * Find workout exercises by workout session, ordered by order in workout.
     * 
     * @param workoutSession the workout session
     * @return list of workout exercises ordered by their position in the workout
     */
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession = :workoutSession ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findByWorkoutSessionOrderByOrderInWorkout(@Param("workoutSession") WorkoutSession workoutSession);
    
    /**
     * Find workout exercises by workout session ID, ordered by order in workout.
     * 
     * @param sessionId the workout session ID
     * @return list of workout exercises ordered by their position in the workout
     */
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutSession.sessionId = :sessionId ORDER BY we.orderInWorkout ASC")
    List<WorkoutExercise> findByWorkoutSessionIdOrderByOrderInWorkout(@Param("sessionId") Long sessionId);
}
