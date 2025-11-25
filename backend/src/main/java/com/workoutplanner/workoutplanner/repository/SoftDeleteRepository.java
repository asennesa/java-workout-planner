package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.SoftDeletable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Custom repository interface that provides soft delete functionality for entities.
 * <p>
 * This interface extends JpaRepository and overrides standard methods to automatically
 * filter soft-deleted entities. This provides "secure by default" behavior where deleted
 * records are excluded from normal operations.
 * <p>
 * <b>How It Works:</b>
 * <ul>
 *   <li>Standard methods (findById, findAll, count) are overridden with @Query to filter deleted = false</li>
 *   <li>This ensures deleted entities are automatically excluded from all standard operations</li>
 *   <li>Explicit *IncludingDeleted() methods are provided when you need deleted records</li>
 *   <li>Simple, clear, and explicit - no hidden behavior</li>
 * </ul>
 * <p>
 * <b>Method Categories:</b>
 * <ul>
 *   <li><b>Standard methods</b> (findById, findAll, count) - Automatically filter deleted records</li>
 *   <li><b>*IncludingDeleted methods</b> - Explicitly access ALL records (active and deleted)</li>
 *   <li><b>*Deleted methods</b> - Explicitly access only deleted records (for admin/recycle bin)</li>
 * </ul>
 * 
 * @param <T> the entity type that implements SoftDeletable
 * @param <ID> the ID type of the entity
 * 
 * @see SoftDeletable
 * @see JpaRepository
 * @see com.workoutplanner.workoutplanner.entity.AuditableEntity
 * @author Workout Planner Team
 * @since 1.0
 */
@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeletable, ID> extends JpaRepository<T, ID> {

    /**
     * Override default findAll() to return only active (non-deleted) entities.
     * This makes the repository "secure by default" - deleted entities are excluded automatically.
     * Use findAllIncludingDeleted() if you need to see deleted entities.
     * 
     * ⚠️ WARNING: This method loads ALL active entities into memory. For large datasets,
     * use findAll(Pageable) instead to avoid OutOfMemoryError. Only use this method
     * if you're certain the result set is small (<1000 records).
     *
     * @return list of all active entities
     */
    @Override
    @NonNull
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<T> findAll();

    /**
     * Override findAll(Sort) to return only active (non-deleted) entities with sorting.
     * This makes the repository "secure by default" - deleted entities are excluded automatically.
     *
     * @param sort the sorting specification
     * @return sorted list of active entities
     */
    @Override
    @NonNull
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<T> findAll(@NonNull Sort sort);

    /**
     * Override findAll(Pageable) to return only active (non-deleted) entities with pagination.
     * This makes the repository "secure by default" - deleted entities are excluded automatically.
     * Use findAllIncludingDeleted() if you need to see deleted entities.
     * 
     * Performance: Uses explicit countQuery for optimization. Requires database index on 'deleted' column.
     *
     * @param pageable the pagination specification
     * @return page of active entities
     */
    @Override
    @NonNull
    @Query(
        value = "SELECT e FROM #{#entityName} e WHERE e.deleted = false",
        countQuery = "SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false"
    )
    Page<T> findAll(@NonNull Pageable pageable);

    /**
     * Finds all entities including soft deleted ones.
     * This method explicitly queries ALL records (active and deleted).
     * Useful for admin interfaces or restoration operations.
     * 
     * ⚠️ WARNING: This method loads ALL entities (including deleted) into memory.
     * Use with caution on large datasets. Consider pagination for production use.
     *
     * @return list of all entities (both active and deleted)
     */
    @Query("SELECT e FROM #{#entityName} e")
    List<T> findAllIncludingDeleted();

    /**
     * Finds all soft deleted entities only.
     * Useful for admin "recycle bin" interfaces or restoration operations.
     *
     * @return list of all deleted entities
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<T> findAllDeleted();

    /**
     * Override default findById() to return only active (non-deleted) entities.
     * This makes the repository "secure by default" - deleted entities return empty Optional.
     * Use findByIdIncludingDeleted() if you need to access deleted entities.
     *
     * @param id the entity ID
     * @return Optional containing the entity if found and active, empty otherwise
     */
    @Override
    @NonNull
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    Optional<T> findById(@NonNull @Param("id") ID id);

    /**
     * Finds an entity by ID including soft deleted entities.
     * Use this when you need to access deleted entities (e.g., for restoration).
     *
     * @param id the entity ID
     * @return Optional containing the entity if found (regardless of deleted status)
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdIncludingDeleted(@Param("id") ID id);

    /**
     * Soft deletes an entity by ID.
     * Marks the entity as deleted and sets deletion timestamp.
     * 
     * This is the preferred deletion method in the application.
     *
     * @param id the entity ID to soft delete
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void softDeleteById(@Param("id") ID id);

    /**
     * Restores a soft deleted entity by ID.
     * Clears the deleted flag and deletion timestamp.
     *
     * @param id the entity ID to restore
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null WHERE e.id = :id")
    void restoreById(@Param("id") ID id);

    /**
     * Permanently deletes an entity by ID (hard delete).
     * This physically removes the entity from the database.
     * 
     * WARNING: This operation is irreversible and should be used with caution.
     * Typically restricted to admin operations or compliance requirements.
     *
     * @param id the entity ID to permanently delete
     */
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.id = :id")
    void hardDeleteById(@Param("id") ID id);

    /**
     * Permanently deletes all entities that have been soft deleted for more than specified days.
     * Used for compliance with data retention policies.
     * 
     * WARNING: This operation is irreversible.
     *
     * @param days the number of days after which to permanently delete
     * @return the number of entities permanently deleted
     */
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.deleted = true AND e.deletedAt < CURRENT_TIMESTAMP - :days DAY")
    int permanentlyDeleteOldSoftDeleted(@Param("days") int days);

    /**
     * Override default count() to count only active (non-deleted) entities.
     * This makes the repository "secure by default" - deleted entities are not counted.
     *
     * @return the count of active entities
     */
    @Override
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false")
    long count();

    /**
     * Counts all entities including soft deleted ones.
     * This method explicitly counts ALL records (active and deleted).
     *
     * @return the count of all entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e")
    long countIncludingDeleted();

    /**
     * Counts all soft deleted entities only.
     * Useful for admin "recycle bin" statistics.
     *
     * @return the count of deleted entities
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = true")
    long countDeleted();

    /**
     * Override default existsById() to check only active (non-deleted) entities.
     * Returns true only if an active entity with the given ID exists.
     *
     * @param id the entity ID
     * @return true if an active entity with the given ID exists, false otherwise
     */
    @Override
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    boolean existsById(@NonNull @Param("id") ID id);
}

