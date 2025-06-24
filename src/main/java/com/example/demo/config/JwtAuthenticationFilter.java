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
    private JwtProperties jwtProperties;@Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;        // JWT Token is in the form "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            
            // Use centralized service or fallback to local validation
            if (jwtProperties.isEnableCentralizedService()) {
                try {
                    JwtServiceClient.JwtValidationResponse validationResponse = jwtServiceClient.validateToken(jwtToken);
                    
                    if (validationResponse.getValid() != null && validationResponse.getValid()) {
                        username = validationResponse.getUsername();                    } else {
                        logger.warn("JWT Token validation failed via centralized service: " + 
                            validationResponse.getMessage());
                    }
                } catch (Exception e) {
                    logger.warn("Error validating JWT token via centralized service: " + e.getMessage());
                    
                    // Fallback to local validation if enabled
                    if (jwtProperties.getCentralizedService().isEnableFallback()) {
                        try {
                            username = jwtUtil.extractUsername(jwtToken);
                        } catch (RuntimeException ex) {
                            logger.warn("JWT Token error during fallback: " + ex.getMessage());
                        }
                    }
                }
            } else {
                // Use local JWT processing
                try {
                    username = jwtUtil.extractUsername(jwtToken);
                } catch (RuntimeException e) {
                    logger.warn("JWT Token error: " + e.getMessage());
                }
            }
        } else {
            logger.debug("JWT Token does not begin with Bearer String");
        }        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String role = null;
                boolean isTokenValid = false;
                
                // Use centralized service validation or fallback to local
                if (jwtProperties.isEnableCentralizedService()) {
                    try {
                        JwtServiceClient.JwtValidationResponse validationResponse = jwtServiceClient.validateToken(jwtToken);
                        
                        if (validationResponse.getValid() != null && validationResponse.getValid()) {
                            isTokenValid = true;
                            role = validationResponse.getRole();
                        }
                    } catch (Exception e) {
                        logger.warn("Error during centralized token validation: " + e.getMessage());
                        
                        // Fallback to local validation if enabled
                        if (jwtProperties.getCentralizedService().isEnableFallback()) {
                            isTokenValid = jwtUtil.validateToken(jwtToken, username);
                            if (isTokenValid) {
                                role = jwtUtil.extractRole(jwtToken);
                            }
                        }
                    }
                } else {
                    // Use local validation
                    isTokenValid = jwtUtil.validateToken(jwtToken, username);
                    if (isTokenValid) {
                        role = jwtUtil.extractRole(jwtToken);
                    }
                }
                
                if (isTokenValid && role != null) {
                    // Create authorities with ROLE_ prefix for Spring Security
                    Collection<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    
                    // Create authentication token with JWT-based authorities
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.warn("Cannot set user authentication: " + e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }    /**
     * Check if the endpoint is public and doesn't require JWT authentication
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/v1/auth/") ||               path.startsWith("/h2-console/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/swagger-ui.html") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/swagger-resources/") ||
               path.equals("/") ||
               path.equals("/error") ||
               path.equals("/favicon.ico") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/webjars/");
    }
}
