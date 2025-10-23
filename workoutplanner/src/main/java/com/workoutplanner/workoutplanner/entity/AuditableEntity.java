package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * Base audit entity providing comprehensive audit trail functionality.
 * 
 * This abstract class provides enterprise-grade audit fields that are automatically
 * managed by JPA lifecycle callbacks and Spring Data JPA auditing.
 * 
 * Features:
 * - Automatic timestamp management (created_at, updated_at)
 * - User tracking (created_by, updated_by)
 * - JPA lifecycle callbacks for automatic updates
 * - Spring Data JPA auditing integration
 * - Enterprise compliance and debugging support
 * 
 * Usage:
 * Extend this class in all entities that require audit functionality.
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@MappedSuperclass
public abstract class AuditableEntity {

    /**
     * Timestamp when the entity was created.
     * This field is automatically set on entity creation and cannot be updated.
     * 
     * Used for:
     * - Compliance tracking
     * - Debugging creation issues
     * - Business intelligence and analytics
     * - Data recovery and audit trails
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last modified.
     * This field is automatically updated on every entity modification.
     * 
     * Used for:
     * - Tracking data changes
     * - Optimistic locking support
     * - Performance optimization (identify stale data)
     * - Compliance and regulatory requirements
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ID of the user who created this entity.
     * This field is automatically set on entity creation and cannot be updated.
     * 
     * Used for:
     * - User accountability
     * - Security auditing
     * - Data ownership tracking
     * - Compliance requirements
     */
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    /**
     * ID of the user who last modified this entity.
     * This field is automatically updated on every entity modification.
     * 
     * Used for:
     * - Change tracking
     * - Security auditing
     * - User accountability
     * - Compliance and regulatory requirements
     */
    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    /**
     * JPA lifecycle callback for entity creation.
     * Automatically sets creation timestamp and user.
     * 
     * This method is called by JPA before persisting a new entity.
     * It ensures audit fields are properly initialized.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    /**
     * JPA lifecycle callback for entity updates.
     * Automatically updates modification timestamp.
     * 
     * This method is called by JPA before updating an existing entity.
     * It ensures the updated_at field is always current.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    /**
     * Get the creation timestamp.
     * 
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the creation timestamp.
     * Note: This should only be called by JPA lifecycle callbacks.
     * 
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the last modification timestamp.
     * 
     * @return the last modification timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set the last modification timestamp.
     * Note: This should only be called by JPA lifecycle callbacks.
     * 
     * @param updatedAt the last modification timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get the ID of the user who created this entity.
     * 
     * @return the creator user ID
     */
    public Long getCreatedBy() {
        return createdBy;
    }

    /**
     * Set the ID of the user who created this entity.
     * Note: This should only be called by JPA lifecycle callbacks.
     * 
     * @param createdBy the creator user ID
     */
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Get the ID of the user who last modified this entity.
     * 
     * @return the last modifier user ID
     */
    public Long getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the ID of the user who last modified this entity.
     * Note: This should only be called by JPA lifecycle callbacks.
     * 
     * @param updatedBy the last modifier user ID
     */
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Check if this entity has been modified since creation.
     * 
     * @return true if the entity has been modified, false otherwise
     */
    public boolean isModified() {
        return createdAt != null && updatedAt != null && !createdAt.equals(updatedAt);
    }

    /**
     * Get the age of this entity in days.
     * 
     * @return the age in days, or null if creation date is not set
     */
    public Long getAgeInDays() {
        if (createdAt == null) {
            return null;
        }
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
    }

    /**
     * Get the time since last modification in hours.
     * 
     * @return the time since last modification in hours, or null if update date is not set
     */
    public Long getHoursSinceLastUpdate() {
        if (updatedAt == null) {
            return null;
        }
        return java.time.Duration.between(updatedAt, LocalDateTime.now()).toHours();
    }
}
