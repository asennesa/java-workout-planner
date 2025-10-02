package com.workoutplanner.workoutplanner.validation;

import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

/**
 * Validator implementation for ValidWorkoutDates annotation.
 * Validates workout session date constraints.
 */
public class ValidWorkoutDatesValidator implements ConstraintValidator<ValidWorkoutDates, WorkoutSession> {
    
    @Override
    public void initialize(ValidWorkoutDates constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(WorkoutSession workoutSession, ConstraintValidatorContext context) {
        if (workoutSession == null) {
            return true; // Let other validations handle null
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedAt = workoutSession.getStartedAt();
        LocalDateTime completedAt = workoutSession.getCompletedAt();
        
        // If startedAt is provided, it cannot be in the future
        if (startedAt != null && startedAt.isAfter(now)) {
            return false;
        }
        
        // If both dates are provided, completedAt cannot be before startedAt
        if (startedAt != null && completedAt != null && completedAt.isBefore(startedAt)) {
            return false;
        }
        
        // If completedAt is provided, it cannot be in the future
        if (completedAt != null && completedAt.isAfter(now)) {
            return false;
        }
        
        return true;
    }
}
