package com.workoutplanner.workoutplanner.mapper;

import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.*;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for BaseSetMapper.
 * 
 * Industry Best Practices Demonstrated:
 * 1. Use Mappers.getMapper() for fast, lightweight testing (50x faster)
 * 2. Test all concrete set type mappings
 * 3. Verify polymorphic mapping (toConcreteSetResponse)
 * 4. Test list mappings
 * 5. Verify field-specific mappings for each set type
 * 6. Use AssertJ for fluent, readable assertions
 * 
 * PERFORMANCE: Using MapStruct's Mappers.getMapper() instead of @SpringBootTest
 * reduces test execution time from ~4 seconds to ~80ms (50x faster!)
 */
@DisplayName("BaseSetMapper Unit Tests")
class BaseSetMapperTest {
    
    private BaseSetMapper baseSetMapper;
    
    @BeforeEach
    void setUp() {
        // Direct instantiation via MapStruct - no Spring context needed!
        baseSetMapper = Mappers.getMapper(BaseSetMapper.class);
    }
    
    // ==================== STRENGTH SET MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map StrengthSet to SetResponse")
    void shouldMapStrengthSetToResponse() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createStrengthExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        StrengthSet strengthSet = TestDataBuilder.createStrengthSet(workoutExercise);
        
        // Act
        SetResponse response = baseSetMapper.toSetResponse(strengthSet);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSetId()).isEqualTo(1L);
        assertThat(response.getWorkoutExerciseId()).isEqualTo(1L);
        assertThat(response.getSetNumber()).isEqualTo(1);
        assertThat(response.getNotes()).isEqualTo("Good form");
        
        // Verify strength-specific fields are mapped
        assertThat(response.getReps()).isEqualTo(10);
        assertThat(response.getWeight()).isEqualTo(100.0);
        
        // Verify non-strength fields are null
        assertThat(response.getDistance()).isNull();
        assertThat(response.getDurationInSeconds()).isNull();
        assertThat(response.getIntensity()).isNull();
    }
    
    @Test
    @DisplayName("Should map list of StrengthSets to SetResponse list")
    void shouldMapStrengthSetListToResponseList() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createStrengthExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        StrengthSet set1 = TestDataBuilder.createStrengthSet(workoutExercise);
        set1.setSetId(1L);
        set1.setSetNumber(1);
        
        StrengthSet set2 = TestDataBuilder.createStrengthSet(workoutExercise);
        set2.setSetId(2L);
        set2.setSetNumber(2);
        set2.setReps(12);
        
        List<StrengthSet> sets = List.of(set1, set2);
        
        // Act
        List<SetResponse> responses = baseSetMapper.toSetResponseList(sets);
        
        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getSetNumber()).isEqualTo(1);
        assertThat(responses.get(0).getReps()).isEqualTo(10);
        assertThat(responses.get(1).getSetNumber()).isEqualTo(2);
        assertThat(responses.get(1).getReps()).isEqualTo(12);
    }
    
    // ==================== CARDIO SET MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map CardioSet to SetResponse")
    void shouldMapCardioSetToResponse() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createCardioExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        CardioSet cardioSet = TestDataBuilder.createCardioSet(workoutExercise);
        
        // Act
        SetResponse response = baseSetMapper.toSetResponse(cardioSet);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSetId()).isEqualTo(1L);
        assertThat(response.getWorkoutExerciseId()).isEqualTo(1L);
        assertThat(response.getSetNumber()).isEqualTo(1);
        assertThat(response.getNotes()).isEqualTo("Good pace");
        
        // Verify cardio-specific fields are mapped
        assertThat(response.getDurationInSeconds()).isEqualTo(1800); // 30 minutes
        assertThat(response.getDistance()).isEqualTo(5.0);
        
        // Verify non-cardio fields are null
        assertThat(response.getReps()).isNull();
        assertThat(response.getWeight()).isNull();
        assertThat(response.getIntensity()).isNull();
    }
    
    @Test
    @DisplayName("Should map list of CardioSets to SetResponse list")
    void shouldMapCardioSetListToResponseList() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createCardioExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        CardioSet set1 = TestDataBuilder.createCardioSet(workoutExercise);
        set1.setSetId(1L);
        
        CardioSet set2 = TestDataBuilder.createCardioSet(workoutExercise);
        set2.setSetId(2L);
        set2.setSetNumber(2);
        set2.setDistance(java.math.BigDecimal.valueOf(7.5));
        
        List<CardioSet> sets = List.of(set1, set2);
        
        // Act
        List<SetResponse> responses = baseSetMapper.toCardioSetResponseList(sets);
        
        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getDistance()).isEqualTo(5.0);
        assertThat(responses.get(1).getDistance()).isEqualTo(7.5);
    }
    
    // ==================== FLEXIBILITY SET MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should map FlexibilitySet to SetResponse")
    void shouldMapFlexibilitySetToResponse() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createFlexibilityExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        FlexibilitySet flexibilitySet = TestDataBuilder.createFlexibilitySet(workoutExercise);
        
        // Act
        SetResponse response = baseSetMapper.toSetResponse(flexibilitySet);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSetId()).isEqualTo(1L);
        assertThat(response.getWorkoutExerciseId()).isEqualTo(1L);
        assertThat(response.getSetNumber()).isEqualTo(1);
        assertThat(response.getNotes()).isEqualTo("Good stretch");
        
        // Verify flexibility-specific fields are mapped
        assertThat(response.getIntensity()).isEqualTo(3);
        
        // Verify non-flexibility fields are null
        assertThat(response.getReps()).isNull();
        assertThat(response.getWeight()).isNull();
        assertThat(response.getDistance()).isNull();
    }
    
    @Test
    @DisplayName("Should map list of FlexibilitySets to SetResponse list")
    void shouldMapFlexibilitySetListToResponseList() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createFlexibilityExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        FlexibilitySet set1 = TestDataBuilder.createFlexibilitySet(workoutExercise);
        set1.setSetId(1L);
        
        FlexibilitySet set2 = TestDataBuilder.createFlexibilitySet(workoutExercise);
        set2.setSetId(2L);
        set2.setSetNumber(2);
        set2.setIntensity(5);
        
        List<FlexibilitySet> sets = List.of(set1, set2);
        
        // Act
        List<SetResponse> responses = baseSetMapper.toFlexibilitySetResponseList(sets);
        
        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getIntensity()).isEqualTo(3);
        assertThat(responses.get(1).getIntensity()).isEqualTo(5);
    }
    
    // ==================== POLYMORPHIC MAPPING TESTS ====================
    
    @Test
    @DisplayName("Should handle polymorphic mapping for StrengthSet")
    void shouldHandlePolymorphicMappingForStrengthSet() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createStrengthExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        BaseSet baseSet = TestDataBuilder.createStrengthSet(workoutExercise);
        
        // Act
        SetResponse response = baseSetMapper.toConcreteSetResponse(baseSet);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getReps()).isEqualTo(10);
        assertThat(response.getWeight()).isEqualTo(100.0);
    }
    
    @Test
    @DisplayName("Should handle polymorphic mapping for CardioSet")
    void shouldHandlePolymorphicMappingForCardioSet() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createCardioExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        BaseSet baseSet = TestDataBuilder.createCardioSet(workoutExercise);
        
        // Act
        SetResponse response = baseSetMapper.toConcreteSetResponse(baseSet);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDistance()).isEqualTo(5.0);
        assertThat(response.getDurationInSeconds()).isEqualTo(1800);
    }
    
    @Test
    @DisplayName("Should handle polymorphic mapping for FlexibilitySet")
    void shouldHandlePolymorphicMappingForFlexibilitySet() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createFlexibilityExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        BaseSet baseSet = TestDataBuilder.createFlexibilitySet(workoutExercise);
        
        // Act
        SetResponse response = baseSetMapper.toConcreteSetResponse(baseSet);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIntensity()).isEqualTo(3);
    }
    
    // ==================== EDGE CASE TESTS ====================
    
    @Test
    @DisplayName("Should handle empty list mapping")
    void shouldHandleEmptyListMapping() {
        // Arrange
        List<StrengthSet> emptyList = List.of();
        
        // Act
        List<SetResponse> responses = baseSetMapper.toSetResponseList(emptyList);
        
        // Assert
        assertThat(responses).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle null notes in set mapping")
    void shouldHandleNullNotesInSetMapping() {
        // Arrange
        User user = TestDataBuilder.createPersistedUser();
        WorkoutSession workout = TestDataBuilder.createDefaultWorkoutSession(user);
        Exercise exercise = TestDataBuilder.createStrengthExercise(); // Has ID
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(workout, exercise);
        
        StrengthSet strengthSet = TestDataBuilder.createStrengthSet(workoutExercise);
        strengthSet.setNotes(null);
        
        // Act
        SetResponse response = baseSetMapper.toSetResponse(strengthSet);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getNotes()).isNull();
    }
}

