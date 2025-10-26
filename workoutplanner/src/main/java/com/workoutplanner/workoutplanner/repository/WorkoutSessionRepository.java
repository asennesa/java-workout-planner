package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WorkoutSession entity.
 * Provides CRUD operations and custom queries for workout session management.
 */
@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {
    
    /**
     * Find workout sessions by status.
     * 
     * @param status the workout status
     * @return list of workout sessions with the specified status
     */
    List<WorkoutSession> findByStatus(WorkoutStatus status);
    
    /**
     * Find workout sessions by user ID and status.
     * 
     * @param userId the user ID
     * @param status the workout status
     * @return list of workout sessions for the user with the specified status
     */
    List<WorkoutSession> findByUser_UserIdAndStatus(Long userId, WorkoutStatus status);
    
    /**
     * Find workout sessions by user ID, ordered by started date descending.
     * 
     * @param userId the user ID
     * @return list of workout sessions for the user ordered by started date
     */
    @EntityGraph(attributePaths = "user")
    List<WorkoutSession> findByUser_UserIdOrderByStartedAtDesc(Long userId);
    
    /**
     * Find workout session by ID with user eagerly fetched.
     * Prevents N+1 query problem when accessing user details.
     * 
     * @param sessionId the session ID
     * @return optional workout session with user eagerly loaded
     */
    @EntityGraph(attributePaths = "user")
    Optional<WorkoutSession> findWithUserBySessionId(Long sessionId);
}
