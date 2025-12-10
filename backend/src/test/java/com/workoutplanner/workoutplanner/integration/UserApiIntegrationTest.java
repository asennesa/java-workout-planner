package com.workoutplanner.workoutplanner.integration;

import com.workoutplanner.workoutplanner.config.AbstractIntegrationTest;
import com.workoutplanner.workoutplanner.config.TestSecurityConfig;
import com.workoutplanner.workoutplanner.dto.request.CreateUserRequest;
import com.workoutplanner.workoutplanner.dto.request.UserUpdateRequest;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * API Integration tests for User endpoints.
 *
 * Tests the complete HTTP request-response cycle for:
 * - User creation
 * - User retrieval (by ID, current user profile)
 * - User updates
 * - User deletion
 * - Username/email existence checks
 * - Search functionality
 *
 * Uses REST Assured for HTTP testing with real PostgreSQL (Testcontainers).
 *
 * @see <a href="https://rest-assured.io/">REST Assured Documentation</a>
 */
@DisplayName("User API Integration Tests")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class UserApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createNewUser();
        testUser.setRole(UserRole.USER);
        testUser = userRepository.saveAndFlush(testUser);
        TestSecurityConfig.TestAuthFilter.setTestUserId(testUser.getUserId());
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/v1/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user and return 201")
        void shouldCreateUserAndReturn201() {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("newuser");
            request.setEmail("newuser@example.com");
            request.setFirstName("New");
            request.setLastName("User");

            given()
                .body(request)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .body("userId", notNullValue())
                .body("username", equalTo("newuser"))
                .body("email", equalTo("newuser@example.com"))
                .body("firstName", equalTo("New"))
                .body("lastName", equalTo("User"));
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() {
            CreateUserRequest request = new CreateUserRequest();
            // Missing all required fields

            given()
                .body(request)
            .when()
                .post("/users")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should return 409 for duplicate username")
        void shouldReturn409ForDuplicateUsername() {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername(testUser.getUsername()); // Duplicate
            request.setEmail("unique@example.com");
            request.setFirstName("Test");
            request.setLastName("User");

            given()
                .body(request)
            .when()
                .post("/users")
            .then()
                .statusCode(409);  // Conflict - resource already exists
        }

        @Test
        @DisplayName("Should return 409 for duplicate email")
        void shouldReturn409ForDuplicateEmail() {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("uniqueuser");
            request.setEmail(testUser.getEmail()); // Duplicate
            request.setFirstName("Test");
            request.setLastName("User");

            given()
                .body(request)
            .when()
                .post("/users")
            .then()
                .statusCode(409);  // Conflict - resource already exists
        }

        @Test
        @DisplayName("Should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmailFormat() {
            CreateUserRequest request = new CreateUserRequest();
            request.setUsername("validuser");
            request.setEmail("invalid-email"); // Invalid format
            request.setFirstName("Test");
            request.setLastName("User");

            given()
                .body(request)
            .when()
                .post("/users")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId} - Get User By ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user by ID")
        void shouldReturnUserById() {
            given()
            .when()
                .get("/users/" + testUser.getUserId())
            .then()
                .statusCode(200)
                .body("userId", equalTo(testUser.getUserId().intValue()))
                .body("username", equalTo(testUser.getUsername()))
                .body("email", equalTo(testUser.getEmail()));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            given()
            .when()
                .get("/users/99999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/me - Get Current User Profile")
    class GetCurrentUserProfileTests {

        @Test
        @DisplayName("Should return current user profile")
        void shouldReturnCurrentUserProfile() {
            given()
            .when()
                .get("/users/me")
            .then()
                .statusCode(200)
                .body("userId", equalTo(testUser.getUserId().intValue()))
                .body("username", equalTo(testUser.getUsername()));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users - Get All Users (Paginated)")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return paginated users")
        void shouldReturnPaginatedUsers() {
            // Create additional users
            for (int i = 0; i < 3; i++) {
                User user = TestDataBuilder.createNewUser();
                userRepository.saveAndFlush(user);
            }

            given()
                .queryParam("page", 0)
                .queryParam("size", 10)
            .when()
                .get("/users")
            .then()
                .statusCode(200)
                .body("content", hasSize(greaterThanOrEqualTo(4)))
                .body("totalElements", greaterThanOrEqualTo(4))
                .body("pageNumber", equalTo(0));
        }

        @Test
        @DisplayName("Should respect page size")
        void shouldRespectPageSize() {
            // Create additional users
            for (int i = 0; i < 5; i++) {
                User user = TestDataBuilder.createNewUser();
                userRepository.saveAndFlush(user);
            }

            given()
                .queryParam("page", 0)
                .queryParam("size", 2)
            .when()
                .get("/users")
            .then()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("totalPages", greaterThanOrEqualTo(3));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{userId} - Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user and return 200")
        void shouldUpdateUserAndReturn200() {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("UpdatedFirst");
            request.setLastName("UpdatedLast");

            given()
                .body(request)
            .when()
                .put("/users/" + testUser.getUserId())
            .then()
                .statusCode(200)
                .body("firstName", equalTo("UpdatedFirst"))
                .body("lastName", equalTo("UpdatedLast"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent user")
        void shouldReturn404WhenUpdatingNonExistentUser() {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setFirstName("Test");
            request.setLastName("User");

            given()
                .body(request)
            .when()
                .put("/users/99999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{userId} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user and return 204")
        void shouldDeleteUserAndReturn204() {
            // Create a user to delete
            User userToDelete = TestDataBuilder.createNewUser();
            userToDelete = userRepository.saveAndFlush(userToDelete);

            given()
            .when()
                .delete("/users/" + userToDelete.getUserId())
            .then()
                .statusCode(204);

            // Verify user is deleted
            given()
            .when()
                .get("/users/" + userToDelete.getUserId())
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent user")
        void shouldReturn404WhenDeletingNonExistentUser() {
            given()
            .when()
                .delete("/users/99999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/check-username - Check Username Exists")
    class CheckUsernameExistsTests {

        @Test
        @DisplayName("Should return true for existing username")
        void shouldReturnTrueForExistingUsername() {
            given()
                .queryParam("username", testUser.getUsername())
            .when()
                .get("/users/check-username")
            .then()
                .statusCode(200)
                .body("exists", equalTo(true));
        }

        @Test
        @DisplayName("Should return false for non-existing username")
        void shouldReturnFalseForNonExistingUsername() {
            given()
                .queryParam("username", "nonexistentuser123")
            .when()
                .get("/users/check-username")
            .then()
                .statusCode(200)
                .body("exists", equalTo(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/check-email - Check Email Exists")
    class CheckEmailExistsTests {

        @Test
        @DisplayName("Should return true for existing email")
        void shouldReturnTrueForExistingEmail() {
            given()
                .queryParam("email", testUser.getEmail())
            .when()
                .get("/users/check-email")
            .then()
                .statusCode(200)
                .body("exists", equalTo(true));
        }

        @Test
        @DisplayName("Should return false for non-existing email")
        void shouldReturnFalseForNonExistingEmail() {
            given()
                .queryParam("email", "nonexistent@example.com")
            .when()
                .get("/users/check-email")
            .then()
                .statusCode(200)
                .body("exists", equalTo(false));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/search - Search Users")
    class SearchUsersTests {

        @Test
        @DisplayName("Should find users by first name")
        void shouldFindUsersByFirstName() {
            // Create users with specific first names
            User user1 = TestDataBuilder.createNewUser();
            user1.setFirstName("SearchableJohn");
            userRepository.saveAndFlush(user1);

            User user2 = TestDataBuilder.createNewUser();
            user2.setFirstName("SearchableJohnny");
            userRepository.saveAndFlush(user2);

            given()
                .queryParam("firstName", "SearchableJohn")
            .when()
                .get("/users/search")
            .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(2)));
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            given()
                .queryParam("firstName", "NonExistentName12345")
            .when()
                .get("/users/search")
            .then()
                .statusCode(200)
                .body("$", hasSize(0));
        }
    }
}
