package com.workoutplanner.workoutplanner.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Configures a shared PostgreSQL Testcontainer for integration tests.
 *
 * Uses ApplicationContextInitializer pattern (recommended over @DynamicPropertySource)
 * to ensure the container starts BEFORE Spring context initialization.
 *
 * @see <a href="https://maciejwalkowiak.com/blog/testcontainers-spring-boot-setup/">Best way to use Testcontainers</a>
 */
public class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("workout_planner_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        POSTGRES.start();
    }

    public static PostgreSQLContainer<?> getPostgresContainer() {
        return POSTGRES;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                "spring.datasource.username=" + POSTGRES.getUsername(),
                "spring.datasource.password=" + POSTGRES.getPassword(),
                "spring.datasource.driver-class-name=org.postgresql.Driver"
        ).applyTo(applicationContext.getEnvironment());
    }
}
