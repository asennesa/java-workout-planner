package com.workoutplanner.workoutplanner.validation;

/**
 * Validation group interfaces for different business scenarios.
 * 
 * This class defines validation groups that allow different validation rules
 * to be applied based on the business context (create, update, etc.).
 * 
 * Benefits:
 * - Different validation rules for different scenarios
 * - Clear separation of business logic
 * - Improved API design and user experience
 * - Better testing and maintenance
 * 
 * Usage:
 * - Use @Validated(Create.class) for creation operations
 * - Use @Validated(Update.class) for update operations
 * - Use @Validated(Delete.class) for deletion operations
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
public final class ValidationGroups {

    /**
     * Validation group for entity creation operations.
     * 
     * This group is used when creating new entities and typically requires
     * all mandatory fields to be provided. It ensures data integrity for
     * new records and prevents incomplete data from being persisted.
     * 
     * Use cases:
     * - User registration
     * - Creating new exercises
     * - Starting new workout sessions
     * - Adding new sets to workouts
     * 
     * Example:
     * @Validated(Create.class)
     * public ResponseEntity<User> createUser(@RequestBody User user) { ... }
     */
    public interface Create {}

    /**
     * Validation group for entity update operations.
     *
     * This group is used when updating existing entities and typically allows
     * partial updates. Only provided fields are validated, allowing users to
     * update specific attributes without providing all data.
     *
     * Use cases:
     * - Exercise modifications
     * - Workout session updates
     * - Set modifications
     *
     * Example:
     * @Validated(Update.class)
     * public ResponseEntity<Exercise> updateExercise(@RequestBody Exercise exercise) { ... }
     */
    public interface Update {}

    /**
     * Validation group for basic user profile update operations.
     *
     * This group is used for non-sensitive profile updates that don't require
     * password verification. Typically used for name changes and other basic
     * profile information updates.
     *
     * Use cases:
     * - First name/last name changes
     * - Basic profile information updates
     * - Non-sensitive data modifications
     *
     * Example:
     * @Validated(BasicUpdate.class)
     * public ResponseEntity<User> updateUserProfile(@RequestBody User user) { ... }
     */
    public interface BasicUpdate {}

    /**
     * Validation group for secure profile update operations.
     * 
     * This group is used when updating user profiles and requires password
     * verification for security. It ensures that sensitive profile changes
     * are properly authenticated.
     * 
     * Use cases:
     * - User profile updates (email, name changes)
     * - Password changes
     * - Account settings modifications
     * - Sensitive data updates
     * 
     * Example:
     * @Validated(SecureUpdate.class)
     * public ResponseEntity<User> updateUserProfile(@RequestBody User user) { ... }
     */
    public interface SecureUpdate {}

    /**
     * Validation group for entity deletion operations.
     * 
     * This group is used when deleting entities and typically requires
     * minimal validation, focusing on ensuring the operation is safe
     * and authorized.
     * 
     * Use cases:
     * - User account deletion
     * - Exercise removal
     * - Workout session cancellation
     * - Set removal
     * 
     * Example:
     * @Validated(Delete.class)
     * public ResponseEntity<Void> deleteUser(@PathVariable Long id) { ... }
     */
    public interface Delete {}

    /**
     * Validation group for authentication operations.
     * 
     * This group is used for login, password reset, and other authentication
     * related operations. It focuses on credential validation and security.
     * 
     * Use cases:
     * - User login
     * - Password reset
     * - Token validation
     * - Authentication flows
     * 
     * Example:
     * @Validated(Auth.class)
     * public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) { ... }
     */
    public interface Auth {}

    /**
     * Validation group for administrative operations.
     * 
     * This group is used for administrative tasks that require elevated
     * permissions and different validation rules than regular user operations.
     * 
     * Use cases:
     * - Admin user management
     * - System configuration
     * - Bulk operations
     * - Administrative overrides
     * 
     * Example:
     * @Validated(Admin.class)
     * public ResponseEntity<User> adminUpdateUser(@RequestBody User user) { ... }
     */
    public interface Admin {}

    /**
     * Validation group for public operations.
     * 
     * This group is used for operations that don't require authentication
     * or have minimal validation requirements, such as public API endpoints.
     * 
     * Use cases:
     * - Public exercise listings
     * - Health checks
     * - Public information
     * - Unauthenticated endpoints
     * 
     * Example:
     * @Validated(Public.class)
     * public ResponseEntity<List<Exercise>> getPublicExercises() { ... }
     */
    public interface Public {}

    /**
     * Validation group for bulk operations.
     * 
     * This group is used for operations that process multiple entities
     * at once, with different validation rules than single entity operations.
     * 
     * Use cases:
     * - Bulk user imports
     * - Mass exercise updates
     * - Batch workout processing
     * - Bulk data operations
     * 
     * Example:
     * @Validated(Bulk.class)
     * public ResponseEntity<BulkResult> bulkUpdateExercises(@RequestBody List<Exercise> exercises) { ... }
     */
    public interface Bulk {}

    /**
     * Validation group for system operations.
     * 
     * This group is used for system-level operations that require
     * special validation rules and are typically performed by the system
     * rather than users.
     * 
     * Use cases:
     * - System maintenance
     * - Automated processes
     * - Background jobs
     * - System integrations
     * 
     * Example:
     * @Validated(System.class)
     * public ResponseEntity<Void> systemCleanup() { ... }
     */
    public interface System {}

    /**
     * Private constructor to prevent instantiation.
     * This class contains only static interfaces and should not be instantiated.
     */
    private ValidationGroups() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
