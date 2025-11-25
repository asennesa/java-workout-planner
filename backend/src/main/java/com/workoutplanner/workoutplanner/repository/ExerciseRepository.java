package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.enums.DifficultyLevel;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import com.workoutplanner.workoutplanner.enums.TargetMuscleGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Exercise entity with soft delete support.
 * Provides CRUD operations and custom queries for exercise management.
 * All query methods automatically filter out soft-deleted exercises unless explicitly stated.
 */
@Repository
public interface ExerciseRepository extends SoftDeleteRepository<Exercise, Long> {
    
    /**
     * Find active exercises by type.
     * 
     * @param type the exercise type
     * @return list of active exercises of the specified type
     */
    @Query("SELECT e FROM Exercise e WHERE e.type = :type AND e.deleted = false")
    List<Exercise> findByType(@Param("type") ExerciseType type);
    
    /**
     * Find active exercises by target muscle group.
     * 
     * @param targetMuscleGroup the target muscle group
     * @return list of active exercises targeting the specified muscle group
     */
    @Query("SELECT e FROM Exercise e WHERE e.targetMuscleGroup = :targetMuscleGroup AND e.deleted = false")
    List<Exercise> findByTargetMuscleGroup(@Param("targetMuscleGroup") TargetMuscleGroup targetMuscleGroup);
    
    /**
     * Find active exercises by difficulty level.
     * 
     * @param difficultyLevel the difficulty level
     * @return list of active exercises of the specified difficulty
     */
    @Query("SELECT e FROM Exercise e WHERE e.difficultyLevel = :difficultyLevel AND e.deleted = false")
    List<Exercise> findByDifficultyLevel(@Param("difficultyLevel") DifficultyLevel difficultyLevel);
    
    /**
     * Find active exercises by type and target muscle group.
     * 
     * @param type the exercise type
     * @param targetMuscleGroup the target muscle group
     * @return list of active exercises matching both criteria
     */
    @Query("SELECT e FROM Exercise e WHERE e.type = :type AND e.targetMuscleGroup = :targetMuscleGroup AND e.deleted = false")
    List<Exercise> findByTypeAndTargetMuscleGroup(@Param("type") ExerciseType type, @Param("targetMuscleGroup") TargetMuscleGroup targetMuscleGroup);
    
    /**
     * Find active exercises by type and difficulty level.
     * 
     * @param type the exercise type
     * @param difficultyLevel the difficulty level
     * @return list of active exercises matching both criteria
     */
    @Query("SELECT e FROM Exercise e WHERE e.type = :type AND e.difficultyLevel = :difficultyLevel AND e.deleted = false")
    List<Exercise> findByTypeAndDifficultyLevel(@Param("type") ExerciseType type, @Param("difficultyLevel") DifficultyLevel difficultyLevel);
    
    /**
     * Find active exercises by target muscle group and difficulty level.
     * 
     * @param targetMuscleGroup the target muscle group
     * @param difficultyLevel the difficulty level
     * @return list of active exercises matching both criteria
     */
    @Query("SELECT e FROM Exercise e WHERE e.targetMuscleGroup = :targetMuscleGroup AND e.difficultyLevel = :difficultyLevel AND e.deleted = false")
    List<Exercise> findByTargetMuscleGroupAndDifficultyLevel(@Param("targetMuscleGroup") TargetMuscleGroup targetMuscleGroup, @Param("difficultyLevel") DifficultyLevel difficultyLevel);
    
    /**
     * Find active exercises by name containing (case-insensitive).
     * Spring Data JPA automatically generates safe query with proper escaping.
     * Input should be pre-sanitized in the service layer to escape LIKE wildcards.
     * 
     * @param name the name to search for (should be pre-sanitized)
     * @return list of active exercises with names containing the search term
     */
    @Query("SELECT e FROM Exercise e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) AND e.deleted = false")
    List<Exercise> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Find active exercises by multiple criteria.
     * 
     * @param type the exercise type (optional)
     * @param targetMuscleGroup the target muscle group (optional)
     * @param difficultyLevel the difficulty level (optional)
     * @return list of active exercises matching the criteria
     */
    @Query("SELECT e FROM Exercise e WHERE " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:targetMuscleGroup IS NULL OR e.targetMuscleGroup = :targetMuscleGroup) AND " +
           "(:difficultyLevel IS NULL OR e.difficultyLevel = :difficultyLevel) AND " +
           "e.deleted = false")
    List<Exercise> findByTypeAndTargetMuscleGroupAndDifficultyLevel(@Param("type") ExerciseType type,
                                                                    @Param("targetMuscleGroup") TargetMuscleGroup targetMuscleGroup,
                                                                    @Param("difficultyLevel") DifficultyLevel difficultyLevel);
}
