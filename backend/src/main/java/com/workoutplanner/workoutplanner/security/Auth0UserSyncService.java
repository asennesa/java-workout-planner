package com.workoutplanner.workoutplanner.security;

import com.workoutplanner.workoutplanner.entity.User;
import com.workoutplanner.workoutplanner.enums.UserRole;
import com.workoutplanner.workoutplanner.repository.UserRepository;
import com.workoutplanner.workoutplanner.security.exception.Auth0AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for synchronizing Auth0 users with local database.
 * Returns DTO (Auth0Principal) not JPA entity. Cache key is Auth0 user ID.
 */
@Service
@Profile("!test & !dev")  // Only active in production (Auth0 mode)
public class Auth0UserSyncService {

    private static final Logger logger = LoggerFactory.getLogger(Auth0UserSyncService.class);

    private final UserRepository userRepository;
    private final String audience;

    public Auth0UserSyncService(
            UserRepository userRepository,
            @Value("${auth0.audience}") String audience) {
        this.userRepository = userRepository;
        this.audience = audience;
    }

    @Transactional
    @Cacheable(value = "auth0Users", key = "#jwt.subject")
    public Auth0Principal syncUser(Jwt jwt) {
        String auth0UserId = jwt.getSubject();
        logger.debug("Cache miss - syncing user from DB: {}", auth0UserId);

        Optional<User> existingUser = userRepository.findByAuth0UserId(auth0UserId);

        User user;
        if (existingUser.isPresent()) {
            user = updateUserIfNeeded(existingUser.get(), jwt);
        } else {
            user = createUser(jwt);
        }

        return toAuth0Principal(user);
    }

    private Auth0Principal toAuth0Principal(User user) {
        return new Auth0Principal(
            user.getUserId(),
            user.getAuth0UserId(),
            user.getEmail(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole()
        );
    }

    private User createUser(Jwt jwt) {
        User user = new User();
        user.setAuth0UserId(jwt.getSubject());
        user.setEmail(extractEmail(jwt));
        user.setUsername(extractUsername(jwt));
        user.setFirstName(extractFirstName(jwt));
        user.setLastName(extractLastName(jwt));
        user.setRole(extractRole(jwt));

        User savedUser = userRepository.save(user);

        logger.info("Created new user from Auth0: userId={}, auth0UserId={}, email={}",
            savedUser.getUserId(), savedUser.getAuth0UserId(), savedUser.getEmail());

        return savedUser;
    }

    private User updateUserIfNeeded(User user, Jwt jwt) {
        boolean updated = false;

        String newEmail = extractEmail(jwt);
        if (!user.getEmail().equals(newEmail)) {
            logger.info("Updating email for user {}: {} -> {}",
                user.getAuth0UserId(), user.getEmail(), newEmail);
            user.setEmail(newEmail);
            updated = true;
        }

        String newFirstName = extractFirstName(jwt);
        if (newFirstName != null && !newFirstName.equals(user.getFirstName())) {
            user.setFirstName(newFirstName);
            updated = true;
        }

        String newLastName = extractLastName(jwt);
        if (newLastName != null && !newLastName.equals(user.getLastName())) {
            user.setLastName(newLastName);
            updated = true;
        }

        UserRole newRole = extractRole(jwt);
        if (user.getRole() != newRole) {
            logger.info("Updating role for user {}: {} -> {}",
                user.getAuth0UserId(), user.getRole(), newRole);
            user.setRole(newRole);
            updated = true;
        }

        if (updated) {
            user = userRepository.save(user);
        }

        return user;
    }

    private String extractEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isEmpty()) {
            logger.error("Email claim missing from JWT. Available claims: {}", jwt.getClaims().keySet());
            throw new Auth0AuthenticationException(
                Auth0AuthenticationException.ErrorCode.EMAIL_MISSING,
                "Email claim missing from JWT. Update your Auth0 Action to include: " +
                "api.accessToken.setCustomClaim('email', event.user.email);"
            );
        }
        return email;
    }

    private String extractUsername(Jwt jwt) {
        String username = jwt.getClaimAsString("nickname");
        if (username == null || username.isEmpty()) {
            username = jwt.getClaimAsString("preferred_username");
        }
        if (username == null || username.isEmpty()) {
            String email = extractEmail(jwt);
            username = email.substring(0, email.indexOf('@'));
        }
        return username;
    }

    private String extractFirstName(Jwt jwt) {
        String firstName = jwt.getClaimAsString("given_name");
        if (firstName == null || firstName.isEmpty()) {
            String name = jwt.getClaimAsString("name");
            if (name != null && name.contains(" ")) {
                firstName = name.substring(0, name.indexOf(' '));
            } else if (name != null) {
                firstName = name;
            } else {
                firstName = extractUsername(jwt);
            }
        }
        return firstName;
    }

    private String extractLastName(Jwt jwt) {
        String lastName = jwt.getClaimAsString("family_name");
        if (lastName == null || lastName.isEmpty()) {
            String name = jwt.getClaimAsString("name");
            if (name != null && name.contains(" ")) {
                lastName = name.substring(name.lastIndexOf(' ') + 1);
            } else {
                lastName = "";
            }
        }
        return lastName;
    }

    private UserRole extractRole(Jwt jwt) {
        String roleClaim = audience + "/role";
        try {
            Object roleObj = jwt.getClaim(roleClaim);
            if (roleObj instanceof String roleStr) {
                return UserRole.valueOf(roleStr.toUpperCase());
            }
        } catch (Exception e) {
            logger.debug("Could not extract role from JWT, defaulting to USER: {}", e.getMessage());
        }
        return UserRole.USER;
    }
}
