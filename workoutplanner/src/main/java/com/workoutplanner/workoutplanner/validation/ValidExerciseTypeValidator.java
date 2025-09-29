package com.workoutplanner.workoutplanner.validation;

import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.enums.ExerciseType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for ValidExerciseType annotation.
 * Validates that exercise type matches the provided set types.
 */
public class ValidExerciseTypeValidator implements ConstraintValidator<ValidExerciseType, WorkoutExercise> {
    
    @Override
    public void initialize(ValidExerciseType constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(WorkoutExercise workoutExercise, ConstraintValidatorContext context) {
        if (workoutExercise == null || workoutExercise.getExercise() == null) {
            return true; // Let other validations handle null
        }
        
        ExerciseType exerciseType = workoutExercise.getExercise().getType();
        
        if (exerciseType == null) {
            return true; // Let other validations handle null
        }
        
        boolean hasStrengthSets = workoutExercise.getStrengthSets() != null && !workoutExercise.getStrengthSets().isEmpty();
        boolean hasCardioSets = workoutExercise.getCardioSets() != null && !workoutExercise.getCardioSets().isEmpty();
        boolean hasFlexibilitySets = workoutExercise.getFlexibilitySets() != null && !workoutExercise.getFlexibilitySets().isEmpty();
        
        switch (exerciseType) {
            case STRENGTH:
                return hasStrengthSets && !hasCardioSets && !hasFlexibilitySets;
            case CARDIO:
                return hasCardioSets && !hasStrengthSets && !hasFlexibilitySets;
            case FLEXIBILITY:
                return hasFlexibilitySets && !hasStrengthSets && !hasCardioSets;
            default:
                return false;
        }
    }
}
