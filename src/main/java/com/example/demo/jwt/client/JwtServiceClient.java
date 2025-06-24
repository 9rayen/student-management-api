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
    public JwtResponse generateToken(String username, String role) {
        try {
            String url = jwtProperties.getCentralizedService().getFullGenerateUrl();
            
            JwtGenerateRequest request = new JwtGenerateRequest(username, role, 
                jwtProperties.getExpiration(), jwtProperties.getIssuer());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<JwtGenerateRequest> entity = new HttpEntity<>(request, headers);
            
            logger.debug("Generating JWT token for user: {} with role: {}", username, role);
            
            ResponseEntity<JwtResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, JwtResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Successfully generated JWT token for user: {}", username);
                return response.getBody();
            } else {
                logger.error("Failed to generate JWT token. Status: {}", response.getStatusCode());
                throw new JwtServiceException("Failed to generate JWT token from centralized service");
            }
            
        } catch (RestClientException e) {
            logger.error("Error communicating with JWT service for token generation: {}", e.getMessage());
            throw new JwtServiceException("JWT service communication error during token generation", e);
        }
    }

    /**
     * Validate JWT token using centralized service
     */
    public JwtValidationResponse validateToken(String token) {
        try {
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
            
        } catch (RestClientException e) {
            logger.error("Error communicating with JWT service for token validation: {}", e.getMessage());
            return new JwtValidationResponse(false, null, null, "JWT service communication error");
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
        private String role;
        private Long expirationMs;
        private String issuer;

        public JwtGenerateRequest() {}

        public JwtGenerateRequest(String username, String role, Long expirationMs, String issuer) {
            this.username = username;
            this.role = role;
            this.expirationMs = expirationMs;
            this.issuer = issuer;
        }

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(Long expirationMs) { this.expirationMs = expirationMs; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
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
