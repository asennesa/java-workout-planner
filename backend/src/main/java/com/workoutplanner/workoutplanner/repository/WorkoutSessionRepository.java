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
 * Repository interface for WorkoutSession entity with soft delete support.
 * Provides CRUD operations and custom queries for workout session management.
 * All query methods automatically filter out soft-deleted workout sessions unless explicitly stated.
 */
@Repository
public interface WorkoutSessionRepository extends SoftDeleteRepository<WorkoutSession, Long> {
    
    /**
     * Find active workout sessions by status.
     * 
     * @param status the workout status
     * @return list of active workout sessions with the specified status
     */
    @Query("SELECT w FROM WorkoutSession w WHERE w.status = :status AND w.deleted = false")
    List<WorkoutSession> findByStatus(@Param("status") WorkoutStatus status);
    
    /**
     * Find active workout sessions by user ID and status.
     * 
     * @param userId the user ID
     * @param status the workout status
     * @return list of active workout sessions for the user with the specified status
     */
    @Query("SELECT w FROM WorkoutSession w WHERE w.user.userId = :userId AND w.status = :status AND w.deleted = false")
    List<WorkoutSession> findByUser_UserIdAndStatus(@Param("userId") Long userId, @Param("status") WorkoutStatus status);
    
    /**
     * Find active workout sessions by user ID, ordered by started date descending.
     * Uses EntityGraph to eagerly fetch user and workoutExercises to prevent N+1 queries.
     * 
     * @param userId the user ID
     * @return list of active workout sessions for the user ordered by started date
     */
    @EntityGraph(attributePaths = {"user", "workoutExercises", "workoutExercises.exercise"})
    @Query("SELECT w FROM WorkoutSession w WHERE w.user.userId = :userId AND w.deleted = false ORDER BY w.startedAt DESC")
    List<WorkoutSession> findByUser_UserIdOrderByStartedAtDesc(@Param("userId") Long userId);
    
    /**
     * Find active workout session by ID with user and exercises eagerly fetched.
     * Prevents N+1 query problem when accessing user details and workout exercises.
     * 
     * @param sessionId the session ID
     * @return optional workout session with user and workoutExercises eagerly loaded
     */
    @EntityGraph(attributePaths = {"user", "workoutExercises", "workoutExercises.exercise"})
    @Query("SELECT w FROM WorkoutSession w WHERE w.sessionId = :sessionId AND w.deleted = false")
    Optional<WorkoutSession> findWithUserBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * Check if user has any active workout sessions (for deletion validation).
     * 
     * @param userId the user ID
     * @return true if user has active workout sessions, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WorkoutSession w WHERE w.user.userId = :userId AND w.deleted = false")
    boolean existsByUserId(@Param("userId") Long userId);
}
