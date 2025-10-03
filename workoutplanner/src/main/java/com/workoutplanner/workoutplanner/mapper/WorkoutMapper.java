package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.StrengthSetResponse;
import com.workoutplanner.workoutplanner.dto.response.CardioSetResponse;
import com.workoutplanner.workoutplanner.dto.response.FlexibilitySetResponse;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
@Mapper(componentModel = "spring")
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
     * Maps StrengthSet entity to StrengthSetResponse DTO
     */
    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    StrengthSetResponse toStrengthSetResponse(StrengthSet strengthSet);

    /**
     * Maps list of StrengthSet entities to list of StrengthSetResponse DTOs
     */
    List<StrengthSetResponse> toStrengthSetResponseList(List<StrengthSet> strengthSets);

    // ========== CARDIO SET MAPPINGS ==========

    /**
     * Maps CardioSet entity to CardioSetResponse DTO
     */
    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    CardioSetResponse toCardioSetResponse(CardioSet cardioSet);

    /**
     * Maps list of CardioSet entities to list of CardioSetResponse DTOs
     */
    List<CardioSetResponse> toCardioSetResponseList(List<CardioSet> cardioSets);

    // ========== FLEXIBILITY SET MAPPINGS ==========

    /**
     * Maps FlexibilitySet entity to FlexibilitySetResponse DTO
     */
    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    FlexibilitySetResponse toFlexibilitySetResponse(FlexibilitySet flexibilitySet);

    /**
     * Maps list of FlexibilitySet entities to list of FlexibilitySetResponse DTOs
     */
    List<FlexibilitySetResponse> toFlexibilitySetResponseList(List<FlexibilitySet> flexibilitySets);
}
