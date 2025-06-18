package com.example.demo.jwt.model;

import java.time.LocalDateTime;

/**
 * JWT Authentication Response Model
 * Contains the generated JWT token and related information
 */
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private long expiresIn; // in milliseconds
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    
    // Default constructor
    public JwtResponse() {
        this.issuedAt = LocalDateTime.now();
    }
    
    // Constructor with token only
    public JwtResponse(String token) {
        this();
        this.token = token;
    }
    
    // Full constructor
    public JwtResponse(String token, String username, String role, long expiresIn) {
        this();
        this.token = token;
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
        this.expiresAt = this.issuedAt.plusSeconds(expiresIn / 1000);
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
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
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        this.expiresAt = this.issuedAt.plusSeconds(expiresIn / 1000);
    }
    
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
    
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    @Override
    public String toString() {
        return "JwtResponse{" +
                "type='" + type + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", expiresIn=" + expiresIn +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
