package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.validation.ValidationGroups;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.dto.response.ExistenceCheckResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.workoutplanner.workoutplanner.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User operations.
 * Provides endpoints for user management following REST API best practices.
 * 
 * CORS is configured globally in CorsConfig.java
 * API Version: v1
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/users")
@Validated
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
    public ResponseEntity<UserResponse> createUser(@Validated(ValidationGroups.UserRegistration.class) @RequestBody CreateUserRequest createUserRequest) {
        logger.debug("Creating user with username: {}", createUserRequest.getUsername());
        
        UserResponse userResponse = userService.createUser(createUserRequest);
        
        logger.info("User created successfully. userId={}, username={}", 
                   userResponse.getUserId(), userResponse.getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
    
    /**
     * Get user by ID.
     * 
     * Spring Security Integration:
     * - @PreAuthorize ensures proper authorization
     * - Users can only access their own profile or admins can access any profile
     * 
     * @param userId the user ID
     * @return ResponseEntity containing the user response
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        // Spring Security provides current user context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        logger.debug("Getting user by ID: userId={}, requestedBy={}", userId, currentUsername);
        
        UserResponse userResponse = userService.getUserById(userId);
        
        logger.info("User retrieved successfully. userId={}, username={}, requestedBy={}", 
                   userResponse.getUserId(), userResponse.getUsername(), currentUsername);
        
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Get current user profile using Spring Security context.
     * 
     * This method demonstrates proper Spring Security integration:
     * - Uses SecurityContext to get current user
     * - Eliminates need for manual user ID extraction
     * - Provides better user experience
     * 
     * @return ResponseEntity containing the current user's profile
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        logger.debug("Getting profile for current user: {}", username);
        
        UserResponse userResponse = userService.getUserByUsername(username);
        
        logger.info("Current user profile retrieved. username={}", username);
        
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/lookup")
    public ResponseEntity<UserResponse> findUserByUsernameOrEmail(@RequestParam String identifier) {
        UserResponse userResponse;
        if (identifier.contains("@")) {
            userResponse = userService.getUserByEmail(identifier);
        } else {
            userResponse = userService.getUserByUsername(identifier);
        }
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Get all users with pagination.
     * Supports pagination, sorting, and filtering.
     * 
     * @param pageable pagination parameters (page, size, sort)
     * @return ResponseEntity containing paginated user responses
     * 
     * Examples:
     * - GET /api/v1/users?page=0&size=20
     * - GET /api/v1/users?page=1&size=10&sort=username,asc
     * - GET /api/v1/users?page=0&size=50&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.debug("Fetching users with pagination. page={}, size={}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        
        PagedResponse<UserResponse> pagedResponse = userService.getAllUsers(pageable);
        
        logger.info("Retrieved {} users on page {} of {}", 
                   pagedResponse.getContent().size(), 
                   pagedResponse.getPageNumber(), 
                   pagedResponse.getTotalPages());
        
        return ResponseEntity.ok(pagedResponse);
    }
    
    /**
     * Update user information with unified DTO.
     * 
     * This endpoint supports both basic and secure updates:
     * - Basic updates: firstName, lastName (no password required)
     * - Secure updates: email, password changes (password verification required)
     * 
     * @param userId the user ID
     * @param userUpdateRequest the unified user update request
     * @return ResponseEntity containing the updated user response
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('USER') and @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, 
                                                  @Validated(ValidationGroups.UserProfileUpdate.class) @RequestBody UserUpdateRequest userUpdateRequest) {
        // Spring Security provides current user context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        logger.debug("Updating user: userId={}, currentUser={}, isSecureUpdate={}", 
                   userId, currentUsername, userUpdateRequest.isSecureUpdate());
        
        UserResponse userResponse = userService.updateUser(userId, userUpdateRequest);
        
        logger.info("User updated successfully. userId={}, username={}, updatedBy={}", 
                   userResponse.getUserId(), userResponse.getUsername(), currentUsername);
        
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
        logger.warn("Deleting user: userId={}", userId);
        
        userService.deleteUser(userId);
        
        logger.info("User deleted successfully. userId={}", userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Search users by first name.
     * Input is validated and sanitized to prevent SQL injection and wildcard abuse.
     * 
     * @param firstName the first name to search for (min 2, max 50 characters)
     * @return ResponseEntity containing list of matching user responses
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsersByFirstName(
            @RequestParam 
            @NotBlank(message = "Search term cannot be empty")
            @Size(min = 2, max = 50, message = "Search term must be between 2 and 50 characters") 
            String firstName) {
        logger.debug("Searching users by first name: {}", firstName);
        
        List<UserResponse> userResponses = userService.searchUsersByFirstName(firstName);
        
        logger.info("Found {} users matching firstName search", userResponses.size());
        
        return ResponseEntity.ok(userResponses);
    }
    
    /**
     * Check if username exists.
     * 
     * @param username the username to check
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/check-username")
    public ResponseEntity<ExistenceCheckResponse> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(new ExistenceCheckResponse(exists));
    }
    
    /**
     * Check if email exists.
     * 
     * @param email the email to check
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/check-email")
    public ResponseEntity<ExistenceCheckResponse> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(new ExistenceCheckResponse(exists));
    }
}
