package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for WorkoutSession entity.
 * Provides CRUD operations and custom queries for workout session management.
 */
@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {
    
    /**
     * Find workout sessions by user.
     * 
     * @param user the user
     * @return list of workout sessions for the user
     */
    List<WorkoutSession> findByUser(User user);
    
    /**
     * Find workout sessions by user ID.
     * 
     * @param userId the user ID
     * @return list of workout sessions for the user
     */
    List<WorkoutSession> findByUserUserId(Long userId);
    
    /**
     * Find workout sessions by status.
     * 
     * @param status the workout status
     * @return list of workout sessions with the specified status
     */
    List<WorkoutSession> findByStatus(WorkoutStatus status);
    
    /**
     * Find workout sessions by user and status.
     * 
     * @param user the user
     * @param status the workout status
     * @return list of workout sessions for the user with the specified status
     */
    List<WorkoutSession> findByUserAndStatus(User user, WorkoutStatus status);
    
    /**
     * Find workout sessions by user ID and status.
     * 
     * @param userId the user ID
     * @param status the workout status
     * @return list of workout sessions for the user with the specified status
     */
    List<WorkoutSession> findByUserUserIdAndStatus(Long userId, WorkoutStatus status);
    
    /**
     * Find workout sessions by date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return list of workout sessions within the date range
     */
    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.startedAt BETWEEN :startDate AND :endDate")
    List<WorkoutSession> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find workout sessions by user and date range.
     * 
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of workout sessions for the user within the date range
     */
    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user.userId = :userId AND ws.startedAt BETWEEN :startDate AND :endDate")
    List<WorkoutSession> findByUserAndDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find recent workout sessions for a user.
     * 
     * @param userId the user ID
     * @param limit the maximum number of sessions to return
     * @return list of recent workout sessions for the user
     */
    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user.userId = :userId ORDER BY ws.startedAt DESC")
    List<WorkoutSession> findRecentByUser(@Param("userId") Long userId, 
                                         @Param("limit") int limit);
    
    /**
     * Find workout sessions by user ID, ordered by started date descending.
     * 
     * @param userId the user ID
     * @return list of workout sessions for the user ordered by started date
     */
    List<WorkoutSession> findByUserUserIdOrderByStartedAtDesc(Long userId);
}
