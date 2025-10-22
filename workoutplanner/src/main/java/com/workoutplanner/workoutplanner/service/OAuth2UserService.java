package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Custom OAuth2 user service for handling OAuth2 authentication.
 * Maps OAuth2 users to application users and handles user creation/updates.
 * 
 * This service follows industry best practices:
 * - Automatic user creation for new OAuth2 users
 * - Email-based user matching
 * - Secure role assignment
 * - Comprehensive logging
 */
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserService.class);
    
    private final UserRepository userRepository;
    
    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Load OAuth2 user and map to application user.
     * Creates new user if not exists, updates existing user if needed.
     * 
     * @param userRequest OAuth2 user request
     * @return OAuth2User with application user details
     * @throws OAuth2AuthenticationException if user processing fails
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.debug("Loading OAuth2 user from provider: {}", userRequest.getClientRegistration().getRegistrationId());
        
        // Get OAuth2 user from provider
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 user: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user: " + ex.getMessage());
        }
    }
    
    /**
     * Process OAuth2 user and map to application user.
     * 
     * @param userRequest OAuth2 user request
     * @param oauth2User OAuth2 user from provider
     * @return OAuth2User with application user details
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        logger.debug("Processing OAuth2 user from {} with attributes: {}", registrationId, attributes.keySet());
        
        // Extract user information based on provider
        OAuth2UserInfo userInfo = extractUserInfo(registrationId, attributes);
        
        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            logger.error("OAuth2 user email is null or empty");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        // Find existing user by email
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        
        User user;
        if (existingUser.isPresent()) {
            user = updateExistingUser(existingUser.get(), userInfo, registrationId);
            logger.info("Updated existing user via OAuth2: {}", user.getEmail());
        } else {
            user = createNewUser(userInfo, registrationId);
            logger.info("Created new user via OAuth2: {}", user.getEmail());
        }
        
        // Create authorities
        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        
        // Create custom attributes for OAuth2User
        Map<String, Object> customAttributes = new HashMap<>(attributes);
        customAttributes.put("userId", user.getUserId());
        customAttributes.put("username", user.getUsername());
        customAttributes.put("email", user.getEmail());
        customAttributes.put("firstName", user.getFirstName());
        customAttributes.put("lastName", user.getLastName());
        
        return new DefaultOAuth2User(authorities, customAttributes, "email");
    }
    
    /**
     * Extract user information from OAuth2 attributes based on provider.
     * 
     * @param registrationId OAuth2 provider registration ID
     * @param attributes OAuth2 user attributes
     * @return OAuth2UserInfo with extracted user information
     */
    private OAuth2UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GitHubOAuth2UserInfo(attributes);
            case "facebook" -> new FacebookOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
    
    /**
     * Update existing user with OAuth2 information.
     * 
     * @param existingUser Existing user
     * @param userInfo OAuth2 user information
     * @param registrationId OAuth2 provider ID
     * @return Updated user
     */
    private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo, String registrationId) {
        // Update user information if needed
        boolean updated = false;
        
        if (userInfo.getFirstName() != null && !userInfo.getFirstName().equals(existingUser.getFirstName())) {
            existingUser.setFirstName(userInfo.getFirstName());
            updated = true;
        }
        
        if (userInfo.getLastName() != null && !userInfo.getLastName().equals(existingUser.getLastName())) {
            existingUser.setLastName(userInfo.getLastName());
            updated = true;
        }
        
        if (userInfo.getPictureUrl() != null) {
            // You might want to store profile picture URL in user entity
            // existingUser.setProfilePictureUrl(userInfo.getPictureUrl());
        }
        
        if (updated) {
            existingUser = userRepository.save(existingUser);
        }
        
        return existingUser;
    }
    
    /**
     * Create new user from OAuth2 information.
     * 
     * @param userInfo OAuth2 user information
     * @param registrationId OAuth2 provider ID
     * @return New user
     */
    private User createNewUser(OAuth2UserInfo userInfo, String registrationId) {
        User user = new User();
        
        // Generate unique username if needed
        String username = generateUniqueUsername(userInfo);
        
        user.setUsername(username);
        user.setEmail(userInfo.getEmail());
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setRole(determineUserRole(userInfo.getEmail(), registrationId)); // Role based on email domain and provider
        
        // OAuth2 users don't have passwords - use a secure random hash
        user.setPasswordHash(generateSecureOAuth2PasswordHash());
        
        return userRepository.save(user);
    }
    
    /**
     * Generate unique username for OAuth2 user.
     * 
     * @param userInfo OAuth2 user information
     * @return Unique username
     */
    private String generateUniqueUsername(OAuth2UserInfo userInfo) {
        String baseUsername = userInfo.getFirstName() != null ? 
            userInfo.getFirstName().toLowerCase().replaceAll("[^a-z0-9]", "") : 
            userInfo.getEmail().split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
    
    /**
     * Generate secure password hash for OAuth2 users.
     * OAuth2 users cannot login with password, so we use a secure random hash.
     * 
     * @return Secure random hash for OAuth2 users
     */
    private String generateSecureOAuth2PasswordHash() {
        // Generate a secure random hash that cannot be used for login
        java.security.SecureRandom random = new java.security.SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "OAUTH2_" + java.util.Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Determine user role based on email domain and OAuth2 provider.
     * 
     * @param email user email
     * @param registrationId OAuth2 provider
     * @return UserRole
     */
    private UserRole determineUserRole(String email, String registrationId) {
        // Check for admin email domains
        if (email != null) {
            String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
            
            // Admin domains (configure these based on your organization)
            if (domain.equals("admin.workoutplanner.com") || 
                domain.equals("workoutplanner.com")) {
                logger.info("Assigning ADMIN role to user with email domain: {}", domain);
                return UserRole.ADMIN;
            }
            
            // Moderator domains
            if (domain.equals("moderator.workoutplanner.com")) {
                logger.info("Assigning MODERATOR role to user with email domain: {}", domain);
                return UserRole.MODERATOR;
            }
        }
        
        // Default role for OAuth2 users
        logger.info("Assigning default USER role to OAuth2 user from provider: {}", registrationId);
        return UserRole.USER;
    }
    
    /**
     * Interface for OAuth2 user information extraction.
     */
    public interface OAuth2UserInfo {
        String getId();
        String getEmail();
        String getFirstName();
        String getLastName();
        String getPictureUrl();
    }
    
    /**
     * Google OAuth2 user information extractor.
     */
    public static class GoogleOAuth2UserInfo implements OAuth2UserInfo {
        private final Map<String, Object> attributes;
        
        public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
            this.attributes = attributes;
        }
        
        @Override
        public String getId() {
            return (String) attributes.get("sub");
        }
        
        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }
        
        @Override
        public String getFirstName() {
            return (String) attributes.get("given_name");
        }
        
        @Override
        public String getLastName() {
            return (String) attributes.get("family_name");
        }
        
        @Override
        public String getPictureUrl() {
            return (String) attributes.get("picture");
        }
    }
    
    /**
     * GitHub OAuth2 user information extractor.
     */
    public static class GitHubOAuth2UserInfo implements OAuth2UserInfo {
        private final Map<String, Object> attributes;
        
        public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
            this.attributes = attributes;
        }
        
        @Override
        public String getId() {
            return String.valueOf(attributes.get("id"));
        }
        
        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }
        
        @Override
        public String getFirstName() {
            String name = (String) attributes.get("name");
            if (name != null && name.contains(" ")) {
                return name.split(" ")[0];
            }
            return name;
        }
        
        @Override
        public String getLastName() {
            String name = (String) attributes.get("name");
            if (name != null && name.contains(" ")) {
                String[] parts = name.split(" ");
                if (parts.length > 1) {
                    return parts[1];
                }
            }
            return null;
        }
        
        @Override
        public String getPictureUrl() {
            return (String) attributes.get("avatar_url");
        }
    }
    
    /**
     * Facebook OAuth2 user information extractor.
     */
    public static class FacebookOAuth2UserInfo implements OAuth2UserInfo {
        private final Map<String, Object> attributes;
        
        public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
            this.attributes = attributes;
        }
        
        @Override
        public String getId() {
            return (String) attributes.get("id");
        }
        
        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }
        
        @Override
        public String getFirstName() {
            return (String) attributes.get("first_name");
        }
        
        @Override
        public String getLastName() {
            return (String) attributes.get("last_name");
        }
        
        @Override
        public String getPictureUrl() {
            return (String) attributes.get("picture");
        }
    }
}
