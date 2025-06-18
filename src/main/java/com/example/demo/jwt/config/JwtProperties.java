package com.example.demo.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties
 * Configuration properties for JWT token management
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT secret key for signing tokens
     */
    private String secret = "mySecretKeyForJWTTokenGenerationAndValidationInStudentManagementAPI2025";
    
    /**
     * JWT token expiration time in milliseconds (default: 24 hours)
     */
    private long expiration = 86400000L; // 24 hours
    
    /**
     * JWT token issuer
     */
    private String issuer = "student-management-api";
    
    /**
     * Enable token blacklisting/revocation support
     */
    private boolean enableRevocation = true;
    
    /**
     * Redis configuration for centralized storage
     */
    private Redis redis = new Redis();
    
    /**
     * Fallback to in-memory storage if Redis is not available
     */
    private boolean fallbackToMemory = true;
    
    // Getters and Setters
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public boolean isEnableRevocation() {
        return enableRevocation;
    }
    
    public void setEnableRevocation(boolean enableRevocation) {
        this.enableRevocation = enableRevocation;
    }
    
    public Redis getRedis() {
        return redis;
    }
    
    public void setRedis(Redis redis) {
        this.redis = redis;
    }
    
    public boolean isFallbackToMemory() {
        return fallbackToMemory;
    }
    
    public void setFallbackToMemory(boolean fallbackToMemory) {
        this.fallbackToMemory = fallbackToMemory;
    }
    
    /**
     * Redis configuration nested class
     */
    public static class Redis {
        
        private String host = "localhost";
        private int port = 6379;
        private String password;
        private int database = 0;
        private int timeout = 2000;
        private boolean enabled = true;
        
        // Getters and Setters
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public int getDatabase() {
            return database;
        }
        
        public void setDatabase(int database) {
            this.database = database;
        }
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
