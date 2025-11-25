package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.BaseSet;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class BaseSetMapper {

    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    @Mapping(target = "reps", ignore = true)
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "distance", ignore = true)
    @Mapping(target = "distanceUnit", ignore = true)
    @Mapping(target = "durationInSeconds", ignore = true)
    @Mapping(target = "intensity", ignore = true)
    @Mapping(target = "stretchType", ignore = true)
    public abstract SetResponse toSetResponse(BaseSet set);

    public SetResponse toConcreteSetResponse(BaseSet set) {
        if (set instanceof StrengthSet) {
            return toSetResponse((StrengthSet) set);
        } else if (set instanceof CardioSet) {
            return toSetResponse((CardioSet) set);
        } else if (set instanceof FlexibilitySet) {
            return toSetResponse((FlexibilitySet) set);
        }
        return toSetResponse(set);
    }

    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    @Mapping(target = "distance", ignore = true)
    @Mapping(target = "distanceUnit", ignore = true)
    @Mapping(target = "durationInSeconds", ignore = true)
    @Mapping(target = "intensity", ignore = true)
    @Mapping(target = "stretchType", ignore = true)
    public abstract SetResponse toSetResponse(StrengthSet set);

    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    @Mapping(target = "reps", ignore = true)
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "distanceUnit", ignore = true)
    @Mapping(target = "intensity", ignore = true)
    @Mapping(target = "stretchType", ignore = true)
    public abstract SetResponse toSetResponse(CardioSet set);

    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    @Mapping(target = "reps", ignore = true)
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "distance", ignore = true)
    @Mapping(target = "distanceUnit", ignore = true)
    @Mapping(target = "durationInSeconds", ignore = true)
    public abstract SetResponse toSetResponse(FlexibilitySet set);

    /**
     * Maps list of StrengthSet entities to list of SetResponse DTOs
     */
    public abstract List<SetResponse> toSetResponseList(List<StrengthSet> strengthSets);

    /**
     * Maps list of CardioSet entities to list of SetResponse DTOs
     */
    public abstract List<SetResponse> toCardioSetResponseList(List<CardioSet> cardioSets);

    /**
     * Maps list of FlexibilitySet entities to list of SetResponse DTOs
     */
    public abstract List<SetResponse> toFlexibilitySetResponseList(List<FlexibilitySet> flexibilitySets);
}
