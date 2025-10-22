package com.workoutplanner.workoutplanner.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token operations.
 * Handles token generation, validation, and extraction of claims.
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret:#{null}}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:900000}")
    private int jwtExpirationMs;
    
    @Value("${app.jwt.refresh-expiration:604800000}")
    private int jwtRefreshExpirationMs;

    private final KeyPair keyPair;
    private final boolean useRS256;
    private final TokenRevocationService tokenRevocationService;

    public JwtService(KeyPair keyPair, TokenRevocationService tokenRevocationService) {
        this.keyPair = keyPair;
        this.tokenRevocationService = tokenRevocationService;
        this.useRS256 = true; // Use RS256 for production security
    }

    /**
     * Extracts username from JWT token.
     *
     * @param token JWT token
     * @return username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts expiration date from JWT token.
     *
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from JWT token.
     *
     * @param token JWT token
     * @param claimsResolver function to extract the claim
     * @param <T> type of the claim
     * @return extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates JWT token for user.
     *
     * @param userDetails user details
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generates JWT token with custom claims.
     *
     * @param extraClaims additional claims
     * @param userDetails user details
     * @return JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Generates JWT token for OAuth2 users.
     *
     * @param username username
     * @param userId user ID
     * @param role user role
     * @return JWT token
     */
    public String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("authType", "oauth2");
        return createToken(claims, username);
    }

    /**
     * Generates refresh token for OAuth2 users.
     *
     * @param username username
     * @param userId user ID
     * @return refresh token
     */
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("tokenType", "refresh");
        
        // Refresh tokens have longer expiration
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);

        logger.debug("Creating refresh token for user: {}", username);

        if (useRS256) {
            return Jwts.builder()
                    .claims(claims)
                    .subject(username)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                    .compact();
        } else {
            return Jwts.builder()
                    .claims(claims)
                    .subject(username)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getSigningKey())
                    .compact();
        }
    }

    /**
     * Creates JWT token with claims and subject.
     *
     * @param claims token claims
     * @param subject token subject (usually username)
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        logger.debug("Creating JWT token for user: {} using {}", subject, useRS256 ? "RS256" : "HS256");

        if (useRS256) {
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                    .compact();
        } else {
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getSigningKey())
                    .compact();
        }
    }

    /**
     * Validates JWT token.
     *
     * @param token JWT token
     * @param userDetails user details
     * @return true if token is valid
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            
            // Check if token is revoked
            if (tokenRevocationService.isTokenRevoked(token)) {
                logger.warn("JWT token is revoked for user: {}", username);
                return false;
            }
            
            // Check if token is expired
            if (isTokenExpired(token)) {
                logger.warn("JWT token expired for user: {}", username);
                return false;
            }
            
            // Check if username matches
            if (!username.equals(userDetails.getUsername())) {
                logger.warn("JWT token username mismatch: {} vs {}", username, userDetails.getUsername());
                return false;
            }
            
            // Validate token signature by parsing it
            try {
                extractAllClaims(token);
                logger.debug("JWT token validation successful for user: {}", username);
                return true;
            } catch (Exception e) {
                logger.warn("JWT token signature validation failed for user: {} - {}", username, e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if JWT token is expired.
     *
     * @param token JWT token
     * @return true if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts all claims from JWT token.
     *
     * @param token JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        if (useRS256) {
            return Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } else {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
    }

    /**
     * Gets the signing key for JWT operations.
     * Validates that the secret is properly configured.
     *
     * @return secret key
     * @throws IllegalStateException if secret is not configured
     */
    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured. Set JWT_SECRET environment variable.");
        }
        
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long for security.");
        }
        
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get token expiration in seconds.
     *
     * @return token expiration in seconds
     */
    public int getTokenExpirationSeconds() {
        return jwtExpirationMs / 1000;
    }

    /**
     * Get refresh token expiration in seconds.
     *
     * @return refresh token expiration in seconds
     */
    public int getRefreshTokenExpirationSeconds() {
        return jwtRefreshExpirationMs / 1000;
    }
}

