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
     * Get user by email.
     * 
     * @param email the email
     * @return the user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     */
    UserResponse getUserByEmail(String email);
    
    /**
     * Get all users with pagination.
     * 
     * @param pageable pagination information
     * @return paginated user responses
     */
    PagedResponse<UserResponse> getAllUsers(Pageable pageable);
    
    /**
     * Unified method to update user profile with automatic business logic routing (Auth0 mode).
     * 
     * This method determines whether to use email update or basic name update based on the request content.
     * Business logic decision is made in the service layer, not the controller.
     * 
     * Authorization: Handled via JWT token validation (@PreAuthorize in controller)
     * - No password verification needed (Auth0 manages authentication)
     * - Password changes NOT supported (handled by Auth0)
     * 
     * @param userId the user ID
     * @param userUpdateRequest the user update request
     * @return UserResponse the updated user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     * @throws com.workoutplanner.workoutplanner.exception.ResourceConflictException if email already exists
     */
    UserResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest);

    /**
     * Update user profile with basic information (Auth0 mode).
     * 
     * This method handles basic profile updates like firstName and lastName.
     * Authorization is handled via JWT token validation.
     * 
     * @param userId the user ID
     * @param userUpdateRequest the user update request
     * @return UserResponse the updated user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     */
    UserResponse updateUserBasic(Long userId, UserUpdateRequest userUpdateRequest);

    /**
     * Update user profile including email changes (Auth0 mode).
     * 
     * This method handles profile updates including email changes.
     * Authorization is handled via JWT token validation (no password verification needed).
     * 
     * Note: Email changes should ideally be done through Auth0 for proper email verification flow.
     * Password changes are NOT supported through this API (Auth0 handles password management).
     * 
     * @param userId the user ID
     * @param userUpdateRequest the user update request
     * @return UserResponse the updated user response
     * @throws com.workoutplanner.workoutplanner.exception.ResourceNotFoundException if user not found
     * @throws com.workoutplanner.workoutplanner.exception.ResourceConflictException if email already exists
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
