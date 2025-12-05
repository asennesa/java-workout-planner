package com.workoutplanner.workoutplanner.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for API documentation.
 *
 * Accessible at:
 * - Swagger UI: /swagger-ui.html
 * - OpenAPI JSON: /v3/api-docs
 *
 * @see <a href="https://springdoc.org/">Springdoc OpenAPI</a>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI workoutPlannerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Workout Planner API")
                        .description("REST API for managing workouts, exercises, and fitness tracking. " +
                                "Uses JWT Bearer token authentication via Auth0.")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Auth0 JWT access token")));
    }
}

