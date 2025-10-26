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
 * 2. Exclude sensitive fields like passwordHash from response DTOs
 * 3. Use @Mapping annotations for field mappings with different names
 * 4. Create separate methods for different mapping scenarios
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Maps CreateUserRequest to User entity.
     * Note: passwordHash should be set separately in the service layer after hashing
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toEntity(CreateUserRequest createUserRequest);

    /**
     * Maps User entity to UserResponse DTO.
     * Excludes sensitive information like passwordHash
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
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateEntity(CreateUserRequest createUserRequest, @MappingTarget User user);

    /**
     * Updates existing User entity with data from UpdateUserRequest.
     * Specifically for user profile updates (excludes password)
     */
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateFromUpdateRequest(UserUpdateRequest userUpdateRequest, @MappingTarget User user);
}
