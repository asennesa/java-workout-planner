package com.workoutplanner.workoutplanner.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for API documentation.
 * 
 * This configuration provides comprehensive API documentation accessible at:
 * - Swagger UI: http://localhost:8081/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8081/v3/api-docs
 * 
 * Features:
 * - Interactive API documentation
 * - JWT Bearer token authentication (Auth0)
 * - API versioning information
 * - Request/response examples
 * - Model schemas
 * 
 * @see <a href="https://springdoc.org/">Springdoc OpenAPI Documentation</a>
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    /**
     * Configures OpenAPI documentation with API information and security schemes.
     * 
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI workoutPlannerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Workout Planner API")
                        .description("""
                                A comprehensive REST API for managing workout routines, exercises, and fitness tracking.
                                
                                ## Features
                                - User authentication and authorization
                                - Exercise library management
                                - Workout session tracking
                                - Support for Strength, Cardio, and Flexibility exercises
                                - Role-based access control (USER, ADMIN, MODERATOR)
                                - Rate limiting for API protection
                                
                                ## Authentication
                                This API uses JWT Bearer token authentication via Auth0. Include your access token in the Authorization header:
                                ```
                                Authorization: Bearer <your-access-token>
                                ```

                                Obtain your access token from Auth0 by authenticating with your Auth0 tenant.
                                
                                ## Rate Limiting
                                API endpoints are rate-limited to prevent abuse. Limits vary by endpoint.
                                """)
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Workout Planner Team")
                                .email("support@workoutplanner.com")
                                .url("https://github.com/your-repo/workout-planner"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.workoutplanner.com")
                                .description("Production Server")))
                // Note: Security is applied per-endpoint using @SecurityRequirement annotations
                // Auth0 handles authentication; this API validates JWT tokens
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer token authentication via Auth0. Provide your access token.")));
    }
}

