package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.mapper.UserMapper;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for User entity operations.
 * Handles business logic for user management including creation, retrieval, and updates.
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Create a new user.
     * 
     * @param createUserRequest the user creation request
     * @return the created user response
     * @throws IllegalArgumentException if username or email already exists
     */
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + createUserRequest.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + createUserRequest.getEmail());
        }
        
        // Convert request to entity
        User user = userMapper.toEntity(createUserRequest);
        
        // Hash the password
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        
        // Save the user
        User savedUser = userRepository.save(user);
        
        // Convert to response and return
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Get user by ID.
     * 
     * @param userId the user ID
     * @return the user response
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get user by username.
     * 
     * @param username the username
     * @return the user response
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
        
        return userMapper.toResponse(user);
    }
    
    /**
     * Get user by email.
     * 
     * @param email the email
     * @return the user response
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        
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
     * Update user information.
     * 
     * @param userId the user ID
     * @param createUserRequest the updated user information
     * @return the updated user response
     * @throws IllegalArgumentException if user not found
     */
    public UserResponse updateUser(Long userId, CreateUserRequest createUserRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Check if new username conflicts with existing users (excluding current user)
        if (!user.getUsername().equals(createUserRequest.getUsername()) && 
            userRepository.existsByUsername(createUserRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + createUserRequest.getUsername());
        }
        
        // Check if new email conflicts with existing users (excluding current user)
        if (!user.getEmail().equals(createUserRequest.getEmail()) && 
            userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + createUserRequest.getEmail());
        }
        
        // Update user fields
        userMapper.updateEntity(createUserRequest, user);
        
        // Hash new password if provided
        if (createUserRequest.getPassword() != null && !createUserRequest.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        }
        
        // Save updated user
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Delete user by ID.
     * 
     * @param userId the user ID
     * @throws IllegalArgumentException if user not found
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        userRepository.deleteById(userId);
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
