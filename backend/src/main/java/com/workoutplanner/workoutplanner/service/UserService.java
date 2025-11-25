package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceConflictException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.UserMapper;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutSessionRepository;
import com.workoutplanner.workoutplanner.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for User entity operations.
 * Handles business logic for user management including creation, retrieval, and updates.
 * 
 * Auth0 Integration:
 * - Authentication is handled by Auth0 (no passwords stored locally)
 * - User records are synced from Auth0 via Auth0UserSyncService
 * - Local user records maintain business data relationships (workouts, exercises)
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
    private final WorkoutSessionRepository workoutSessionRepository;
    
    /**
     * Constructor injection for dependencies.
     * Makes dependencies explicit, immutable, and easier to test.
     * 
     * Note: PasswordEncoder removed - Auth0 handles password management.
     */
    public UserService(UserRepository userRepository, 
                      UserMapper userMapper, 
                      WorkoutSessionRepository workoutSessionRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.workoutSessionRepository = workoutSessionRepository;
    }
    
    /**
     * Create a new user (for Auth0 synchronization).
     *
     * Note: This method is primarily used by Auth0UserSyncService to create local user records
     * when Auth0 users first authenticate. Direct user registration should be done through Auth0.
     * 
     * @param createUserRequest the user creation request
     * @return the created user response
     * @throws ResourceConflictException if username or email already exists
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        logger.debug("SERVICE: Creating new user. username={}, email={}", 
                    createUserRequest.getUsername(), createUserRequest.getEmail());
        
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            logger.warn("SERVICE: User creation failed - username already exists. username={}", 
                       createUserRequest.getUsername());
            throw new ResourceConflictException("User", "username", createUserRequest.getUsername());
        }
        
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            logger.warn("SERVICE: User creation failed - email already exists. email={}", 
                       createUserRequest.getEmail());
            throw new ResourceConflictException("User", "email", createUserRequest.getEmail());
        }
        
        User user = userMapper.toEntity(createUserRequest);
        
        // No password hash - Auth0 manages all password/authentication
        
        user.setRole(UserRole.USER);
        
        User savedUser = userRepository.save(user);
        
        // Best Practice: Self-reference audit fields for user registration
        // Since user doesn't exist during creation, we set created_by to their own ID
        if (savedUser.getCreatedBy() == null) {
            savedUser.setCreatedBy(savedUser.getUserId());
            savedUser.setUpdatedBy(savedUser.getUserId());
            savedUser = userRepository.save(savedUser);
        }
        
        logger.info("SERVICE: User created successfully. userId={}, username={}, email={}", 
                   savedUser.getUserId(), savedUser.getUsername(), savedUser.getEmail());
        
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
     * Get all users with pagination.
     * 
     * @param pageable pagination information (page number, size, sort)
     * @return paginated user responses
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        logger.debug("SERVICE: Fetching users with pagination. page={}, size={}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponse> userResponses = userMapper.toResponseList(userPage.getContent());
        
        logger.info("SERVICE: Retrieved {} users on page {} of {}", 
                   userResponses.size(), userPage.getNumber(), userPage.getTotalPages());
        
        return new PagedResponse<>(
            userResponses,
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages()
        );
    }
    
    /**
     * Update user information.
     * 
     * Note: Password changes are handled by Auth0, not through this API.
     * 
     * @param userId the user ID
     * @param updateUserRequest the updated user information
     * @return the updated user response
     * @throws ResourceNotFoundException if user not found
     * @throws ResourceConflictException if username or email already exists
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        logger.debug("SERVICE: User update requested. userId={}, isEmailUpdate={}", userId, userUpdateRequest.isSecureUpdate());
        
        if (userUpdateRequest.isSecureUpdate()) {
            logger.info("SERVICE: Routing to email update (includes email change)");
            return updateUserProfileSecurely(userId, userUpdateRequest);
        } else {
            logger.info("SERVICE: Routing to basic update for non-sensitive changes");
            return updateUserBasic(userId, userUpdateRequest);
        }
    }
    
    /**
     * Update user profile with basic information (no password verification required).
     * 
     * This method handles non-sensitive profile updates like firstName and lastName.
     * 
     * @param userId the user ID
     * @param userUpdateRequest the user update request
     * @return UserResponse the updated user response
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserResponse updateUserBasic(Long userId, UserUpdateRequest userUpdateRequest) {
        logger.debug("SERVICE: Basic user update requested. userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        // Update first name if provided
        if (userUpdateRequest.getFirstName() != null && !userUpdateRequest.getFirstName().trim().isEmpty()) {
            user.setFirstName(userUpdateRequest.getFirstName());
            logger.info("SERVICE: User first name updated. userId={}, newFirstName={}", userId, userUpdateRequest.getFirstName());
        }
        
        // Update last name if provided
        if (userUpdateRequest.getLastName() != null && !userUpdateRequest.getLastName().trim().isEmpty()) {
            user.setLastName(userUpdateRequest.getLastName());
            logger.info("SERVICE: User last name updated. userId={}, newLastName={}", userId, userUpdateRequest.getLastName());
        }
        
        // Save updated user
        User savedUser = userRepository.save(user);
        
        logger.info("SERVICE: User updated with basic information. userId={}, nameChanged={}", 
                   userId, userUpdateRequest.isNameChangeRequested());
        
        return userMapper.toResponse(savedUser);
    }

    /**
     * Update user profile (Auth0 mode - JWT validation handles authorization).
     * 
     * Note: With Auth0, password verification is not needed. Authorization is
     * handled via JWT token validation. Email changes in Auth0 require email
     * verification through Auth0's flow, not password confirmation.
     * 
     * @param userId the user ID
     * @param userUpdateRequest the user update request
     * @return UserResponse the updated user response
     * @throws ResourceNotFoundException if user not found
     * @throws BusinessLogicException if validation fails
     */
    @Transactional
    public UserResponse updateUserProfileSecurely(Long userId, UserUpdateRequest userUpdateRequest) {
        logger.debug("SERVICE: Profile update requested (Auth0 mode). userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        // No password verification - Auth0 handles all authentication
        
        // Check for email conflicts
        if (userUpdateRequest.isEmailChangeRequested()) {
            if (!user.getEmail().equals(userUpdateRequest.getEmail()) && 
                userRepository.existsByEmail(userUpdateRequest.getEmail())) {
                logger.warn("SERVICE: Secure profile update failed - email conflict. userId={}, conflictEmail={}", 
                           userId, userUpdateRequest.getEmail());
                throw new ResourceConflictException("User", "email", userUpdateRequest.getEmail());
            }
        }
        
        // Update email
        if (userUpdateRequest.isEmailChangeRequested()) {
            user.setEmail(userUpdateRequest.getEmail());
            logger.info("SERVICE: User email updated. userId={}, newEmail={}", userId, userUpdateRequest.getEmail());
        }
        
        // Update names (can be included with email change)
        if (userUpdateRequest.getFirstName() != null && !userUpdateRequest.getFirstName().trim().isEmpty()) {
            user.setFirstName(userUpdateRequest.getFirstName());
            logger.info("SERVICE: User first name updated. userId={}, newFirstName={}", userId, userUpdateRequest.getFirstName());
        }
        
        if (userUpdateRequest.getLastName() != null && !userUpdateRequest.getLastName().trim().isEmpty()) {
            user.setLastName(userUpdateRequest.getLastName());
            logger.info("SERVICE: User last name updated. userId={}, newLastName={}", userId, userUpdateRequest.getLastName());
        }
        
        User savedUser = userRepository.save(user);
        
        logger.info("SERVICE: User profile updated securely. userId={}, emailChanged={}, nameChanged={}", 
                   userId, userUpdateRequest.isEmailChangeRequested(), 
                   userUpdateRequest.isNameChangeRequested());
        
        return userMapper.toResponse(savedUser);
    }
    
    /**
     * Soft delete user by ID.
     * Marks the user as deleted without physically removing from database.
     * This allows for data recovery and maintains audit trail.
     * 
     * Business Rule: Users with active workout sessions cannot be deleted.
     * This prevents data integrity issues and accidental loss of workout history.
     * 
     * @param userId the user ID
     * @throws ResourceNotFoundException if user not found
     * @throws BusinessLogicException if user has active workout sessions
     */
    @Transactional
    public void deleteUser(Long userId) {
        logger.debug("SERVICE: Soft deleting user. userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        String username = user.getUsername();
        String email = user.getEmail();
        
        // Business Logic: Check for dependent data (workout sessions)
        // Following best practice: prevent deletion if dependencies exist
        if (workoutSessionRepository.existsByUserId(userId)) {
            logger.warn("SERVICE: Cannot delete user with existing workout sessions. userId={}, username={}", 
                       userId, username);
            throw new BusinessLogicException(
                "Cannot delete user account with workout history. " +
                "User has active workout sessions. " +
                "Please contact support if you need to delete your account."
            );
        }
        
        // Perform soft delete
        user.softDelete();
        userRepository.save(user);
        
        logger.info("SERVICE: User soft deleted successfully. userId={}, username={}, email={}", 
                   userId, username, email);
    }
    
    /**
     * Restore a soft deleted user by ID.
     * Allows recovery of accidentally deleted users.
     * 
     * @param userId the user ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void restoreUser(Long userId) {
        logger.debug("SERVICE: Restoring soft deleted user. userId={}", userId);
        
        // Use findByIdIncludingDeleted because we need to access the deleted user to restore it
        User user = userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        if (user.isActive()) {
            logger.warn("SERVICE: User is already active. userId={}", userId);
            throw new BusinessLogicException("User is not deleted and cannot be restored");
        }
        
        String username = user.getUsername();
        String email = user.getEmail();
        
        user.restore();
        userRepository.save(user);
        
        logger.info("SERVICE: User restored successfully. userId={}, username={}, email={}", 
                   userId, username, email);
    }
    
    /**
     * Permanently delete a user (hard delete).
     * This physically removes the user from the database.
     * 
     * WARNING: This operation is irreversible.
     * Should only be used by administrators for compliance requirements (GDPR, etc.).
     * 
     * @param userId the user ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void permanentlyDeleteUser(Long userId) {
        logger.warn("SERVICE: PERMANENTLY deleting user. userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        String username = user.getUsername();
        String email = user.getEmail();
        
        userRepository.delete(user);
        
        logger.warn("SERVICE: User PERMANENTLY deleted. userId={}, username={}, email={}", 
                   userId, username, email);
    }

    /**
     * Search users by first name.
     * Sanitizes input to prevent SQL LIKE wildcard abuse.
     * 
     * @param firstName the first name to search for
     * @return list of users matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersByFirstName(String firstName) {
        logger.debug("SERVICE: Searching users by first name. searchTerm={}", firstName);
        
        String sanitizedFirstName = ValidationUtils.sanitizeLikeWildcards(firstName.trim());
        
        List<User> users = userRepository.findByFirstNameContainingIgnoreCase(sanitizedFirstName);
        
        logger.info("SERVICE: Found {} users matching first name search. searchTerm={}", 
                   users.size(), sanitizedFirstName);
        
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

    /**
     * Check if the current authenticated user matches the given user ID.
     * 
     * This method is used by Spring Security's @PreAuthorize annotation
     * to determine if a user can access their own resources.
     * 
     * @param userId the user ID to check
     * @return true if the current user matches the given user ID, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isCurrentUser(Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.debug("No authenticated user found for isCurrentUser check");
                return false;
            }
            
            if ("anonymousUser".equals(authentication.getPrincipal().toString())) {
                logger.debug("Anonymous user cannot access user resources");
                return false;
            }
            
            String currentUsername = authentication.getName();
            logger.debug("Checking if current user {} matches userId {}", currentUsername, userId);
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.debug("User with ID {} not found", userId);
                return false;
            }
            
            boolean isCurrentUser = currentUsername.equals(user.getUsername());
            logger.debug("isCurrentUser check result: {} (currentUser={}, targetUser={})", 
                         isCurrentUser, currentUsername, user.getUsername());
            
            return isCurrentUser;
            
        } catch (Exception e) {
            logger.error("Error checking if current user matches userId {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
}
