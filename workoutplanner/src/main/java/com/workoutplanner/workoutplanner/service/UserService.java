package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.ChangePasswordRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateUserRequest;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceConflictException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.UserMapper;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for User entity operations.
 * Handles business logic for user management including creation, retrieval, and updates.
 * 
 * Uses method-level @Transactional for optimal performance:
 * - Read operations use @Transactional(readOnly = true) for better performance
 * - Write operations use @Transactional for data modification
 */
@Service
public class UserService implements UserServiceInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     */
    public UserService(UserRepository userRepository, 
                      UserMapper userMapper, 
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Create a new user.
     * 
     * @param createUserRequest the user creation request
     * @return the created user response
     * @throws ResourceConflictException if username or email already exists
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        logger.debug("SERVICE: Creating new user. username={}, email={}", 
                    createUserRequest.getUsername(), createUserRequest.getEmail());
        
        // Check if username already exists
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            logger.warn("SERVICE: User creation failed - username already exists. username={}", 
                       createUserRequest.getUsername());
            throw new ResourceConflictException("User", "username", createUserRequest.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            logger.warn("SERVICE: User creation failed - email already exists. email={}", 
                       createUserRequest.getEmail());
            throw new ResourceConflictException("User", "email", createUserRequest.getEmail());
        }
        
        // Convert request to entity
        User user = userMapper.toEntity(createUserRequest);
        
        // Hash the password
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        
        // Save the user
        User savedUser = userRepository.save(user);
        
        logger.info("SERVICE: User created successfully. userId={}, username={}, email={}", 
                   savedUser.getUserId(), savedUser.getUsername(), savedUser.getEmail());
        
        // Convert to response and return
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Get user by ID.
     * 
     * @param userId the user ID
     * @return the user response
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get user by username.
     * 
     * @param username the username
     * @return the user response
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get user by email.
     * 
     * @param email the email
     * @return the user response
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get all users.
     * 
     * @return list of all user responses
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }
    
    /**
     * Update user information (excluding password).
     * For password changes, use changePassword method.
     * 
     * @param userId the user ID
     * @param updateUserRequest the updated user information
     * @return the updated user response
     * @throws ResourceNotFoundException if user not found
     * @throws ResourceConflictException if username or email already exists
     */
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest updateUserRequest) {
        logger.debug("SERVICE: Updating user. userId={}, newUsername={}, newEmail={}", 
                    userId, updateUserRequest.getUsername(), updateUserRequest.getEmail());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        // Check if new username conflicts with existing users (excluding current user)
        if (!user.getUsername().equals(updateUserRequest.getUsername()) && 
            userRepository.existsByUsername(updateUserRequest.getUsername())) {
            logger.warn("SERVICE: User update failed - username conflict. userId={}, conflictUsername={}", 
                       userId, updateUserRequest.getUsername());
            throw new ResourceConflictException("User", "username", updateUserRequest.getUsername());
        }
        
        // Check if new email conflicts with existing users (excluding current user)
        if (!user.getEmail().equals(updateUserRequest.getEmail()) && 
            userRepository.existsByEmail(updateUserRequest.getEmail())) {
            logger.warn("SERVICE: User update failed - email conflict. userId={}, conflictEmail={}", 
                       userId, updateUserRequest.getEmail());
            throw new ResourceConflictException("User", "email", updateUserRequest.getEmail());
        }
        
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();
        
        // Update user fields using mapper (no password update here)
        userMapper.updateFromUpdateRequest(updateUserRequest, user);
        
        // Save updated user
        User savedUser = userRepository.save(user);
        
        logger.info("SERVICE: User updated successfully. userId={}, oldUsername={}, newUsername={}, oldEmail={}, newEmail={}", 
                   userId, oldUsername, savedUser.getUsername(), oldEmail, savedUser.getEmail());
        
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Change user password with verification of current password.
     * 
     * @param userId the user ID
     * @param changePasswordRequest the password change request
     * @throws ResourceNotFoundException if user not found
     * @throws BusinessLogicException if current password is incorrect or passwords don't match
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest changePasswordRequest) {
        logger.debug("SERVICE: Password change requested. userId={}", userId);
        
        // Validate that new password and confirmation match
        if (!changePasswordRequest.passwordsMatch()) {
            logger.warn("SERVICE: Password change failed - password mismatch. userId={}", userId);
            throw new BusinessLogicException("New password and confirmation do not match");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPasswordHash())) {
            logger.warn("SERVICE: Password change failed - incorrect current password. userId={}, username={}", 
                       userId, user.getUsername());
            throw new BusinessLogicException("Current password is incorrect");
        }
        
        // Ensure new password is different from current
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPasswordHash())) {
            logger.warn("SERVICE: Password change failed - new password same as current. userId={}", userId);
            throw new BusinessLogicException("New password must be different from current password");
        }
        
        // Hash and set new password
        user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        
        userRepository.save(user);
        
        logger.info("SERVICE: Password changed successfully. userId={}, username={}", 
                   userId, user.getUsername());
    }
    
    /**
     * Delete user by ID.
     * 
     * @param userId the user ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(Long userId) {
        logger.debug("SERVICE: Deleting user. userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        String username = user.getUsername();
        String email = user.getEmail();
        
        userRepository.delete(user);
        
        logger.info("SERVICE: User deleted successfully. userId={}, username={}, email={}", 
                   userId, username, email);
    }
    
    /**
     * Search users by first name.
     * 
     * @param firstName the first name to search for
     * @return list of users matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersByFirstName(String firstName) {
        List<User> users = userRepository.findByFirstNameContainingIgnoreCase(firstName);
        return userMapper.toResponseList(users);
    }
    
    /**
     * Check if username exists.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if email exists.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
