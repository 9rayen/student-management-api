package com.example.demo.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.jwt.client.JwtServiceClient;
import com.example.demo.jwt.config.JwtProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Filter to validate JWT tokens on each request using centralized service
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtServiceClient jwtServiceClient;

    @Autowired
    private JwtProperties jwtProperties;    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Processing request: " + method + " " + path);
        
        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(path)) {
            logger.debug("Skipping JWT processing for public endpoint: " + path);
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: " + (requestTokenHeader != null ? "Bearer [token]" : "null"));

        String username = null;
        String jwtToken = null;
        
        logger.debug("Centralized service enabled: " + jwtProperties.isEnableCentralizedService());
        
        logger.debug("=== JWT AUTHENTICATION FILTER START ===");
        logger.debug("Request path: " + path + ", method: " + method);
        logger.debug("Authorization header present: " + (requestTokenHeader != null));
        
        // JWT Token is in the form "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            logger.debug("Extracted token: " + (jwtToken != null ? "Token found (length: " + jwtToken.length() + ")" : "No token"));
            
            // Use centralized service or fallback to local validation
            if (jwtProperties.isEnableCentralizedService()) {
                logger.debug("Using centralized JWT service for validation");
                try {
                    JwtServiceClient.JwtValidationResponse validationResponse = jwtServiceClient.validateToken(jwtToken);
                    logger.debug("Centralized service response received: " + (validationResponse != null ? "Valid response" : "Null response"));
                    
                    if (validationResponse != null && validationResponse.getValid() != null && validationResponse.getValid()) {
                        username = validationResponse.getUsername();
                        logger.debug("Centralized validation SUCCESS - Username: " + username + ", Role: " + validationResponse.getRole());
                        logger.debug("JWT Token validated successfully via centralized service for user: " + username);
                    } else {
                        logger.warn("JWT Token validation failed via centralized service: " + 
                            (validationResponse != null ? validationResponse.getMessage() : "null response"));
                    }
                } catch (Exception e) {
                    logger.warn("Error validating JWT token via centralized service: " + e.getMessage());
                    
                    // Fallback to local validation if enabled
                    if (jwtProperties.getCentralizedService().isEnableFallback()) {
                        try {
                            username = jwtUtil.extractUsername(jwtToken);
                            logger.debug("Fallback to local JWT processing for user: " + username);
                        } catch (RuntimeException ex) {
                            logger.warn("JWT Token error during fallback: " + ex.getMessage());
                        }
                    }
                }
            } else {
                // Use local JWT processing
                try {
                    username = jwtUtil.extractUsername(jwtToken);
                    logger.debug("Using local JWT processing for user: " + username);
                } catch (RuntimeException e) {
                    logger.warn("JWT Token error: " + e.getMessage());
                }
            }
        } else {
            logger.debug("JWT Token does not begin with Bearer String");
        }
        
        logger.debug("Token extraction complete. Username: " + username + ", Token present: " + (jwtToken != null));
        
        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("=== TOKEN VALIDATION PHASE ===");
            logger.debug("Username found: " + username + ", proceeding with validation");
            logger.debug("Current SecurityContext authentication: " + SecurityContextHolder.getContext().getAuthentication());
            
            try {
                String role = null;
                boolean isTokenValid = false;
                
                // Use centralized service validation or fallback to local
                if (jwtProperties.isEnableCentralizedService()) {
                    try {
                        logger.debug("Attempting centralized token validation for user: " + username);
                        JwtServiceClient.JwtValidationResponse validationResponse = jwtServiceClient.validateToken(jwtToken);
                        
                        logger.debug("Centralized validation response - Valid: " + 
                            (validationResponse != null ? validationResponse.getValid() : "null") + 
                            ", Username: " + (validationResponse != null ? validationResponse.getUsername() : "null") +
                            ", Role: " + (validationResponse != null ? validationResponse.getRole() : "null"));
                        
                        if (validationResponse != null && validationResponse.getValid() != null && validationResponse.getValid()) {
                            isTokenValid = true;
                            role = validationResponse.getRole();
                            logger.debug("Token validation successful via centralized service for user: " + username);
                            logger.debug("CENTRALIZED SERVICE RETURNED ROLE: '" + role + "' for user: " + username);
                        } else {
                            logger.warn("Token validation failed via centralized service. Message: " + 
                                (validationResponse != null ? validationResponse.getMessage() : "null response"));
                        }
                    } catch (Exception e) {
                        logger.warn("Error during centralized token validation: " + e.getMessage());
                        
                        // Fallback to local validation if enabled
                        if (jwtProperties.getCentralizedService().isEnableFallback()) {
                            logger.debug("Falling back to local token validation for user: " + username);
                            isTokenValid = jwtUtil.validateToken(jwtToken, username);
                            if (isTokenValid) {
                                role = jwtUtil.extractRole(jwtToken);
                                logger.debug("Local token validation successful for user: " + username);
                            }
                        }
                    }
                } else {
                    // Use local validation
                    logger.debug("Using local token validation for user: " + username);
                    isTokenValid = jwtUtil.validateToken(jwtToken, username);
                    if (isTokenValid) {
                        role = jwtUtil.extractRole(jwtToken);
                        logger.debug("Local token validation successful for user: " + username + ", role: " + role);
                    }
                }
                
                logger.debug("Final validation result - Valid: " + isTokenValid + ", Role: " + role + ", Username: " + username);
                
                if (isTokenValid && role != null) {
                    logger.debug("=== SETTING SECURITY CONTEXT ===");
                    logger.debug("Setting authentication for user: " + username + " with role: " + role);
                    
                    // Create authorities with ROLE_ prefix for Spring Security (check if already prefixed)
                    Collection<GrantedAuthority> authorities = new ArrayList<>();
                    String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
                    logger.debug("Created authorities: " + authorities);
                    
                    // Create authentication token with JWT-based authorities
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    logger.debug("Created authentication token: " + authToken);
                    logger.debug("Authentication principal: " + authToken.getPrincipal());
                    logger.debug("Authentication authorities: " + authToken.getAuthorities());
                    logger.debug("Authentication is authenticated: " + authToken.isAuthenticated());
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("Set authentication for user: " + username + " with authorities: " + authToken.getAuthorities());
                    logger.debug("SecurityContext authentication after setting: " + SecurityContextHolder.getContext().getAuthentication());
                    logger.debug("SecurityContext principal after setting: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    logger.debug("=== SECURITY CONTEXT SET COMPLETE ===");
                } else {
                    logger.warn("Authentication failed for user: " + username + ". Valid: " + isTokenValid + ", Role: " + role);
                }
            } catch (Exception e) {
                logger.warn("Cannot set user authentication: " + e.getMessage());
            }
        }
        
        // Final debug before proceeding to next filter
        logger.debug("=== JWT AUTHENTICATION FILTER END ===");
        logger.debug("Final SecurityContext authentication: " + SecurityContextHolder.getContext().getAuthentication());
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.debug("Final authentication principal: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            logger.debug("Final authentication authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            logger.debug("Final authentication is authenticated: " + SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        } else {
            logger.debug("Final authentication is NULL - user will be anonymous");
        }
        logger.debug("Proceeding to next filter in chain...");
        
        chain.doFilter(request, response);
    }    /**
     * Check if the endpoint is public and doesn't require JWT authentication
     */
    private boolean isPublicEndpoint(String path) {
        logger.debug("Checking if path is public: " + path);
        
        boolean isPublic = path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/v1/jwt/") ||
               path.startsWith("/h2-console/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/swagger-ui.html") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/v3/api-docs/") ||
               path.equals("/v3/api-docs") ||
               path.startsWith("/swagger-resources/") ||
               path.startsWith("/actuator/") ||
               path.equals("/") ||
               path.equals("/error") ||
               path.equals("/favicon.ico") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/webjars/");
               
        logger.debug("Path " + path + " is " + (isPublic ? "public" : "protected"));
        return isPublic;
    }
}
