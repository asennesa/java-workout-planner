package com.workoutplanner.workoutplanner.entity;

import java.time.LocalDateTime;

/**
 * Interface for entities that support soft delete functionality.
 */
public interface SoftDeletable {

    Boolean getDeleted();

    void setDeleted(Boolean deleted);

    LocalDateTime getDeletedAt();

    void setDeletedAt(LocalDateTime deletedAt);

    default void softDelete() {
        setDeleted(true);
        setDeletedAt(LocalDateTime.now());
    }

    default void restore() {
        setDeleted(false);
        setDeletedAt(null);
    }

    default boolean isActive() {
        return !Boolean.TRUE.equals(getDeleted());
    }
}
