package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.JwtUtil;
import com.example.demo.jwt.client.JwtServiceClient;
import com.example.demo.jwt.config.JwtProperties;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Authentication Controller for JWT token management with centralized service support
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "JWT Authentication endpoints")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtServiceClient jwtServiceClient;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * Login endpoint to authenticate user and return JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user credentials
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword())
            );            // Load user details
            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            
            // Log user authorities for debugging
            logger.debug("User '{}' has authorities: {}", loginRequest.getUsername(), userDetails.getAuthorities());
            
            // Extract role from authorities - prioritize ADMIN over USER
            String role = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .peek(auth -> logger.debug("Found authority without ROLE_ prefix: {}", auth))
                .filter(auth -> "ADMIN".equals(auth)) // First try to find ADMIN
                .findFirst()
                .orElse(userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("USER"));
                
            logger.debug("Selected role for user '{}': {}", loginRequest.getUsername(), role);            // Generate JWT token using centralized service or fallback to local
            String jwt = null;
            Long expiresIn = null;
            
            if (jwtProperties.isEnableCentralizedService()) {
                try {
                    JwtServiceClient.JwtResponse jwtResponse = jwtServiceClient.generateToken(userDetails.getUsername(), role);
                    jwt = jwtResponse.getToken();
                    expiresIn = jwtResponse.getExpiresIn();
                    logger.info("Successfully generated JWT token via centralized service for user: {}", userDetails.getUsername());
                } catch (Exception e) {
                    logger.warn("Centralized JWT service failed: {}", e.getMessage());
                    // Fallback to local JWT generation if centralized service fails
                    if (jwtProperties.getCentralizedService().isEnableFallback()) {
                        logger.info("Falling back to local JWT generation for user: {}", userDetails.getUsername());
                        jwt = jwtUtil.generateToken(userDetails.getUsername(), role);
                        expiresIn = jwtProperties.getExpiration();
                    } else {
                        throw new RuntimeException("Centralized JWT service is unavailable and fallback is disabled", e);
                    }
                }
            } else {
                // Use local JWT generation
                logger.info("Using local JWT generation for user: {}", userDetails.getUsername());
                jwt = jwtUtil.generateToken(userDetails.getUsername(), role);
                expiresIn = jwtProperties.getExpiration();
            }
            
            // Ensure we have a token before returning
            if (jwt == null || expiresIn == null) {
                throw new RuntimeException("Failed to generate JWT token");
            }// Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities().toString());
            response.put("expiresIn", expiresIn);
            response.put("issuedAt", LocalDateTime.now());

            return ResponseEntity.ok(response);        } catch (BadCredentialsException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication Failed");
            errorResponse.put("message", "Invalid username or password. Please check your credentials and try again.");
            errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("path", "/api/v1/auth/login");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (IllegalArgumentException | NullPointerException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication Error");
            errorResponse.put("message", "An unexpected error occurred during authentication. Please try again.");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("path", "/api/v1/auth/login");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }    /**
     * Endpoint to validate JWT token
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate JWT Token", description = "Validate the provided JWT token")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    })
    public ResponseEntity<?> validateToken(
            @Parameter(description = "Authorization header with Bearer token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {                String token = authHeader.substring(7);
                
                // Validate token using centralized service or fallback to local
                Map<String, Object> response = new HashMap<>();
                
                if (jwtProperties.isEnableCentralizedService()) {
                    try {
                        JwtServiceClient.JwtValidationResponse validationResponse = jwtServiceClient.validateToken(token);
                        
                        if (validationResponse.getValid() != null && validationResponse.getValid()) {
                            response.put("valid", true);
                            response.put("username", validationResponse.getUsername());
                            response.put("role", validationResponse.getRole());
                            response.put("message", validationResponse.getMessage());
                            
                            return ResponseEntity.ok(response);
                        } else {
                            response.put("valid", false);
                            response.put("error", "Token validation failed");
                            response.put("message", validationResponse.getMessage() != null ? 
                                validationResponse.getMessage() : "Token is invalid or expired");
                            
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                        }
                    } catch (Exception e) {
                        // Fallback to local validation if centralized service fails
                        if (jwtProperties.getCentralizedService().isEnableFallback()) {
                            return validateTokenLocally(token);
                        } else {
                            response.put("valid", false);
                            response.put("error", "JWT service validation failed");
                            response.put("message", "Centralized JWT service is unavailable and fallback is disabled");
                            
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                        }
                    }
                } else {
                    // Use local JWT validation
                    return validateTokenLocally(token);
                }
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("valid", false);
                errorResponse.put("error", "Missing or invalid Authorization header");
                errorResponse.put("message", "Authorization header must start with 'Bearer '");
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", "Token validation failed");
            errorResponse.put("message", e.getMessage());
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Helper method for local JWT token validation
     */
    private ResponseEntity<?> validateTokenLocally(String token) {
        try {
            // Extract username from token
            String username = jwtUtil.extractUsername(token);
            
            // Validate token with extracted username
            if (jwtUtil.validateToken(token, username)) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", username);
                response.put("role", jwtUtil.extractRole(token));
                response.put("expiresAt", jwtUtil.extractExpiration(token));
                response.put("message", "Token is valid");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("valid", false);
                errorResponse.put("error", "Token validation failed");
                errorResponse.put("message", "Token is invalid or expired");
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", "Token validation failed");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Endpoint to revoke JWT token
     */
    @PostMapping("/revoke")
    @Operation(summary = "Revoke JWT Token", description = "Revoke the provided JWT token")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token revoked successfully", 
                    content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Token is invalid or already revoked")
    })
    public ResponseEntity<?> revokeToken(
            @Parameter(description = "Authorization header with Bearer token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                Map<String, Object> response = new HashMap<>();
                
                if (jwtProperties.isEnableCentralizedService()) {
                    try {
                        boolean revoked = jwtServiceClient.revokeToken(token);
                        
                        if (revoked) {
                            response.put("success", true);
                            response.put("message", "Token revoked successfully");
                            return ResponseEntity.ok(response);
                        } else {
                            response.put("success", false);
                            response.put("error", "Token revocation failed");
                            response.put("message", "Failed to revoke token via centralized service");
                            
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                    } catch (Exception e) {
                        response.put("success", false);
                        response.put("error", "JWT service error");
                        response.put("message", "Error communicating with centralized JWT service");
                        
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                } else {
                    // Local token revocation is not supported in this implementation
                    response.put("success", false);
                    response.put("error", "Revocation not supported");
                    response.put("message", "Token revocation is only supported with centralized JWT service");
                    
                    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
                }
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Missing or invalid Authorization header");
                errorResponse.put("message", "Authorization header must start with 'Bearer '");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Token revocation failed");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Login Request DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;

        // Constructors
        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "LoginRequest{" +
                    "username='" + username + '\'' +
                    ", password='[PROTECTED]'" +
                    '}';
        }
    }
}
