package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.LoginRequest;
import com.workoutplanner.workoutplanner.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations.
 * Handles user login with basic session authentication.
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and session management")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Authenticate user and establish session.
     *
     * @param loginRequest the login credentials
     * @param request HTTP request for IP tracking
     * @return ResponseEntity containing user details
     */
    @Operation(
        summary = "User login",
        description = "Authenticates a user with username/email and password, establishing a session. Returns user details upon successful authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful - user authenticated",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"Login successful\", \"userId\": 1, \"username\": \"johndoe\", \"email\": \"john@example.com\", \"firstName\": \"John\", \"lastName\": \"Doe\", \"role\": \"USER\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed - invalid credentials",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"success\": false, \"message\": \"Invalid username or password\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation errors",
            content = @Content
        )
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "Login credentials (username/email and password)", required = true)
            @Valid @RequestBody LoginRequest loginRequest, 
            HttpServletRequest request) {
        logger.debug("Login attempt for username: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            logger.info("User logged in successfully. userId={}, username={}", 
                       user.getUserId(), user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("role", user.getRole().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.warn("Authentication failed for username: {}", loginRequest.getUsername());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid username or password");
            
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    /**
     * Logout current user.
     * 
     * Note: For user profile, use GET /api/v1/users/me instead.
     * This follows REST best practices by keeping profile management under /users resource.
     * 
     * @return ResponseEntity with logout result
     */
    @Operation(
        summary = "User logout",
        description = "Logs out the currently authenticated user and clears the security context.",
        security = @SecurityRequirement(name = "basicAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"Logout successful\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - user not authenticated",
            content = @Content
        )
    })
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> logout() {
        SecurityContextHolder.clearContext();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        
        return ResponseEntity.ok(response);
    }
}
