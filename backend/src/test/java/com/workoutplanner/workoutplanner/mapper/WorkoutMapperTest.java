package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.request.*;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.entity.*;
import com.workoutplanner.workoutplanner.enums.*;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for WorkoutMapper.
 * 
 * Industry Best Practices Demonstrated:
 * 1. Use Mappers.getMapper() for fast, lightweight testing (50x faster)
 * 2. Test all mapping methods thoroughly
 * 3. Use AssertJ for fluent assertions
 * 4. Test null handling and edge cases
 * 5. Verify ignored fields are not mapped
 * 6. Test list mappings
 * 
 * PERFORMANCE: Using MapStruct's Mappers.getMapper() instead of @SpringBootTest
 * reduces test execution time from ~4 seconds to ~80ms (50x faster!)
 * 
 * Testing Philosophy:
 * - Mappers are critical infrastructure code that should be tested thoroughly
 * - Even though MapStruct generates code, we test the mapping configuration
 * - Ensures business logic in expressions is correct
 */
@DisplayName("WorkoutMapper Unit Tests")
class WorkoutMapperTest {
    
    private WorkoutMapper workoutMapper;
    
    @BeforeEach
    void setUp() {
        // Direct instantiation via MapStruct - no Spring context needed!
        workoutMapper = Mappers.getMapper(WorkoutMapper.class);
    }
    
    // ==================== REQUEST TO ENTITY MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map CreateWorkoutRequest to WorkoutSession entity")
    void shouldMapCreateWorkoutRequestToEntity() {
        // Arrange
        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setName("Morning Workout");
        request.setDescription("Upper body training");
        request.setStatus(WorkoutStatus.PLANNED);
        request.setStartedAt(null);
        request.setCompletedAt(null);
        
        // Act
        WorkoutSession entity = workoutMapper.toEntity(request);
        
        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Morning Workout");
        assertThat(entity.getDescription()).isEqualTo("Upper body training");
        assertThat(entity.getStatus()).isEqualTo(WorkoutStatus.PLANNED);
        assertThat(entity.getStartedAt()).isNull();
        assertThat(entity.getCompletedAt()).isNull();
        
        // Verify ignored fields are null (set by service layer)
        assertThat(entity.getSessionId()).isNull();
        assertThat(entity.getUser()).isNull();
        assertThat(entity.getWorkoutExercises()).isNull();
    }
    
    @Test
    @DisplayName("Should map CreateWorkoutRequest with dates to WorkoutSession")
    void shouldMapCreateWorkoutRequestWithDatesToEntity() {
        // Arrange
        LocalDateTime startedAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime completedAt = LocalDateTime.of(2024, 1, 15, 11, 0);
        
        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setName("Completed Workout");
        request.setStatus(WorkoutStatus.COMPLETED);
        request.setStartedAt(startedAt);
        request.setCompletedAt(completedAt);
        
        // Act
        WorkoutSession entity = workoutMapper.toEntity(request);
        
        // Assert
        assertThat(entity.getStartedAt()).isEqualTo(startedAt);
        assertThat(entity.getCompletedAt()).isEqualTo(completedAt);
        assertThat(entity.getStatus()).isEqualTo(WorkoutStatus.COMPLETED);
    }
    
    // ==================== ENTITY TO RESPONSE MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map WorkoutSession entity to WorkoutResponse DTO")
    void shouldMapEntityToWorkoutResponse() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession entity = TestDataBuilder.createDefaultWorkoutSession(user);
        entity.setWorkoutExercises(new ArrayList<>());
        
        // Act
        WorkoutResponse response = workoutMapper.toWorkoutResponse(entity);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSessionId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Morning Workout");
        assertThat(response.getDescription()).isEqualTo("Upper body strength training");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserFullName()).isEqualTo("Test User");
        assertThat(response.getStatus()).isEqualTo(WorkoutStatus.PLANNED);
        assertThat(response.getWorkoutExercises()).isEmpty();
    }
    
    @Test
    @DisplayName("Should map user full name correctly using expression")
    void shouldMapUserFullNameCorrectly() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        user.setFirstName("John");
        user.setLastName("Doe");
        
        WorkoutSession entity = TestDataBuilder.createDefaultWorkoutSession(user);
        entity.setWorkoutExercises(new ArrayList<>());
        
        // Act
        WorkoutResponse response = workoutMapper.toWorkoutResponse(entity);
        
        // Assert
        assertThat(response.getUserFullName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should map WorkoutSession list to WorkoutResponse list")
    void shouldMapEntityListToResponseList() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        
        WorkoutSession workout1 = TestDataBuilder.createDefaultWorkoutSession(user);
        workout1.setName("Workout 1");
        workout1.setWorkoutExercises(new ArrayList<>());
        
        WorkoutSession workout2 = TestDataBuilder.createDefaultWorkoutSession(user);
        workout2.setSessionId(2L);
        workout2.setName("Workout 2");
        workout2.setWorkoutExercises(new ArrayList<>());
        
        List<WorkoutSession> entities = List.of(workout1, workout2);
        
        // Act
        List<WorkoutResponse> responses = workoutMapper.toWorkoutResponseList(entities);
        
        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Workout 1");
        assertThat(responses.get(1).getName()).isEqualTo("Workout 2");
    }
    
    // ==================== UPDATE ENTITY MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should update existing WorkoutSession entity from request")
    void shouldUpdateEntity() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession existingEntity = TestDataBuilder.createDefaultWorkoutSession(user);
        existingEntity.setVersion(1L);
        
        UpdateWorkoutRequest updateRequest = new UpdateWorkoutRequest();
        updateRequest.setName("Updated Workout");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(WorkoutStatus.IN_PROGRESS);
        
        // Act
        workoutMapper.updateEntity(updateRequest, existingEntity);
        
        // Assert
        assertThat(existingEntity.getName()).isEqualTo("Updated Workout");
        assertThat(existingEntity.getDescription()).isEqualTo("Updated description");
        assertThat(existingEntity.getStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
        
        // Verify ignored fields are not changed
        assertThat(existingEntity.getSessionId()).isEqualTo(1L);
        assertThat(existingEntity.getUser()).isEqualTo(user);
        assertThat(existingEntity.getVersion()).isEqualTo(1L);
    }
    
    // ==================== WORKOUT EXERCISE MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map CreateWorkoutExerciseRequest to WorkoutExercise entity")
    void shouldMapWorkoutExerciseRequestToEntity() {
        // Arrange
        CreateWorkoutExerciseRequest request = new CreateWorkoutExerciseRequest();
        request.setExerciseId(5L);
        request.setOrderInWorkout(1);
        request.setNotes("Test notes");
        
        // Act
        WorkoutExercise entity = workoutMapper.toWorkoutExerciseEntity(request);
        
        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getOrderInWorkout()).isEqualTo(1);
        assertThat(entity.getNotes()).isEqualTo("Test notes");
        
        // Verify ignored fields
        assertThat(entity.getWorkoutExerciseId()).isNull();
        assertThat(entity.getWorkoutSession()).isNull();
        assertThat(entity.getExercise()).isNull();
    }
    
    @Test
    @DisplayName("Should map WorkoutExercise entity to WorkoutExerciseResponse")
    void shouldMapWorkoutExerciseEntityToResponse() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createStrengthExercise(); // Has ID
        
        WorkoutExercise entity = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        // Act
        WorkoutExerciseResponse response = workoutMapper.toWorkoutExerciseResponse(entity);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getWorkoutExerciseId()).isEqualTo(1L);
        assertThat(response.getExerciseId()).isEqualTo(1L);
        assertThat(response.getExerciseName()).isEqualTo("Bench Press");
        assertThat(response.getOrderInWorkout()).isEqualTo(1);
        assertThat(response.getNotes()).isEqualTo("Test notes");
    }
    
    // ==================== STRENGTH SET MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map CreateStrengthSetRequest to StrengthSet entity")
    void shouldMapStrengthSetRequestToEntity() {
        // Arrange
        CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
        
        // Act
        StrengthSet entity = workoutMapper.toStrengthSetEntity(request);
        
        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getSetNumber()).isEqualTo(1);
        assertThat(entity.getReps()).isEqualTo(10);
        assertThat(entity.getWeight()).isEqualByComparingTo("100.00");
        assertThat(entity.getRestTimeInSeconds()).isEqualTo(60);
        assertThat(entity.getNotes()).isEqualTo("Good form");
        
        // Verify ignored fields
        assertThat(entity.getSetId()).isNull();
        assertThat(entity.getWorkoutExercise()).isNull();
    }
    
    @Test
    @DisplayName("Should update existing StrengthSet from request")
    void shouldUpdateStrengthSetEntity() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createStrengthExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        StrengthSet existingSet = TestDataBuilder.createStrengthSet(workoutExercise);
        existingSet.setVersion(1L);
        
        CreateStrengthSetRequest updateRequest = new CreateStrengthSetRequest();
        updateRequest.setSetNumber(2);
        updateRequest.setReps(12);
        updateRequest.setWeight(java.math.BigDecimal.valueOf(110.0));
        updateRequest.setRestTimeInSeconds(90);
        
        // Act
        workoutMapper.updateStrengthSetEntity(updateRequest, existingSet);
        
        // Assert
        assertThat(existingSet.getSetNumber()).isEqualTo(2);
        assertThat(existingSet.getReps()).isEqualTo(12);
        assertThat(existingSet.getWeight()).isEqualByComparingTo("110.0");
        assertThat(existingSet.getRestTimeInSeconds()).isEqualTo(90);
        
        // Verify ignored fields unchanged
        assertThat(existingSet.getSetId()).isEqualTo(1L);
        assertThat(existingSet.getWorkoutExercise()).isEqualTo(workoutExercise);
        assertThat(existingSet.getVersion()).isEqualTo(1L);
    }
    
    // ==================== CARDIO SET MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map CreateCardioSetRequest to CardioSet entity")
    void shouldMapCardioSetRequestToEntity() {
        // Arrange
        CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
        
        // Act
        CardioSet entity = workoutMapper.toCardioSetEntity(request);
        
        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getSetNumber()).isEqualTo(1);
        assertThat(entity.getDurationInSeconds()).isEqualTo(1800);
        assertThat(entity.getDistance()).isEqualByComparingTo("5.00");
        assertThat(entity.getDistanceUnit()).isEqualTo("km");
        assertThat(entity.getNotes()).isEqualTo("Good pace");
    }
    
    @Test
    @DisplayName("Should update existing CardioSet from request")
    void shouldUpdateCardioSetEntity() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createCardioExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        CardioSet existingSet = TestDataBuilder.createCardioSet(workoutExercise);
        
        CreateCardioSetRequest updateRequest = new CreateCardioSetRequest();
        updateRequest.setSetNumber(2);
        updateRequest.setDurationInSeconds(2700); // 45 minutes
        updateRequest.setDistance(java.math.BigDecimal.valueOf(7.5));
        updateRequest.setDistanceUnit("km");
        
        // Act
        workoutMapper.updateCardioSetEntity(updateRequest, existingSet);
        
        // Assert
        assertThat(existingSet.getSetNumber()).isEqualTo(2);
        assertThat(existingSet.getDurationInSeconds()).isEqualTo(2700);
        assertThat(existingSet.getDistance()).isEqualByComparingTo("7.5");
        assertThat(existingSet.getDistanceUnit()).isEqualTo("km");
    }
    
    // ==================== FLEXIBILITY SET MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map CreateFlexibilitySetRequest to FlexibilitySet entity")
    void shouldMapFlexibilitySetRequestToEntity() {
        // Arrange
        CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
        
        // Act
        FlexibilitySet entity = workoutMapper.toFlexibilitySetEntity(request);
        
        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getSetNumber()).isEqualTo(1);
        assertThat(entity.getDurationInSeconds()).isEqualTo(60);
        assertThat(entity.getIntensity()).isEqualTo(3);
        assertThat(entity.getNotes()).isEqualTo("Good stretch");
    }
    
    @Test
    @DisplayName("Should update existing FlexibilitySet from request")
    void shouldUpdateFlexibilitySetEntity() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createFlexibilityExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        FlexibilitySet existingSet = TestDataBuilder.createFlexibilitySet(workoutExercise);
        
        CreateFlexibilitySetRequest updateRequest = new CreateFlexibilitySetRequest();
        updateRequest.setSetNumber(2);
        updateRequest.setDurationInSeconds(90);
        updateRequest.setIntensity(5);
        updateRequest.setNotes("Deeper stretch");
        
        // Act
        workoutMapper.updateFlexibilitySetEntity(updateRequest, existingSet);
        
        // Assert
        assertThat(existingSet.getSetNumber()).isEqualTo(2);
        assertThat(existingSet.getDurationInSeconds()).isEqualTo(90);
        assertThat(existingSet.getIntensity()).isEqualTo(5);
        assertThat(existingSet.getNotes()).isEqualTo("Deeper stretch");
    }
    
    // ==================== NULL HANDLING TESTS ====================
    
    @Test
    @DisplayName("Should handle null values gracefully in request mapping")
    void shouldHandleNullValuesInRequest() {
        // Arrange
        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setName("Minimal Workout");
        request.setStatus(WorkoutStatus.PLANNED);
        // description, startedAt, completedAt are null
        
        // Act
        WorkoutSession entity = workoutMapper.toEntity(request);
        
        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Minimal Workout");
        assertThat(entity.getDescription()).isNull();
        assertThat(entity.getStartedAt()).isNull();
        assertThat(entity.getCompletedAt()).isNull();
    }
    
    @Test
    @DisplayName("Should handle empty list mapping")
    void shouldHandleEmptyListMapping() {
        // Arrange
        List<WorkoutSession> emptyList = List.of();
        
        // Act
        List<WorkoutResponse> responses = workoutMapper.toWorkoutResponseList(emptyList);
        
        // Assert
        assertThat(responses).isEmpty();
    }
}

