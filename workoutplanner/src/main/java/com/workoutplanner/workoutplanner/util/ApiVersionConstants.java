package com.workoutplanner.workoutplanner.util;

/**
 * Constants for API versioning.
 * 
 * This class defines constants for API version paths to maintain consistency
 * across the application and make version management easier.
 * 
 * Versioning Strategy:
 * - URL Path Versioning: /api/v{version}/{resource}
 * - Current Version: v1
 * - Future versions can be added without breaking existing clients
 * 
 * Usage Example:
 * @RequestMapping(ApiVersionConstants.V1_BASE_PATH + "/users")
 * 
 * When introducing a new version:
 * 1. Add a new constant (e.g., V2_BASE_PATH = "/api/v2")
 * 2. Create new controllers or use @RequestMapping with the new path
 * 3. Keep v1 controllers active for backward compatibility
 * 4. Document migration guide for clients
 */
public final class ApiVersionConstants {
    
    /**
     * Base path for API version 1.
     * All v1 endpoints should use this prefix.
     */
    public static final String V1_BASE_PATH = "/api/v1";
    
    /**
     * Current API version number.
     */
    public static final String CURRENT_VERSION = "v1";
    
    /**
     * API version header name for clients that prefer header-based versioning.
     * This is not currently used but can be implemented if needed.
     */
    public static final String VERSION_HEADER = "X-API-Version";
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static members.
     */
    private ApiVersionConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
