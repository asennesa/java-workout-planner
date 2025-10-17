package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateUserRequest;
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
    @Mapping(target = "userId", ignore = true) // Will be set by JPA
    @Mapping(target = "passwordHash", ignore = true) // Will be set in service layer
    @Mapping(target = "createdAt", ignore = true) // Will be set by JPA @PrePersist
    @Mapping(target = "updatedAt", ignore = true) // Will be set by JPA @PrePersist
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
    @Mapping(target = "userId", ignore = true) // Never update the ID
    @Mapping(target = "passwordHash", ignore = true) // Handle password updates separately
    @Mapping(target = "createdAt", ignore = true) // Never update createdAt
    @Mapping(target = "updatedAt", ignore = true) // Will be set by JPA @PreUpdate
    void updateEntity(CreateUserRequest createUserRequest, @MappingTarget User user);

    /**
     * Updates existing User entity with data from UpdateUserRequest.
     * Specifically for user profile updates (excludes password)
     */
    @Mapping(target = "userId", ignore = true) // Never update the ID
    @Mapping(target = "passwordHash", ignore = true) // Never update password in profile update
    @Mapping(target = "createdAt", ignore = true) // Never update createdAt
    @Mapping(target = "updatedAt", ignore = true) // Will be set by JPA @PreUpdate
    void updateFromUpdateRequest(UpdateUserRequest updateUserRequest, @MappingTarget User user);
}
