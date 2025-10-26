package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Exercise entity.
 * Provides CRUD operations and custom queries for exercise management.
 */
@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    
    /**
     * Find exercises by type.
     * 
     * @param type the exercise type
     * @return list of exercises of the specified type
     */
    List<Exercise> findByType(ExerciseType type);
    
    /**
     * Find exercises by target muscle group.
     * 
     * @param targetMuscleGroup the target muscle group
     * @return list of exercises targeting the specified muscle group
     */
    List<Exercise> findByTargetMuscleGroup(TargetMuscleGroup targetMuscleGroup);
    
    /**
     * Find exercises by difficulty level.
     * 
     * @param difficultyLevel the difficulty level
     * @return list of exercises of the specified difficulty
     */
    List<Exercise> findByDifficultyLevel(DifficultyLevel difficultyLevel);
    
    /**
     * Find exercises by type and target muscle group.
     * 
     * @param type the exercise type
     * @param targetMuscleGroup the target muscle group
     * @return list of exercises matching both criteria
     */
    List<Exercise> findByTypeAndTargetMuscleGroup(ExerciseType type, TargetMuscleGroup targetMuscleGroup);
    
    /**
     * Find exercises by type and difficulty level.
     * 
     * @param type the exercise type
     * @param difficultyLevel the difficulty level
     * @return list of exercises matching both criteria
     */
    List<Exercise> findByTypeAndDifficultyLevel(ExerciseType type, DifficultyLevel difficultyLevel);
    
    /**
     * Find exercises by target muscle group and difficulty level.
     * 
     * @param targetMuscleGroup the target muscle group
     * @param difficultyLevel the difficulty level
     * @return list of exercises matching both criteria
     */
    List<Exercise> findByTargetMuscleGroupAndDifficultyLevel(TargetMuscleGroup targetMuscleGroup, DifficultyLevel difficultyLevel);
    
    /**
     * Find exercises by name containing (case-insensitive).
     * Spring Data JPA automatically generates safe query with proper escaping.
     * Input should be pre-sanitized in the service layer to escape LIKE wildcards.
     * 
     * @param name the name to search for (should be pre-sanitized)
     * @return list of exercises with names containing the search term
     */
    List<Exercise> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find exercises by multiple criteria.
     * 
     * @param type the exercise type (optional)
     * @param targetMuscleGroup the target muscle group (optional)
     * @param difficultyLevel the difficulty level (optional)
     * @return list of exercises matching the criteria
     */
    @Query("SELECT e FROM Exercise e WHERE " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:targetMuscleGroup IS NULL OR e.targetMuscleGroup = :targetMuscleGroup) AND " +
           "(:difficultyLevel IS NULL OR e.difficultyLevel = :difficultyLevel)")
    List<Exercise> findByTypeAndTargetMuscleGroupAndDifficultyLevel(@Param("type") ExerciseType type,
                                                                    @Param("targetMuscleGroup") TargetMuscleGroup targetMuscleGroup,
                                                                    @Param("difficultyLevel") DifficultyLevel difficultyLevel);
}
