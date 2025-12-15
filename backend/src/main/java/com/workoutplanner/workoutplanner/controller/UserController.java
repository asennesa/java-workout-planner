package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.dto.response.ExistenceCheckResponse;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.security.SecurityContextHelper;
import com.workoutplanner.workoutplanner.service.UserService;
import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Controller for user management operations.
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/users")
@Validated
@Tag(name = "User Management", description = "User registration, profile management, and admin operations")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create user (Admin/Testing)",
            description = "Creates user in local DB. Does NOT create in Auth0. Normal users should sign up via Auth0.")
    @ApiResponse(responseCode = "201", description = "User created",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error or duplicate username/email", content = @Content)
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = @Content)
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        logger.debug("Creating user: {}", request.getUsername());

        UserResponse response = userService.createUser(request);

        logger.info("User created. userId={}, username={}", response.getUserId(), response.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('read:users') or @userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId) {
        logger.debug("Getting userId={}", userId);

        UserResponse response = userService.getUserById(userId);

        logger.info("Retrieved userId={}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Profile retrieved",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        Long userId = SecurityContextHelper.getCurrentUserId();
        logger.debug("Getting profile for userId={}", userId);

        UserResponse response = userService.getUserById(userId);

        logger.info("Profile retrieved for userId={}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users (paginated, Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Users retrieved",
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @GetMapping
    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.debug("Fetching users page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        PagedResponse<UserResponse> response = userService.getAllUsers(pageable);

        logger.info("Retrieved {} users on page {}/{}",
                response.getContent().size(), response.getPageNumber(), response.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User updated",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @PutMapping("/{userId}")
    @PreAuthorize("@userService.isCurrentUser(#userId)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        logger.debug("Updating userId={}", userId);

        UserResponse response = userService.updateUser(userId, request);

        logger.info("Updated userId={}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('delete:users')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId) {
        logger.debug("Deleting userId={}", userId);

        userService.deleteUser(userId);

        logger.info("Deleted userId={}", userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search users by first name (Admin)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Search completed")
    @ApiResponse(responseCode = "400", description = "Invalid search term", content = @Content)
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<List<UserResponse>> searchUsersByFirstName(
            @RequestParam
            @NotBlank(message = "Search term cannot be empty")
            @Size(min = 2, max = 50, message = "Search term must be between 2 and 50 characters")
            String firstName) {
        logger.debug("Searching users by firstName: {}", firstName);

        List<UserResponse> results = userService.searchUsersByFirstName(firstName);

        logger.info("Found {} users matching '{}'", results.size(), firstName);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Check if username exists")
    @ApiResponse(responseCode = "200", description = "Check completed")
    @GetMapping("/check-username")
    public ResponseEntity<ExistenceCheckResponse> checkUsernameExists(
            @RequestParam
            @NotBlank(message = "Username cannot be empty")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username) {
        // Add artificial delay (50-150ms) to prevent timing attacks for user enumeration
        addAntiTimingDelay();
        return ResponseEntity.ok(new ExistenceCheckResponse(userService.usernameExists(username)));
    }

    @Operation(summary = "Check if email exists")
    @ApiResponse(responseCode = "200", description = "Check completed")
    @GetMapping("/check-email")
    public ResponseEntity<ExistenceCheckResponse> checkEmailExists(
            @RequestParam
            @NotBlank(message = "Email cannot be empty")
            @Email(message = "Email must be a valid email address")
            @Size(max = 255, message = "Email must not exceed 255 characters")
            String email) {
        // Add artificial delay (50-150ms) to prevent timing attacks for user enumeration
        addAntiTimingDelay();
        return ResponseEntity.ok(new ExistenceCheckResponse(userService.emailExists(email)));
    }

    /**
     * Adds a random delay (50-150ms) to prevent timing-based user enumeration attacks.
     * This makes it harder for attackers to determine if a username/email exists
     * based on response time differences.
     *
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html#authentication-responses">OWASP Authentication Responses</a>
     */
    private void addAntiTimingDelay() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 151));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
