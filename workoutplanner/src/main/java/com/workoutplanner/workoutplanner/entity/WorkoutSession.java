package com.workoutplanner.workoutplanner.entity;

import com.workoutplanner.workoutplanner.enums.WorkoutStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkoutStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "actual_duration_in_minutes")
    private Integer actualDurationInMinutes;

    @Column(name = "session_notes", length = 1000)
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
