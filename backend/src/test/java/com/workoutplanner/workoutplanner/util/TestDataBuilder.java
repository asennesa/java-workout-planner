package com.workoutplanner.workoutplanner.util;

import com.workoutplanner.workoutplanner.dto.request.*;
import com.workoutplanner.workoutplanner.entity.*;
import com.workoutplanner.workoutplanner.enums.*;
import com.workoutplanner.workoutplanner.security.Auth0Principal;
import com.workoutplanner.workoutplanner.security.Auth0AuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Test Data Builder utility for creating test objects.
 *
 * Industry Best Practices:
 * - Centralized test data creation
 * - Clear separation between new entities (no ID) and mocked persisted entities (with ID)
 * - Default values for all required fields
 * - Easy customization of specific fields
 * - Uses UUID for unique identifiers to prevent test pollution in parallel execution
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
 *
 * @see <a href="https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html">Spring Boot Testing</a>
 */
public class TestDataBuilder {

    /**
     * Generates a unique 8-character suffix using UUID.
     * This ensures test data uniqueness even in parallel test execution.
     */
    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // ==================== USER BUILDERS ====================

    /**
     * Creates a NEW user ready to be saved (no ID set).
     * Use this when testing user creation or saving new users.
     * Each call generates unique auth0_user_id, username, and email using UUID to avoid conflicts
     * even in parallel test execution.
     *
     * @return User entity without ID (ready for persistence)
     */
    public static User createNewUser() {
        String suffix = uniqueSuffix();
        User user = new User();
        // No ID set - this is a new entity
        // Use UUID suffix to ensure unique values for auth0_user_id, username, and email
        user.setAuth0UserId("auth0|" + suffix);
        user.setUsername("testuser_" + suffix);
        user.setEmail("test_" + suffix + "@example.com");
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
     * Creates a default CreateWorkoutRequest.
     * Note: userId is no longer part of the request - it's derived from JWT token.
     */
    public static CreateWorkoutRequest createWorkoutRequest() {
        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setName("Test Workout");
        request.setDescription("Test workout description");
        request.setStatus(WorkoutStatus.PLANNED);
        return request;
    }

    // ==================== SECURITY CONTEXT UTILITIES ====================

    /**
     * Sets up the security context with a mock authenticated user.
     * Use this in @BeforeEach to simulate an authenticated user.
     *
     * @param userId the user's database ID
     */
    public static void setupSecurityContext(Long userId) {
        setupSecurityContext(userId, UserRole.USER);
    }

    /**
     * Sets up the security context with a mock authenticated user with specific role.
     *
     * @param userId the user's database ID
     * @param role the user's role
     */
    public static void setupSecurityContext(Long userId, UserRole role) {
        Auth0Principal principal = new Auth0Principal(
            userId,
            "auth0|test-" + userId,
            "test" + userId + "@example.com",
            "testuser" + userId,
            "Test",
            "User",
            role
        );

        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .claim("sub", principal.auth0UserId())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("read:workouts"),
            new SimpleGrantedAuthority("write:workouts")
        );

        Auth0AuthenticationToken authToken = new Auth0AuthenticationToken(principal, jwt, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * Clears the security context. Call in @AfterEach to clean up.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
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

