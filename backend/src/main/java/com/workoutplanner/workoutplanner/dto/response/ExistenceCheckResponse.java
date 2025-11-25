package com.workoutplanner.workoutplanner.dto.response;

public class ExistenceCheckResponse {
    private boolean exists;

    public ExistenceCheckResponse(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
