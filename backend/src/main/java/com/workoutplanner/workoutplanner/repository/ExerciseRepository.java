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
 * Repository for Exercise entity with soft delete support.
 */
@Repository
public interface ExerciseRepository extends SoftDeleteRepository<Exercise, Long> {

    @Query("SELECT e FROM Exercise e WHERE e.type = :type AND e.deleted = false")
    List<Exercise> findByType(@Param("type") ExerciseType type);

    @Query("SELECT e FROM Exercise e WHERE e.targetMuscleGroup = :targetMuscleGroup AND e.deleted = false")
    List<Exercise> findByTargetMuscleGroup(@Param("targetMuscleGroup") TargetMuscleGroup targetMuscleGroup);

    @Query("SELECT e FROM Exercise e WHERE e.difficultyLevel = :difficultyLevel AND e.deleted = false")
    List<Exercise> findByDifficultyLevel(@Param("difficultyLevel") DifficultyLevel difficultyLevel);

    @Query("SELECT e FROM Exercise e WHERE e.type = :type AND e.targetMuscleGroup = :targetMuscleGroup AND e.deleted = false")
    List<Exercise> findByTypeAndTargetMuscleGroup(@Param("type") ExerciseType type, @Param("targetMuscleGroup") TargetMuscleGroup targetMuscleGroup);

    @Query("SELECT e FROM Exercise e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')) AND e.deleted = false")
    List<Exercise> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT e FROM Exercise e WHERE " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:targetMuscleGroup IS NULL OR e.targetMuscleGroup = :targetMuscleGroup) AND " +
           "(:difficultyLevel IS NULL OR e.difficultyLevel = :difficultyLevel) AND " +
           "e.deleted = false")
    List<Exercise> findByFilters(@Param("type") ExerciseType type,
                                 @Param("targetMuscleGroup") TargetMuscleGroup targetMuscleGroup,
                                 @Param("difficultyLevel") DifficultyLevel difficultyLevel);
}
