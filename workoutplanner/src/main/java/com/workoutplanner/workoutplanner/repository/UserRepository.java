package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity with soft delete support.
 * Provides CRUD operations and custom queries for user management.
 * All query methods automatically filter out soft-deleted users unless explicitly stated.
 */
@Repository
public interface UserRepository extends SoftDeleteRepository<User, Long> {
    
    /**
     * Find active user by username.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found and not deleted
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * Find active user by email.
     * 
     * @param email the email to search for
     * @return Optional containing the user if found and not deleted
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * Check if active username exists.
     * 
     * @param username the username to check
     * @return true if active username exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.deleted = false")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Check if active email exists.
     * 
     * @param email the email to check
     * @return true if active email exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deleted = false")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Find active user by username (case-insensitive).
     * 
     * @param username the username to search for
     * @return Optional containing the user if found and not deleted
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) AND u.deleted = false")
    Optional<User> findByUsernameIgnoreCase(@Param("username") String username);
    
    /**
     * Find active user by email (case-insensitive).
     * 
     * @param email the email to search for
     * @return Optional containing the user if found and not deleted
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.deleted = false")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
    
    /**
     * Find active users by first name containing (case-insensitive).
     * Spring Data JPA automatically generates safe query with proper escaping.
     * Input should be pre-sanitized in the service layer to escape LIKE wildcards.
     * 
     * @param firstName the first name to search for (should be pre-sanitized)
     * @return list of active users matching the criteria
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND u.deleted = false")
    List<User> findByFirstNameContainingIgnoreCase(@Param("firstName") String firstName);
}
