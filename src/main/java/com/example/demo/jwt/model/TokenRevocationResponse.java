package com.example.demo.jwt.model;

import java.time.LocalDateTime;

/**
 * Token Revocation Response Model
 * Contains the result of token revocation
 */
public class TokenRevocationResponse {
    
    private boolean revoked;
    private String message;
    private String username;
    private int tokensRevoked; // Number of tokens revoked
    private LocalDateTime revokedAt;
    
    // Default constructor
    public TokenRevocationResponse() {
        this.revokedAt = LocalDateTime.now();
    }
    
    // Constructor
    public TokenRevocationResponse(boolean revoked, String message) {
        this();
        this.revoked = revoked;
        this.message = message;
        this.tokensRevoked = revoked ? 1 : 0;
    }
    
    // Constructor with username and tokens count
    public TokenRevocationResponse(boolean revoked, String message, String username, int tokensRevoked) {
        this();
        this.revoked = revoked;
        this.message = message;
        this.username = username;
        this.tokensRevoked = tokensRevoked;
    }
    
    // Static factory methods
    public static TokenRevocationResponse success(String message) {
        return new TokenRevocationResponse(true, message);
    }
    
    public static TokenRevocationResponse success(String message, String username, int tokensRevoked) {
        return new TokenRevocationResponse(true, message, username, tokensRevoked);
    }
    
    public static TokenRevocationResponse failure(String message) {
        return new TokenRevocationResponse(false, message);
    }
    
    public boolean isRevoked() {
        return revoked;
    }
    
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getTokensRevoked() {
        return tokensRevoked;
    }
    
    public void setTokensRevoked(int tokensRevoked) {
        this.tokensRevoked = tokensRevoked;
    }
    
    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
    
    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
    
    @Override
    public String toString() {
        return "TokenRevocationResponse{" +
                "revoked=" + revoked +
                ", message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", tokensRevoked=" + tokensRevoked +
                ", revokedAt=" + revokedAt +
                '}';
    }
}
