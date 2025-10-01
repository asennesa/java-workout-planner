package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email.
     * 
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by first name containing (case-insensitive).
     * 
     * @param firstName the first name to search for
     * @return list of users matching the criteria
     */
    @Query(value = "SELECT * FROM users WHERE LOWER(first_name) LIKE LOWER(CONCAT('%', :firstName, '%'))", nativeQuery = true)
    java.util.List<User> findByFirstNameContainingIgnoreCase(@Param("firstName") String firstName);
}
