package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkoutExercise entity.
 * Provides CRUD operations and custom queries for workout exercise management.
 */
@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
    
    /**
     * Find workout exercise by ID with exercise eagerly fetched.
     * Prevents N+1 query problem when accessing exercise details.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return optional workout exercise with exercise eagerly loaded
     */
    @EntityGraph(attributePaths = "exercise")
    Optional<WorkoutExercise> findWithExerciseByWorkoutExerciseId(Long workoutExerciseId);
    
    /**
     * Find workout exercises by session ID with exercise eagerly fetched.
     * Prevents N+1 query problem when accessing exercise details.
     * 
     * @param sessionId the workout session ID
     * @return list of workout exercises with exercise eagerly loaded
     */
    @EntityGraph(attributePaths = "exercise")
    List<WorkoutExercise> findByWorkoutSession_SessionIdOrderByOrderInWorkoutAsc(Long sessionId);
    
    /**
     * Find strength exercises with their sets for a session.
     * Only loads strength sets for strength exercises.
     * 
     * @param sessionId the workout session ID
     * @return list of strength workout exercises with strength sets loaded
     */
    @EntityGraph(attributePaths = {"exercise", "strengthSets"})
    List<WorkoutExercise> findStrengthExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(Long sessionId, String type);
    
    /**
     * Find cardio exercises with their sets for a session.
     * Only loads cardio sets for cardio exercises.
     * 
     * @param sessionId the workout session ID
     * @return list of cardio workout exercises with cardio sets loaded
     */
    @EntityGraph(attributePaths = {"exercise", "cardioSets"})
    List<WorkoutExercise> findCardioExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(Long sessionId, String type);
    
    /**
     * Find flexibility exercises with their sets for a session.
     * Only loads flexibility sets for flexibility exercises.
     * 
     * @param sessionId the workout session ID
     * @return list of flexibility workout exercises with flexibility sets loaded
     */
    @EntityGraph(attributePaths = {"exercise", "flexibilitySets"})
    List<WorkoutExercise> findFlexibilityExercisesWithSetsByWorkoutSession_SessionIdAndExercise_TypeOrderByOrderInWorkoutAsc(Long sessionId, String type);
}
