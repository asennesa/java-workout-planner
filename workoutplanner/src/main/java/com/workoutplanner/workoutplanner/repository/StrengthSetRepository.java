package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.StrengthSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StrengthSet entity.
 * Provides CRUD operations and custom queries for strength set management.
 */
@Repository
public interface StrengthSetRepository extends JpaRepository<StrengthSet, Long> {
    
    /**
     * Find strength sets by workout exercise ID, ordered by set number.
     * 
     * @param workoutExerciseId the workout exercise ID
     * @return list of strength sets ordered by set number
     */
    List<StrengthSet> findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(Long workoutExerciseId);
    
    /**
     * Find strength sets by workout session ID.
     * 
     * @param sessionId the workout session ID
     * @return list of strength sets for the workout session
     */
    List<StrengthSet> findByWorkoutExercise_WorkoutSession_SessionId(Long sessionId);
}
