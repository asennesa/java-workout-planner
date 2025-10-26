package com.workoutplanner.workoutplanner.validation;

import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.service.WorkoutSessionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Simplified validator implementation for ValidExerciseType annotation.
 * 
 * This validator now delegates complex business logic to the service layer,
 * following best practices of keeping validators simple and focused on data validation.
 * 
 * The actual business logic for exercise type validation is handled by
 * WorkoutSessionService.validateExerciseTypeConsistency() method.
 */
public class ValidExerciseTypeValidator implements ConstraintValidator<ValidExerciseType, WorkoutExercise> {
    
    @Autowired
    private WorkoutSessionService workoutSessionService;
    
    @Override
    public void initialize(ValidExerciseType constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(WorkoutExercise workoutExercise, ConstraintValidatorContext context) {
        if (workoutExercise == null) {
            return true;
        }
        
        return workoutSessionService.validateExerciseTypeConsistency(workoutExercise);
    }
}
