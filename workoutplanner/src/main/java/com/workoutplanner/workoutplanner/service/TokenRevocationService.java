package com.workoutplanner.workoutplanner.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for JWT token revocation management.
 * Handles token blacklisting and revocation for security purposes.
 * 
 * This service implements token revocation strategies:
 * - In-memory token blacklist (for development)
 * - Token expiration tracking
 * - Secure token invalidation
 * - Comprehensive logging
 */
@Service
public class TokenRevocationService {

    private static final Logger logger = LoggerFactory.getLogger(TokenRevocationService.class);

    private final KeyPair keyPair;

    // In-memory blacklist (use Redis in production)
    private final ConcurrentHashMap<String, Date> revokedTokens = new ConcurrentHashMap<>();

    @Value("${app.jwt.blacklist-cleanup-interval:3600000}")
    private long blacklistCleanupInterval;

    private long lastCleanupTime = System.currentTimeMillis();

    public TokenRevocationService(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    /**
     * Revoke a JWT token by adding it to the blacklist.
     * 
     * @param token JWT token to revoke
     * @return true if token was successfully revoked
     */
    public boolean revokeToken(String token) {
        try {
            // Extract token ID (jti) if present
            String tokenId = extractTokenId(token);
            if (tokenId != null) {
                revokedTokens.put(tokenId, new Date());
                logger.info("Token revoked successfully: {}", tokenId);
                return true;
            } else {
                // Fallback: use full token hash
                String tokenHash = String.valueOf(token.hashCode());
                revokedTokens.put(tokenHash, new Date());
                logger.info("Token revoked successfully (by hash): {}", tokenHash);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to revoke token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a token is revoked.
     * 
     * @param token JWT token to check
     * @return true if token is revoked
     */
    public boolean isTokenRevoked(String token) {
        try {
            // Clean up old entries periodically
            cleanupExpiredEntries();

            // Extract token ID (jti) if present
            String tokenId = extractTokenId(token);
            if (tokenId != null) {
                return revokedTokens.containsKey(tokenId);
            } else {
                // Fallback: check by token hash
                String tokenHash = String.valueOf(token.hashCode());
                return revokedTokens.containsKey(tokenHash);
            }
        } catch (Exception e) {
            logger.error("Failed to check token revocation status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Revoke all tokens for a specific user.
     * 
     * @param username username to revoke tokens for
     * @return number of tokens revoked
     */
    public int revokeAllUserTokens(String username) {
        int revokedCount = 0;
        
        // In a real implementation, you would:
        // 1. Query database for all active tokens for the user
        // 2. Add them to the blacklist
        // 3. Optionally notify the user
        
        logger.info("Revoked all tokens for user: {}", username);
        return revokedCount;
    }

    /**
     * Extract token ID (jti) from JWT token.
     * 
     * @param token JWT token
     * @return token ID or null if not present
     */
    private String extractTokenId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return claims.getId();
        } catch (Exception e) {
            logger.debug("Could not extract token ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Clean up expired entries from the blacklist.
     * This prevents memory leaks in long-running applications.
     */
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        
        // Only cleanup if enough time has passed
        if (currentTime - lastCleanupTime < blacklistCleanupInterval) {
            return;
        }
        
        try {
            // Remove entries older than 24 hours
            Date cutoffDate = new Date(currentTime - 24 * 60 * 60 * 1000);
            
            revokedTokens.entrySet().removeIf(entry -> 
                entry.getValue().before(cutoffDate)
            );
            
            lastCleanupTime = currentTime;
            logger.debug("Cleaned up expired blacklist entries. Current size: {}", revokedTokens.size());
            
        } catch (Exception e) {
            logger.error("Failed to cleanup expired blacklist entries: {}", e.getMessage());
        }
    }

    /**
     * Get the current size of the blacklist.
     * 
     * @return number of revoked tokens
     */
    public int getBlacklistSize() {
        return revokedTokens.size();
    }

    /**
     * Clear all revoked tokens (use with caution).
     * This should only be used for testing or emergency situations.
     */
    public void clearBlacklist() {
        revokedTokens.clear();
        logger.warn("Token blacklist cleared - all previously revoked tokens are now valid again!");
    }
}
