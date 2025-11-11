package com.workoutplanner.workoutplanner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base audit entity providing comprehensive audit trail and soft delete functionality.
 * 
 * This abstract class provides enterprise-grade audit fields that are automatically
 * managed by JPA lifecycle callbacks and Spring Data JPA auditing.
 * 
 * Features:
 * - Automatic timestamp management (created_at, updated_at)
 * - User tracking (created_by, updated_by)
 * - Soft delete support (deleted flag and timestamp)
 * - JPA lifecycle callbacks for automatic updates
 * - Spring Data JPA auditing integration
 * - Enterprise compliance and debugging support
 * 
 * Soft Delete Implementation:
 * - Soft delete filtering is handled at the repository level via query overrides
 * - Standard repository methods (findById, findAll, etc.) automatically filter deleted records
 * - Use *IncludingDeleted() methods when you need to access deleted records (e.g., restore)
 * 
 * Usage:
 * Extend this class in all entities that require audit functionality and soft delete.
 * 
 * @author WorkoutPlanner Team
 * @version 1.0
 * @since 1.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity implements SoftDeletable {

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
    @Column(name = "created_by", nullable = true, updatable = false)
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
    @Column(name = "updated_by", nullable = true)
    private Long updatedBy;

    /**
     * Soft delete flag indicating whether the entity is marked as deleted.
     * When true, the entity is logically deleted but remains in the database.
     * 
     * Used for:
     * - Data recovery capabilities
     * - Audit trail preservation
     * - GDPR compliance (scheduled permanent deletion)
     * - Prevention of accidental data loss
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * Timestamp when the entity was soft deleted.
     * This field is set when the entity is marked as deleted.
     * 
     * Used for:
     * - Tracking deletion time for compliance
     * - Scheduled permanent deletion (e.g., after 30 days)
     * - Audit trail and recovery operations
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    // SoftDeletable implementation

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * Get the time since deletion in days.
     * 
     * @return the time since deletion in days, or null if not deleted
     */
    public Long getDaysSinceDeletion() {
        if (deletedAt == null) {
            return null;
        }
        return java.time.Duration.between(deletedAt, LocalDateTime.now()).toDays();
    }

    /**
     * Check if this entity is eligible for permanent deletion based on retention policy.
     * Default policy: 30 days after soft delete.
     * 
     * @return true if the entity can be permanently deleted, false otherwise
     */
    public boolean isEligibleForPermanentDeletion() {
        if (deletedAt == null || !Boolean.TRUE.equals(deleted)) {
            return false;
        }
        Long daysSinceDeletion = getDaysSinceDeletion();
        return daysSinceDeletion != null && daysSinceDeletion >= 30;
    }
}
