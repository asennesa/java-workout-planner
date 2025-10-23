package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateSetRequest;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
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
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkoutMapper {

    /**
     * Maps CreateWorkoutRequest to WorkoutSession entity.
     * Note: User entity needs to be set separately in service layer
     */
    @Mapping(target = "sessionId", ignore = true) // Will be set by JPA
    @Mapping(target = "user", ignore = true) // Will be set in service layer from userId
    @Mapping(target = "workoutExercises", ignore = true) // Handle separately
    WorkoutSession toEntity(CreateWorkoutRequest createWorkoutRequest);

    /**
     * Maps WorkoutSession entity to WorkoutResponse DTO.
     * Includes user information and nested workout exercises
     */
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userFullName", expression = "java(workoutSession.getUser().getFirstName() + \" \" + workoutSession.getUser().getLastName())")
    @Mapping(target = "workoutExercises", source = "workoutExercises")
    WorkoutResponse toWorkoutResponse(WorkoutSession workoutSession);

    /**
     * Maps list of WorkoutSession entities to list of WorkoutResponse DTOs
     */
    List<WorkoutResponse> toWorkoutResponseList(List<WorkoutSession> workoutSessions);

    /**
     * Maps CreateWorkoutExerciseRequest to WorkoutExercise entity.
     * Note: WorkoutSession and Exercise need to be set separately in service layer
     */
    @Mapping(target = "workoutExerciseId", ignore = true) // Will be set by JPA
    @Mapping(target = "workoutSession", ignore = true) // Will be set in service layer
    @Mapping(target = "exercise", ignore = true) // Will be set in service layer from exerciseId
    @Mapping(target = "strengthSets", ignore = true) // Managed by cascade operations
    @Mapping(target = "cardioSets", ignore = true) // Managed by cascade operations
    @Mapping(target = "flexibilitySets", ignore = true) // Managed by cascade operations
    WorkoutExercise toWorkoutExerciseEntity(CreateWorkoutExerciseRequest createWorkoutExerciseRequest);

    /**
     * Maps WorkoutExercise entity to WorkoutExerciseResponse DTO
     */
    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "exerciseName", source = "exercise.name")
    WorkoutExerciseResponse toWorkoutExerciseResponse(WorkoutExercise workoutExercise);

    /**
     * Maps list of WorkoutExercise entities to list of WorkoutExerciseResponse DTOs
     */
    List<WorkoutExerciseResponse> toWorkoutExerciseResponseList(List<WorkoutExercise> workoutExercises);

    /**
     * Updates existing WorkoutSession entity with data from CreateWorkoutRequest.
     * Useful for update operations
     */
    @Mapping(target = "sessionId", ignore = true) // Never update the ID
    @Mapping(target = "user", ignore = true) // Handle user updates separately
    @Mapping(target = "workoutExercises", ignore = true) // Handle exercises separately
    void updateEntity(CreateWorkoutRequest createWorkoutRequest, @MappingTarget WorkoutSession workoutSession);

    // ========== STRENGTH SET MAPPINGS ==========

    /**
     * Maps CreateStrengthSetRequest to StrengthSet entity.
     * Note: WorkoutExercise needs to be set separately in service layer
     */
    @Mapping(target = "setId", ignore = true) // Will be set by JPA
    @Mapping(target = "workoutExercise", ignore = true) // Will be set in service layer from workoutExerciseId
    StrengthSet toStrengthSetEntity(CreateSetRequest createSetRequest);

    /**
     * Maps StrengthSet entity to StrengthSetResponse DTO
     */
    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    SetResponse toSetResponse(StrengthSet strengthSet);

    /**
     * Maps list of StrengthSet entities to list of StrengthSetResponse DTOs
     */
    List<SetResponse> toSetResponseList(List<StrengthSet> strengthSets);

    /**
     * Updates existing StrengthSet entity with data from CreateStrengthSetRequest.
     */
    @Mapping(target = "setId", ignore = true) // Never update the ID
    @Mapping(target = "workoutExercise", ignore = true) // Handle separately
    void updateStrengthSetEntity(CreateSetRequest createSetRequest, @MappingTarget StrengthSet strengthSet);

    // ========== CARDIO SET MAPPINGS ==========

    /**
     * Maps CreateCardioSetRequest to CardioSet entity.
     * Note: WorkoutExercise needs to be set separately in service layer
     */
    @Mapping(target = "setId", ignore = true) // Will be set by JPA
    @Mapping(target = "workoutExercise", ignore = true) // Will be set in service layer from workoutExerciseId
    CardioSet toCardioSetEntity(CreateSetRequest createSetRequest);

    /**
     * Maps CardioSet entity to CardioSetResponse DTO
     */
    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    SetResponse toSetResponse(CardioSet cardioSet);

    /**
     * Maps list of CardioSet entities to list of CardioSetResponse DTOs
     */
    List<SetResponse> toCardioSetResponseList(List<CardioSet> cardioSets);

    /**
     * Updates existing CardioSet entity with data from CreateCardioSetRequest.
     */
    @Mapping(target = "setId", ignore = true) // Never update the ID
    @Mapping(target = "workoutExercise", ignore = true) // Handle separately
    void updateCardioSetEntity(CreateSetRequest createSetRequest, @MappingTarget CardioSet cardioSet);

    // ========== FLEXIBILITY SET MAPPINGS ==========

    /**
     * Maps CreateFlexibilitySetRequest to FlexibilitySet entity.
     * Note: WorkoutExercise needs to be set separately in service layer
     */
    @Mapping(target = "setId", ignore = true) // Will be set by JPA
    @Mapping(target = "workoutExercise", ignore = true) // Will be set in service layer from workoutExerciseId
    FlexibilitySet toFlexibilitySetEntity(CreateSetRequest createSetRequest);

    /**
     * Maps FlexibilitySet entity to FlexibilitySetResponse DTO
     */
    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    SetResponse toSetResponse(FlexibilitySet flexibilitySet);

    /**
     * Maps list of FlexibilitySet entities to list of FlexibilitySetResponse DTOs
     */
    List<SetResponse> toFlexibilitySetResponseList(List<FlexibilitySet> flexibilitySets);

    /**
     * Updates existing FlexibilitySet entity with data from CreateFlexibilitySetRequest.
     */
    @Mapping(target = "setId", ignore = true) // Never update the ID
    @Mapping(target = "workoutExercise", ignore = true) // Handle separately
    void updateFlexibilitySetEntity(CreateSetRequest createSetRequest, @MappingTarget FlexibilitySet flexibilitySet);
}
