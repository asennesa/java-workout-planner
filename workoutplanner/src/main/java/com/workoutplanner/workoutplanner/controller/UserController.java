package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User operations.
 * Provides endpoints for user management following REST API best practices.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Configure CORS as needed for your frontend
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new user.
     * 
     * @param createUserRequest the user creation request
     * @return ResponseEntity containing the created user response
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        UserResponse userResponse = userService.createUser(createUserRequest);
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
        UserResponse userResponse = userService.getUserById(userId);
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
        List<UserResponse> userResponses = userService.getAllUsers();
        return ResponseEntity.ok(userResponses);
    }
    
    /**
     * Update user information.
     * 
     * @param userId the user ID
     * @param createUserRequest the updated user information
     * @return ResponseEntity containing the updated user response
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, 
                                                  @Valid @RequestBody CreateUserRequest createUserRequest) {
        UserResponse userResponse = userService.updateUser(userId, createUserRequest);
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Delete user by ID.
     * 
     * @param userId the user ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
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
