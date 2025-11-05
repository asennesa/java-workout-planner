package com.workoutplanner.workoutplanner.validation;

import jakarta.validation.GroupSequence;

/**
 * Simplified validation groups following best practices.
 * 
 * This class provides three core validation groups and group sequences
 * for common validation scenarios. This approach reduces complexity
 * while maintaining flexibility for different validation contexts.
 * 
 * @author WorkoutPlanner Team
 * @version 2.0
 */
public final class ValidationGroups {
    
    /**
     * Validation group for creation operations.
     * Used when creating new entities.
     */
    public interface Create {}
    
    /**
     * Validation group for update operations.
     * Used when updating existing entities.
     */
    public interface Update {}
    
    /**
     * Validation group for deletion operations.
     * Used when deleting entities.
     */
    public interface Delete {}
    
    /**
     * Group sequence for user registration.
     * Validates basic fields first, then business rules.
     */
    @GroupSequence({Create.class})
    public interface UserRegistration {}
    
    /**
     * Group sequence for user profile updates.
     * Validates basic fields first, then business rules.
     */
    @GroupSequence({Update.class})
    public interface UserProfileUpdate {}
    
    /**
     * Group sequence for secure user updates.
     * Validates basic fields first, then security constraints.
     */
    @GroupSequence({Update.class})
    public interface SecureUserUpdate {}
    
    /**
     * Group sequence for exercise creation.
     * Validates basic fields first, then business rules.
     */
    @GroupSequence({Create.class})
    public interface ExerciseCreation {}
    
    /**
     * Group sequence for exercise updates.
     * Validates basic fields first, then business rules.
     */
    @GroupSequence({Update.class})
    public interface ExerciseUpdate {}
    
    /**
     * Group sequence for workout session creation.
     * Validates basic fields first, then business rules.
     */
    @GroupSequence({Create.class})
    public interface WorkoutSessionCreation {}
    
    /**
     * Group sequence for workout session updates.
     * Validates basic fields first, then business rules.
     */
    @GroupSequence({Update.class})
    public interface WorkoutSessionUpdate {}
    
    /**
     * Group sequence for set creation.
     * Validates basic fields first, then type-specific business rules.
     */
    @GroupSequence({Create.class})
    public interface SetCreation {}
    
    /**
     * Group sequence for set updates.
     * Validates basic fields first, then type-specific business rules.
     */
    @GroupSequence({Update.class})
    public interface SetUpdate {}
    
    /**
     * Group sequence for workout exercise creation.
     * Validates basic fields first, then business rules.
     */
    @GroupSequence({Create.class})
    public interface WorkoutExerciseCreation {}
    
    /**
     * Group sequence for workout exercise updates.
     * Validates basic fields first, then business rules.
     */
    @GroupSequence({Update.class})
    public interface WorkoutExerciseUpdate {}
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class containing only static interfaces.
     */
    private ValidationGroups() {
        throw new UnsupportedOperationException("Utility class");
    }
}