package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateStrengthSetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.StrengthSet;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.BaseSetMapper;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.StrengthSetRepository;
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
 * Unit tests for StrengthSetService.
 * Tests business logic for strength set management operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StrengthSetService Unit Tests")
class StrengthSetServiceTest {
    
    @Mock
    private StrengthSetRepository strengthSetRepository;
    
    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;
    
    @Mock
    private WorkoutMapper workoutMapper;
    
    @Mock
    private BaseSetMapper baseSetMapper;
    
    @InjectMocks
    private StrengthSetService strengthSetService;
    
    private User testUser;
    private WorkoutSession testWorkout;
    private Exercise testExercise;
    private WorkoutExercise testWorkoutExercise;
    private StrengthSet testStrengthSet;
    private SetResponse testSetResponse;
    
    @BeforeEach
    void setUp() {
        // Best Practice: Use entities WITHOUT IDs for unit tests
        // IDs should only be set when mocking repository return values
        testUser = TestDataBuilder.createNewUser();
        testWorkout = TestDataBuilder.createNewWorkoutSession(testUser);
        testExercise = TestDataBuilder.createNewStrengthExercise();
        
        // Set IDs only for mocked "persisted" entities (simulating DB returns)
        testUser.setUserId(1L);
        testWorkout.setSessionId(1L);
        testExercise.setExerciseId(1L);
        
        testWorkoutExercise = TestDataBuilder.createWorkoutExercise(testWorkout, testExercise);
        testWorkoutExercise.setWorkoutExerciseId(1L);
        
        testStrengthSet = TestDataBuilder.createStrengthSet(testWorkoutExercise);
        testStrengthSet.setSetId(1L);
        
        testSetResponse = new SetResponse();
        testSetResponse.setSetId(1L);
        testSetResponse.setSetNumber(1);
    }
    
    // ==================== CREATE SET TESTS ====================
    
    @Nested
    @DisplayName("Create StrengthSet Tests")
    class CreateSetTests {
        
        @Test
        @DisplayName("Should create strength set successfully")
        void shouldCreateStrengthSetSuccessfully() {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(testWorkoutExercise));
            when(workoutMapper.toStrengthSetEntity(request)).thenReturn(testStrengthSet);
            when(strengthSetRepository.save(any(StrengthSet.class))).thenReturn(testStrengthSet);
            when(baseSetMapper.toSetResponse(testStrengthSet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = strengthSetService.createSet(1L, request);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSetId()).isEqualTo(1L);
            verify(workoutExerciseRepository).findById(1L);
            verify(strengthSetRepository).save(any(StrengthSet.class));
        }
        
        @Test
        @DisplayName("Should throw exception when workout exercise not found")
        void shouldThrowExceptionWhenWorkoutExerciseNotFound() {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            when(workoutExerciseRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> strengthSetService.createSet(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout exercise")
                .hasMessageContaining("999");
            
            verify(strengthSetRepository, never()).save(any(StrengthSet.class));
        }
        
        @Test
        @DisplayName("Should throw exception when adding strength set to non-strength exercise")
        void shouldThrowExceptionWhenAddingStrengthSetToNonStrengthExercise() {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            Exercise cardioExercise = TestDataBuilder.createCardioExercise();
            WorkoutExercise wrongTypeWorkoutExercise = TestDataBuilder.createWorkoutExercise(testWorkout, cardioExercise);
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(wrongTypeWorkoutExercise));
            
            // Act & Assert
            assertThatThrownBy(() -> strengthSetService.createSet(1L, request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Cannot add strength sets to a CARDIO exercise");
            
            verify(strengthSetRepository, never()).save(any(StrengthSet.class));
        }
        
        @Test
        @DisplayName("Should set workout exercise relationship")
        void shouldSetWorkoutExerciseRelationship() {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(testWorkoutExercise));
            when(workoutMapper.toStrengthSetEntity(request)).thenReturn(testStrengthSet);
            when(strengthSetRepository.save(any(StrengthSet.class))).thenReturn(testStrengthSet);
            when(baseSetMapper.toSetResponse(testStrengthSet)).thenReturn(testSetResponse);
            
            // Act
            strengthSetService.createSet(1L, request);
            
            // Assert
            verify(strengthSetRepository).save(argThat(set -> 
                set.getWorkoutExercise() != null &&
                set.getWorkoutExercise().equals(testWorkoutExercise)
            ));
        }
    }
    
    // ==================== GET SET TESTS ====================
    
    @Nested
    @DisplayName("Get StrengthSet Tests")
    class GetSetTests {
        
        @Test
        @DisplayName("Should get strength set by ID successfully")
        void shouldGetStrengthSetByIdSuccessfully() {
            // Arrange
            when(strengthSetRepository.findById(1L)).thenReturn(Optional.of(testStrengthSet));
            when(baseSetMapper.toSetResponse(testStrengthSet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = strengthSetService.getSetById(1L);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSetId()).isEqualTo(1L);
            verify(strengthSetRepository).findById(1L);
        }
        
        @Test
        @DisplayName("Should throw exception when strength set not found")
        void shouldThrowExceptionWhenStrengthSetNotFound() {
            // Arrange
            when(strengthSetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> strengthSetService.getSetById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Strength set")
                .hasMessageContaining("999");
        }
        
        @Test
        @DisplayName("Should get strength sets by workout exercise")
        void shouldGetStrengthSetsByWorkoutExercise() {
            // Arrange
            List<StrengthSet> strengthSets = List.of(testStrengthSet);
            List<SetResponse> responses = List.of(testSetResponse);
            
            when(strengthSetRepository.findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(1L))
                .thenReturn(strengthSets);
            when(baseSetMapper.toSetResponseList(strengthSets)).thenReturn(responses);
            
            // Act
            List<SetResponse> result = strengthSetService.getSetsByWorkoutExercise(1L);
            
            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSetId()).isEqualTo(1L);
        }
        
        @Test
        @DisplayName("Should get strength sets by workout session")
        void shouldGetStrengthSetsByWorkoutSession() {
            // Arrange
            List<StrengthSet> strengthSets = List.of(testStrengthSet);
            List<SetResponse> responses = List.of(testSetResponse);
            
            when(strengthSetRepository.findByWorkoutExercise_WorkoutSession_SessionId(1L))
                .thenReturn(strengthSets);
            when(baseSetMapper.toSetResponseList(strengthSets)).thenReturn(responses);
            
            // Act
            List<SetResponse> result = strengthSetService.getSetsByWorkoutSession(1L);
            
            // Assert
            assertThat(result).hasSize(1);
        }
    }
    
    // ==================== UPDATE SET TESTS ====================
    
    @Nested
    @DisplayName("Update StrengthSet Tests")
    class UpdateSetTests {
        
        @Test
        @DisplayName("Should update strength set successfully")
        void shouldUpdateStrengthSetSuccessfully() {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            
            when(strengthSetRepository.findById(1L)).thenReturn(Optional.of(testStrengthSet));
            when(strengthSetRepository.save(any(StrengthSet.class))).thenReturn(testStrengthSet);
            when(baseSetMapper.toSetResponse(testStrengthSet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = strengthSetService.updateSet(1L, request);
            
            // Assert
            assertThat(result).isNotNull();
            verify(workoutMapper).updateStrengthSetEntity(eq(request), eq(testStrengthSet));
            verify(strengthSetRepository).save(testStrengthSet);
        }
        
        @Test
        @DisplayName("Should throw exception when updating non-existent set")
        void shouldThrowExceptionWhenUpdatingNonExistentSet() {
            // Arrange
            CreateStrengthSetRequest request = TestDataBuilder.createStrengthSetRequest();
            when(strengthSetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> strengthSetService.updateSet(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Strength set");
        }
    }
    
    // ==================== DELETE SET TESTS ====================
    
    @Nested
    @DisplayName("Delete StrengthSet Tests")
    class DeleteSetTests {
        
        @Test
        @DisplayName("Should soft delete strength set successfully")
        void shouldSoftDeleteStrengthSetSuccessfully() {
            // Arrange
            when(strengthSetRepository.findById(1L)).thenReturn(Optional.of(testStrengthSet));
            when(strengthSetRepository.save(any(StrengthSet.class))).thenReturn(testStrengthSet);
            
            // Act
            strengthSetService.deleteSet(1L);
            
            // Assert
            verify(strengthSetRepository).save(argThat(set -> !set.isActive()));
        }
        
        @Test
        @DisplayName("Should throw exception when deleting non-existent set")
        void shouldThrowExceptionWhenDeletingNonExistentSet() {
            // Arrange
            when(strengthSetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> strengthSetService.deleteSet(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Strength set");
        }
    }
    
    // ==================== RESTORE SET TESTS ====================
    
    @Nested
    @DisplayName("Restore StrengthSet Tests")
    class RestoreSetTests {
        
        @Test
        @DisplayName("Should restore soft deleted strength set successfully")
        void shouldRestoreSoftDeletedStrengthSetSuccessfully() {
            // Arrange
            testStrengthSet.softDelete();
            when(strengthSetRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testStrengthSet));
            when(strengthSetRepository.save(any(StrengthSet.class))).thenReturn(testStrengthSet);
            
            // Act
            strengthSetService.restoreSet(1L);
            
            // Assert
            verify(strengthSetRepository).save(argThat(StrengthSet::isActive));
        }
        
        @Test
        @DisplayName("Should throw exception when restoring active set")
        void shouldThrowExceptionWhenRestoringActiveSet() {
            // Arrange
            when(strengthSetRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testStrengthSet));
            
            // Act & Assert
            assertThatThrownBy(() -> strengthSetService.restoreSet(1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("not deleted");
        }
        
        @Test
        @DisplayName("Should throw exception when restoring non-existent set")
        void shouldThrowExceptionWhenRestoringNonExistentSet() {
            // Arrange
            when(strengthSetRepository.findByIdIncludingDeleted(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> strengthSetService.restoreSet(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Strength set");
        }
    }
}

