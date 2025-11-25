package com.workoutplanner.workoutplanner.util;

import com.workoutplanner.workoutplanner.dto.request.*;
import com.workoutplanner.workoutplanner.entity.*;
import com.workoutplanner.workoutplanner.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test Data Builder utility for creating test objects.
 * 
 * Industry Best Practices:
 * - Centralized test data creation
 * - Clear separation between new entities (no ID) and mocked persisted entities (with ID)
 * - Default values for all required fields
 * - Easy customization of specific fields
 * 
 * METHOD NAMING CONVENTIONS:
 * - createNew*() - For NEW entities to be saved (ID = null)
 * - createPersisted*() - For MOCKED already-persisted entities (ID set)
 * - create*() - Legacy methods with IDs (deprecated for new tests)
 * 
 * USAGE GUIDELINES:
 * - Use createNew*() when testing repository.save() or service.create()
 * - Use createPersisted*() when mocking repository.findById() results
 * - Setting IDs on new entities can cause JPA/Hibernate issues
 */
public class TestDataBuilder {
    
    // ==================== USER BUILDERS ====================
    
    /**
     * Creates a NEW user ready to be saved (no ID set).
     * Use this when testing user creation or saving new users.
     * 
     * @return User entity without ID (ready for persistence)
     */
    public static User createNewUser() {
        User user = new User();
        // No ID set - this is a new entity
        user.setAuth0UserId("auth0|507f1f77bcf86cd799439011");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.USER);
        return user;
    }
    
    /**
     * Creates a PERSISTED user (with ID) for mocking find operations.
     * Use this when mocking repository.findById() or similar.
     * 
     * @return User entity with ID=1 (simulating already persisted entity)
     */
    public static User createPersistedUser() {
        User user = createNewUser();
        user.setUserId(1L); // Simulate persisted entity
        return user;
    }
    
    /**
     * Creates a user with custom username and email.
     * 
     * @param username custom username
     * @param email custom email
     * @return User entity with ID set (for mocking)
     */
    public static User createUser(String username, String email) {
        User user = createPersistedUser();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
    
    // ==================== EXERCISE BUILDERS ====================
    
    /**
     * Creates a NEW strength exercise (no ID).
     * Use this when testing exercise creation.
     */
    public static Exercise createNewStrengthExercise() {
        Exercise exercise = new Exercise();
        // No ID - new entity
        exercise.setName("Bench Press");
        exercise.setType(ExerciseType.STRENGTH);
        exercise.setDescription("Classic chest exercise");
        exercise.setTargetMuscleGroup(TargetMuscleGroup.CHEST);
        exercise.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
        return exercise;
    }
    
    /**
     * Creates a PERSISTED strength exercise (with ID).
     * Use this for mocking findById() results.
     */
    public static Exercise createStrengthExercise() {
        Exercise exercise = createNewStrengthExercise();
        exercise.setExerciseId(1L); // Mock persisted entity
        return exercise;
    }
    
    /**
     * Creates a NEW cardio exercise (no ID).
     */
    public static Exercise createNewCardioExercise() {
        Exercise exercise = new Exercise();
        exercise.setName("Running");
        exercise.setType(ExerciseType.CARDIO);
        exercise.setDescription("Cardiovascular running");
        exercise.setTargetMuscleGroup(TargetMuscleGroup.FULL_BODY);
        exercise.setDifficultyLevel(DifficultyLevel.BEGINNER);
        return exercise;
    }
    
    /**
     * Creates a PERSISTED cardio exercise (with ID).
     */
    public static Exercise createCardioExercise() {
        Exercise exercise = createNewCardioExercise();
        exercise.setExerciseId(2L);
        return exercise;
    }
    
    /**
     * Creates a NEW flexibility exercise (no ID).
     */
    public static Exercise createNewFlexibilityExercise() {
        Exercise exercise = new Exercise();
        exercise.setName("Hamstring Stretch");
        exercise.setType(ExerciseType.FLEXIBILITY);
        exercise.setDescription("Stretch for hamstrings");
        exercise.setTargetMuscleGroup(TargetMuscleGroup.HAMSTRINGS);
        exercise.setDifficultyLevel(DifficultyLevel.BEGINNER);
        return exercise;
    }
    
    /**
     * Creates a PERSISTED flexibility exercise (with ID).
     */
    public static Exercise createFlexibilityExercise() {
        Exercise exercise = createNewFlexibilityExercise();
        exercise.setExerciseId(3L);
        return exercise;
    }
    
    // ==================== WORKOUT SESSION BUILDERS ====================
    
    /**
     * Creates a NEW workout session ready to be saved (no ID).
     * Use this when testing workout creation or repository.save().
     * 
     * @param user the user who owns the workout
     * @return WorkoutSession entity without ID (ready for persistence)
     */
    public static WorkoutSession createNewWorkoutSession(User user) {
        WorkoutSession workout = new WorkoutSession();
        // No ID set - this is a new entity
        workout.setName("Morning Workout");
        workout.setDescription("Upper body strength training");
        workout.setUser(user);
        workout.setStatus(WorkoutStatus.PLANNED);
        workout.setStartedAt(null);
        workout.setCompletedAt(null);
        workout.setActualDurationInMinutes(null);
        workout.setSessionNotes("");
        return workout;
    }
    
    /**
     * Creates a PERSISTED workout session (with ID) for mocking.
     * Use this when mocking repository.findById() or similar operations.
     * 
     * @param user the user who owns the workout
     * @return WorkoutSession entity with ID=1 (simulating already persisted entity)
     */
    public static WorkoutSession createPersistedWorkoutSession(User user) {
        WorkoutSession workout = createNewWorkoutSession(user);
        workout.setSessionId(1L); // Simulate persisted entity
        return workout;
    }
    
    /**
     * Creates a default workout session.
     * Kept for backward compatibility - prefer createNewWorkoutSession() or createPersistedWorkoutSession().
     * 
     * @param user the user who owns the workout
     * @return WorkoutSession entity with ID set
     */
    public static WorkoutSession createDefaultWorkoutSession(User user) {
        return createPersistedWorkoutSession(user);
    }
    
    /**
     * Creates an in-progress workout session.
     */
    public static WorkoutSession createInProgressWorkout(User user, LocalDateTime startedAt) {
        WorkoutSession workout = createNewWorkoutSession(user);
        workout.setStatus(WorkoutStatus.IN_PROGRESS);
        workout.setStartedAt(startedAt);
        return workout;
    }
    
    /**
     * Creates a completed workout session.
     */
    public static WorkoutSession createCompletedWorkout(User user, LocalDateTime startedAt, LocalDateTime completedAt) {
        WorkoutSession workout = createNewWorkoutSession(user);
        workout.setStatus(WorkoutStatus.COMPLETED);
        workout.setStartedAt(startedAt);
        workout.setCompletedAt(completedAt);
        workout.setActualDurationInMinutes(60);
        return workout;
    }
    
    // ==================== DTO REQUEST BUILDERS ====================
    
    /**
     * Creates a default CreateUserRequest for testing user registration.
     * All validation constraints are satisfied.
     */
    public static CreateUserRequest createUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");
        return request;
    }
    
    /**
     * Creates a CreateUserRequest with custom username and email.
     */
    public static CreateUserRequest createUserRequest(String username, String email) {
        CreateUserRequest request = createUserRequest();
        request.setUsername(username);
        request.setEmail(email);
        return request;
    }
    
    /**
     * Creates a UserUpdateRequest for testing profile updates.
     */
    public static UserUpdateRequest createUserUpdateRequest() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setEmail("updated@example.com");
        return request;
    }

    /**
     * Creates a default CreateWorkoutRequest.
     */
    public static CreateWorkoutRequest createWorkoutRequest(Long userId) {
        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setUserId(userId);
        request.setName("Test Workout");
        request.setDescription("Test workout description");
        request.setStatus(WorkoutStatus.PLANNED);
        return request;
    }
    
    /**
     * Creates a CreateExerciseRequest.
     */
    public static CreateExerciseRequest createExerciseRequest() {
        CreateExerciseRequest request = new CreateExerciseRequest();
        request.setName("Test Exercise");
        request.setType(ExerciseType.STRENGTH);
        request.setDescription("Test description");
        request.setTargetMuscleGroup(TargetMuscleGroup.CHEST);
        request.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
        return request;
    }
    
    /**
     * Creates a CreateWorkoutExerciseRequest.
     */
    public static CreateWorkoutExerciseRequest createWorkoutExerciseRequest(Long exerciseId) {
        CreateWorkoutExerciseRequest request = new CreateWorkoutExerciseRequest();
        request.setExerciseId(exerciseId);
        request.setOrderInWorkout(1);
        request.setNotes("Test notes");
        return request;
    }
    
    /**
     * Creates a CreateStrengthSetRequest.
     */
    public static CreateStrengthSetRequest createStrengthSetRequest() {
        CreateStrengthSetRequest request = new CreateStrengthSetRequest();
        request.setSetNumber(1);
        request.setReps(10);
        request.setWeight(new BigDecimal("100.00"));
        request.setRestTimeInSeconds(60);
        request.setNotes("Good form");
        return request;
    }
    
    /**
     * Creates a CreateCardioSetRequest.
     */
    public static CreateCardioSetRequest createCardioSetRequest() {
        CreateCardioSetRequest request = new CreateCardioSetRequest();
        request.setSetNumber(1);
        request.setDurationInSeconds(1800); // 30 minutes
        request.setDistance(new BigDecimal("5.00"));
        request.setDistanceUnit("km");
        request.setNotes("Good pace");
        return request;
    }
    
    /**
     * Creates a CreateFlexibilitySetRequest.
     */
    public static CreateFlexibilitySetRequest createFlexibilitySetRequest() {
        CreateFlexibilitySetRequest request = new CreateFlexibilitySetRequest();
        request.setSetNumber(1);
        request.setDurationInSeconds(60);
        request.setStretchType("Static");
        request.setIntensity(3);
        request.setNotes("Good stretch");
        return request;
    }
    
    // ==================== ENTITY BUILDERS ====================
    
    /**
     * Creates a StrengthSet entity.
     */
    public static StrengthSet createStrengthSet(WorkoutExercise workoutExercise) {
        StrengthSet set = new StrengthSet();
        set.setSetId(1L);
        set.setWorkoutExercise(workoutExercise);
        set.setSetNumber(1);
        set.setReps(10);
        set.setWeight(new BigDecimal("100.00"));
        set.setRestTimeInSeconds(60);
        set.setNotes("Good form");
        return set;
    }
    
    /**
     * Creates a CardioSet entity.
     */
    public static CardioSet createCardioSet(WorkoutExercise workoutExercise) {
        CardioSet set = new CardioSet();
        set.setSetId(1L);
        set.setWorkoutExercise(workoutExercise);
        set.setSetNumber(1);
        set.setDurationInSeconds(1800); // 30 minutes
        set.setDistance(new BigDecimal("5.00"));
        set.setDistanceUnit("km");
        set.setNotes("Good pace");
        return set;
    }
    
    /**
     * Creates a FlexibilitySet entity.
     */
    public static FlexibilitySet createFlexibilitySet(WorkoutExercise workoutExercise) {
        FlexibilitySet set = new FlexibilitySet();
        set.setSetId(1L);
        set.setWorkoutExercise(workoutExercise);
        set.setSetNumber(1);
        set.setDurationInSeconds(60);
        set.setStretchType("Static");
        set.setIntensity(3);
        set.setNotes("Good stretch");
        return set;
    }
    
    /**
     * Creates a WorkoutExercise entity.
     */
    public static WorkoutExercise createWorkoutExercise(WorkoutSession workoutSession, Exercise exercise) {
        WorkoutExercise we = new WorkoutExercise();
        we.setWorkoutExerciseId(1L);
        we.setWorkoutSession(workoutSession);
        we.setExercise(exercise);
        we.setOrderInWorkout(1);
        we.setNotes("Test notes");
        return we;
    }
}

