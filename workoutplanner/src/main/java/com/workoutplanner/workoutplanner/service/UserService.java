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
import com.workoutplanner.workoutplanner.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class UserService implements UserServiceInterface, UserDetailsService {
    
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
        
        user.setPasswordHash(passwordEncoder.encode(createUserRequest.getPassword()));
        
        user.setRole(UserRole.USER);
        
        User savedUser = userRepository.save(user);
        
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
    public UserResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        logger.debug("SERVICE: User update requested. userId={}, isSecureUpdate={}", userId, userUpdateRequest.isSecureUpdate());
        
        if (userUpdateRequest.isSecureUpdate()) {
            logger.info("SERVICE: Routing to secure update for sensitive changes");
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
     * Update user profile securely with password verification.
     * 
     * This method handles sensitive profile updates like email and password changes.
     * Requires current password verification for security.
     * 
     * @param userId the user ID
     * @param userUpdateRequest the user update request
     * @return UserResponse the updated user response
     * @throws ResourceNotFoundException if user not found
     * @throws BusinessLogicException if current password is incorrect or validation fails
     */
    @Transactional
    public UserResponse updateUserProfileSecurely(Long userId, UserUpdateRequest userUpdateRequest) {
        logger.debug("SERVICE: Secure profile update requested. userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        
        if (!passwordEncoder.matches(userUpdateRequest.getCurrentPassword(), user.getPasswordHash())) {
            logger.warn("SERVICE: Secure profile update failed - incorrect current password. userId={}", userId);
            throw new BusinessLogicException("Current password is incorrect");
        }
        
        if (userUpdateRequest.isEmailChangeRequested()) {
            if (!user.getEmail().equals(userUpdateRequest.getEmail()) && 
                userRepository.existsByEmail(userUpdateRequest.getEmail())) {
                logger.warn("SERVICE: Secure profile update failed - email conflict. userId={}, conflictEmail={}", 
                           userId, userUpdateRequest.getEmail());
                throw new ResourceConflictException("User", "email", userUpdateRequest.getEmail());
            }
        }
        
        if (userUpdateRequest.isEmailChangeRequested()) {
            user.setEmail(userUpdateRequest.getEmail());
            logger.info("SERVICE: User email updated. userId={}, newEmail={}", userId, userUpdateRequest.getEmail());
        }
        
        if (userUpdateRequest.getFirstName() != null && !userUpdateRequest.getFirstName().trim().isEmpty()) {
            user.setFirstName(userUpdateRequest.getFirstName());
            logger.info("SERVICE: User first name updated. userId={}, newFirstName={}", userId, userUpdateRequest.getFirstName());
        }
        
        if (userUpdateRequest.getLastName() != null && !userUpdateRequest.getLastName().trim().isEmpty()) {
            user.setLastName(userUpdateRequest.getLastName());
            logger.info("SERVICE: User last name updated. userId={}, newLastName={}", userId, userUpdateRequest.getLastName());
        }
        
        if (userUpdateRequest.isPasswordChangeRequested()) {
            if (!userUpdateRequest.passwordsMatch()) {
                logger.warn("SERVICE: Secure profile update failed - password confirmation mismatch. userId={}", userId);
                throw new BusinessLogicException("New password and confirmation do not match");
            }
            
            String hashedPassword = passwordEncoder.encode(userUpdateRequest.getNewPassword());
            user.setPasswordHash(hashedPassword);
            logger.info("SERVICE: User password updated. userId={}", userId);
        }
        
        User savedUser = userRepository.save(user);
        
        logger.info("SERVICE: User profile updated securely. userId={}, emailChanged={}, nameChanged={}, passwordChanged={}", 
                   userId, userUpdateRequest.isEmailChangeRequested(), 
                   userUpdateRequest.isNameChangeRequested(), 
                   userUpdateRequest.isPasswordChangeRequested());
        
        return userMapper.toResponse(savedUser);
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
     * Load user by username for Spring Security authentication.
     * 
     * @param username the username
     * @return UserDetails object
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        logger.debug("User loaded successfully: {}", username);
        return user;
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
