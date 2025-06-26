package com.example.demo.jwt.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.demo.jwt.config.JwtProperties;

/**
 * Client service for communicating with the centralized JWT service
 */
@Service
public class JwtServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceClient.class);

    private final RestTemplate restTemplate;
    private final JwtProperties jwtProperties;

    public JwtServiceClient(@Qualifier("jwtServiceRestTemplate") RestTemplate restTemplate,
                           JwtProperties jwtProperties) {
        this.restTemplate = restTemplate;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Generate JWT token using centralized service
     */
    @SuppressWarnings({"java:S2142", "java:S2273"}) // Sleep in loop is acceptable for retry logic
    public JwtResponse generateToken(String username, String role) {
        int retries = jwtProperties.getCentralizedService().getMaxRetries();
        
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                return attemptTokenGeneration(username);
            } catch (RestClientException e) {
                logger.warn("Attempt {}/{} failed for JWT token generation: {}", attempt, retries, e.getMessage());
                
                if (attempt == retries) {
                    logger.error("All {} attempts failed for JWT token generation", retries);
                    throw new JwtServiceException("JWT service communication error after " + retries + " attempts", e);
                }
                
                // Wait before retry with exponential backoff (avoiding Thread.sleep in loop)
                if (attempt < retries) {
                    try {
                        long delay = Math.min(1000L * (1L << (attempt - 1)), 5000L); // Cap at 5 seconds
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new JwtServiceException("JWT service communication interrupted", ie);
                    }
                }
            }
        }
        
        throw new JwtServiceException("Failed to generate token after " + retries + " attempts");
    }

    /**
     * Internal method to attempt token generation
     */
    private JwtResponse attemptTokenGeneration(String username) {
        String url = jwtProperties.getCentralizedService().getFullGenerateUrl();
        
        // Send only username as per centralized service specification
        JwtGenerateRequest request = new JwtGenerateRequest(username);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<JwtGenerateRequest> entity = new HttpEntity<>(request, headers);
        
        logger.debug("Generating JWT token for user: {}", username);
        
        ResponseEntity<CentralizedJwtResponse> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, CentralizedJwtResponse.class);
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            CentralizedJwtResponse centralizedResponse = response.getBody();
            
            // Check if the response indicates success
            if (centralizedResponse != null && centralizedResponse.isSuccess() && centralizedResponse.getData() != null) {
                logger.debug("Successfully generated JWT token for user: {}", username);
                
                // Extract data from nested response structure
                CentralizedJwtResponse.JwtData data = centralizedResponse.getData();
                if (data != null && data.getToken() != null && data.getExpiresIn() != null) {
                    return new JwtResponse(data.getToken(), data.getType(), data.getExpiresIn(), "Success");
                } else {
                    throw new JwtServiceException("Invalid response data from centralized service - missing token or expiration");
                }
            } else {
                String errorMsg = centralizedResponse != null ? centralizedResponse.getMessage() : "Unknown error";
                logger.error("Failed to generate JWT token. Response indicates failure: {}", errorMsg);
                throw new JwtServiceException("Failed to generate JWT token from centralized service: " + errorMsg);
            }
        } else {
            logger.error("Failed to generate JWT token. Status: {}", response.getStatusCode());
            throw new JwtServiceException("Failed to generate JWT token from centralized service");
        }
    }

    /**
     * Validate JWT token using centralized service
     */
    @SuppressWarnings({"java:S2142", "java:S2273"}) // Sleep in loop is acceptable for retry logic
    public JwtValidationResponse validateToken(String token) {
        int retries = jwtProperties.getCentralizedService().getMaxRetries();
        
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                return attemptTokenValidation(token);
            } catch (RestClientException e) {
                logger.warn("Attempt {}/{} failed for JWT token validation: {}", attempt, retries, e.getMessage());
                
                if (attempt == retries) {
                    logger.error("All {} attempts failed for JWT token validation", retries);
                    return new JwtValidationResponse(false, null, null, "JWT service communication error after " + retries + " attempts");
                }
                
                // Wait before retry with exponential backoff
                if (attempt < retries) {
                    try {
                        long delay = Math.min(1000L * (1L << (attempt - 1)), 5000L); // Cap at 5 seconds
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new JwtValidationResponse(false, null, null, "JWT service communication interrupted");
                    }
                }
            }
        }
        
        return new JwtValidationResponse(false, null, null, "Failed to validate token after " + retries + " attempts");
    }

    /**
     * Internal method to attempt token validation
     */
    private JwtValidationResponse attemptTokenValidation(String token) {
        String url = jwtProperties.getCentralizedService().getFullValidateUrl();
        
        JwtValidateRequest request = new JwtValidateRequest(token);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<JwtValidateRequest> entity = new HttpEntity<>(request, headers);
        
        logger.debug("Validating JWT token");
        
        ResponseEntity<JwtValidationResponse> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, JwtValidationResponse.class);
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            logger.debug("Successfully validated JWT token");
            return response.getBody();
        } else {
            logger.warn("JWT token validation failed. Status: {}", response.getStatusCode());
            return new JwtValidationResponse(false, null, null, "Token validation failed");
        }
    }

    /**
     * Revoke JWT token using centralized service
     */
    public boolean revokeToken(String token) {
        try {
            String url = jwtProperties.getCentralizedService().getFullRevokeUrl();
            
            JwtRevokeRequest request = new JwtRevokeRequest(token);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<JwtRevokeRequest> entity = new HttpEntity<>(request, headers);
            
            logger.debug("Revoking JWT token");            ResponseEntity<?> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Object.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("Successfully revoked JWT token");
                return true;
            } else {
                logger.error("Failed to revoke JWT token. Status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (RestClientException e) {
            logger.error("Error communicating with JWT service for token revocation: {}", e.getMessage());
            return false;
        }
    }

    // Request/Response DTOs

    public static class JwtGenerateRequest {
        private String username;

        public JwtGenerateRequest() {}

        public JwtGenerateRequest(String username) {
            this.username = username;
        }

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class JwtValidateRequest {
        private String token;

        public JwtValidateRequest() {}

        public JwtValidateRequest(String token) {
            this.token = token;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class JwtRevokeRequest {
        private String token;

        public JwtRevokeRequest() {}

        public JwtRevokeRequest(String token) {
            this.token = token;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class JwtResponse {
        private String token;
        private String type;
        private Long expiresIn;
        private String message;

        public JwtResponse() {}

        public JwtResponse(String token, String type, Long expiresIn, String message) {
            this.token = token;
            this.type = type;
            this.expiresIn = expiresIn;
            this.message = message;
        }

        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class JwtValidationResponse {
        private Boolean valid;
        private String username;
        private String role;
        private String message;

        public JwtValidationResponse() {}

        public JwtValidationResponse(Boolean valid, String username, String role, String message) {
            this.valid = valid;
            this.username = username;
            this.role = role;
            this.message = message;
        }

        // Getters and setters
        public Boolean getValid() { return valid; }
        public void setValid(Boolean valid) { this.valid = valid; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Response structure for centralized JWT service
     * Matches format: {success: true, data: {token, type, expiresIn, username, role}}
     */
    public static class CentralizedJwtResponse {
        private boolean success;
        private JwtData data;
        private String message;

        public CentralizedJwtResponse() {}

        public CentralizedJwtResponse(boolean success, JwtData data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public JwtData getData() { return data; }
        public void setData(JwtData data) { this.data = data; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        /**
         * Inner data structure containing JWT information
         */
        public static class JwtData {
            private String token;
            private String type;
            private String username;
            private String role;
            private Long expiresIn;

            public JwtData() {}

            public JwtData(String token, String type, String username, String role, Long expiresIn) {
                this.token = token;
                this.type = type;
                this.username = username;
                this.role = role;
                this.expiresIn = expiresIn;
            }

            // Getters and setters
            public String getToken() { return token; }
            public void setToken(String token) { this.token = token; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
            public Long getExpiresIn() { return expiresIn; }
            public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        }
    }

    /**
     * Custom exception for JWT service communication errors
     */
    public static class JwtServiceException extends RuntimeException {
        public JwtServiceException(String message) {
            super(message);
        }

        public JwtServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
