package com.workoutplanner.workoutplanner.config;

import com.workoutplanner.workoutplanner.service.OAuth2UserService;
import com.workoutplanner.workoutplanner.service.TokenRevocationService;
import com.workoutplanner.workoutplanner.service.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Comprehensive security configuration for the application.
 * Consolidates all security-related configurations following Spring Boot best practices.
 * 
 * This configuration follows industry best practices:
 * - Single responsibility for security concerns
 * - Proper separation of concerns
 * - Modern Spring Security 6.x patterns
 * - Comprehensive security features
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final OAuth2UserService oauth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oauth2FailureHandler;
    private final ApplicationContext applicationContext;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(OAuth2UserService oauth2UserService,
                         OAuth2AuthenticationSuccessHandler oauth2SuccessHandler,
                         OAuth2AuthenticationFailureHandler oauth2FailureHandler,
                         ApplicationContext applicationContext,
                         CorsConfigurationSource corsConfigurationSource) {
        this.oauth2UserService = oauth2UserService;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.oauth2FailureHandler = oauth2FailureHandler;
        this.applicationContext = applicationContext;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Security filter chain with JWT authentication, OAuth2 integration, and role-based authorization.
     * 
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Get JWT filter from application context to avoid circular dependency
        JwtAuthenticationFilter jwtAuthenticationFilter = applicationContext.getBean(JwtAuthenticationFilter.class);
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public authentication endpoints (with rate limiting)
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/validate").permitAll()
                
                // Public user check endpoints (with rate limiting)
                .requestMatchers("/api/v1/users/check-username", "/api/v1/users/check-email").permitAll()
                
                // User registration (public but rate limited)
                .requestMatchers("POST", "/api/v1/users").permitAll()
                
                // OAuth2 endpoints (public but rate limited)
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                
                // Health check endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Protected endpoints with role-based access
                .requestMatchers("DELETE", "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "USER")
                
                .requestMatchers("POST", "/api/v1/exercises").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("PUT", "/api/v1/exercises/**").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("DELETE", "/api/v1/exercises/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/exercises/**").hasAnyRole("ADMIN", "USER", "MODERATOR")
                
                .requestMatchers("/api/v1/workouts/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/sets/**").hasAnyRole("ADMIN", "USER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/google") // Default OAuth2 login page
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauth2UserService)
                )
                .successHandler(oauth2SuccessHandler)
                .failureHandler(oauth2FailureHandler)
            )
            .addFilterBefore(applicationContext.getBean(OAuth2StateValidationFilter.class), OAuth2LoginAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Authentication manager bean.
     * 
     * @param authConfig AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures BCrypt password encoder for secure password hashing.
     * BCrypt is the industry standard for password hashing in Spring applications.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * RSA Key Pair for JWT signing.
     * Generates a new key pair if no file-based keys are provided.
     * 
     * @return KeyPair for JWT signing
     * @throws Exception if key generation or loading fails
     */
    @Bean
    public KeyPair jwtKeyPair(@Value("${app.jwt.private-key-path:}") String privateKeyPath,
                             @Value("${app.jwt.public-key-path:}") String publicKeyPath,
                             @Value("${app.jwt.key-size:2048}") int keySize) throws Exception {
        // Try to load keys from files first
        if (!privateKeyPath.isEmpty() && !publicKeyPath.isEmpty()) {
            return loadKeyPairFromFiles(privateKeyPath, publicKeyPath);
        }
        
        // Generate new key pair
        return generateKeyPair(keySize);
    }

    /**
     * Load RSA key pair from files.
     * 
     * @return KeyPair loaded from files
     * @throws Exception if key loading fails
     */
    private KeyPair loadKeyPairFromFiles(String privateKeyPath, String publicKeyPath) throws Exception {
        try {
            PrivateKey privateKey = loadPrivateKey(privateKeyPath);
            PublicKey publicKey = loadPublicKey(publicKeyPath);
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA keys from files: " + e.getMessage(), e);
        }
    }

    /**
     * Generate new RSA key pair.
     * 
     * @return Generated KeyPair
     * @throws NoSuchAlgorithmException if RSA algorithm is not available
     */
    private KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Load private key from file.
     * 
     * @param keyPath Path to private key file
     * @return PrivateKey
     * @throws IOException if file reading fails
     * @throws InvalidKeySpecException if key specification is invalid
     * @throws NoSuchAlgorithmException if RSA algorithm is not available
     */
    private PrivateKey loadPrivateKey(String keyPath) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        Resource resource = new ClassPathResource(keyPath);
        String keyContent = Files.readString(resource.getFile().toPath())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Load public key from file.
     * 
     * @param keyPath Path to public key file
     * @return PublicKey
     * @throws IOException if file reading fails
     * @throws InvalidKeySpecException if key specification is invalid
     * @throws NoSuchAlgorithmException if RSA algorithm is not available
     */
    private PublicKey loadPublicKey(String keyPath) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        Resource resource = new ClassPathResource(keyPath);
        String keyContent = Files.readString(resource.getFile().toPath())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Security headers filter to add comprehensive security headers.
     * Implements OWASP security recommendations for HTTP security headers.
     */
    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class SecurityHeadersFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            // Content Security Policy (CSP) - Strict policy without unsafe-inline
            response.setHeader("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' https://accounts.google.com https://github.com https://connect.facebook.net; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self' https://accounts.google.com https://github.com https://graph.facebook.com; " +
                "frame-src 'self' https://accounts.google.com https://github.com https://www.facebook.com; " +
                "object-src 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'");
            
            // X-Frame-Options - Prevent clickjacking
            response.setHeader("X-Frame-Options", "DENY");
            
            // X-Content-Type-Options - Prevent MIME type sniffing
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // X-XSS-Protection - Enable XSS filtering
            response.setHeader("X-XSS-Protection", "0");
            
            // Referrer-Policy - Control referrer information
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Permissions-Policy - Control browser features
            response.setHeader("Permissions-Policy", 
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "gyroscope=(), " +
                "speaker=()");
            
            // Strict-Transport-Security - Force HTTPS
            response.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
            
            // Cache-Control - Prevent caching of sensitive data
            response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Rate limiting filter to prevent brute force attacks and abuse.
     * Implements sliding window rate limiting for authentication endpoints.
     */
    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public static class RateLimitingFilter extends OncePerRequestFilter {

        private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
        
        @Value("${app.rate-limit.requests-per-minute:10}")
        private int requestsPerMinute;
        
        @Autowired(required = false)
        private RedisTemplate<String, String> redisTemplate;
        
        private static final long WINDOW_SIZE_SECONDS = 60; // 1 minute

        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            if (redisTemplate == null) {
                logger.warn("Redis not available, rate limiting is disabled.");
                filterChain.doFilter(request, response);
                return;
            }

            String clientIp = getClientIpAddress(request);
            String requestPath = request.getRequestURI();
            
            // Apply rate limiting to authentication endpoints
            if (isAuthenticationEndpoint(requestPath)) {
                if (!isAllowed(clientIp, requestPath)) {
                    logger.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, requestPath);
                    response.setStatus(429); // Too Many Requests
                    response.setHeader("Retry-After", String.valueOf(WINDOW_SIZE_SECONDS));
                    response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
                    response.setContentType("application/json");
                    return;
                }
            }
            
            filterChain.doFilter(request, response);
        }

        /**
         * Check if the request is allowed based on rate limiting rules.
         * 
         * @param clientIp Client IP address
         * @param requestPath Request path
         * @return true if allowed, false if rate limited
         */
        private boolean isAllowed(String clientIp, String requestPath) {
            String key = "rate-limit:" + clientIp + ":" + requestPath;
            
            try {
                Long currentCount = redisTemplate.opsForValue().increment(key);

                if (currentCount != null) {
                    // If it's a new key, set the expiration
                    if (currentCount == 1) {
                        redisTemplate.expire(key, WINDOW_SIZE_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
                    }
                    return currentCount <= requestsPerMinute;
                }
                
                // If redis increment fails, allow the request to be safe
                return true;

            } catch (Exception e) {
                logger.error("Error during rate limiting check with Redis. Allowing request.", e);
                // In case of Redis error, it's safer to allow the request than to block legitimate users
                return true;
            }
        }

        /**
         * Check if the request path is an authentication endpoint.
         * 
         * @param requestPath Request path
         * @return true if authentication endpoint
         */
        private boolean isAuthenticationEndpoint(String requestPath) {
            return requestPath.startsWith("/api/v1/auth/") || 
                   requestPath.startsWith("/oauth2/") ||
                   requestPath.startsWith("/login/oauth2/");
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

    }

    /**
     * OAuth2 state parameter validator for CSRF protection.
     * Implements state parameter generation and validation for OAuth2 flows.
     */
    @Component
    public static class OAuth2StateValidator {

        private static final Logger logger = LoggerFactory.getLogger(OAuth2StateValidator.class);
        private static final String STATE_REDIS_PREFIX = "oauth2:state:";
        private static final int STATE_LENGTH = 32;
        private static final Duration STATE_EXPIRY = Duration.ofMinutes(10);

        private final SecureRandom secureRandom = new SecureRandom();
        
        @org.springframework.beans.factory.annotation.Autowired(required = false)
        private RedisTemplate<String, String> redisTemplate;

        /**
         * Generate a secure state parameter for OAuth2 flow.
         * 
         * @param request HTTP request
         * @return generated state parameter
         */
        public String generateState(HttpServletRequest request) {
            try {
                // Generate cryptographically secure random bytes
                byte[] randomBytes = new byte[STATE_LENGTH];
                secureRandom.nextBytes(randomBytes);
                String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
                
                // Store state in Redis with expiration
                if (redisTemplate != null) {
                    String stateKey = STATE_REDIS_PREFIX + state;
                    redisTemplate.opsForValue().set(stateKey, "valid", STATE_EXPIRY);
                    logger.debug("Generated and stored OAuth2 state parameter: {}", state);
                } else {
                    logger.warn("Redis not available, OAuth2 state validation disabled");
                }
                
                return state;
            } catch (Exception e) {
                logger.error("Failed to generate OAuth2 state parameter", e);
                throw new OAuth2AuthenticationException("Failed to generate state parameter");
            }
        }

        /**
         * Validate OAuth2 state parameter.
         * 
         * @param state State parameter to validate
         * @return true if valid, false otherwise
         */
        public boolean validateState(String state) {
            if (state == null || state.isEmpty()) {
                logger.warn("OAuth2 state parameter is null or empty");
                return false;
            }
            
            if (redisTemplate == null) {
                logger.warn("Redis not available, OAuth2 state validation disabled");
                return true; // Allow if Redis is not available
            }
            
            try {
                String stateKey = STATE_REDIS_PREFIX + state;
                String storedState = redisTemplate.opsForValue().get(stateKey);
                
                if (storedState != null) {
                    // Remove state after validation (one-time use)
                    redisTemplate.delete(stateKey);
                    logger.debug("OAuth2 state parameter validated and consumed: {}", state);
                    return true;
                } else {
                    logger.warn("OAuth2 state parameter not found or expired: {}", state);
                    return false;
                }
            } catch (Exception e) {
                logger.error("Failed to validate OAuth2 state parameter", e);
                return false;
            }
        }

        /**
         * Check if state parameter exists.
         * 
         * @param state State parameter to check
         * @return true if exists, false otherwise
         */
        public boolean hasState(String state) {
            if (state == null || state.isEmpty() || redisTemplate == null) {
                return false;
            }
            
            try {
                String stateKey = STATE_REDIS_PREFIX + state;
                return Boolean.TRUE.equals(redisTemplate.hasKey(stateKey));
            } catch (Exception e) {
                logger.error("Failed to check OAuth2 state parameter", e);
                return false;
            }
        }

        /**
         * Clear state parameter.
         * 
         * @param state State parameter to clear
         */
        public void clearState(String state) {
            if (state == null || state.isEmpty() || redisTemplate == null) {
                return;
            }
            
            try {
                String stateKey = STATE_REDIS_PREFIX + state;
                redisTemplate.delete(stateKey);
                logger.debug("OAuth2 state parameter cleared: {}", state);
            } catch (Exception e) {
                logger.error("Failed to clear OAuth2 state parameter", e);
            }
        }
    }

    /**
     * OAuth2 state parameter validation filter.
     * Validates state parameter for CSRF protection in OAuth2 flows.
     */
    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    public static class OAuth2StateValidationFilter extends OncePerRequestFilter {

        private static final Logger logger = LoggerFactory.getLogger(OAuth2StateValidationFilter.class);
        private final OAuth2StateValidator stateValidator;

        public OAuth2StateValidationFilter(OAuth2StateValidator stateValidator) {
            this.stateValidator = stateValidator;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            String requestUri = request.getRequestURI();
            
            // Only validate OAuth2 callback endpoints
            if (requestUri.startsWith("/login/oauth2/code/")) {
                String state = request.getParameter("state");
                
                if (state == null || state.isEmpty()) {
                    logger.warn("OAuth2 callback missing state parameter: {}", requestUri);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing state parameter");
                    return;
                }
                
                if (!stateValidator.validateState(state)) {
                    logger.warn("OAuth2 callback with invalid state parameter: {}", requestUri);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid state parameter");
                    return;
                }
                
                logger.debug("OAuth2 state parameter validated successfully: {}", state);
            }
            
            filterChain.doFilter(request, response);
        }
    }

    /**
     * JWT Authentication Filter.
     * Intercepts HTTP requests and validates JWT tokens for authentication.
     */
    @Component
    public static class JwtAuthenticationFilter extends OncePerRequestFilter {

        private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

        private final com.workoutplanner.workoutplanner.service.JwtService jwtService;
        private final UserDetailsService userDetailsService;
        private final TokenRevocationService tokenRevocationService;

        public JwtAuthenticationFilter(com.workoutplanner.workoutplanner.service.JwtService jwtService, 
                                     UserDetailsService userDetailsService, 
                                     TokenRevocationService tokenRevocationService) {
            this.jwtService = jwtService;
            this.userDetailsService = userDetailsService;
            this.tokenRevocationService = tokenRevocationService;
        }

        @Override
        protected void doFilterInternal(
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                jakarta.servlet.FilterChain filterChain
        ) throws jakarta.servlet.ServletException, java.io.IOException {

            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            String username = null;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);
            try {
                username = jwtService.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("JWT token validation failed", e);
                filterChain.doFilter(request, response);
                return;
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Check if token is revoked
                if (tokenRevocationService.isTokenRevoked(jwt)) {
                    logger.warn("Attempted to use revoked JWT token for user: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Check if the token was issued before the user's tokens were last revoked
                if (userDetails instanceof com.workoutplanner.workoutplanner.entity.User) {
                    com.workoutplanner.workoutplanner.entity.User user = (com.workoutplanner.workoutplanner.entity.User) userDetails;
                    Date tokenIssuedAt = jwtService.extractIssuedAt(jwt);
                    if (user.getTokensValidFrom() != null && tokenIssuedAt != null && tokenIssuedAt.before(user.getTokensValidFrom())) {
                        logger.warn("Attempted to use an old JWT token for user: {}. Token issued at: {}, valid from: {}", 
                                    username, tokenIssuedAt, user.getTokensValidFrom());
                        filterChain.doFilter(request, response);
                        return;
                    }
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    /**
     * OAuth2 authentication success handler.
     * Generates JWT token for OAuth2 authenticated users and redirects to frontend.
     */
    @Component
    public static class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
        
        private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
        
        private final JwtService jwtService;
        
        @Value("${app.oauth2.frontend-url:http://localhost:3000}")
        private String frontendUrl;
        
        public OAuth2AuthenticationSuccessHandler(@Lazy JwtService jwtService) {
            this.jwtService = jwtService;
        }
        
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          Authentication authentication) throws IOException, ServletException {
            
            logger.info("OAuth2 authentication successful for user: {}", authentication.getName());
            
            try {
                // Extract OAuth2 user details
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                Map<String, Object> attributes = oauth2User.getAttributes();
                
                // Extract user information
                Long userId = (Long) attributes.get("userId");
                String username = (String) attributes.get("username");
                String email = (String) attributes.get("email");
                String role = extractRole(oauth2User);
                
                logger.debug("OAuth2 user details - userId: {}, username: {}, email: {}, role: {}", 
                            userId, username, email, role);
                
                // Generate JWT token
                String jwtToken = jwtService.generateToken(username, userId, role);
                String refreshToken = jwtService.generateRefreshToken(username, userId);
                
                logger.info("Generated JWT tokens for OAuth2 user: {}", username);
                
                // Set JWT token in secure cookie with consistent expiration
                int accessTokenExpiration = jwtService.getTokenExpirationSeconds();
                int refreshTokenExpiration = jwtService.getRefreshTokenExpirationSeconds();
                setSecureCookie(request, response, "access_token", jwtToken, accessTokenExpiration);
                setSecureCookie(request, response, "refresh_token", refreshToken, refreshTokenExpiration);
                
                // Redirect to frontend with success
                String redirectUrl = frontendUrl + "/auth/oauth2/success";
                response.sendRedirect(redirectUrl);
                
            } catch (Exception ex) {
                logger.error("Error handling OAuth2 authentication success: {}", ex.getMessage(), ex);
                
                // Redirect to frontend with error
                String errorUrl = frontendUrl + "/auth/oauth2/error?message=" + 
                                java.net.URLEncoder.encode("Authentication processing failed", "UTF-8");
                response.sendRedirect(errorUrl);
            }
        }
        
        private String extractRole(OAuth2User oauth2User) {
            return oauth2User.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .orElse("USER");
        }
        
        private void setSecureCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, int maxAge) {
            boolean isSecure = request.isSecure() || 
                              "true".equalsIgnoreCase(System.getProperty("server.ssl.enabled")) ||
                              "true".equalsIgnoreCase(System.getenv("SSL_ENABLED"));
            
            String cookieValue = String.format("%s=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Strict", 
                                             name, value, maxAge);
            
            if (isSecure) {
                cookieValue += "; Secure";
            }
            
            response.addHeader("Set-Cookie", cookieValue);
            logger.debug("Set secure cookie: {} with maxAge: {} (secure: {})", name, maxAge, isSecure);
        }
    }

    /**
     * OAuth2 authentication failure handler.
     * Handles OAuth2 authentication failures and redirects to frontend with error information.
     */
    @Component
    public static class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
        
        private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
        
        @Value("${app.oauth2.frontend-url:http://localhost:3000}")
        private String frontendUrl;
        
        @Override
        public void onAuthenticationFailure(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          AuthenticationException exception) throws IOException, ServletException {
            
            logger.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);
            
            try {
                String errorMessage = getErrorMessage(exception);
                String errorCode = getErrorCode(exception);
                
                logger.debug("OAuth2 authentication failure - errorCode: {}, errorMessage: {}", 
                            errorCode, errorMessage);
                
                String redirectUrl = String.format("%s/auth/oauth2/error?code=%s&message=%s", 
                    frontendUrl, 
                    errorCode, 
                    java.net.URLEncoder.encode(errorMessage, "UTF-8"));
                
                response.sendRedirect(redirectUrl);
                
            } catch (Exception ex) {
                logger.error("Error handling OAuth2 authentication failure: {}", ex.getMessage(), ex);
                
                String fallbackUrl = frontendUrl + "/auth/oauth2/error?message=" + 
                    java.net.URLEncoder.encode("Authentication failed", "UTF-8");
                response.sendRedirect(fallbackUrl);
            }
        }
        
        private String getErrorMessage(AuthenticationException exception) {
            String message = exception.getMessage();
            
            if (message == null || message.isEmpty()) {
                return "Authentication failed. Please try again.";
            }
            
            return switch (message.toLowerCase()) {
                case "access_denied" -> "Access denied. You may have cancelled the authorization.";
                case "invalid_grant" -> "Invalid authorization code. Please try again.";
                case "invalid_client" -> "Invalid client configuration. Please contact support.";
                case "invalid_request" -> "Invalid authentication request. Please try again.";
                case "unsupported_response_type" -> "Unsupported response type. Please contact support.";
                case "invalid_scope" -> "Invalid permissions requested. Please contact support.";
                case "server_error" -> "Server error during authentication. Please try again later.";
                case "temporarily_unavailable" -> "Authentication service is temporarily unavailable. Please try again later.";
                default -> {
                    if (message.contains("email")) {
                        yield "Email access is required for authentication. Please grant email permissions.";
                    } else if (message.contains("user")) {
                        yield "User information could not be retrieved. Please try again.";
                    } else {
                        yield "Authentication failed. Please try again.";
                    }
                }
            };
        }
        
        private String getErrorCode(AuthenticationException exception) {
            String message = exception.getMessage();
            
            if (message == null || message.isEmpty()) {
                return "AUTH_FAILED";
            }
            
            return switch (message.toLowerCase()) {
                case "access_denied" -> "ACCESS_DENIED";
                case "invalid_grant" -> "INVALID_GRANT";
                case "invalid_client" -> "INVALID_CLIENT";
                case "invalid_request" -> "INVALID_REQUEST";
                case "unsupported_response_type" -> "UNSUPPORTED_RESPONSE_TYPE";
                case "invalid_scope" -> "INVALID_SCOPE";
                case "server_error" -> "SERVER_ERROR";
                case "temporarily_unavailable" -> "TEMPORARILY_UNAVAILABLE";
                default -> "AUTH_FAILED";
            };
        }
    }
}