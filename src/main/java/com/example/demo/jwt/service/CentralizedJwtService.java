package com.example.demo.jwt.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.config.JwtUtil;
import com.example.demo.jwt.exception.JwtTokenRevokedException;
import com.example.demo.jwt.model.JwtResponse;
import com.example.demo.jwt.model.TokenRevocationRequest;
import com.example.demo.jwt.model.TokenRevocationResponse;
import com.example.demo.jwt.model.TokenValidationRequest;
import com.example.demo.jwt.model.TokenValidationResponse;

/**
 * Centralized JWT Service
 * Manages JWT token generation, validation, and revocation
 */
@Service
public class CentralizedJwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(CentralizedJwtService.class);
    private static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String JWT_ACTIVE_PREFIX = "jwt:active:";
    private static final String USER_TOKENS_PREFIX = "user:tokens:";
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * Generate JWT token for authenticated user
     */
    public JwtResponse generateToken(String username, String role) {
        try {
            logger.info("Generating JWT token for user: {}", username);
            
            // Generate the JWT token
            String token = jwtUtil.generateToken(username, role);
            
            // Store token in active tokens cache
            storeActiveToken(token, username);
            
            // Store user's token mapping
            addTokenToUser(username, token);
            
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
            if (isTokenBlacklisted(token)) {
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
            // Check blacklist first (faster)
            if (isTokenBlacklisted(token)) {
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
        try {
            String userTokensKey = USER_TOKENS_PREFIX + username;
            Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
            return tokens != null ? tokens.size() : 0;
        } catch (Exception e) {
            logger.error("Error getting active token count for user: {}", username, e);
            return 0;
        }
    }
    
    // Private helper methods
    
    private void storeActiveToken(String token, String username) {
        try {
            String activeTokenKey = JWT_ACTIVE_PREFIX + token;
            long expirationSeconds = JwtUtil.getJwtExpiration() / 1000;
            
            redisTemplate.opsForValue().set(
                activeTokenKey, 
                username, 
                Duration.ofSeconds(expirationSeconds)
            );
        } catch (Exception e) {
            logger.warn("Failed to store active token in cache", e);
        }
    }
    
    private void addTokenToUser(String username, String token) {
        try {
            String userTokensKey = USER_TOKENS_PREFIX + username;
            redisTemplate.opsForSet().add(userTokensKey, token);
            
            // Set expiration for user tokens set
            long expirationSeconds = JwtUtil.getJwtExpiration() / 1000;
            redisTemplate.expire(userTokensKey, Duration.ofSeconds(expirationSeconds));
        } catch (Exception e) {
            logger.warn("Failed to add token to user mapping", e);
        }
    }
    
    private boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = JWT_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            logger.warn("Error checking token blacklist status", e);
            return false; // Fail open for availability
        }
    }
    
    private boolean revokeSingleToken(String token, String username) {
        try {
            // Add to blacklist
            String blacklistKey = JWT_BLACKLIST_PREFIX + token;
            Date expiration = jwtUtil.extractExpiration(token);
            long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(blacklistKey, username, Duration.ofSeconds(ttlSeconds));
            }
            
            // Remove from active tokens
            String activeTokenKey = JWT_ACTIVE_PREFIX + token;
            redisTemplate.delete(activeTokenKey);
            
            // Remove from user tokens
            String userTokensKey = USER_TOKENS_PREFIX + username;
            redisTemplate.opsForSet().remove(userTokensKey, token);
            
            return true;
        } catch (Exception e) {
            logger.error("Error revoking single token", e);
            return false;
        }
    }
    
    private int revokeAllUserTokens(String username) {
        try {
            String userTokensKey = USER_TOKENS_PREFIX + username;
            Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
            
            if (tokens == null || tokens.isEmpty()) {
                return 0;
            }
            
            int revokedCount = 0;
            for (String token : tokens) {
                if (revokeSingleToken(token, username)) {
                    revokedCount++;
                }
            }
            
            // Clear user tokens set
            redisTemplate.delete(userTokensKey);
            
            return revokedCount;
        } catch (Exception e) {
            logger.error("Error revoking all user tokens", e);
            return 0;
        }
    }
}
