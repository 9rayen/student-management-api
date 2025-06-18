package com.example.demo.jwt.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.config.JwtUtil;
import com.example.demo.jwt.exception.JwtTokenRevokedException;
import com.example.demo.jwt.model.JwtResponse;
import com.example.demo.jwt.model.TokenRevocationRequest;
import com.example.demo.jwt.model.TokenRevocationResponse;
import com.example.demo.jwt.model.TokenValidationRequest;
import com.example.demo.jwt.model.TokenValidationResponse;

/**
 * In-Memory JWT Service (Fallback)
 * Used when Redis is not available - stores tokens in memory
 * WARNING: This is for development only and will not work in clustered environments
 */
@Service
public class InMemoryJwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryJwtService.class);
    
    // In-memory storage (not suitable for production)
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();
    private final Map<String, String> blacklistedTokens = new ConcurrentHashMap<>();
    private final Map<String, java.util.Set<String>> userTokens = new ConcurrentHashMap<>();
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Generate JWT token for authenticated user
     */
    public JwtResponse generateToken(String username, String role) {
        try {
            logger.info("Generating JWT token for user: {}", username);
            
            // Generate the JWT token
            String token = jwtUtil.generateToken(username, role);
            
            // Store token in active tokens cache
            activeTokens.put(token, username);
            
            // Store user's token mapping
            userTokens.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(token);
            
            // Create response
            JwtResponse response = new JwtResponse(
                token, 
                username, 
                role, 
                JwtUtil.getJwtExpiration()
            );
            
            logger.info("JWT token generated successfully for user: {}", username);
            return response;
            
        } catch (Exception e) {
            logger.error("Error generating JWT token for user: {}", username, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
    
    /**
     * Validate JWT token
     */
    public TokenValidationResponse validateToken(TokenValidationRequest request) {
        try {
            String token = request.getToken();
            logger.debug("Validating JWT token");
            
            // Check if token is blacklisted
            if (blacklistedTokens.containsKey(token)) {
                logger.warn("Attempt to use blacklisted token");
                throw new JwtTokenRevokedException("Token has been revoked");
            }
            
            // Extract username from token
            String username = jwtUtil.extractUsername(token);
            
            // Validate token
            boolean isValid = jwtUtil.validateToken(token, username);
            
            if (!isValid) {
                logger.warn("Invalid JWT token for user: {}", username);
                return TokenValidationResponse.invalid("Token validation failed");
            }
            
            // Check if username matches request (if provided)
            if (request.getUsername() != null && !request.getUsername().equals(username)) {
                logger.warn("Username mismatch in token validation: expected {}, found {}", 
                    request.getUsername(), username);
                return TokenValidationResponse.invalid("Username mismatch");
            }
            
            // Get token details
            String role = jwtUtil.extractRole(token);
            Date expiration = jwtUtil.extractExpiration(token);
            LocalDateTime expiresAt = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
            
            logger.debug("JWT token validated successfully for user: {}", username);
            return TokenValidationResponse.valid(username, role, expiresAt);
            
        } catch (JwtTokenRevokedException e) {
            return TokenValidationResponse.invalid("Token has been revoked");
        } catch (Exception e) {
            logger.error("Error validating JWT token", e);
            if (e.getMessage().contains("expired")) {
                return TokenValidationResponse.expired();
            } else if (e.getMessage().contains("malformed") || e.getMessage().contains("invalid")) {
                return TokenValidationResponse.malformed();
            }
            return TokenValidationResponse.invalid("Token validation error: " + e.getMessage());
        }
    }
    
    /**
     * Revoke JWT token(s)
     */
    public TokenRevocationResponse revokeToken(TokenRevocationRequest request) {
        try {
            String token = request.getToken();
            logger.info("Revoking JWT token");
            
            // Get username from token
            String username = jwtUtil.extractUsername(token);
            
            if (request.isRevokeAllUserTokens()) {
                // Revoke all tokens for the user
                int revokedCount = revokeAllUserTokens(username);
                logger.info("Revoked {} tokens for user: {}", revokedCount, username);
                
                return TokenRevocationResponse.success(
                    "All tokens revoked successfully for user: " + username,
                    username,
                    revokedCount
                );
            } else {
                // Revoke single token
                boolean revoked = revokeSingleToken(token, username);
                
                if (revoked) {
                    logger.info("JWT token revoked successfully for user: {}", username);
                    return TokenRevocationResponse.success(
                        "Token revoked successfully",
                        username,
                        1
                    );
                } else {
                    return TokenRevocationResponse.failure("Token was already revoked or invalid");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error revoking JWT token", e);
            return TokenRevocationResponse.failure("Failed to revoke token: " + e.getMessage());
        }
    }
    
    /**
     * Check if token is valid and not revoked
     */
    public boolean isTokenValidAndActive(String token) {
        try {
            // Check blacklist first
            if (blacklistedTokens.containsKey(token)) {
                return false;
            }
            
            // Extract username and validate
            String username = jwtUtil.extractUsername(token);
            return jwtUtil.validateToken(token, username);
            
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get active tokens count for a user
     */
    public long getUserActiveTokenCount(String username) {
        java.util.Set<String> tokens = userTokens.get(username);
        return tokens != null ? tokens.size() : 0;
    }
    
    // Private helper methods
    
    private boolean revokeSingleToken(String token, String username) {
        try {
            // Add to blacklist
            blacklistedTokens.put(token, username);
            
            // Remove from active tokens
            activeTokens.remove(token);
            
            // Remove from user tokens
            java.util.Set<String> tokens = userTokens.get(username);
            if (tokens != null) {
                tokens.remove(token);
                if (tokens.isEmpty()) {
                    userTokens.remove(username);
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error revoking single token", e);
            return false;
        }
    }
    
    private int revokeAllUserTokens(String username) {
        try {
            java.util.Set<String> tokens = userTokens.get(username);
            
            if (tokens == null || tokens.isEmpty()) {
                return 0;
            }
            
            int revokedCount = 0;
            for (String token : tokens) {
                blacklistedTokens.put(token, username);
                activeTokens.remove(token);
                revokedCount++;
            }
            
            // Clear user tokens
            userTokens.remove(username);
            
            return revokedCount;
        } catch (Exception e) {
            logger.error("Error revoking all user tokens", e);
            return 0;
        }
    }

    public Map<String, String> getActiveTokens() {
        return activeTokens;
    }
}
