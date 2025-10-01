package com.workoutplanner.workoutplanner.dto.response;

public class WorkoutExerciseResponse {

    private Long workoutExerciseId;
    private Long exerciseId;
    private String exerciseName;
    private Integer orderInWorkout;
    private String notes;

    // Constructors
    public WorkoutExerciseResponse() {
    }

    public WorkoutExerciseResponse(Long workoutExerciseId, Long exerciseId, String exerciseName, 
                                  Integer orderInWorkout, String notes) {
        this.workoutExerciseId = workoutExerciseId;
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.orderInWorkout = orderInWorkout;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getWorkoutExerciseId() {
        return workoutExerciseId;
    }

    public void setWorkoutExerciseId(Long workoutExerciseId) {
        this.workoutExerciseId = workoutExerciseId;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Integer getOrderInWorkout() {
        return orderInWorkout;
    }

    public void setOrderInWorkout(Integer orderInWorkout) {
        this.orderInWorkout = orderInWorkout;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
