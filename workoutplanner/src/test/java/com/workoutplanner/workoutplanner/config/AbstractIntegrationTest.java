package com.workoutplanner.workoutplanner.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Abstract base class for integration tests using Testcontainers.
 * 
 * Industry Best Practices:
 * - Single PostgreSQL container shared across all integration tests (fast)
 * - @Transactional for repository/JPA tests (automatic rollback)
 * - REST Assured configured for API testing
 * - Provides MockMvc for controller testing
 * 
 * IMPORTANT: @Transactional annotation provides automatic rollback for:
 * - Repository tests (direct JPA/Hibernate operations)
 * - MockMvc tests (in-process, same transaction)
 * 
 * However, for REST Assured API tests, you MUST add @AfterEach cleanup methods
 * because HTTP calls execute in separate transactions that won't rollback!
 * 
 * Usage: 
 * - Repository tests: Extend this class (auto cleanup via @Transactional)
 * - API tests: Extend this class AND add @AfterEach cleanup methods
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.test.context.cache.maxSize=1" // Optimize test performance
    }
)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional // Rollback for repository/JPA tests (NOT for REST Assured!)
public abstract class AbstractIntegrationTest {
    
    /**
     * Shared PostgreSQL container for all integration tests.
     * Using a single static container improves test performance significantly.
     * 
     * Note: The @Container annotation manages the lifecycle, so the resource leak warning
     * can be safely suppressed. Testcontainers handles proper cleanup.
     */
    @Container
    @SuppressWarnings("resource") // Managed by @Container annotation
    protected static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("workout_planner_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true); // Reuse container across test runs
    
    @LocalServerPort
    protected int port;
    
    @Autowired
    protected MockMvc mockMvc;
    
    /**
     * Dynamically configure datasource properties from Testcontainers.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }
    
    /**
     * Get the base path for REST Assured API calls.
     * Override this method in subclasses to test different API versions.
     * 
     * @return base path for API endpoints (default: "/api/v1")
     */
    protected String getApiBasePath() {
        return "/api/v1";
    }
    
    /**
     * Configure REST Assured before each test.
     * Uses the base path from getApiBasePath() for flexibility.
     */
    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = getApiBasePath(); // Configurable base path
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Default content type for requests
        RestAssured.requestSpecification = RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }
}

