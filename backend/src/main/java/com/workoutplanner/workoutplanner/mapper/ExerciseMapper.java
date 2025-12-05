package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.response.ExerciseResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for Exercise entity conversions.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExerciseMapper {

    ExerciseResponse toResponse(Exercise exercise);

    List<ExerciseResponse> toResponseList(List<Exercise> exercises);
}
