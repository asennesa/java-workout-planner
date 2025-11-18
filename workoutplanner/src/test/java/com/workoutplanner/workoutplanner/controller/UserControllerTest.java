package com.workoutplanner.workoutplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.PasswordChangeRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.dto.response.PagedResponse;
import com.workoutplanner.workoutplanner.dto.response.UserResponse;
import com.workoutplanner.workoutplanner.exception.ResourceConflictException;
import com.workoutplanner.workoutplanner.exception.ResourceNotFoundException;
import com.workoutplanner.workoutplanner.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController using MockMvc.
 * 
 * Industry Best Practices:
 * - Use @WebMvcTest for controller layer testing
 * - Mock service layer with @MockitoBean
 * - Test HTTP endpoints, status codes, and validation
 * - Use nested classes for better organization
 * - Test security annotations
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@DisplayName("UserController Unit Tests")
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private UserService userService;
    
    // ==================== CREATE USER TESTS ====================
    
    @Nested
    @DisplayName("POST /api/v1/users - Create User")
    class CreateUserTests {
        
        @Test
        @DisplayName("Should create user and return 201")
        void shouldCreateUserAndReturn201() throws Exception {
            // Arrange
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("newuser");
            request.setPassword("Password123!");
            request.setEmail("new@example.com");
            request.setFirstName("New");
            request.setLastName("User");
            
            UserResponse response = new UserResponse();
            response.setUserId(1L);
            response.setUsername("newuser");
            response.setEmail("new@example.com");
            
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
            
            verify(userService).createUser(any(CreateUserRequest.class));
        }
        
        @Test
        @DisplayName("Should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // Arrange - Missing required fields
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("u"); // Too short
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }
        
        @Test
        @DisplayName("Should return 409 when username exists")
        void shouldReturn409WhenUsernameExists() throws Exception {
            // Arrange
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("existing");
            request.setPassword("Password123!");
            request.setEmail("new@example.com");
            request.setFirstName("New");
            request.setLastName("User");
            
            when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new ResourceConflictException("User", "username", "existing"));
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
        }
    }
    
    // ==================== GET USER TESTS ====================
    
    @Nested
    @DisplayName("GET /api/v1/users - Get Users")
    class GetUserTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should get user by ID")
        void shouldGetUserById() throws Exception {
            // Arrange
            UserResponse response = new UserResponse();
            response.setUserId(1L);
            response.setUsername("testuser");
            
            when(userService.getUserById(1L)).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
            
            verify(userService).getUserById(1L);
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Arrange
            when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("User", "ID", 999L));
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should get all users with pagination")
        void shouldGetAllUsersWithPagination() throws Exception {
            // Arrange
            UserResponse user1 = new UserResponse();
            user1.setUserId(1L);
            user1.setUsername("user1");
            
            UserResponse user2 = new UserResponse();
            user2.setUserId(2L);
            user2.setUsername("user2");
            
            PagedResponse<UserResponse> pagedResponse = new PagedResponse<>(
                List.of(user1, user2),
                0, 20, 2, 1
            );
            
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(pagedResponse);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/users")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
            
            verify(userService).getAllUsers(any(Pageable.class));
        }
    }
    
    // ==================== UPDATE USER TESTS ====================
    
    @Nested
    @DisplayName("PUT /api/v1/users/{id} - Update User")
    class UpdateUserTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should update user profile")
        void shouldUpdateUserProfile() throws Exception {
            // Arrange
            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Updated");
            request.setLastName("Name");
            
            UserResponse response = new UserResponse();
            response.setUserId(1L);
            response.setFirstName("Updated");
            response.setLastName("Name");
            
            when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(response);
            
            // Act & Assert
            mockMvc.perform(put("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
            
            verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
        }
    }
    
    // ==================== PASSWORD CHANGE TESTS ====================
    
    @Nested
    @DisplayName("POST /api/v1/users/{id}/password - Change Password")
    class PasswordChangeTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() throws Exception {
            // Arrange
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("OldPassword123!");
            request.setNewPassword("NewPassword123!");
            request.setConfirmPassword("NewPassword123!");
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/users/1/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
            
            verify(userService).changePassword(eq(1L), any(PasswordChangeRequest.class));
        }
        
        @Test
        @WithMockUser
        @DisplayName("Should return 400 for invalid password request")
        void shouldReturn400ForInvalidPasswordRequest() throws Exception {
            // Arrange - Empty passwords
            PasswordChangeRequest request = new PasswordChangeRequest();
            
            // Act & Assert
            mockMvc.perform(post("/api/v1/users/1/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }
    }
    
    // ==================== DELETE USER TESTS ====================
    
    @Nested
    @DisplayName("DELETE /api/v1/users/{id} - Delete User")
    class DeleteUserTests {
        
        @Test
        @WithMockUser
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
            
            verify(userService).deleteUser(1L);
        }
    }
    
    // ==================== VALIDATION TESTS ====================
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should check if username exists")
        void shouldCheckIfUsernameExists() throws Exception {
            // Arrange
            when(userService.usernameExists("testuser")).thenReturn(true);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/users/check-username")
                    .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
            
            verify(userService).usernameExists("testuser");
        }
        
        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() throws Exception {
            // Arrange
            when(userService.emailExists("test@example.com")).thenReturn(true);
            
            // Act & Assert
            mockMvc.perform(get("/api/v1/users/check-email")
                    .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
            
            verify(userService).emailExists("test@example.com");
        }
    }
}

