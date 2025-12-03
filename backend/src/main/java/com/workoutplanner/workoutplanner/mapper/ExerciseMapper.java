package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for Exercise entity conversions.
 *
 * Provides read-only mapping from Exercise entities to ExerciseResponse DTOs.
 * Exercise creation and modification is not available to users.
 *
 * Best practices for MapStruct:
 * 1. Use @Mapper(componentModel = "spring") for Spring integration
 * 2. Create separate methods for different mapping scenarios
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExerciseMapper {

    /**
     * Maps Exercise entity to ExerciseResponse DTO.
     */
    ExerciseResponse toResponse(Exercise exercise);

    /**
     * Maps list of Exercise entities to list of ExerciseResponse DTOs.
     */
    List<ExerciseResponse> toResponseList(List<Exercise> exercises);
}
