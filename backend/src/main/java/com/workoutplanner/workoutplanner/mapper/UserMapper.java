package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for User entity conversions.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

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

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}
