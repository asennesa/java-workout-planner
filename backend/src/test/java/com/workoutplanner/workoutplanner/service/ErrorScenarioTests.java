package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateWorkoutRequest;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.entity.WorkoutSession;
import com.workoutplanner.workoutplanner.exception.OptimisticLockConflictException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.ExerciseRepository;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutExerciseRepository;
import com.workoutplanner.workoutplanner.repository.WorkoutSessionRepository;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Error Scenario Tests - Testing edge cases and failure modes.
 * 
 * Industry Best Practices:
 * - Test all expected error conditions
 * - Verify exception handling and recovery
 * - Test database constraint violations
 * - Test concurrent modification scenarios
 * - Test invalid input handling
 * - Test resource not found scenarios
 * 
 * Why Error Testing Matters:
 * - Ensures graceful degradation under failure
 * - Prevents cascading failures
 * - Validates error messages are helpful
 * - Tests exception translation layer
 * - Verifies logging and monitoring
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Error Scenario Tests")
class ErrorScenarioTests {
    
    @Mock
    private WorkoutSessionRepository workoutSessionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private WorkoutMapper workoutMapper;
    
    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;
    
    @Mock
    private ExerciseRepository exerciseRepository;
    
    @Mock
    private Clock clock;
    
    @InjectMocks
    private WorkoutSessionService workoutSessionService;
    
    private User testUser;
    private WorkoutSession testWorkout;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createPersistedUser();
        testWorkout = TestDataBuilder.createDefaultWorkoutSession(testUser);

        // Mock clock to return a fixed instant (lenient since not all tests use it)
        lenient().when(clock.instant()).thenReturn(Instant.now());
        lenient().when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        // Setup default security context
        TestDataBuilder.setupSecurityContext(testUser.getUserId());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }
    
    // ==================== CONCURRENT MODIFICATION TESTS ====================

    @Nested
    @DisplayName("Concurrent Modification Error Tests")
    class ConcurrentModificationTests {

        @Test
        @DisplayName("Should handle optimistic locking failure gracefully on update")
        void shouldHandleOptimisticLockingFailureOnUpdate() {
            // Arrange - Simulate concurrent modification during update
            // Note: OptimisticLockConflictException handling only exists in update methods,
            // not in create methods, which is by design.
            testWorkout.setSessionId(1L);

            when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(testWorkout));
            when(workoutSessionRepository.save(any(WorkoutSession.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(
                    WorkoutSession.class, testWorkout.getSessionId()));

            com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutRequest updateRequest =
                new com.workoutplanner.workoutplanner.dto.request.UpdateWorkoutRequest();
            updateRequest.setName("Updated Name");

            // Act & Assert - Should translate to our custom exception
            assertThatThrownBy(() -> workoutSessionService.updateWorkoutSession(1L, updateRequest))
                .isInstanceOf(OptimisticLockConflictException.class)
                .hasMessageContaining("modified by another")
                .hasMessageContaining("Please refresh and try again");

            // Verify repository interactions
            verify(workoutSessionRepository).findById(1L);
            verify(workoutSessionRepository).save(any(WorkoutSession.class));
        }

        @Test
        @DisplayName("Should propagate transient database failure from repository")
        void shouldPropagateTransientDatabaseFailure() {
            // This test demonstrates that transient failures propagate correctly.
            // Note: getWorkoutSessionById uses findWithUserBySessionId, not findById

            // Arrange
            when(workoutSessionRepository.findWithUserBySessionId(1L))
                .thenThrow(new RuntimeException("Transient database connection error"));

            // Act & Assert
            // Without retry logic (like Spring Retry @Retryable), the exception propagates
            assertThatThrownBy(() -> workoutSessionService.getWorkoutSessionById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("database connection");
        }
    }
    
    // ==================== DATABASE CONSTRAINT VIOLATION TESTS ====================
    
    @Nested
    @DisplayName("Database Constraint Violation Tests")
    class DatabaseConstraintTests {
        
        @Test
        @DisplayName("Should handle duplicate key violation")
        void shouldHandleDuplicateKeyViolation() {
            // Arrange - Simulate unique constraint violation
            CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
            
            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(workoutMapper.toEntity(any(CreateWorkoutRequest.class))).thenReturn(testWorkout);
            when(workoutSessionRepository.save(any(WorkoutSession.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key violation"));
            
            // Act & Assert
            assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Duplicate key");
        }
        
        @Test
        @DisplayName("Should handle foreign key constraint violation")
        void shouldHandleForeignKeyConstraintViolation() {
            // Arrange - Try to save workout with non-existent user
            TestDataBuilder.setupSecurityContext(999L);
            CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();

            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert - Should fail before hitting database
            assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
        }
    }
    
    // ==================== INVALID INPUT TESTS ====================
    
    @Nested
    @DisplayName("Invalid Input Tests")
    class InvalidInputTests {
        
        @Test
        @DisplayName("Should reject null request")
        void shouldRejectNullRequest() {
            // Act & Assert
            assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(null))
                .isInstanceOf(NullPointerException.class);
        }
        
        @Test
        @DisplayName("Should throw exception when no user authenticated")
        void shouldThrowExceptionWhenNoUserAuthenticated() {
            // Arrange - Clear security context to simulate unauthenticated request
            TestDataBuilder.clearSecurityContext();
            CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();

            // Act & Assert - Should fail because no user in security context
            assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No authenticated user");
        }
        
        @Test
        @DisplayName("Should handle extremely large data gracefully")
        void shouldHandleExtremelyLargeDataGracefully() {
            // Arrange - Create request with extremely long string
            CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
            String extremelyLongName = "A".repeat(10000); // 10K characters
            request.setName(extremelyLongName);
            
            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(workoutMapper.toEntity(any(CreateWorkoutRequest.class))).thenReturn(testWorkout);
            when(workoutSessionRepository.save(any(WorkoutSession.class)))
                .thenThrow(new DataIntegrityViolationException("Value too long"));
            
            // Act & Assert - Should be handled gracefully
            assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
    
    // ==================== RESOURCE NOT FOUND TESTS ====================
    
    @Nested
    @DisplayName("Resource Not Found Tests")
    class ResourceNotFoundTests {
        
        @Test
        @DisplayName("Should throw clear exception when workout not found")
        void shouldThrowClearExceptionWhenWorkoutNotFound() {
            // Arrange
            lenient().when(workoutSessionRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> workoutSessionService.getWorkoutSessionById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout")
                .hasMessageContaining("999")
                .hasMessageContaining("not found");
        }
        
        @Test
        @DisplayName("Should provide helpful message when user not found")
        void shouldProvideHelpfulMessageWhenUserNotFound() {
            // Arrange
            TestDataBuilder.setupSecurityContext(999L);
            CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(exception -> {
                    assertThat(exception.getMessage())
                        .contains("User")
                        .contains("999")
                        .contains("not found");
                });
        }
    }
    
    // ==================== NULL SAFETY TESTS ====================
    
    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {
        
        @Test
        @DisplayName("Should handle null return from repository gracefully")
        void shouldHandleNullReturnFromRepositoryGracefully() {
            // Arrange - Simulate repository returning empty Optional (not found)
            // Service uses findWithUserBySessionId, not findById
            when(workoutSessionRepository.findWithUserBySessionId(1L)).thenReturn(Optional.empty());

            // Act & Assert - Should throw ResourceNotFoundException (correct behavior)
            assertThatThrownBy(() -> workoutSessionService.getWorkoutSessionById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout session not found");

            // NOTE: Service correctly handles missing data by throwing domain exception
            // This is better than letting NullPointerException bubble up
        }
        
        @Test
        @DisplayName("Should handle mapper returning null")
        void shouldHandleMapperReturningNull() {
            // Arrange
            CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
            
            when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
            when(workoutMapper.toEntity(any(CreateWorkoutRequest.class))).thenReturn(null); // Mapper fails
            
            // Act & Assert - Should detect null from mapper
            assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
                .isInstanceOf(NullPointerException.class);
        }
    }
    
}

