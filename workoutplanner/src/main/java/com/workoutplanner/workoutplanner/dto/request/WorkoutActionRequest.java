package com.workoutplanner.workoutplanner.dto.request;

import jakarta.validation.constraints.NotBlank;

public class WorkoutActionRequest {
    @NotBlank(message = "Action cannot be blank")
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
