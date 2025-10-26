package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.CreateExerciseRequest;
import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for Exercise entity conversions.
 * 
 * Best practices for MapStruct:
 * 1. Use @Mapper(componentModel = "spring") for Spring integration
 * 2. Use @Mapping annotations for field mappings with different names
 * 3. Create separate methods for different mapping scenarios
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExerciseMapper {

    /**
     * Maps CreateExerciseRequest to Exercise entity.
     */
    @Mapping(target = "exerciseId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Exercise toEntity(CreateExerciseRequest createExerciseRequest);

    /**
     * Maps Exercise entity to ExerciseResponse DTO.
     */
    ExerciseResponse toResponse(Exercise exercise);

    /**
     * Maps list of Exercise entities to list of ExerciseResponse DTOs
     */
    List<ExerciseResponse> toResponseList(List<Exercise> exercises);

    /**
     * Updates existing Exercise entity with data from CreateExerciseRequest.
     * Useful for update operations
     */
    @InheritConfiguration(name = "toEntity")
    void updateEntity(CreateExerciseRequest createExerciseRequest, @MappingTarget Exercise exercise);
}
