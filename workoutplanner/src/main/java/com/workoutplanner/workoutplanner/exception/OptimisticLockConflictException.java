package com.workoutplanner.workoutplanner.exception;

/**
 * Exception thrown when an optimistic lock conflict occurs.
 * This happens when two users try to modify the same entity simultaneously.
 * 
 * This is a business logic exception that should be handled gracefully
 * by providing the user with a clear message and option to refresh.
 */
public class OptimisticLockConflictException extends BusinessLogicException {

    private final String entityType;
    private final Long entityId;
    private final Long currentVersion;
    private final Long expectedVersion;

    public OptimisticLockConflictException(String entityType, Long entityId, 
                                         Long currentVersion, Long expectedVersion) {
        super(String.format(
            "The %s with ID %d was modified by another user. " +
            "Current version: %d, Expected version: %d. " +
            "Please refresh and try again.",
            entityType, entityId, currentVersion, expectedVersion
        ));
        
        this.entityType = entityType;
        this.entityId = entityId;
        this.currentVersion = currentVersion;
        this.expectedVersion = expectedVersion;
    }

    public OptimisticLockConflictException(String message) {
        super(message);
        this.entityType = null;
        this.entityId = null;
        this.currentVersion = null;
        this.expectedVersion = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Long getCurrentVersion() {
        return currentVersion;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }
}
