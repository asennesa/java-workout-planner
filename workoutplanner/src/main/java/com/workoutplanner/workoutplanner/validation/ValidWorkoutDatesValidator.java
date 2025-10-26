package com.workoutplanner.workoutplanner.validation;

import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Validator implementation for ValidWorkoutDates annotation.
 * Validates workout session date constraints using an injectable Clock bean
 * for better testability and time zone consistency.
 */
public class ValidWorkoutDatesValidator implements ConstraintValidator<ValidWorkoutDates, WorkoutSession> {
    
    @Autowired
    private Clock clock;
    
    @Override
    public void initialize(ValidWorkoutDates constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(WorkoutSession workoutSession, ConstraintValidatorContext context) {
        if (workoutSession == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime startedAt = workoutSession.getStartedAt();
        LocalDateTime completedAt = workoutSession.getCompletedAt();
        
        if (startedAt != null && startedAt.isAfter(now)) {
            return false;
        }
        
        if (startedAt != null && completedAt != null && completedAt.isBefore(startedAt)) {
            return false;
        }
        
        if (completedAt != null && completedAt.isAfter(now)) {
            return false;
        }
        
        return true;
    }
}
