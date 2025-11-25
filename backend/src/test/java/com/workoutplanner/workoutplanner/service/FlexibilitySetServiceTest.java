package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateFlexibilitySetRequest;
import com.workoutplanner.workoutplanner.dto.response.SetResponse;
import com.workoutplanner.workoutplanner.entity.Exercise;
import com.workoutplanner.workoutplanner.entity.FlexibilitySet;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutExercise;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.BaseSetMapper;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.FlexibilitySetRepository;
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
 * Unit tests for FlexibilitySetService.
 * Tests business logic for flexibility set management operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FlexibilitySetService Unit Tests")
class FlexibilitySetServiceTest {
    
    @Mock
    private FlexibilitySetRepository flexibilitySetRepository;
    
    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;
    
    @Mock
    private WorkoutMapper workoutMapper;
    
    @Mock
    private BaseSetMapper baseSetMapper;
    
    @InjectMocks
    private FlexibilitySetService flexibilitySetService;
    
    private User testUser;
    private WorkoutSession testWorkout;
    private Exercise testExercise;
    private WorkoutExercise testWorkoutExercise;
    private FlexibilitySet testFlexibilitySet;
    private SetResponse testSetResponse;
    
    @BeforeEach
    void setUp() {
        // Best Practice: Use entities WITHOUT IDs for unit tests
        // IDs should only be set when mocking repository return values
        testUser = TestDataBuilder.createNewUser();
        testWorkout = TestDataBuilder.createNewWorkoutSession(testUser);
        testExercise = TestDataBuilder.createNewFlexibilityExercise();
        
        // Set IDs only for mocked "persisted" entities (simulating DB returns)
        testUser.setUserId(1L);
        testWorkout.setSessionId(1L);
        testExercise.setExerciseId(1L);
        
        testWorkoutExercise = TestDataBuilder.createWorkoutExercise(testWorkout, testExercise);
        testWorkoutExercise.setWorkoutExerciseId(1L);
        
        testFlexibilitySet = TestDataBuilder.createFlexibilitySet(testWorkoutExercise);
        testFlexibilitySet.setSetId(1L);
        
        testSetResponse = new SetResponse();
        testSetResponse.setSetId(1L);
        testSetResponse.setSetNumber(1);
    }
    
    // ==================== CREATE SET TESTS ====================
    
    @Nested
    @DisplayName("Create FlexibilitySet Tests")
    class CreateSetTests {
        
        @Test
        @DisplayName("Should create flexibility set successfully")
        void shouldCreateFlexibilitySetSuccessfully() {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(testWorkoutExercise));
            when(workoutMapper.toFlexibilitySetEntity(request)).thenReturn(testFlexibilitySet);
            when(flexibilitySetRepository.save(any(FlexibilitySet.class))).thenReturn(testFlexibilitySet);
            when(baseSetMapper.toSetResponse(testFlexibilitySet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = flexibilitySetService.createSet(1L, request);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSetId()).isEqualTo(1L);
            verify(workoutExerciseRepository).findById(1L);
            verify(flexibilitySetRepository).save(any(FlexibilitySet.class));
        }
        
        @Test
        @DisplayName("Should throw exception when workout exercise not found")
        void shouldThrowExceptionWhenWorkoutExerciseNotFound() {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            when(workoutExerciseRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> flexibilitySetService.createSet(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout exercise")
                .hasMessageContaining("999");
            
            verify(flexibilitySetRepository, never()).save(any(FlexibilitySet.class));
        }
        
        @Test
        @DisplayName("Should throw exception when adding flexibility set to non-flexibility exercise")
        void shouldThrowExceptionWhenAddingFlexibilitySetToNonFlexibilityExercise() {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            Exercise strengthExercise = TestDataBuilder.createStrengthExercise();
            WorkoutExercise wrongTypeWorkoutExercise = TestDataBuilder.createWorkoutExercise(testWorkout, strengthExercise);
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(wrongTypeWorkoutExercise));
            
            // Act & Assert
            assertThatThrownBy(() -> flexibilitySetService.createSet(1L, request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Cannot add flexibility sets to a STRENGTH exercise");
            
            verify(flexibilitySetRepository, never()).save(any(FlexibilitySet.class));
        }
        
        @Test
        @DisplayName("Should set workout exercise relationship")
        void shouldSetWorkoutExerciseRelationship() {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            
            when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(testWorkoutExercise));
            when(workoutMapper.toFlexibilitySetEntity(request)).thenReturn(testFlexibilitySet);
            when(flexibilitySetRepository.save(any(FlexibilitySet.class))).thenReturn(testFlexibilitySet);
            when(baseSetMapper.toSetResponse(testFlexibilitySet)).thenReturn(testSetResponse);
            
            // Act
            flexibilitySetService.createSet(1L, request);
            
            // Assert
            verify(flexibilitySetRepository).save(argThat(set -> 
                set.getWorkoutExercise() != null &&
                set.getWorkoutExercise().equals(testWorkoutExercise)
            ));
        }
    }
    
    // ==================== GET SET TESTS ====================
    
    @Nested
    @DisplayName("Get FlexibilitySet Tests")
    class GetSetTests {
        
        @Test
        @DisplayName("Should get flexibility set by ID successfully")
        void shouldGetFlexibilitySetByIdSuccessfully() {
            // Arrange
            when(flexibilitySetRepository.findById(1L)).thenReturn(Optional.of(testFlexibilitySet));
            when(baseSetMapper.toSetResponse(testFlexibilitySet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = flexibilitySetService.getSetById(1L);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSetId()).isEqualTo(1L);
            verify(flexibilitySetRepository).findById(1L);
        }
        
        @Test
        @DisplayName("Should throw exception when flexibility set not found")
        void shouldThrowExceptionWhenFlexibilitySetNotFound() {
            // Arrange
            when(flexibilitySetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> flexibilitySetService.getSetById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Flexibility set")
                .hasMessageContaining("999");
        }
        
        @Test
        @DisplayName("Should get flexibility sets by workout exercise")
        void shouldGetFlexibilitySetsByWorkoutExercise() {
            // Arrange
            List<FlexibilitySet> flexibilitySets = List.of(testFlexibilitySet);
            List<SetResponse> responses = List.of(testSetResponse);
            
            when(flexibilitySetRepository.findByWorkoutExercise_WorkoutExerciseIdOrderBySetNumberAsc(1L))
                .thenReturn(flexibilitySets);
            when(baseSetMapper.toFlexibilitySetResponseList(flexibilitySets)).thenReturn(responses);
            
            // Act
            List<SetResponse> result = flexibilitySetService.getSetsByWorkoutExercise(1L);
            
            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSetId()).isEqualTo(1L);
        }
        
        @Test
        @DisplayName("Should get flexibility sets by workout session")
        void shouldGetFlexibilitySetsByWorkoutSession() {
            // Arrange
            List<FlexibilitySet> flexibilitySets = List.of(testFlexibilitySet);
            List<SetResponse> responses = List.of(testSetResponse);
            
            when(flexibilitySetRepository.findByWorkoutExercise_WorkoutSession_SessionId(1L))
                .thenReturn(flexibilitySets);
            when(baseSetMapper.toFlexibilitySetResponseList(flexibilitySets)).thenReturn(responses);
            
            // Act
            List<SetResponse> result = flexibilitySetService.getSetsByWorkoutSession(1L);
            
            // Assert
            assertThat(result).hasSize(1);
        }
    }
    
    // ==================== UPDATE SET TESTS ====================
    
    @Nested
    @DisplayName("Update FlexibilitySet Tests")
    class UpdateSetTests {
        
        @Test
        @DisplayName("Should update flexibility set successfully")
        void shouldUpdateFlexibilitySetSuccessfully() {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            
            when(flexibilitySetRepository.findById(1L)).thenReturn(Optional.of(testFlexibilitySet));
            when(flexibilitySetRepository.save(any(FlexibilitySet.class))).thenReturn(testFlexibilitySet);
            when(baseSetMapper.toSetResponse(testFlexibilitySet)).thenReturn(testSetResponse);
            
            // Act
            SetResponse result = flexibilitySetService.updateSet(1L, request);
            
            // Assert
            assertThat(result).isNotNull();
            verify(workoutMapper).updateFlexibilitySetEntity(eq(request), eq(testFlexibilitySet));
            verify(flexibilitySetRepository).save(testFlexibilitySet);
        }
        
        @Test
        @DisplayName("Should throw exception when updating non-existent set")
        void shouldThrowExceptionWhenUpdatingNonExistentSet() {
            // Arrange
            CreateFlexibilitySetRequest request = TestDataBuilder.createFlexibilitySetRequest();
            when(flexibilitySetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> flexibilitySetService.updateSet(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Flexibility set");
        }
    }
    
    // ==================== DELETE SET TESTS ====================
    
    @Nested
    @DisplayName("Delete FlexibilitySet Tests")
    class DeleteSetTests {
        
        @Test
        @DisplayName("Should soft delete flexibility set successfully")
        void shouldSoftDeleteFlexibilitySetSuccessfully() {
            // Arrange
            when(flexibilitySetRepository.findById(1L)).thenReturn(Optional.of(testFlexibilitySet));
            when(flexibilitySetRepository.save(any(FlexibilitySet.class))).thenReturn(testFlexibilitySet);
            
            // Act
            flexibilitySetService.deleteSet(1L);
            
            // Assert
            verify(flexibilitySetRepository).save(argThat(set -> !set.isActive()));
        }
        
        @Test
        @DisplayName("Should throw exception when deleting non-existent set")
        void shouldThrowExceptionWhenDeletingNonExistentSet() {
            // Arrange
            when(flexibilitySetRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> flexibilitySetService.deleteSet(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Flexibility set");
        }
    }
    
    // ==================== RESTORE SET TESTS ====================
    
    @Nested
    @DisplayName("Restore FlexibilitySet Tests")
    class RestoreSetTests {
        
        @Test
        @DisplayName("Should restore soft deleted flexibility set successfully")
        void shouldRestoreSoftDeletedFlexibilitySetSuccessfully() {
            // Arrange
            testFlexibilitySet.softDelete();
            when(flexibilitySetRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testFlexibilitySet));
            when(flexibilitySetRepository.save(any(FlexibilitySet.class))).thenReturn(testFlexibilitySet);
            
            // Act
            flexibilitySetService.restoreSet(1L);
            
            // Assert
            verify(flexibilitySetRepository).save(argThat(FlexibilitySet::isActive));
        }
        
        @Test
        @DisplayName("Should throw exception when restoring active set")
        void shouldThrowExceptionWhenRestoringActiveSet() {
            // Arrange
            when(flexibilitySetRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testFlexibilitySet));
            
            // Act & Assert
            assertThatThrownBy(() -> flexibilitySetService.restoreSet(1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("not deleted");
        }
        
        @Test
        @DisplayName("Should throw exception when restoring non-existent set")
        void shouldThrowExceptionWhenRestoringNonExistentSet() {
            // Arrange
            when(flexibilitySetRepository.findByIdIncludingDeleted(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> flexibilitySetService.restoreSet(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Flexibility set");
        }
    }
}

