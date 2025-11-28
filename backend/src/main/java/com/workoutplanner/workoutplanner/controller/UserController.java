package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.annotation.RateLimited;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.dto.response.ExistenceCheckResponse;
import com.workoutplanner.workoutplanner.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.workoutplanner.workoutplanner.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
import java.util.concurrent.TimeUnit;

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
@Tag(name = "User Management", description = "Endpoints for user registration, profile management, and authentication")
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
     * WARNING - Auth0 Integration Note:
     * This endpoint creates users WITHOUT auth0UserId, meaning they cannot authenticate via Auth0.
     *
     * In a production Auth0 environment, users should be:
     * 1. Created in Auth0 first (via Auth0 signup UI/API)
     * 2. Automatically synced to local database on first login via Auth0UserSyncService
     *
     * This endpoint is primarily for:
     * - Administrative/testing purposes
     * - Migration scenarios
     * - Manual user provisioning by admins
     *
     * Users created via this endpoint will need their auth0UserId set separately
     * before they can authenticate with the application.
     *
     * Following Jakarta Bean Validation best practices:
     * - Uses @Valid for automatic validation of Default group constraints
     * - Business logic validation (uniqueness checks) handled in service layer
     * - Clean separation of concerns
     *
     * @param createUserRequest the user creation request
     * @return ResponseEntity containing the created user response
     */
    @Operation(
        summary = "Create user (Admin/Testing)",
        description = """
            Creates a new user record in the local database.

            **IMPORTANT:** This endpoint does NOT create users in Auth0. Users created here will not be able
            to authenticate unless their Auth0 user ID is set separately.

            **Normal User Flow:** Users should sign up via Auth0, and their profile will be automatically
            synced to the local database on first login.

            **Use Cases for this endpoint:**
            - Administrative user provisioning
            - Testing and development
            - Data migration scenarios
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User successfully created",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input - validation errors or username/email already exists",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded",
            content = @Content
        )
    })
    @PostMapping
    @RateLimited(capacity = 10, refillTokens = 10, refillPeriod = 1,
                 timeUnit = TimeUnit.HOURS, keyType = RateLimited.KeyType.IP)
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "User registration details", required = true)
            @Valid @RequestBody CreateUserRequest createUserRequest) {
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
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves user details by user ID. Users can only view their own profile unless they are an admin.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        )
    })
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('read:users') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
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
    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the profile of the currently authenticated user.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Current user profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        // Get user ID from SecurityContext (Auth0Principal)
        Long userId = SecurityContextHelper.getCurrentUserId();

        logger.debug("Getting profile for current user: userId={}", userId);

        // Fetch fresh user data from database
        UserResponse userResponse = userService.getUserById(userId);

        logger.info("Current user profile retrieved. userId={}, email={}",
                   userResponse.getUserId(), userResponse.getEmail());

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
    @Operation(
        summary = "Get all users (paginated)",
        description = "Retrieves a paginated list of all users. Supports sorting and pagination parameters.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        )
    })
    @GetMapping
    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Pagination parameters (page, size, sort)", example = "page=0&size=20&sort=username,asc")
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
     * This endpoint supports both basic and email updates (Auth0 mode):
     * - Basic updates: firstName, lastName
     * - Email updates: email address changes
     * - Password changes: NOT supported (handled by Auth0)
     * 
     * Authorization: JWT token validation via @PreAuthorize
     * - No password verification needed (Auth0 handles authentication)
     * - Email changes should ideally be done through Auth0 for proper verification
     * 
     * Following Spring Framework best practices:
     * - Uses @Valid for format/structure validation at controller level
     * - Business logic validation (uniqueness checks) in service layer
     * - Clean separation between presentation and business concerns
     * 
     * @param userId the user ID
     * @param userUpdateRequest the unified user update request
     * @return ResponseEntity containing the updated user response
     */
    @PutMapping("/{userId}")
    @PreAuthorize("@userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, 
                                                  @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        // Spring Security provides current user context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        logger.debug("Updating user: userId={}, currentUser={}, isEmailUpdate={}", 
                   userId, currentUsername, userUpdateRequest.isSecureUpdate());
        
        UserResponse userResponse = userService.updateUser(userId, userUpdateRequest);
        
        logger.info("User updated successfully. userId={}, username={}, updatedBy={}", 
                   userResponse.getUserId(), userResponse.getUsername(), currentUsername);
        
        return ResponseEntity.ok(userResponse);
    }
    
    /**
     * Delete user by ID.
     * 
     * Security: Only administrators can delete users to prevent unauthorized account removal.
     * 
     * @param userId the user ID
     * @return ResponseEntity with no content
     */
    @Operation(
        summary = "Delete user (Admin only)",
        description = "Permanently deletes a user account. Only administrators can perform this operation.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User successfully deleted",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - admin role required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content
        )
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('delete:users')")
    @RateLimited(capacity = 10, refillTokens = 10, refillPeriod = 1,
                 timeUnit = TimeUnit.HOURS, keyType = RateLimited.KeyType.USER)
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID to delete", required = true, example = "1")
            @PathVariable Long userId) {
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
    @PreAuthorize("hasAuthority('read:users')")
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
     * Security: Rate limited to prevent username enumeration attacks.
     * Attackers could use this to discover valid usernames.
     * 
     * @param username the username to check (validated for format)
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/check-username")
    @RateLimited(capacity = 10, refillTokens = 10, refillPeriod = 1, timeUnit = TimeUnit.MINUTES, keyType = RateLimited.KeyType.IP)
    public ResponseEntity<ExistenceCheckResponse> checkUsernameExists(
            @RequestParam 
            @NotBlank(message = "Username cannot be empty")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username) {
        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(new ExistenceCheckResponse(exists));
    }
    
    /**
     * Check if email exists.
     * 
     * Security: Rate limited to prevent email enumeration attacks.
     * Attackers could use this to discover registered emails.
     * 
     * @param email the email to check (validated for format)
     * @return ResponseEntity containing boolean result
     */
    @GetMapping("/check-email")
    @RateLimited(capacity = 10, refillTokens = 10, refillPeriod = 1, timeUnit = TimeUnit.MINUTES, keyType = RateLimited.KeyType.IP)
    public ResponseEntity<ExistenceCheckResponse> checkEmailExists(
            @RequestParam 
            @NotBlank(message = "Email cannot be empty")
            @Email(message = "Email must be a valid email address")
            @Size(max = 255, message = "Email must not exceed 255 characters")
            String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(new ExistenceCheckResponse(exists));
    }
}
