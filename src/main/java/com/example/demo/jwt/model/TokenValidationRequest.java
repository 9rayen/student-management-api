package com.example.demo.jwt.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Token Validation Request Model
 * Used for validating JWT tokens
 */
public class TokenValidationRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    private String username; // Optional - for additional validation
    
    // Default constructor
    public TokenValidationRequest() {
    }
    
    // Constructor with token
    public TokenValidationRequest(String token) {
        this.token = token;
    }
    
    // Constructor with token and username
    public TokenValidationRequest(String token, String username) {
        this.token = token;
        this.username = username;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    @Override
    public String toString() {
        return "TokenValidationRequest{" +
                "username='" + username + '\'' +
                ", token='[PROTECTED]'" +
                '}';
    }
}
