package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

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
     * Get all users with pagination.
     * 
     * @param pageable pagination information
     * @return paginated user responses
     */
    PagedResponse<UserResponse> getAllUsers(Pageable pageable);
    
    /**
     * Update user profile with basic information (no password verification required).
     * 
     * This method handles non-sensitive profile updates like firstName and lastName.
     * 
     * @param userId the user ID
     * @param userUpdateRequest the user update request
     * @return UserResponse the updated user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     */
    UserResponse updateUserBasic(Long userId, UserUpdateRequest userUpdateRequest);

    /**
     * Update user profile securely with password verification.
     * 
     * This method handles sensitive profile updates like email and password changes.
     * Requires current password verification for security.
     * 
     * @param userId the user ID
     * @param userUpdateRequest the user update request
     * @return UserResponse the updated user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     * @throws com.workoutplanner.workoutplanner.exception.BusinessLogicException if current password is incorrect or validation fails
     */
    UserResponse updateUserProfileSecurely(Long userId, UserUpdateRequest userUpdateRequest);
    
    
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
