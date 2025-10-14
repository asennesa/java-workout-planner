package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.config.ApiVersionConfig;
import com.workoutplanner.workoutplanner.dto.request.ChangePasswordRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateUserRequest;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for User operations.
 * Provides endpoints for user management following REST API best practices.
 * 
 * CORS is configured globally in CorsConfig.java
 * API Version: v1
 */
@RestController
@RequestMapping(ApiVersionConfig.V1_BASE_PATH + "/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    /**
     * Constructor injection for dependencies.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create a new user.
     * 
     * @param createUserRequest the user creation request
     * @return ResponseEntity containing the created user response
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        logger.debug("Creating user with username: {}", createUserRequest.getUsername());
        
        UserResponse userResponse = userService.createUser(createUserRequest);
        
        logger.info("User created successfully. userId={}, username={}", 
                   userResponse.getUserId(), userResponse.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
    
    /**
     * Get user by ID.
     * 
     * @param userId the user ID
     * @return ResponseEntity containing the user response
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        logger.debug("Fetching user by ID: {}", userId);
        
        UserResponse userResponse = userService.getUserById(userId);
        
        logger.trace("User retrieved: userId={}, username={}", userId, userResponse.getUsername());
        
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Get user by username.
     * 
     * @param username the username
     * @return ResponseEntity containing the user response
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse userResponse = userService.getUserByUsername(username);
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Get user by email.
     * 
     * @param email the email
     * @return ResponseEntity containing the user response
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Get all users.
     * 
     * @return ResponseEntity containing list of all user responses
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.debug("Fetching all users");
        
        List<UserResponse> userResponses = userService.getAllUsers();
        
        logger.info("Retrieved {} users", userResponses.size());
        
        return ResponseEntity.ok(userResponses);
    }
    
    /**
     * Update user information (excluding password).
     * For password changes, use the /users/{userId}/change-password endpoint.
     * 
     * @param userId the user ID
     * @param updateUserRequest the updated user information
     * @return ResponseEntity containing the updated user response
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, 
                                                  @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        logger.debug("Updating user: userId={}", userId);
        
        UserResponse userResponse = userService.updateUser(userId, updateUserRequest);
        
        logger.info("User updated successfully. userId={}, username={}", 
                   userResponse.getUserId(), userResponse.getUsername());
        
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Change user password.
     * Requires current password for verification.
     * 
     * @param userId the user ID
     * @param changePasswordRequest the password change request
     * @return ResponseEntity with success message
     */
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@PathVariable Long userId,
                                                              @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        logger.info("Password change requested for userId={}", userId);
        
        userService.changePassword(userId, changePasswordRequest);
        
        logger.info("Password changed successfully for userId={}", userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete user by ID.
     * 
     * @param userId the user ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        logger.warn("Deleting user: userId={}", userId);
        
        userService.deleteUser(userId);
        
        logger.info("User deleted successfully. userId={}", userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Search users by first name.
     * 
     * @param firstName the first name to search for
     * @return ResponseEntity containing list of matching user responses
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsersByFirstName(@RequestParam String firstName) {
        List<UserResponse> userResponses = userService.searchUsersByFirstName(firstName);
        return ResponseEntity.ok(userResponses);
    }
    
    /**
     * Check if username exists.
     * 
     * @param username the username to check
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * Check if email exists.
     * 
     * @param email the email to check
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }
}
