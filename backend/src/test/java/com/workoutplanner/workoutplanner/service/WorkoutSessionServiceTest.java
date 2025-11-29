package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.*;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutExerciseResponse;
import com.workoutplanner.workoutplanner.dto.response.WorkoutResponse;
import com.workoutplanner.workoutplanner.entity.*;
import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.OptimisticLockConflictException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.WorkoutMapper;
import com.workoutplanner.workoutplanner.repository.*;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkoutSessionService.
 * 
 * Industry Best Practices Demonstrated:
 * 1. Use @ExtendWith(MockitoExtension.class) for clean Mockito integration
 * 2. Mock all dependencies (@Mock)
 * 3. Test business logic in isolation
 * 4. Use fixed Clock for deterministic time testing
 * 5. Verify all repository interactions
 * 6. Test exception scenarios
 * 7. Test validation logic
 * 8. Use AssertJ for fluent assertions
 * 
 * Testing Philosophy:
 * - Unit tests focus on service layer business logic
 * - All dependencies are mocked
 * - No database access (fast tests)
 * - Test behavior, not implementation details
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutSessionService Unit Tests")
class WorkoutSessionServiceTest {
    
    @Mock
    private WorkoutSessionRepository workoutSessionRepository;
    
    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ExerciseRepository exerciseRepository;
    
    @Mock
    private WorkoutMapper workoutMapper;
    
    @Mock
    private Clock clock;
    
    @InjectMocks
    private WorkoutSessionService workoutSessionService;
    
    private User testUser;
    private WorkoutSession testWorkoutSession;
    private Exercise testExercise;
    private LocalDateTime fixedNow;
    
    @BeforeEach
    void setUp() {
        // Fixed time for deterministic testing
        fixedNow = LocalDateTime.of(2024, 1, 15, 10, 0);
        Clock fixedClock = Clock.fixed(
            Instant.parse("2024-01-15T10:00:00Z"),
            ZoneId.of("UTC")
        );
        // Use lenient() for shared setup stubs that won't be used in every test
        lenient().when(clock.instant()).thenReturn(fixedClock.instant());
        lenient().when(clock.getZone()).thenReturn(fixedClock.getZone());
        
        // Setup test data
        testUser = TestDataBuilder.createPersistedUser();
        testWorkoutSession = TestDataBuilder.createDefaultWorkoutSession(testUser);
        testExercise = TestDataBuilder.createStrengthExercise(); // Has ID

        // Setup default security context with user ID 1
        TestDataBuilder.setupSecurityContext(1L);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TestDataBuilder.clearSecurityContext();
    }

    // ==================== CREATE WORKOUT TESTS ====================
    
    @Test
    @DisplayName("Should create workout session successfully")
    void shouldCreateWorkoutSessionSuccessfully() {
        // Arrange
        CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
        WorkoutResponse expectedResponse = new WorkoutResponse();
        expectedResponse.setSessionId(1L);
        expectedResponse.setName("Test Workout");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(workoutMapper.toEntity(request)).thenReturn(testWorkoutSession);
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(testWorkoutSession);
        when(workoutMapper.toWorkoutResponse(testWorkoutSession)).thenReturn(expectedResponse);
        
        // Act
        WorkoutResponse result = workoutSessionService.createWorkoutSession(request);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Workout");
        
        // Verify interactions
        verify(userRepository).findById(1L);
        verify(workoutMapper).toEntity(request);
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
        verify(workoutMapper).toWorkoutResponse(testWorkoutSession);
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found during workout creation")
    void shouldThrowExceptionWhenUserNotFoundDuringCreation() {
        // Arrange
        TestDataBuilder.setupSecurityContext(999L);
        CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User")
            .hasMessageContaining("999");
        
        // Verify repository was called but save was not
        verify(userRepository).findById(999L);
        verify(workoutSessionRepository, never()).save(any(WorkoutSession.class));
    }
    
    @Test
    @DisplayName("Should set startedAt when creating IN_PROGRESS workout without startedAt")
    void shouldSetStartedAtWhenCreatingInProgressWorkout() {
        // Arrange
        CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
        request.setStatus(WorkoutStatus.IN_PROGRESS);
        request.setStartedAt(null); // No start time provided
        
        WorkoutSession workoutSession = TestDataBuilder.createDefaultWorkoutSession(testUser);
        workoutSession.setStatus(WorkoutStatus.IN_PROGRESS);
        workoutSession.setStartedAt(null);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(workoutMapper.toEntity(request)).thenReturn(workoutSession);
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(workoutSession);
        when(workoutMapper.toWorkoutResponse(any(WorkoutSession.class))).thenReturn(new WorkoutResponse());
        
        // Act
        workoutSessionService.createWorkoutSession(request);
        
        // Assert - verify startedAt was set
        verify(workoutSessionRepository).save(argThat(ws -> 
            ws.getStartedAt() != null && ws.getStatus() == WorkoutStatus.IN_PROGRESS
        ));
    }
    
    @Test
    @DisplayName("Should throw ValidationException when startedAt is in the future")
    void shouldThrowValidationExceptionWhenStartedAtInFuture() {
        // Arrange
        CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
        request.setStartedAt(fixedNow.plusDays(1)); // Future date
        
        // No need to stub repository - validation happens before repository call
        
        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("cannot start in the future");
    }
    
    @Test
    @DisplayName("Should throw ValidationException when completedAt is before startedAt")
    void shouldThrowValidationExceptionWhenCompletedAtBeforeStartedAt() {
        // Arrange
        CreateWorkoutRequest request = TestDataBuilder.createWorkoutRequest();
        request.setStartedAt(fixedNow);
        request.setCompletedAt(fixedNow.minusHours(1)); // Before start
        
        // No need to stub repository - validation happens before repository call
        
        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.createWorkoutSession(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("cannot be completed before it starts");
    }
    
    // ==================== GET WORKOUT TESTS ====================
    
    @Test
    @DisplayName("Should get workout session by ID successfully")
    void shouldGetWorkoutSessionByIdSuccessfully() {
        // Arrange
        WorkoutResponse expectedResponse = new WorkoutResponse();
        expectedResponse.setSessionId(1L);
        
        when(workoutSessionRepository.findWithUserBySessionId(1L))
            .thenReturn(Optional.of(testWorkoutSession));
        when(workoutMapper.toWorkoutResponse(testWorkoutSession)).thenReturn(expectedResponse);
        
        // Act
        WorkoutResponse result = workoutSessionService.getWorkoutSessionById(1L);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(1L);
        
        verify(workoutSessionRepository).findWithUserBySessionId(1L);
        verify(workoutMapper).toWorkoutResponse(testWorkoutSession);
    }
    
    @Test
    @DisplayName("Should throw ResourceNotFoundException when workout not found")
    void shouldThrowExceptionWhenWorkoutNotFound() {
        // Arrange
        when(workoutSessionRepository.findWithUserBySessionId(999L))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.getWorkoutSessionById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Workout session")
            .hasMessageContaining("999");
    }
    
    @Test
    @DisplayName("Should get workouts by user ID successfully")
    void shouldGetWorkoutsByUserIdSuccessfully() {
        // Arrange
        List<WorkoutSession> workouts = List.of(testWorkoutSession);
        List<WorkoutResponse> expectedResponses = List.of(new WorkoutResponse());
        
        when(workoutSessionRepository.findByUser_UserIdOrderByStartedAtDesc(1L))
            .thenReturn(workouts);
        when(workoutMapper.toWorkoutResponseList(workouts)).thenReturn(expectedResponses);
        
        // Act
        List<WorkoutResponse> result = workoutSessionService.getWorkoutSessionsByUserId(1L);
        
        // Assert
        assertThat(result).hasSize(1);
        verify(workoutSessionRepository).findByUser_UserIdOrderByStartedAtDesc(1L);
    }
    
    @Test
    @DisplayName("Should get all workouts with pagination successfully")
    void shouldGetAllWorkoutsWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<WorkoutSession> workouts = List.of(testWorkoutSession);
        Page<WorkoutSession> workoutPage = new PageImpl<>(workouts, pageable, 1);
        List<WorkoutResponse> responses = List.of(new WorkoutResponse());
        
        when(workoutSessionRepository.findAll(pageable)).thenReturn(workoutPage);
        when(workoutMapper.toWorkoutResponseList(workouts)).thenReturn(responses);
        
        // Act
        PagedResponse<WorkoutResponse> result = workoutSessionService.getAllWorkoutSessions(pageable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
    }
    
    // ==================== UPDATE WORKOUT TESTS ====================
    
    @Test
    @DisplayName("Should update workout session successfully")
    void shouldUpdateWorkoutSessionSuccessfully() {
        // Arrange
        UpdateWorkoutRequest updateRequest = new UpdateWorkoutRequest();
        updateRequest.setName("Updated Workout");
        updateRequest.setStatus(WorkoutStatus.IN_PROGRESS);
        
        WorkoutResponse expectedResponse = new WorkoutResponse();
        expectedResponse.setName("Updated Workout");
        
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(testWorkoutSession));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(testWorkoutSession);
        when(workoutMapper.toWorkoutResponse(testWorkoutSession)).thenReturn(expectedResponse);
        
        // Act
        WorkoutResponse result = workoutSessionService.updateWorkoutSession(1L, updateRequest);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Workout");
        
        verify(workoutMapper).updateEntity(eq(updateRequest), any(WorkoutSession.class));
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
    }
    
    @Test
    @DisplayName("Should throw OptimisticLockConflictException on concurrent update")
    void shouldThrowOptimisticLockConflictExceptionOnConcurrentUpdate() {
        // Arrange
        UpdateWorkoutRequest updateRequest = new UpdateWorkoutRequest();
        updateRequest.setName("Updated Workout");
        updateRequest.setStatus(WorkoutStatus.COMPLETED);
        
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(testWorkoutSession));
        when(workoutSessionRepository.save(any(WorkoutSession.class)))
            .thenThrow(new ObjectOptimisticLockingFailureException("version conflict", new Object()));
        
        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.updateWorkoutSession(1L, updateRequest))
            .isInstanceOf(OptimisticLockConflictException.class)
            .hasMessageContaining("modified by another user");
    }
    
    @Test
    @DisplayName("Should update workout status successfully")
    void shouldUpdateWorkoutStatusSuccessfully() {
        // Arrange
        WorkoutResponse expectedResponse = new WorkoutResponse();
        expectedResponse.setStatus(WorkoutStatus.COMPLETED);
        
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(testWorkoutSession));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(testWorkoutSession);
        when(workoutMapper.toWorkoutResponse(testWorkoutSession)).thenReturn(expectedResponse);
        
        // Act
        WorkoutResponse result = workoutSessionService.updateWorkoutSessionStatus(1L, WorkoutStatus.COMPLETED);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(WorkoutStatus.COMPLETED);
        
        verify(workoutSessionRepository).save(any(WorkoutSession.class));
    }
    
    @Test
    @DisplayName("Should perform start action successfully")
    void shouldPerformStartActionSuccessfully() {
        // Arrange
        WorkoutResponse expectedResponse = new WorkoutResponse();
        expectedResponse.setStatus(WorkoutStatus.IN_PROGRESS);
        
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(testWorkoutSession));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(testWorkoutSession);
        when(workoutMapper.toWorkoutResponse(testWorkoutSession)).thenReturn(expectedResponse);
        
        // Act
        WorkoutResponse result = workoutSessionService.performAction(1L, "start");
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(WorkoutStatus.IN_PROGRESS);
    }
    
    @Test
    @DisplayName("Should throw exception for invalid action")
    void shouldThrowExceptionForInvalidAction() {
        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.performAction(1L, "invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid action");
    }
    
    // ==================== DELETE WORKOUT TESTS ====================
    
    @Test
    @DisplayName("Should soft delete workout session successfully")
    void shouldSoftDeleteWorkoutSessionSuccessfully() {
        // Arrange
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(testWorkoutSession));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(testWorkoutSession);
        
        // Act
        workoutSessionService.deleteWorkoutSession(1L);
        
        // Assert
        verify(workoutSessionRepository).findById(1L);
        verify(workoutSessionRepository).save(argThat(ws -> !ws.isActive()));
    }
    
    @Test
    @DisplayName("Should restore soft deleted workout session successfully")
    void shouldRestoreSoftDeletedWorkoutSuccessfully() {
        // Arrange
        testWorkoutSession.softDelete();
        when(workoutSessionRepository.findByIdIncludingDeleted(1L))
            .thenReturn(Optional.of(testWorkoutSession));
        when(workoutSessionRepository.save(any(WorkoutSession.class))).thenReturn(testWorkoutSession);
        
        // Act
        workoutSessionService.restoreWorkoutSession(1L);
        
        // Assert
        verify(workoutSessionRepository).findByIdIncludingDeleted(1L);
        verify(workoutSessionRepository).save(argThat(WorkoutSession::isActive));
    }
    
    @Test
    @DisplayName("Should throw exception when restoring active workout")
    void shouldThrowExceptionWhenRestoringActiveWorkout() {
        // Arrange
        when(workoutSessionRepository.findByIdIncludingDeleted(1L))
            .thenReturn(Optional.of(testWorkoutSession));
        
        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.restoreWorkoutSession(1L))
            .isInstanceOf(BusinessLogicException.class)
            .hasMessageContaining("not deleted");
    }
    
    // ==================== WORKOUT EXERCISE TESTS ====================
    
    @Test
    @DisplayName("Should add exercise to workout successfully")
    void shouldAddExerciseToWorkoutSuccessfully() {
        // Arrange
        CreateWorkoutExerciseRequest request = TestDataBuilder.createWorkoutExerciseRequest(1L);
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(testWorkoutSession, testExercise);
        WorkoutExerciseResponse expectedResponse = new WorkoutExerciseResponse();
        
        when(workoutSessionRepository.findById(1L)).thenReturn(Optional.of(testWorkoutSession));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(testExercise));
        when(workoutMapper.toWorkoutExerciseEntity(request)).thenReturn(workoutExercise);
        when(workoutExerciseRepository.save(any(WorkoutExercise.class))).thenReturn(workoutExercise);
        when(workoutMapper.toWorkoutExerciseResponse(workoutExercise)).thenReturn(expectedResponse);
        
        // Act
        WorkoutExerciseResponse result = workoutSessionService.addExerciseToWorkout(1L, request);
        
        // Assert
        assertThat(result).isNotNull();
        verify(workoutSessionRepository).findById(1L);
        verify(exerciseRepository).findById(1L);
        verify(workoutExerciseRepository).save(any(WorkoutExercise.class));
    }
    
    @Test
    @DisplayName("Should throw exception when adding exercise to non-existent workout")
    void shouldThrowExceptionWhenAddingExerciseToNonExistentWorkout() {
        // Arrange
        CreateWorkoutExerciseRequest request = TestDataBuilder.createWorkoutExerciseRequest(1L);
        when(workoutSessionRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> workoutSessionService.addExerciseToWorkout(999L, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Workout session");
    }
    
    @Test
    @DisplayName("Should get workout exercises successfully")
    void shouldGetWorkoutExercisesSuccessfully() {
        // Arrange
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(testWorkoutSession, testExercise);
        List<WorkoutExercise> exercises = List.of(workoutExercise);
        List<WorkoutExerciseResponse> responses = List.of(new WorkoutExerciseResponse());
        
        when(workoutExerciseRepository.findByWorkoutSession_SessionIdOrderByOrderInWorkoutAsc(1L))
            .thenReturn(exercises);
        when(workoutMapper.toWorkoutExerciseResponseList(exercises)).thenReturn(responses);
        
        // Act
        List<WorkoutExerciseResponse> result = workoutSessionService.getWorkoutExercises(1L);
        
        // Assert
        assertThat(result).hasSize(1);
        verify(workoutExerciseRepository).findByWorkoutSession_SessionIdOrderByOrderInWorkoutAsc(1L);
    }
    
    @Test
    @DisplayName("Should remove exercise from workout successfully")
    void shouldRemoveExerciseFromWorkoutSuccessfully() {
        // Arrange
        WorkoutExercise workoutExercise = TestDataBuilder.createWorkoutExercise(testWorkoutSession, testExercise);
        when(workoutExerciseRepository.findById(1L)).thenReturn(Optional.of(workoutExercise));
        
        // Act
        workoutSessionService.removeExerciseFromWorkout(1L);
        
        // Assert
        verify(workoutExerciseRepository).findById(1L);
        verify(workoutExerciseRepository).delete(workoutExercise);
    }
}

