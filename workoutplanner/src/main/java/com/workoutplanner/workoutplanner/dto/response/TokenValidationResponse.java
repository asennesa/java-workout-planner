package com.workoutplanner.workoutplanner.dto.response;

public class TokenValidationResponse {
    private boolean valid;
    private String username;
    private Long userId;
    private String role;

    public TokenValidationResponse(boolean valid, String username, Long userId, String role) {
        this.valid = valid;
        this.username = username;
        this.userId = userId;
        this.role = role;
    }

    public TokenValidationResponse(boolean valid) {
        this.valid = valid;
    }

    // Getters and setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
