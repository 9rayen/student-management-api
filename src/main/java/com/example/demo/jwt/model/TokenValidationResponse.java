package com.example.demo.jwt.model;

import java.time.LocalDateTime;

/**
 * Token Validation Response Model
 * Contains the result of token validation
 */
public class TokenValidationResponse {
    
    private boolean valid;
    private String username;
    private String role;
    private String message;
    private LocalDateTime validatedAt;
    private LocalDateTime expiresAt;
    private long remainingTimeMs; // remaining time in milliseconds
    
    // Default constructor
    public TokenValidationResponse() {
        this.validatedAt = LocalDateTime.now();
    }
    
    // Constructor for invalid token
    public TokenValidationResponse(boolean valid, String message) {
        this();
        this.valid = valid;
        this.message = message;
    }
    
    // Constructor for valid token
    public TokenValidationResponse(boolean valid, String username, String role, LocalDateTime expiresAt) {
        this();
        this.valid = valid;
        this.username = username;
        this.role = role;
        this.expiresAt = expiresAt;
        this.message = valid ? "Token is valid" : "Token is invalid";
        
        // Calculate remaining time
        if (expiresAt != null) {
            this.remainingTimeMs = java.time.Duration.between(LocalDateTime.now(), expiresAt).toMillis();
        }
    }
    
    // Static factory methods for common responses
    public static TokenValidationResponse valid(String username, String role, LocalDateTime expiresAt) {
        return new TokenValidationResponse(true, username, role, expiresAt);
    }
    
    public static TokenValidationResponse invalid(String message) {
        return new TokenValidationResponse(false, message);
    }
    
    public static TokenValidationResponse expired() {
        return new TokenValidationResponse(false, "Token has expired");
    }
    
    public static TokenValidationResponse malformed() {
        return new TokenValidationResponse(false, "Token is malformed");
    }
    
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
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }
    
    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
        // Recalculate remaining time when expires at is set
        if (expiresAt != null) {
            this.remainingTimeMs = java.time.Duration.between(LocalDateTime.now(), expiresAt).toMillis();
        }
    }
    
    public long getRemainingTimeMs() {
        return remainingTimeMs;
    }
    
    public void setRemainingTimeMs(long remainingTimeMs) {
        this.remainingTimeMs = remainingTimeMs;
    }
    
    @Override
    public String toString() {
        return "TokenValidationResponse{" +
                "valid=" + valid +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", message='" + message + '\'' +
                ", validatedAt=" + validatedAt +
                ", expiresAt=" + expiresAt +
                ", remainingTimeMs=" + remainingTimeMs +
                '}';
    }
}
