package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StrengthSet entity.
 * Provides CRUD operations and custom queries for strength set management.
 */
@Repository
public interface StrengthSetRepository extends JpaRepository<StrengthSet, Long> {
    
    /**
     * Find strength sets by workout exercise.
     * 
     * @param workoutExercise the workout exercise
     * @return list of strength sets for the workout exercise
     */
    List<StrengthSet> findByWorkoutExercise(WorkoutExercise workoutExercise);
    
    /**
     * Find strength sets by workout exercise ID.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of strength sets for the workout exercise
     */
    List<StrengthSet> findByWorkoutExerciseWorkoutExerciseId(Long workoutExerciseId);
    
    /**
     * Find strength sets by workout exercise, ordered by set number.
     * 
     * @param workoutExercise the workout exercise
     * @return list of strength sets ordered by set number
     */
    @Query("SELECT ss FROM StrengthSet ss WHERE ss.workoutExercise = :workoutExercise ORDER BY ss.setNumber ASC")
    List<StrengthSet> findByWorkoutExerciseOrderBySetNumber(@Param("workoutExercise") WorkoutExercise workoutExercise);
    
    /**
     * Find strength sets by workout exercise ID, ordered by set number.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of strength sets ordered by set number
     */
    @Query("SELECT ss FROM StrengthSet ss WHERE ss.workoutExercise.workoutExerciseId = :workoutExerciseId ORDER BY ss.setNumber ASC")
    List<StrengthSet> findByWorkoutExerciseIdOrderBySetNumber(@Param("workoutExerciseId") Long workoutExerciseId);
    
    /**
     * Find strength sets by completion status.
     * 
     * @param completed the completion status
     * @return list of strength sets with the specified completion status
     */
    List<StrengthSet> findByCompleted(Boolean completed);
    
    /**
     * Find strength sets by workout exercise and completion status.
     * 
     * @param workoutExercise the workout exercise
     * @param completed the completion status
     * @return list of strength sets for the workout exercise with the specified completion status
     */
    List<StrengthSet> findByWorkoutExerciseAndCompleted(WorkoutExercise workoutExercise, Boolean completed);
}
