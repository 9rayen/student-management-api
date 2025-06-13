package com.example.demo.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom authentication entry point for JWT authentication
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final String body = objectMapper.writeValueAsString(new AuthenticationError(
                "Authentication required",
                "Please provide a valid JWT token in the Authorization header",
                request.getRequestURI()
        ));

        response.getOutputStream().println(body);
    }

    /**
     * Error response structure
     */
    public static class AuthenticationError {
        private final String error;
        private final String message;
        private final String path;
        private final LocalDateTime timestamp;
        private final int status;

        public AuthenticationError(String error, String message, String path) {
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = LocalDateTime.now();
            this.status = 401;
        }

        // Getters
        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public int getStatus() {
            return status;
        }
    }
}
