package com.example.demo.jwt.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Token Revocation Request Model
 * Used for revoking JWT tokens
 */
public class TokenRevocationRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    private String reason; // Optional reason for revocation
    private boolean revokeAllUserTokens = false; // Flag to revoke all tokens for the user
    
    // Default constructor
    public TokenRevocationRequest() {
    }
    
    // Constructor with token
    public TokenRevocationRequest(String token) {
        this.token = token;
    }
    
    // Constructor with token and reason
    public TokenRevocationRequest(String token, String reason) {
        this.token = token;
        this.reason = reason;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public boolean isRevokeAllUserTokens() {
        return revokeAllUserTokens;
    }
    
    public void setRevokeAllUserTokens(boolean revokeAllUserTokens) {
        this.revokeAllUserTokens = revokeAllUserTokens;
    }
    
    @Override
    public String toString() {
        return "TokenRevocationRequest{" +
                "reason='" + reason + '\'' +
                ", revokeAllUserTokens=" + revokeAllUserTokens +
                ", token='[PROTECTED]'" +
                '}';
    }
}
