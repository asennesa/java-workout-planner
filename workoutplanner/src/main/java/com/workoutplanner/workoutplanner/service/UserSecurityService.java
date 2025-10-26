package com.workoutplanner.workoutplanner.service;

import com.workoutplanner.workoutplanner.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Security service for user access control.
 * Provides method-level security for user operations.
 */
@Service
public class UserSecurityService {

    /**
     * Check if the authenticated user can access a specific user's data.
     * Users can only access their own data unless they are ADMIN.
     * 
     * @param authentication Current authentication
     * @param userId User ID to access
     * @return true if access is allowed
     */
    public boolean canAccessUser(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }
        
        User currentUser = (User) authentication.getPrincipal();
        
        // Admin can access any user's data
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        
        // Users can only access their own data
        return currentUser.getUserId().equals(userId);
    }
    
    /**
     * Check if the authenticated user can modify a specific user's data.
     * Users can only modify their own data unless they are ADMIN.
     * 
     * @param authentication Current authentication
     * @param userId User ID to modify
     * @return true if modification is allowed
     */
    public boolean canModifyUser(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }
        
        User currentUser = (User) authentication.getPrincipal();
        
        // Admin can modify any user's data
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        
        // Users can only modify their own data
        return currentUser.getUserId().equals(userId);
    }
    
    /**
     * Check if the authenticated user can delete a specific user.
     * Only ADMIN can delete users.
     * 
     * @param authentication Current authentication
     * @param userId User ID to delete
     * @return true if deletion is allowed
     */
    public boolean canDeleteUser(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }
        
        User currentUser = (User) authentication.getPrincipal();
        
        // Only ADMIN can delete users
        return hasRole(currentUser, "ADMIN");
    }
    
    /**
     * Check if the authenticated user has a specific role.
     * 
     * @param user User to check
     * @param role Role to check for
     * @return true if user has the role
     */
    private boolean hasRole(User user, String role) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role));
    }
}
