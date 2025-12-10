package com.workoutplanner.workoutplanner.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Abstract base class for integration tests using Testcontainers.
 *
 * <h2>Best Practices Implemented:</h2>
 * <ul>
 *   <li><b>ApplicationContextInitializer pattern</b> - Container starts BEFORE Spring context,
 *       avoiding JUnit 5 timing issues with @BeforeAll</li>
 *   <li><b>Singleton container</b> - One container for entire test suite via static initialization</li>
 *   <li><b>Container reuse</b> - Enabled via ~/.testcontainers.properties for faster subsequent runs</li>
 *   <li><b>Spring context caching</b> - Single context shared across all test classes</li>
 * </ul>
 *
 * <h2>Transaction Behavior:</h2>
 * The {@code @Transactional} annotation provides automatic rollback for:
 * <ul>
 *   <li>Repository tests (direct JPA/Hibernate operations)</li>
 *   <li>MockMvc tests (in-process, same transaction)</li>
 * </ul>
 *
 * <b>IMPORTANT:</b> For REST Assured API tests, you MUST:
 * <ol>
 *   <li>Use {@code @Transactional(propagation = Propagation.NOT_SUPPORTED)}</li>
 *   <li>Add manual cleanup in {@code @AfterEach}</li>
 * </ol>
 * HTTP calls execute in separate transactions that won't rollback automatically!
 *
 * @see TestcontainersInitializer
 * @see <a href="https://maciejwalkowiak.com/blog/testcontainers-spring-boot-setup/">Best way to use Testcontainers</a>
 * @see <a href="https://docs.spring.io/spring-boot/reference/testing/testcontainers.html">Spring Boot Testcontainers</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = TestcontainersInitializer.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional // Rollback for repository/JPA tests (NOT for REST Assured!)
public abstract class AbstractIntegrationTest {

    /**
     * Provides access to the shared PostgreSQL container.
     * Useful for tests that need direct container access (e.g., for JDBC URL).
     *
     * @return The shared PostgreSQLContainer instance
     */
    protected static PostgreSQLContainer<?> getPostgresContainer() {
        return TestcontainersInitializer.getPostgresContainer();
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Default content type for requests - no need to repeat in individual tests
        RestAssured.requestSpecification = RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }

    /**
     * Helper method to reset REST Assured for tests that need different configuration.
     * Call this at the start of tests that need to override default settings.
     */
    protected void resetRestAssured() {
        RestAssured.reset();
        setUpRestAssured();
    }
}
