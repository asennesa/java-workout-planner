package com.workoutplanner.workoutplanner.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.entity.User;

import java.security.KeyPair;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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

    private static final String REVOKED_TOKENS_PREFIX = "revoked_jwt::";

    private final KeyPair keyPair;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    public TokenRevocationService(KeyPair keyPair, 
                                @Autowired(required = false) RedisTemplate<String, String> redisTemplate,
                                UserRepository userRepository) {
        this.keyPair = keyPair;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    /**
     * Revoke a JWT token by adding it to the blacklist.
     * 
     * @param token JWT token to revoke
     * @return true if token was successfully revoked
     */
    public boolean revokeToken(String token) {
        if (redisTemplate == null) {
            logger.warn("Redis is not available. Token revocation is disabled.");
            return false;
        }

        try {
            String tokenId = extractTokenId(token);
            if (tokenId != null) {
                Date expiration = extractExpiration(token);
                if (expiration != null) {
                    long ttl = expiration.getTime() - System.currentTimeMillis();
                    if (ttl > 0) {
                        String redisKey = REVOKED_TOKENS_PREFIX + tokenId;
                        redisTemplate.opsForValue().set(redisKey, String.valueOf(expiration.getTime()), ttl, TimeUnit.MILLISECONDS);
                        logger.info("Token revoked successfully: {} with TTL: {}ms", tokenId, ttl);
                        return true;
                    } else {
                        logger.warn("Token is already expired, no need to revoke: {}", tokenId);
                        return false;
                    }
                } else {
                    logger.error("Failed to revoke token: could not extract expiration time.");
                    return false;
                }
            } else {
                logger.error("Failed to revoke token: it does not contain a 'jti' (JWT ID) claim.");
                return false;
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
        if (redisTemplate == null) {
            logger.warn("Redis is not available. Token revocation is disabled.");
            return false;
        }
        
        try {
            String tokenId = extractTokenId(token);
            if (tokenId != null) {
                String redisKey = REVOKED_TOKENS_PREFIX + tokenId;
                return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
            } else {
                logger.warn("Could not check token revocation: no 'jti' (JWT ID) claim found.");
                return false;
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
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setTokensValidFrom(new Date());
            userRepository.save(user);
            logger.info("Revoked all tokens for user: {} by updating tokens_valid_from timestamp", username);
            return 1; // Indicates success
        } else {
            logger.warn("Could not revoke tokens for user: {}. User not found.", username);
            return 0; // Indicates failure
        }
    }

    /**
     * Extract token ID (jti) from JWT token.
     * 
     * @param token JWT token
     * @return token ID or null if not present
     */
    private String extractTokenId(String token) {
        try {
            return extractClaim(token, Claims::getId);
        } catch (Exception e) {
            logger.debug("Could not extract token ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract expiration date from JWT token.
     *
     * @param token JWT token
     * @return expiration date or null if not present
     */
    private Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.debug("Could not extract token expiration: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts a specific claim from JWT token.
     *
     * @param token JWT token
     * @param claimsResolver function to extract the claim
     * @param <T> type of the claim
     * @return extracted claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
