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
import com.workoutplanner.workoutplanner.security.Auth0AuthenticationToken;
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
 * Service for user management. Authentication is handled by Auth0;
 * this service manages local user records and business data relationships.
 */
@Service
public class UserService implements UserServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final WorkoutSessionRepository workoutSessionRepository;

    public UserService(UserRepository userRepository,
                      UserMapper userMapper,
                      WorkoutSessionRepository workoutSessionRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.workoutSessionRepository = workoutSessionRepository;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        logger.debug("Creating user: username={}, email={}", request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceConflictException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setRole(UserRole.USER);
        User saved = userRepository.save(user);

        // Self-reference audit fields for registration
        if (saved.getCreatedBy() == null) {
            saved.setCreatedBy(saved.getUserId());
            saved.setUpdatedBy(saved.getUserId());
            saved = userRepository.save(saved);
        }

        logger.info("User created: userId={}", saved.getUserId());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        logger.debug("Fetching users: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<User> page = userRepository.findAll(pageable);
        List<UserResponse> responses = userMapper.toResponseList(page.getContent());

        logger.info("Retrieved {} users (page {} of {})", responses.size(), page.getNumber(), page.getTotalPages());

        return new PagedResponse<>(
            responses,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        logger.debug("Updating user: userId={}, isSecureUpdate={}", userId, request.isSecureUpdate());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        if (request.isSecureUpdate()) {
            return performSecureUpdate(user, request);
        } else {
            return performBasicUpdate(user, request);
        }
    }

    /**
     * Internal method for basic user updates.
     */
    private UserResponse performBasicUpdate(User user, UserUpdateRequest request) {
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        User saved = userRepository.save(user);
        logger.info("User updated (basic): userId={}", user.getUserId());
        return userMapper.toResponse(saved);
    }

    /**
     * Internal method for secure user updates including email changes.
     */
    private UserResponse performSecureUpdate(User user, UserUpdateRequest request) {
        if (request.isEmailChangeRequested()) {
            if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceConflictException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        User saved = userRepository.save(user);
        logger.info("User updated (secure): userId={}, emailChanged={}", user.getUserId(), request.isEmailChangeRequested());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        logger.debug("Soft deleting user: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));

        if (workoutSessionRepository.existsByUserId(userId)) {
            throw new BusinessLogicException(
                "Cannot delete user account with workout history. " +
                "Please contact support if you need to delete your account."
            );
        }

        user.softDelete();
        userRepository.save(user);
        logger.info("User deleted: userId={}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersByFirstName(String firstName) {
        logger.debug("Searching users by firstName: {}", firstName);

        String sanitized = ValidationUtils.sanitizeLikeWildcards(firstName.trim());
        List<User> users = userRepository.findByFirstNameContainingIgnoreCase(sanitized);

        logger.info("Found {} users matching '{}'", users.size(), sanitized);
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCurrentUser(Long userId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            // Admin bypass - admins can access all users
            if (hasAuthority(auth, "read:users")) {
                logger.debug("Admin access granted for user operation userId={}", userId);
                return true;
            }

            // Get the userId from the authenticated principal
            if (auth instanceof Auth0AuthenticationToken auth0Token) {
                Long currentUserId = auth0Token.getPrincipal().userId();
                return userId.equals(currentUserId);
            }

            return false;
        } catch (Exception e) {
            logger.error("Error checking isCurrentUser for userId {}: {}", userId, e.getMessage());
            return false;
        }
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
