package com.workoutplanner.workoutplanner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for refresh token rotation and management.
 * Implements secure refresh token rotation to prevent token reuse attacks.
 * 
 * This service follows OAuth2 security best practices:
 * - Refresh token rotation on each use
 * - Secure token storage and validation
 * - Comprehensive logging and monitoring
 */
@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private int jwtRefreshExpirationMs;

    // Store active refresh tokens (use Redis in production)
    private final ConcurrentHashMap<String, RefreshTokenInfo> activeRefreshTokens = new ConcurrentHashMap<>();

    public RefreshTokenService(JwtService jwtService, TokenRevocationService tokenRevocationService) {
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
    }

    /**
     * Rotate refresh token - generate new access and refresh tokens.
     * 
     * @param oldRefreshToken current refresh token
     * @param username username
     * @param userId user ID
     * @return new token pair
     */
    public TokenPair rotateRefreshToken(String oldRefreshToken, String username, Long userId) {
        try {
            // Validate the old refresh token
            if (!isValidRefreshToken(oldRefreshToken)) {
                logger.warn("Invalid refresh token provided for user: {}", username);
                throw new IllegalArgumentException("Invalid refresh token");
            }

            // Revoke the old refresh token
            tokenRevocationService.revokeToken(oldRefreshToken);
            removeRefreshToken(oldRefreshToken);

            // Generate new tokens
            String newAccessToken = jwtService.generateToken(username, userId, "USER");
            String newRefreshToken = jwtService.generateRefreshToken(username, userId);

            // Store the new refresh token
            storeRefreshToken(newRefreshToken, username, userId);

            logger.info("Refresh token rotated successfully for user: {}", username);

            return new TokenPair(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            logger.error("Failed to rotate refresh token for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Token rotation failed", e);
        }
    }

    /**
     * Validate refresh token.
     * 
     * @param refreshToken refresh token to validate
     * @return true if token is valid
     */
    public boolean isValidRefreshToken(String refreshToken) {
        try {
            // Check if token is revoked
            if (tokenRevocationService.isTokenRevoked(refreshToken)) {
                logger.debug("Refresh token is revoked");
                return false;
            }

            // Check if token exists in active tokens
            RefreshTokenInfo tokenInfo = activeRefreshTokens.get(refreshToken);
            if (tokenInfo == null) {
                logger.debug("Refresh token not found in active tokens");
                return false;
            }

            // Check if token is expired
            if (tokenInfo.getExpiresAt().before(new Date())) {
                logger.debug("Refresh token is expired");
                removeRefreshToken(refreshToken);
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Error validating refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Store refresh token information.
     * 
     * @param refreshToken refresh token
     * @param username username
     * @param userId user ID
     */
    private void storeRefreshToken(String refreshToken, String username, Long userId) {
        Date expiresAt = new Date(System.currentTimeMillis() + jwtRefreshExpirationMs);
        RefreshTokenInfo tokenInfo = new RefreshTokenInfo(username, expiresAt);
        activeRefreshTokens.put(refreshToken, tokenInfo);
        
        logger.debug("Stored refresh token for user: {}", username);
    }

    /**
     * Remove refresh token from active tokens.
     * 
     * @param refreshToken refresh token to remove
     */
    private void removeRefreshToken(String refreshToken) {
        activeRefreshTokens.remove(refreshToken);
        logger.debug("Removed refresh token from active tokens");
    }

    /**
     * Revoke all refresh tokens for a user.
     * 
     * @param username username to revoke tokens for
     * @return number of tokens revoked
     */
    public int revokeAllUserRefreshTokens(String username) {
        final int[] revokedCount = {0};
        
        activeRefreshTokens.entrySet().removeIf(entry -> {
            if (entry.getValue().getUsername().equals(username)) {
                tokenRevocationService.revokeToken(entry.getKey());
                revokedCount[0]++;
                return true;
            }
            return false;
        });
        
        logger.info("Revoked all refresh tokens for user: {}", username);
        return revokedCount[0];
    }

    /**
     * Clean up expired refresh tokens.
     */
    public void cleanupExpiredTokens() {
        Date now = new Date();
        final int[] removedCount = {0};
        
        activeRefreshTokens.entrySet().removeIf(entry -> {
            if (entry.getValue().getExpiresAt().before(now)) {
                removedCount[0]++;
                return true;
            }
            return false;
        });
        
        if (removedCount[0] > 0) {
            logger.debug("Cleaned up {} expired refresh tokens", removedCount[0]);
        }
    }

    /**
     * Get the number of active refresh tokens.
     * 
     * @return number of active refresh tokens
     */
    public int getActiveTokenCount() {
        return activeRefreshTokens.size();
    }

    /**
     * Token pair containing access and refresh tokens.
     */
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    /**
     * Refresh token information.
     */
    private static class RefreshTokenInfo {
        private final String username;
        private final Date expiresAt;

        public RefreshTokenInfo(String username, Date expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }

        public String getUsername() {
            return username;
        }

        public Date getExpiresAt() {
            return expiresAt;
        }
    }
}
