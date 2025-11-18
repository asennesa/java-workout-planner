package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for WorkoutSession entity conversions.
 * 
 * Best practices demonstrated:
 * 1. Complex mapping with nested objects
 * 2. Custom field mappings using expressions
 * 3. Handling of collections
 * 4. Ignoring fields that need special handling
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = BaseSetMapper.class)
public abstract class WorkoutMapper {

    /**
     * Maps CreateWorkoutRequest to WorkoutSession entity.
     * Note: User entity needs to be set separately in service layer
     */
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "workoutExercises", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract WorkoutSession toEntity(CreateWorkoutRequest createWorkoutRequest);

    /**
     * Maps WorkoutSession entity to WorkoutResponse DTO.
     * Includes user information and nested workout exercises
     */
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userFullName", expression = "java(workoutSession.getUser().getFirstName() + \" \" + workoutSession.getUser().getLastName())")
    @Mapping(target = "workoutExercises", source = "workoutExercises")
    public abstract WorkoutResponse toWorkoutResponse(WorkoutSession workoutSession);

    /**
     * Maps list of WorkoutSession entities to list of WorkoutResponse DTOs
     */
    public abstract List<WorkoutResponse> toWorkoutResponseList(List<WorkoutSession> workoutSessions);

    /**
     * Maps CreateWorkoutExerciseRequest to WorkoutExercise entity.
     * Note: WorkoutSession and Exercise need to be set separately in service layer
     */
    @Mapping(target = "workoutExerciseId", ignore = true)
    @Mapping(target = "workoutSession", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "strengthSets", ignore = true)
    @Mapping(target = "cardioSets", ignore = true)
    @Mapping(target = "flexibilitySets", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract WorkoutExercise toWorkoutExerciseEntity(CreateWorkoutExerciseRequest createWorkoutExerciseRequest);

    /**
     * Maps WorkoutExercise entity to WorkoutExerciseResponse DTO
     */
    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "exerciseName", source = "exercise.name")
    public abstract WorkoutExerciseResponse toWorkoutExerciseResponse(WorkoutExercise workoutExercise);

    /**
     * Maps list of WorkoutExercise entities to list of WorkoutExerciseResponse DTOs
     */
    public abstract List<WorkoutExerciseResponse> toWorkoutExerciseResponseList(List<WorkoutExercise> workoutExercises);

    /**
     * Updates existing WorkoutSession entity with data from CreateWorkoutRequest.
     * Useful for update operations
     */
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "workoutExercises", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract void updateEntity(CreateWorkoutRequest createWorkoutRequest, @MappingTarget WorkoutSession workoutSession);

    // ========== STRENGTH SET MAPPINGS ==========

    /**
     * Maps CreateStrengthSetRequest to StrengthSet entity.
     * Note: WorkoutExercise needs to be set separately in service layer
     */
    @Mapping(target = "setId", ignore = true)
    @Mapping(target = "workoutExercise", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract StrengthSet toStrengthSetEntity(CreateStrengthSetRequest createStrengthSetRequest);

    /**
     * Updates existing StrengthSet entity with data from CreateStrengthSetRequest.
     */
    @Mapping(target = "setId", ignore = true)
    @Mapping(target = "workoutExercise", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract void updateStrengthSetEntity(CreateStrengthSetRequest createStrengthSetRequest, @MappingTarget StrengthSet strengthSet);

    /**
     * Maps CreateCardioSetRequest to CardioSet entity.
     * Note: WorkoutExercise needs to be set separately in service layer
     */
    @Mapping(target = "setId", ignore = true)
    @Mapping(target = "workoutExercise", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract CardioSet toCardioSetEntity(CreateCardioSetRequest createCardioSetRequest);

    /**
     * Updates existing CardioSet entity with data from CreateCardioSetRequest.
     */
    @Mapping(target = "setId", ignore = true)
    @Mapping(target = "workoutExercise", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract void updateCardioSetEntity(CreateCardioSetRequest createCardioSetRequest, @MappingTarget CardioSet cardioSet);

    /**
     * Maps CreateFlexibilitySetRequest to FlexibilitySet entity.
     * Note: WorkoutExercise needs to be set separately in service layer
     */
    @Mapping(target = "setId", ignore = true)
    @Mapping(target = "workoutExercise", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract FlexibilitySet toFlexibilitySetEntity(CreateFlexibilitySetRequest createFlexibilitySetRequest);

    /**
     * Updates existing FlexibilitySet entity with data from CreateFlexibilitySetRequest.
     */
    @Mapping(target = "setId", ignore = true)
    @Mapping(target = "workoutExercise", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract void updateFlexibilitySetEntity(CreateFlexibilitySetRequest createFlexibilitySetRequest, @MappingTarget FlexibilitySet flexibilitySet);
}
