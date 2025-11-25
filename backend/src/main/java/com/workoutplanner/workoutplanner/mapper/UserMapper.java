package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for User entity conversions.
 * 
 * Best practices for MapStruct:
 * 1. Use @Mapper(componentModel = "spring") for Spring integration
 * 2. Use @Mapping annotations for field mappings with different names
 * 3. Create separate methods for different mapping scenarios
 * 4. Auth0 handles authentication (no local passwords)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Maps CreateUserRequest to User entity.
     * Used by Auth0UserSyncService to create local user records.
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "auth0UserId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);

    /**
     * Maps User entity to UserResponse DTO.
     */
    UserResponse toResponse(User user);

    /**
     * Maps list of User entities to list of UserResponse DTOs
     */
    List<UserResponse> toResponseList(List<User> users);

    /**
     * Updates existing User entity with data from CreateUserRequest.
     * Useful for update operations
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "auth0UserId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateEntity(CreateUserRequest createUserRequest, @MappingTarget User user);

    /**
     * Updates existing User entity with data from UpdateUserRequest.
     * For user profile updates (email, name changes).
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "auth0UserId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateFromUpdateRequest(UserUpdateRequest userUpdateRequest, @MappingTarget User user);
}
