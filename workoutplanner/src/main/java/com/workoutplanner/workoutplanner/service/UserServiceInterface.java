package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.ChangePasswordRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateUserRequest;
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
     * @throws com.workoutplanner.workoutplanner.exception.ResourceConflictException if username or email already exists
     */
    UserResponse createUser(CreateUserRequest createUserRequest);
    
    /**
     * Get user by ID.
     * 
     * @param userId the user ID
     * @return the user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     */
    UserResponse getUserById(Long userId);
    
    /**
     * Get user by username.
     * 
     * @param username the username
     * @return the user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     */
    UserResponse getUserByUsername(String username);
    
    /**
     * Get user by email.
     * 
     * @param email the email
     * @return the user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     */
    UserResponse getUserByEmail(String email);
    
    /**
     * Get all users.
     * 
     * @return list of all user responses
     */
    List<UserResponse> getAllUsers();
    
    /**
     * Update user information (excluding password).
     * For password changes, use changePassword method.
     * 
     * @param userId the user ID
     * @param updateUserRequest the updated user information
     * @return the updated user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     * @throws com.workoutplanner.workoutplanner.exception.ResourceConflictException if username or email already exists
     */
    UserResponse updateUser(Long userId, UpdateUserRequest updateUserRequest);
    
    /**
     * Change user password with verification of current password.
     * 
     * @param userId the user ID
     * @param changePasswordRequest the password change request
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     * @throws com.workoutplanner.workoutplanner.exception.BusinessLogicException if current password is incorrect or passwords don't match
     */
    void changePassword(Long userId, ChangePasswordRequest changePasswordRequest);
    
    /**
     * Delete user by ID.
     * 
     * @param userId the user ID
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
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
