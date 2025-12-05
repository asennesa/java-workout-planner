package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for user management operations.
 */
public interface UserServiceInterface {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long userId);

    PagedResponse<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(Long userId, UserUpdateRequest request);

    void deleteUser(Long userId);

    List<UserResponse> searchUsersByFirstName(String firstName);

    boolean usernameExists(String username);

    boolean emailExists(String email);

    boolean isCurrentUser(Long userId);
}
