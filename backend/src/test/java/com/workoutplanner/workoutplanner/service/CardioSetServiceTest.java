package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateCardioSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.CardioSet;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.BaseSetMapper;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.CardioSetRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CardioSetService.
 * 
 * Industry Best Practices Demonstrated:
 * 1. Mock all dependencies
 * 2. Test business logic in isolation
 * 3. Verify all repository interactions
 * 4. Test exception scenarios
 * 5. Test validation logic
 * 6. Use AssertJ for fluent assertions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CardioSetService Unit Tests")
class CardioSetServiceTest {
    
    @Mock
    private CardioSetRepository cardioSetRepository;
    
    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;
    
    @Mock
    private WorkoutMapper workoutMapper;
    
    @Mock
    private BaseSetMapper baseSetMapper;
    
    @InjectMocks
    private CardioSetService cardioSetService;
    
    private User testUser;
    private WorkoutSession testWorkout;
    private Exercise testExercise;
    private WorkoutExercise testWorkoutExercise;
    private CardioSet testCardioSet;
    private SetResponse testSetResponse;
    
    @BeforeEach
    void setUp() {
        // Best Practice: Use entities WITHOUT IDs for unit tests
        // IDs should only be set when mocking repository return values
        testUser = TestDataBuilder.createNewUser();
        testWorkout = TestDataBuilder.createNewWorkoutSession(testUser);
        testExercise = TestDataBuilder.createNewCardioExercise();
        
        // Set IDs only for mocked "persisted" entities (simulating DB returns)
        testUser.setUserId(1L);
        testWorkout.setSessionId(1L);
        testExercise.setExerciseId(1L);
        
        testWorkoutExercise = TestDataBuilder.createWorkoutExercise(testWorkout, testExercise);
        testWorkoutExercise.setWorkoutExerciseId(1L);
        
        testCardioSet = TestDataBuilder.createCardioSet(testWorkoutExercise);
        testCardioSet.setSetId(1L);
        
        testSetResponse = new SetResponse();
        testSetResponse.setSetId(1L);
        testSetResponse.setSetNumber(1);
    }
    
    // ==================== CREATE SET TESTS ====================
    
    @Nested
    @DisplayName("Create CardioSet Tests")
    class CreateSetTests {
        
        @Test
        @DisplayName("Should create cardio set successfully")
        void shouldCreateCardioSetSuccessfully() {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(testWorkoutExercise));
            when(workoutMapper.toCardioSetEntity(request)).thenReturn(testCardioSet);
            when(cardioSetRepository.save(any(CardioSet.class))).thenReturn(testCardioSet);
            when(baseSetMapper.toSetResponse(testCardioSet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = cardioSetService.createSet(1L, request);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSetId()).isEqualTo(1L);
            verify(workoutExerciseRepository).findById(1L);
            verify(cardioSetRepository).save(any(CardioSet.class));
        }
        
        @Test
        @DisplayName("Should throw exception when workout exercise not found")
        void shouldThrowExceptionWhenWorkoutExerciseNotFound() {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            when(workoutExerciseRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> cardioSetService.createSet(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout exercise")
                .hasMessageContaining("999");
            
            verify(cardioSetRepository, never()).save(any(CardioSet.class));
        }
        
        @Test
        @DisplayName("Should throw exception when adding cardio set to non-cardio exercise")
        void shouldThrowExceptionWhenAddingCardioSetToNonCardioExercise() {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            Exercise strengthExercise = TestDataBuilder.createStrengthExercise();
            WorkoutExercise wrongTypeWorkoutExercise = TestDataBuilder.createWorkoutExercise(testWorkout, strengthExercise);
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(wrongTypeWorkoutExercise));
            
            // Act & Assert
            assertThatThrownBy(() -> cardioSetService.createSet(1L, request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Cannot add cardio sets to a STRENGTH exercise");
            
            verify(cardioSetRepository, never()).save(any(CardioSet.class));
        }
        
        @Test
        @DisplayName("Should set workout exercise relationship")
        void shouldSetWorkoutExerciseRelationship() {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(testWorkoutExercise));
            when(workoutMapper.toCardioSetEntity(request)).thenReturn(testCardioSet);
            when(cardioSetRepository.save(any(CardioSet.class))).thenReturn(testCardioSet);
            when(baseSetMapper.toSetResponse(testCardioSet)).thenReturn(testSetResponse);
            
            // Act
            cardioSetService.createSet(1L, request);
            
            // Assert
            verify(cardioSetRepository).save(argThat(set -> 
                set.getWorkoutExercise() != null &&
                set.getWorkoutExercise().equals(testWorkoutExercise)
            ));
        }
    }
    
    // ==================== GET SET TESTS ====================
    
    @Nested
    @DisplayName("Get CardioSet Tests")
    class GetSetTests {
        
        @Test
        @DisplayName("Should get cardio set by ID successfully")
        void shouldGetCardioSetByIdSuccessfully() {
            // Arrange
            when(cardioSetRepository.findById(1L)).thenReturn(Optional.of(testCardioSet));
            when(baseSetMapper.toSetResponse(testCardioSet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = cardioSetService.getSetById(1L);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSetId()).isEqualTo(1L);
            verify(cardioSetRepository).findById(1L);
        }
        
        @Test
        @DisplayName("Should throw exception when cardio set not found")
        void shouldThrowExceptionWhenCardioSetNotFound() {
            // Arrange
            when(cardioSetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> cardioSetService.getSetById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cardio set")
                .hasMessageContaining("999");
        }
        
        @Test
        @DisplayName("Should get cardio sets by workout exercise")
        void shouldGetCardioSetsByWorkoutExercise() {
            // Arrange
            List<CardioSet> cardioSets = List.of(testCardioSet);
            List<SetResponse> responses = List.of(testSetResponse);
            
            when(cardioSetRepository.findByWorkoutExerciseIdOrderBySetNumber(1L))
                .thenReturn(cardioSets);
            when(baseSetMapper.toCardioSetResponseList(cardioSets)).thenReturn(responses);
            
            // Act
            List<SetResponse> result = cardioSetService.getSetsByWorkoutExercise(1L);
            
            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSetId()).isEqualTo(1L);
        }
        
    }
    
    // ==================== UPDATE SET TESTS ====================
    
    @Nested
    @DisplayName("Update CardioSet Tests")
    class UpdateSetTests {
        
        @Test
        @DisplayName("Should update cardio set successfully")
        void shouldUpdateCardioSetSuccessfully() {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            
            when(cardioSetRepository.findById(1L)).thenReturn(Optional.of(testCardioSet));
            when(cardioSetRepository.save(any(CardioSet.class))).thenReturn(testCardioSet);
            when(baseSetMapper.toSetResponse(testCardioSet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = cardioSetService.updateSet(1L, request);
            
            // Assert
            assertThat(result).isNotNull();
            verify(workoutMapper).updateCardioSetEntity(request, testCardioSet);
            verify(cardioSetRepository).save(testCardioSet);
        }
        
        @Test
        @DisplayName("Should throw exception when updating non-existent set")
        void shouldThrowExceptionWhenUpdatingNonExistentSet() {
            // Arrange
            CreateCardioSetRequest request = TestDataBuilder.createCardioSetRequest();
            when(cardioSetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> cardioSetService.updateSet(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cardio set");
        }
    }
    
    // ==================== DELETE SET TESTS ====================
    
    @Nested
    @DisplayName("Delete CardioSet Tests")
    class DeleteSetTests {
        
        @Test
        @DisplayName("Should soft delete cardio set successfully")
        void shouldSoftDeleteCardioSetSuccessfully() {
            // Arrange
            when(cardioSetRepository.findById(1L)).thenReturn(Optional.of(testCardioSet));
            when(cardioSetRepository.save(any(CardioSet.class))).thenReturn(testCardioSet);
            
            // Act
            cardioSetService.deleteSet(1L);
            
            // Assert
            verify(cardioSetRepository).save(argThat(set -> !set.isActive()));
        }
        
        @Test
        @DisplayName("Should throw exception when deleting non-existent set")
        void shouldThrowExceptionWhenDeletingNonExistentSet() {
            // Arrange
            when(cardioSetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> cardioSetService.deleteSet(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cardio set");
        }
    }
}

