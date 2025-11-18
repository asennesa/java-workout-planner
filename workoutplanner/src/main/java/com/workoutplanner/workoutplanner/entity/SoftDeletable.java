package com.workoutplanner.workoutplanner.entity;

import java.time.LocalDateTime;

/**
 * Interface for entities that support soft delete functionality.
 * <p>
 * Soft delete allows marking records as deleted without physically removing them from the database.
 * This approach provides several benefits:
 * <ul>
 *   <li>Data recovery capabilities</li>
 *   <li>Audit trail preservation</li>
 *   <li>Compliance with data retention policies (GDPR, etc.)</li>
 *   <li>Prevention of accidental data loss</li>
 * </ul>
 * <p>
 * Entities implementing this interface will have:
 * <ul>
 *   <li>A {@code deleted} flag indicating soft delete status</li>
 *   <li>A {@code deletedAt} timestamp recording when the deletion occurred</li>
 *   <li>Methods to perform soft delete and restoration</li>
 * </ul>
 *
 * @see AuditableEntity
 * @author Workout Planner Team
 * @since 1.0
 */
public interface SoftDeletable {

    /**
     * Checks if the entity is soft deleted.
     *
     * @return true if the entity is marked as deleted, false otherwise
     */
    Boolean getDeleted();

    /**
     * Sets the soft delete status of the entity.
     *
     * @param deleted true to mark as deleted, false to mark as active
     */
    void setDeleted(Boolean deleted);

    /**
     * Gets the timestamp when the entity was soft deleted.
     *
     * @return the deletion timestamp, or null if not deleted
     */
    LocalDateTime getDeletedAt();

    /**
     * Sets the timestamp when the entity was soft deleted.
     *
     * @param deletedAt the deletion timestamp
     */
    void setDeletedAt(LocalDateTime deletedAt);

    /**
     * Marks this entity as deleted (soft delete).
     * Sets the deleted flag to true and records the current timestamp.
     * <p>
     * This is the preferred method for deleting entities in the application.
     */
    default void softDelete() {
        setDeleted(true);
        setDeletedAt(LocalDateTime.now());
    }

    /**
     * Restores a soft deleted entity.
     * Sets the deleted flag to false and clears the deletion timestamp.
     * <p>
     * This allows recovery of accidentally deleted data.
     */
    default void restore() {
        setDeleted(false);
        setDeletedAt(null);
    }

    /**
     * Checks if the entity is currently active (not deleted).
     *
     * @return true if the entity is not deleted, false otherwise
     */
    default boolean isActive() {
        return !Boolean.TRUE.equals(getDeleted());
    }
}
