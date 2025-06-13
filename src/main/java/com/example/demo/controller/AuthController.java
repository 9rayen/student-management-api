package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Authentication Controller for JWT token management
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "JWT Authentication endpoints")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;    /**
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
            
            // Extract role from authorities - properly extract without ROLE_ prefix
            String role = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("USER");
            
            // Generate JWT token
            final String jwt = jwtUtil.generateToken(userDetails.getUsername(), role);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities().toString());
            response.put("expiresIn", JwtUtil.getJwtExpiration());
            response.put("issuedAt", LocalDateTime.now());

            return ResponseEntity.ok(response);        } catch (BadCredentialsException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication Failed");
            errorResponse.put("message", "Invalid username or password. Please check your credentials and try again.");
            errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("path", "/api/v1/auth/login");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);        } catch (Exception e) {
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
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
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
