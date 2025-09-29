package com.workoutplanner.workoutplanner.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for exercise type validation.
 * Ensures that the exercise type matches the expected set types:
 * - STRENGTH exercises should have strength sets
 * - CARDIO exercises should have cardio sets  
 * - FLEXIBILITY exercises should have flexibility sets
 */
@Documented
@Constraint(validatedBy = ValidExerciseTypeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExerciseType {
    String message() default "Exercise type does not match the provided set types. STRENGTH exercises should have strength sets, CARDIO exercises should have cardio sets, and FLEXIBILITY exercises should have flexibility sets";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
