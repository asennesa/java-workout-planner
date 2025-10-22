package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.LoginRequest;
import com.workoutplanner.workoutplanner.dto.response.JwtResponse;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.service.JwtService;
import com.workoutplanner.workoutplanner.service.UserService;
import com.workoutplanner.workoutplanner.service.TokenRevocationService;
import com.workoutplanner.workoutplanner.service.RefreshTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations.
 * Handles user login and JWT token generation.
 */
@RestController
@RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenRevocationService tokenRevocationService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                         JwtService jwtService,
                         UserService userService,
                         TokenRevocationService tokenRevocationService,
                         RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.tokenRevocationService = tokenRevocationService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authenticate user and return JWT token.
     *
     * @param loginRequest the login credentials
     * @param request HTTP request for IP tracking
     * @return ResponseEntity containing JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest, 
                                           HttpServletRequest request) {
        logger.info("Login attempt for username: {}", loginRequest.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Get user details
            User user = (User) authentication.getPrincipal();
            
            // Generate JWT token
            String jwt = jwtService.generateToken(user);

            logger.info("User logged in successfully: {}", sanitizeUsername(loginRequest.getUsername()));

            // Create response
            JwtResponse response = new JwtResponse(
                    jwt,
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log security event without exposing sensitive information
            logger.warn("Authentication failed for user: {} from IP: {}", 
                       sanitizeUsername(loginRequest.getUsername()), 
                       getClientIpAddress(request));
            throw new UsernameNotFoundException("Invalid username or password");
        }
    }

    /**
     * Validate JWT token.
     *
     * @param token JWT token
     * @return ResponseEntity with validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam @Size(max = 2000) String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = (User) userService.loadUserByUsername(username);
            
            boolean isValid = jwtService.isTokenValid(token, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            if (isValid) {
                response.put("username", username);
                response.put("userId", user.getUserId());
                response.put("role", user.getRole().name());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get current user profile.
     * Requires valid JWT token.
     *
     * @param authentication current authentication
     * @return ResponseEntity containing user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("role", user.getRole().name());
        profile.put("createdAt", user.getCreatedAt());
        
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Sanitize username for logging to prevent log injection.
     * 
     * @param username Username to sanitize
     * @return Sanitized username
     */
    private String sanitizeUsername(String username) {
        if (username == null) {
            return "null";
        }
        // Remove potentially dangerous characters
        return username.replaceAll("[\\r\\n\\t]", "_");
    }
    
    /**
     * Get client IP address from request.
     * 
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Revoke JWT token (logout).
     * 
     * @param token JWT token to revoke
     * @return ResponseEntity with revocation result
     */
    @PostMapping("/revoke")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> revokeToken(@RequestParam @Size(max = 2000) String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean revoked = tokenRevocationService.revokeToken(token);
            
            if (revoked) {
                response.put("success", true);
                response.put("message", "Token revoked successfully");
                logger.info("Token revoked successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to revoke token");
                logger.warn("Failed to revoke token");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error revoking token: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Token revocation failed");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Refresh JWT token using refresh token.
     * 
     * @param refreshToken refresh token
     * @return ResponseEntity with new token pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestParam @Size(max = 2000) String refreshToken) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);
            User user = (User) userService.loadUserByUsername(username);
            
            // Rotate refresh token
            RefreshTokenService.TokenPair tokenPair = refreshTokenService.rotateRefreshToken(
                refreshToken, username, user.getUserId()
            );
            
            response.put("access_token", tokenPair.getAccessToken());
            response.put("refresh_token", tokenPair.getRefreshToken());
            response.put("token_type", "Bearer");
            response.put("expires_in", 900); // 15 minutes
            
            logger.info("Token refreshed successfully for user: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            response.put("error", "Token refresh failed");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

