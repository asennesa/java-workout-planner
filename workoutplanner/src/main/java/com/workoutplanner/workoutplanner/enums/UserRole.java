package com.workoutplanner.workoutplanner.enums;

/**
 * User roles for authorization.
 * Defines the different access levels in the application.
 */
public enum UserRole {
    /**
     * Regular user with basic permissions.
     * Can manage their own workouts, exercises, and profile.
     */
    USER,
    
    /**
     * Administrator with full system access.
     * Can manage all users, exercises, and system settings.
     */
    ADMIN,
    
    /**
     * Moderator with elevated permissions.
     * Can manage exercises and moderate user content.
     */
    MODERATOR
}

