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
 * Base repository with soft delete support. All standard methods filter out deleted entities.
 */
@NoRepositoryBean
public interface SoftDeleteRepository<E extends SoftDeletable, I> extends JpaRepository<E, I> {

    @Override
    @NonNull
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<E> findAll();

    @Override
    @NonNull
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<E> findAll(@NonNull Sort sort);

    @Override
    @NonNull
    @Query(
        value = "SELECT e FROM #{#entityName} e WHERE e.deleted = false",
        countQuery = "SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false"
    )
    Page<E> findAll(@NonNull Pageable pageable);

    @Query("SELECT e FROM #{#entityName} e")
    List<E> findAllIncludingDeleted();

    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<E> findAllDeleted();

    @Override
    @NonNull
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    Optional<E> findById(@NonNull @Param("id") I id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<E> findByIdIncludingDeleted(@Param("id") I id);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void softDeleteById(@Param("id") I id);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null WHERE e.id = :id")
    void restoreById(@Param("id") I id);

    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.id = :id")
    void hardDeleteById(@Param("id") I id);

    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.deleted = true AND e.deletedAt < CURRENT_TIMESTAMP - :days DAY")
    int permanentlyDeleteOldSoftDeleted(@Param("days") int days);

    @Override
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false")
    long count();

    @Query("SELECT COUNT(e) FROM #{#entityName} e")
    long countIncludingDeleted();

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = true")
    long countDeleted();

    @Override
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    boolean existsById(@NonNull @Param("id") I id);
}

