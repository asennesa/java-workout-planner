package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for WorkoutSession entity with soft delete support.
 */
@Repository
public interface WorkoutSessionRepository extends SoftDeleteRepository<WorkoutSession, Long> {

    @Query("SELECT w FROM WorkoutSession w WHERE w.user.userId = :userId AND w.status = :status AND w.deleted = false")
    List<WorkoutSession> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") WorkoutStatus status);

    @EntityGraph(attributePaths = {"user", "workoutExercises", "workoutExercises.exercise"})
    @Query("SELECT w FROM WorkoutSession w WHERE w.user.userId = :userId AND w.deleted = false ORDER BY w.startedAt DESC")
    List<WorkoutSession> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"user", "workoutExercises", "workoutExercises.exercise"})
    @Query("SELECT w FROM WorkoutSession w WHERE w.sessionId = :sessionId AND w.deleted = false")
    Optional<WorkoutSession> findWithUserBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WorkoutSession w WHERE w.user.userId = :userId AND w.deleted = false")
    boolean existsByUserId(@Param("userId") Long userId);
}
