package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.PasswordChangeRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.exception.BusinessLogicException;
import com.workoutplanner.workoutplanner.exception.ResourceConflictException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.mapper.UserMapper;
import com.workoutplanner.workoutplanner.repository.UserRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * 
 * Industry Best Practices Demonstrated:
 * 1. Nested test classes for better organization
 * 2. Mock all dependencies
 * 3. Test password security (hashing)
 * 4. Test business logic validation
 * 5. Test exception scenarios
 * 6. Verify repository and encoder interactions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private WorkoutSessionRepository workoutSessionRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private UserResponse testResponse;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createPersistedUser();
        testResponse = new UserResponse();
        testResponse.setUserId(1L);
        testResponse.setUsername("testuser");
        testResponse.setEmail("test@example.com");
        testResponse.setFirstName("Test");
        testResponse.setLastName("User");
    }
    
    // ==================== CREATE USER TESTS ====================
    
    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {
        
        @Test
        @DisplayName("Should create user with encrypted password")
        void shouldCreateUserWithEncryptedPassword() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("newuser");
            request.setPassword("plaintext123");
            request.setEmail("new@example.com");
            request.setFirstName("New");
            request.setLastName("User");
            
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(testUser);
            when(passwordEncoder.encode("plaintext123")).thenReturn("$2a$10$encrypted");
            when(userRepository.save(any(User.class))).thenAnswer(i -> {
                User user = i.getArgument(0);
                user.setUserId(1L);
                return user;
            });
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);
            
            // Act
            UserResponse result = userService.createUser(request);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            
            // Verify password was encrypted
            verify(passwordEncoder).encode("plaintext123");
            // Note: createUser saves twice - once to generate ID, once to set audit fields
            verify(userRepository, times(2)).save(any(User.class));
        }
        
        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("existing");
            request.setEmail("new@example.com");
            
            when(userRepository.existsByUsername("existing")).thenReturn(true);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("username");
            
            verify(userRepository).existsByUsername("existing");
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("newuser");
            request.setEmail("existing@example.com");
            
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("email");
            
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("Should set default role to USER")
        void shouldSetDefaultRoleToUser() {
            // Arrange
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("newuser");
            request.setPassword("password123");
            request.setEmail("new@example.com");
            request.setFirstName("New");
            request.setLastName("User");
            
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(testUser);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encrypted");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);
            
            // Act
            userService.createUser(request);
            
            // Assert
            // Note: createUser saves twice - once to generate ID, once to set audit fields
            verify(userRepository, times(2)).save(any(User.class));
            verify(userRepository, atLeastOnce()).save(argThat(user -> 
                user.getRole() == UserRole.USER
            ));
        }
    }
    
    // ==================== GET USER TESTS ====================
    
    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {
        
        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testResponse);
            
            // Act
            UserResponse result = userService.getUserById(1L);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            verify(userRepository).findById(1L);
        }
        
        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void shouldThrowExceptionWhenUserNotFoundById() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
        }
        
        @Test
        @DisplayName("Should get user by username successfully")
        void shouldGetUserByUsernameSuccessfully() {
            // Arrange
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testResponse);
            
            // Act
            UserResponse result = userService.getUserByUsername("testuser");
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
        }
        
        @Test
        @DisplayName("Should get user by email successfully")
        void shouldGetUserByEmailSuccessfully() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testResponse);
            
            // Act
            UserResponse result = userService.getUserByEmail("test@example.com");
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }
        
        @Test
        @DisplayName("Should get all users with pagination")
        void shouldGetAllUsersWithPagination() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            List<User> users = List.of(testUser);
            Page<User> userPage = new PageImpl<>(users, pageable, 1);
            List<UserResponse> responses = List.of(testResponse);
            
            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(userMapper.toResponseList(users)).thenReturn(responses);
            
            // Act
            PagedResponse<UserResponse> result = userService.getAllUsers(pageable);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }
    
    // ==================== UPDATE USER TESTS ====================
    
    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {
        
        @Test
        @DisplayName("Should update basic user info without password")
        void shouldUpdateBasicUserInfo() {
            // Arrange
            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");
            request.setLastName("Name");
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toResponse(testUser)).thenReturn(testResponse);
            
            // Act
            UserResponse result = userService.updateUserBasic(1L, request);
            
            // Assert
            assertThat(result).isNotNull();
            verify(userRepository).save(argThat(user -> 
                user.getFirstName().equals("Updated") && 
                user.getLastName().equals("Name")
            ));
        }
        
        @Test
        @DisplayName("Should update email with password verification")
        void shouldUpdateEmailWithPasswordVerification() {
            // Arrange
            UserUpdateRequest request = new UserUpdateRequest();
            request.setEmail("newemail@example.com");
            request.setCurrentPassword("correctpassword");
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("correctpassword", testUser.getPasswordHash()))
                .thenReturn(true);
            when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(userMapper.toResponse(testUser)).thenReturn(testResponse);
            
            // Act
            UserResponse result = userService.updateUserProfileSecurely(1L, request);
            
            // Assert
            assertThat(result).isNotNull();
            verify(passwordEncoder).matches("correctpassword", testUser.getPasswordHash());
            verify(userRepository).save(argThat(user -> 
                user.getEmail().equals("newemail@example.com")
            ));
        }
        
        @Test
        @DisplayName("Should throw exception when updating email with wrong password")
        void shouldThrowExceptionWhenUpdatingEmailWithWrongPassword() {
            // Arrange
            UserUpdateRequest request = new UserUpdateRequest();
            request.setEmail("newemail@example.com");
            request.setCurrentPassword("wrongpassword");
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash()))
                .thenReturn(false);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.updateUserProfileSecurely(1L, request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("incorrect");
            
            verify(userRepository, never()).save(any(User.class));
        }
    }
    
    // ==================== PASSWORD CHANGE TESTS ====================
    
    @Nested
    @DisplayName("Password Change Tests")
    class PasswordChangeTests {
        
        @Test
        @DisplayName("Should change password with correct current password")
        void shouldChangePasswordWithCorrectCurrentPassword() {
            // Arrange
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("oldpassword");
            request.setNewPassword("newpassword123");
            request.setConfirmPassword("newpassword123");
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldpassword", testUser.getPasswordHash()))
                .thenReturn(true);
            when(passwordEncoder.encode("newpassword123"))
                .thenReturn("$2a$10$newencrypted");
            when(userRepository.save(testUser)).thenReturn(testUser);
            
            // Act
            userService.changePassword(1L, request);
            
            // Assert
            verify(passwordEncoder).encode("newpassword123");
            verify(userRepository).save(argThat(user -> 
                user.getPasswordHash().equals("$2a$10$newencrypted")
            ));
        }
        
        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
            // Arrange
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("wrongpassword");
            request.setNewPassword("newpassword123");
            request.setConfirmPassword("newpassword123");
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash()))
                .thenReturn(false);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("incorrect");
            
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("Should throw exception when passwords don't match")
        void shouldThrowExceptionWhenPasswordsDontMatch() {
            // Arrange
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("oldpassword");
            request.setNewPassword("newpassword123");
            request.setConfirmPassword("different123");
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldpassword", testUser.getPasswordHash()))
                .thenReturn(true);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("do not match");
            
            verify(userRepository, never()).save(any(User.class));
        }
    }
    
    // ==================== DELETE USER TESTS ====================
    
    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {
        
        @Test
        @DisplayName("Should soft delete user when no workout sessions exist")
        void shouldSoftDeleteUserWhenNoWorkoutSessions() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(workoutSessionRepository.existsByUserId(1L)).thenReturn(false);
            when(userRepository.save(testUser)).thenReturn(testUser);
            
            // Act
            userService.deleteUser(1L);
            
            // Assert
            verify(userRepository).save(argThat(user -> !user.isActive()));
        }
        
        @Test
        @DisplayName("Should throw exception when deleting user with workout sessions")
        void shouldThrowExceptionWhenDeletingUserWithWorkoutSessions() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(workoutSessionRepository.existsByUserId(1L)).thenReturn(true);
            
            // Act & Assert
            assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("workout");
            
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("Should restore soft deleted user")
        void shouldRestoreSoftDeletedUser() {
            // Arrange
            testUser.softDelete();
            when(userRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(testUser)).thenReturn(testUser);
            
            // Act
            userService.restoreUser(1L);
            
            // Assert
            verify(userRepository).save(argThat(User::isActive));
        }
        
        @Test
        @DisplayName("Should throw exception when restoring active user")
        void shouldThrowExceptionWhenRestoringActiveUser() {
            // Arrange
            when(userRepository.findByIdIncludingDeleted(1L)).thenReturn(Optional.of(testUser));
            
            // Act & Assert
            assertThatThrownBy(() -> userService.restoreUser(1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("not deleted");
        }
    }
    
    // ==================== VALIDATION TESTS ====================
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should check if username exists")
        void shouldCheckIfUsernameExists() {
            // Arrange
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            
            // Act
            boolean exists = userService.usernameExists("testuser");
            
            // Assert
            assertThat(exists).isTrue();
        }
        
        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            // Arrange
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
            
            // Act
            boolean exists = userService.emailExists("test@example.com");
            
            // Assert
            assertThat(exists).isTrue();
        }
    }
}

