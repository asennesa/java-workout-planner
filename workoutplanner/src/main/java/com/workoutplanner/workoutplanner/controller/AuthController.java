package com.workoutplanner.workoutplanner.controller;

import com.workoutplanner.workoutplanner.util.ApiVersionConstants;
import com.workoutplanner.workoutplanner.dto.request.LoginRequest;
import com.workoutplanner.workoutplanner.dto.response.JwtResponse;
import com.workoutplanner.workoutplanner.dto.response.TokenRefreshResponse;
import com.workoutplanner.workoutplanner.dto.response.TokenRevocationResponse;
import com.workoutplanner.workoutplanner.dto.response.TokenValidationResponse;
import com.workoutplanner.workoutplanner.dto.response.UserProfileResponse;
import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.service.JwtService;
import com.workoutplanner.workoutplanner.service.UserService;
import com.workoutplanner.workoutplanner.service.TokenRevocationService;
import com.workoutplanner.workoutplanner.service.RefreshTokenService;
import com.workoutplanner.workoutplanner.util.SecurityConstants;
import com.workoutplanner.workoutplanner.util.ValidationUtils;
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

import java.util.List;

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

            logger.info("User logged in successfully: {}", ValidationUtils.sanitizeForLogging(loginRequest.getUsername()));

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
                       ValidationUtils.sanitizeForLogging(loginRequest.getUsername()), 
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
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam @Size(max = 2000) String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = (User) userService.loadUserByUsername(username);
            
            boolean isValid = jwtService.isTokenValid(token, user);
            
            if (isValid) {
                return ResponseEntity.ok(new TokenValidationResponse(true, username, user.getUserId(), user.getRole().name()));
            }
            
            return ResponseEntity.ok(new TokenValidationResponse(false));
            
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.ok(new TokenValidationResponse(false));
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
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        UserProfileResponse profile = new UserProfileResponse(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name(),
            user.getCreatedAt()
        );
        
        return ResponseEntity.ok(profile);
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
    public ResponseEntity<TokenRevocationResponse> revokeToken(@RequestParam @Size(max = 2000) String token) {
        try {
            boolean revoked = tokenRevocationService.revokeToken(token);
            
            if (revoked) {
                logger.info("Token revoked successfully");
                return ResponseEntity.ok(new TokenRevocationResponse(true, "Token revoked successfully"));
            } else {
                logger.warn("Failed to revoke token");
                return ResponseEntity.ok(new TokenRevocationResponse(false, "Failed to revoke token"));
            }
            
        } catch (Exception e) {
            logger.error("Error revoking token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new TokenRevocationResponse(false, "Token revocation failed"));
        }
    }

    /**
     * Refresh JWT token using refresh token.
     * 
     * @param refreshToken refresh token
     * @return ResponseEntity with new token pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestParam @Size(max = 2000) String refreshToken) {
        try {
            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);
            User user = (User) userService.loadUserByUsername(username);
            
            // Rotate refresh token
            RefreshTokenService.TokenPair tokenPair = refreshTokenService.rotateRefreshToken(
                refreshToken, username, user.getUserId()
            );
            
            TokenRefreshResponse response = new TokenRefreshResponse(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                SecurityConstants.TOKEN_TYPE_BEARER,
                jwtService.getTokenExpirationSeconds()
            );
            
            logger.info("Token refreshed successfully for user: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            // In a real-world scenario, you might want a more specific error DTO
            throw new RuntimeException("Token refresh failed", e);
        }
    }
}

