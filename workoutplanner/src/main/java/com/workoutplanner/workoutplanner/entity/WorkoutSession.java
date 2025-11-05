package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import com.workoutplanner.workoutplanner.validation.ValidationGroups;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "workout_sessions")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "workoutExercises"})
public class WorkoutSession extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id", nullable = false, updatable = false)
    private Long sessionId;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
              message = "Workout session name is required")
    @Length(min = 2, max = 100, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
            message = "Workout session name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-()]+$", 
             groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Workout session name can only contain letters, numbers, spaces, hyphens, and parentheses")
    private String name;

    @Column(name = "description", length = 1000)
    @Length(max = 1000, 
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
            message = "Description must not exceed 1000 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(groups = {ValidationGroups.Create.class}, 
             message = "User is required for workout session creation")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class}, 
             message = "Workout status is required")
    private WorkoutStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "actual_duration_in_minutes")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours (1440 minutes)")
    private Integer actualDurationInMinutes;

    @Column(name = "session_notes", length = 1000)
    @Length(max = 1000, message = "Session notes must not exceed 1000 characters")
    private String sessionNotes;

    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("orderInWorkout ASC")
    private List<WorkoutExercise> workoutExercises = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Equals method following Hibernate best practices.
     * Uses database ID for equality when entity is persisted,
     * falls back to field comparison for transient entities.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        WorkoutSession that = (WorkoutSession) o;
        
        // If both entities are persisted (have IDs), use ID for equality
        if (sessionId != null && that.sessionId != null) {
            return Objects.equals(sessionId, that.sessionId);
        }
        
        // For transient entities, compare unique fields (name, user, and startedAt)
        return Objects.equals(name, that.name) &&
               Objects.equals(user, that.user) &&
               Objects.equals(startedAt, that.startedAt);
    }

    /**
     * HashCode method following Hibernate best practices.
     * Uses database ID when entity is persisted,
     * falls back to unique fields for transient entities.
     */
    @Override
    public int hashCode() {
        // If entity is persisted, use ID for hash
        if (sessionId != null) {
            return Objects.hash(sessionId);
        }
        
        // For transient entities, use unique fields
        return Objects.hash(name, user, startedAt);
    }
}
