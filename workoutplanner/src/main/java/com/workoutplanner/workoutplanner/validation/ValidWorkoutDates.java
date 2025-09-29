package com.workoutplanner.workoutplanner.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for workout session date validation.
 * Ensures that:
 * - startedAt is not in the future
 * - completedAt is not before startedAt
 * - completedAt is not in the future
 */
@Documented
@Constraint(validatedBy = ValidWorkoutDatesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWorkoutDates {
    String message() default "Invalid workout session dates: startedAt cannot be in the future, completedAt cannot be before startedAt or in the future";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
