package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;

import java.util.List;

/**
 * Service interface for User entity operations.
 * Defines the contract for user management business logic.
 * 
 * This interface follows the Interface Segregation Principle by providing
 * a focused set of methods for user operations.
 */
public interface UserServiceInterface {
    
    /**
     * Create a new user.
     * 
     * @param createUserRequest the user creation request
     * @return the created user response
     * @throws IllegalArgumentException if username or email already exists
     */
    UserResponse createUser(CreateUserRequest createUserRequest);
    
    /**
     * Get user by ID.
     * 
     * @param userId the user ID
     * @return the user response
     * @throws IllegalArgumentException if user not found
     */
    UserResponse getUserById(Long userId);
    
    /**
     * Get user by username.
     * 
     * @param username the username
     * @return the user response
     * @throws IllegalArgumentException if user not found
     */
    UserResponse getUserByUsername(String username);
    
    /**
     * Get user by email.
     * 
     * @param email the email
     * @return the user response
     * @throws IllegalArgumentException if user not found
     */
    UserResponse getUserByEmail(String email);
    
    /**
     * Get all users.
     * 
     * @return list of all user responses
     */
    List<UserResponse> getAllUsers();
    
    /**
     * Update user information.
     * 
     * @param userId the user ID
     * @param createUserRequest the updated user information
     * @return the updated user response
     * @throws IllegalArgumentException if user not found
     */
    UserResponse updateUser(Long userId, CreateUserRequest createUserRequest);
    
    /**
     * Delete user by ID.
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    void deleteUser(Long userId);
    
    /**
     * Search users by first name.
     * 
     * @param firstName the first name to search for
     * @return list of users matching the search criteria
     */
    List<UserResponse> searchUsersByFirstName(String firstName);
    
    /**
     * Check if username exists.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean usernameExists(String username);
    
    /**
     * Check if email exists.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean emailExists(String email);
}
