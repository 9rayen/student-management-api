package com.example.demo.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties
 * Configuration properties for JWT token management with centralized service support
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT secret key for signing tokens (used as fallback)
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
     * Enable centralized JWT service
     */
    private boolean enableCentralizedService = true;
    
    /**
     * Centralized JWT service configuration
     */
    private CentralizedService centralizedService = new CentralizedService();
    
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
    
    public boolean isEnableCentralizedService() {
        return enableCentralizedService;
    }
    
    public void setEnableCentralizedService(boolean enableCentralizedService) {
        this.enableCentralizedService = enableCentralizedService;
    }
    
    public CentralizedService getCentralizedService() {
        return centralizedService;
    }
    
    public void setCentralizedService(CentralizedService centralizedService) {
        this.centralizedService = centralizedService;
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
     * Centralized JWT Service configuration nested class
     */
    public static class CentralizedService {
        
        private String baseUrl = "http://localhost:8091";
        private String generateEndpoint = "/api/v1/jwt/generate";
        private String validateEndpoint = "/api/v1/jwt/validate";
        private String revokeEndpoint = "/api/v1/jwt/revoke";
        private int connectionTimeout = 5000; // 5 seconds
        private int readTimeout = 10000; // 10 seconds
        private int maxRetries = 3;
        private boolean enableFallback = true;
        
        // Getters and Setters
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getGenerateEndpoint() {
            return generateEndpoint;
        }
        
        public void setGenerateEndpoint(String generateEndpoint) {
            this.generateEndpoint = generateEndpoint;
        }
        
        public String getValidateEndpoint() {
            return validateEndpoint;
        }
        
        public void setValidateEndpoint(String validateEndpoint) {
            this.validateEndpoint = validateEndpoint;
        }
        
        public String getRevokeEndpoint() {
            return revokeEndpoint;
        }
        
        public void setRevokeEndpoint(String revokeEndpoint) {
            this.revokeEndpoint = revokeEndpoint;
        }
        
        public int getConnectionTimeout() {
            return connectionTimeout;
        }
        
        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }
        
        public int getReadTimeout() {
            return readTimeout;
        }
        
        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public boolean isEnableFallback() {
            return enableFallback;
        }
        
        public void setEnableFallback(boolean enableFallback) {
            this.enableFallback = enableFallback;
        }
        
        public String getFullGenerateUrl() {
            return baseUrl + generateEndpoint;
        }
        
        public String getFullValidateUrl() {
            return baseUrl + validateEndpoint;
        }
        
        public String getFullRevokeUrl() {
            return baseUrl + revokeEndpoint;
        }
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
