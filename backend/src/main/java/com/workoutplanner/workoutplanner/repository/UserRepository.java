package com.workoutplanner.workoutplanner.repository;

import com.workoutplanner.workoutplanner.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity with soft delete support.
 */
@Repository
public interface UserRepository extends SoftDeleteRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.deleted = false")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deleted = false")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND u.deleted = false")
    List<User> findByFirstNameContainingIgnoreCase(@Param("firstName") String firstName);

    @Query("SELECT u FROM User u WHERE u.auth0UserId = :auth0UserId AND u.deleted = false")
    Optional<User> findByAuth0UserId(@Param("auth0UserId") String auth0UserId);
}
