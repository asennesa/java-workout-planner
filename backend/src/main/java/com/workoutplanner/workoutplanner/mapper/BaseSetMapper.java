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

/**
 * MapStruct mapper for set entity conversions.
 */
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
        if (set instanceof StrengthSet strengthSet) {
            return toSetResponse(strengthSet);
        } else if (set instanceof CardioSet cardioSet) {
            return toSetResponse(cardioSet);
        } else if (set instanceof FlexibilitySet flexibilitySet) {
            return toSetResponse(flexibilitySet);
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
    @Mapping(target = "intensity", ignore = true)
    @Mapping(target = "stretchType", ignore = true)
    public abstract SetResponse toSetResponse(CardioSet set);

    @Mapping(target = "workoutExerciseId", source = "workoutExercise.workoutExerciseId")
    @Mapping(target = "reps", ignore = true)
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "distance", ignore = true)
    @Mapping(target = "distanceUnit", ignore = true)
    public abstract SetResponse toSetResponse(FlexibilitySet set);

    public abstract List<SetResponse> toSetResponseList(List<StrengthSet> strengthSets);

    public abstract List<SetResponse> toCardioSetResponseList(List<CardioSet> cardioSets);

    public abstract List<SetResponse> toFlexibilitySetResponseList(List<FlexibilitySet> flexibilitySets);
}
