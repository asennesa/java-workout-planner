package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutExerciseRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutRequest;
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
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for WorkoutSession and related entity conversions.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = BaseSetMapper.class)
public abstract class WorkoutMapper {

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

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userFullName", expression = "java(workoutSession.getUser().getFirstName() + \" \" + workoutSession.getUser().getLastName())")
    @Mapping(target = "workoutExercises", source = "workoutExercises")
    public abstract WorkoutResponse toWorkoutResponse(WorkoutSession workoutSession);

    public abstract List<WorkoutResponse> toWorkoutResponseList(List<WorkoutSession> workoutSessions);

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

    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "exerciseName", source = "exercise.name")
    @Mapping(target = "exerciseType", source = "exercise.type")
    public abstract WorkoutExerciseResponse toWorkoutExerciseResponse(WorkoutExercise workoutExercise);

    public abstract List<WorkoutExerciseResponse> toWorkoutExerciseResponseList(List<WorkoutExercise> workoutExercises);

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
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(UpdateWorkoutRequest updateWorkoutRequest, @MappingTarget WorkoutSession workoutSession);

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
