package com.example.demo.jwt.controller;

import com.example.demo.jwt.model.*;
import com.example.demo.jwt.service.InMemoryJwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized JWT Controller
 * Provides REST APIs for JWT token management
 */
@RestController
@RequestMapping("/api/v1/jwt")
@Tag(name = "JWT Management", description = "Centralized JWT token management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CentralizedJwtController {
    
    private static final Logger logger = LoggerFactory.getLogger(CentralizedJwtController.class);
    
    @Autowired
    private InMemoryJwtService jwtService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * Generate JWT Token (Login)
     */
    @Operation(
        summary = "Generate JWT Token", 
        description = "Authenticate user and generate JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token generated successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateToken(
            @Valid @RequestBody @Parameter(description = "User credentials") JwtRequest jwtRequest) {
        
        logger.info("JWT token generation request for user: {}", jwtRequest.getUsername());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    jwtRequest.getUsername(),
                    jwtRequest.getPassword()
                )
            );
            
            // Extract role from authorities
            String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");
            
            // Generate token
            JwtResponse jwtResponse = jwtService.generateToken(jwtRequest.getUsername(), role);
            
            // Create success response
            Map<String, Object> response = createSuccessResponse(
                "JWT token generated successfully",
                jwtResponse
            );
            
            logger.info("JWT token generated successfully for user: {}", jwtRequest.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user: {} - Invalid credentials", jwtRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Invalid username or password", "AUTHENTICATION_FAILED"));
                
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user: {} - {}", jwtRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Authentication failed", "AUTHENTICATION_ERROR"));
                
        } catch (Exception e) {
            logger.error("Error generating JWT token for user: {}", jwtRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Internal server error occurred", "INTERNAL_ERROR"));
        }
    }
    
    /**
     * Validate JWT Token
     */
    @Operation(
        summary = "Validate JWT Token", 
        description = "Validate JWT token and return token information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token validation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @Valid @RequestBody @Parameter(description = "Token validation request") TokenValidationRequest request) {
        
        logger.debug("JWT token validation request");
        
        try {
            TokenValidationResponse validationResponse = jwtService.validateToken(request);
            
            Map<String, Object> response = createSuccessResponse(
                "Token validation completed",
                validationResponse
            );
            
            logger.debug("JWT token validation completed - Valid: {}", validationResponse.isValid());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error validating JWT token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error validating token", "VALIDATION_ERROR"));
        }
    }
    
    /**
     * Validate JWT Token via Header
     */
    @Operation(
        summary = "Validate JWT Token via Header", 
        description = "Validate JWT token provided in Authorization header"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token validation completed"),
        @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header")
    })
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateTokenFromHeader(
            @RequestHeader("Authorization") @Parameter(description = "Authorization header with Bearer token") String authHeader) {
        
        logger.debug("JWT token validation request via header");
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid Authorization header format", "INVALID_HEADER"));
            }
            
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            TokenValidationRequest request = new TokenValidationRequest(token);
            TokenValidationResponse validationResponse = jwtService.validateToken(request);
            
            Map<String, Object> response = createSuccessResponse(
                "Token validation completed",
                validationResponse
            );
            
            logger.debug("JWT token validation completed via header - Valid: {}", validationResponse.isValid());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error validating JWT token from header", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error validating token", "VALIDATION_ERROR"));
        }
    }
    
    /**
     * Revoke JWT Token
     */
    @Operation(
        summary = "Revoke JWT Token", 
        description = "Revoke JWT token(s) - single token or all user tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token revocation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    @PostMapping("/revoke")
    public ResponseEntity<Map<String, Object>> revokeToken(
            @Valid @RequestBody @Parameter(description = "Token revocation request") TokenRevocationRequest request) {
        
        logger.info("JWT token revocation request");
        
        try {
            TokenRevocationResponse revocationResponse = jwtService.revokeToken(request);
            
            Map<String, Object> response = createSuccessResponse(
                "Token revocation completed",
                revocationResponse
            );
            
            logger.info("JWT token revocation completed - Success: {}", revocationResponse.isRevoked());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error revoking JWT token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error revoking token", "REVOCATION_ERROR"));
        }
    }
    
    /**
     * Get JWT Service Status
     */
    @Operation(
        summary = "Get JWT Service Status", 
        description = "Get status and statistics of the JWT service"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service status retrieved successfully")
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("service", "Centralized JWT Service");
            status.put("status", "RUNNING");
            status.put("timestamp", LocalDateTime.now());
            status.put("version", "1.0.0");
            status.put("storageType", "IN_MEMORY"); // or "REDIS" when available
            
            Map<String, Object> response = createSuccessResponse(
                "JWT service status retrieved successfully",
                status
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting JWT service status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error getting service status", "STATUS_ERROR"));
        }
    }
    
    /**
     * Get User Token Statistics
     */
    @Operation(
        summary = "Get User Token Statistics", 
        description = "Get token statistics for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User statistics retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Missing username parameter")
    })
    @GetMapping("/user/{username}/stats")
    public ResponseEntity<Map<String, Object>> getUserTokenStats(
            @PathVariable @Parameter(description = "Username") String username) {
        
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("username", username);
            stats.put("activeTokenCount", jwtService.getUserActiveTokenCount(username));
            stats.put("timestamp", LocalDateTime.now());
            
            Map<String, Object> response = createSuccessResponse(
                "User token statistics retrieved successfully",
                stats
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting user token statistics for: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error getting user statistics", "STATS_ERROR"));
        }
    }
    
    // Helper methods for creating standardized responses
    
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("data", data);
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String message, String errorCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errorCode", errorCode);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
