package com.example.demo.jwt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for JWT-related exceptions
 * Handles all JWT exceptions and provides standardized error responses
 */
@RestControllerAdvice
public class JwtExceptionHandler {
    
    /**
     * Handle JWT Token Expired Exception
     */
    @ExceptionHandler(JwtTokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleJwtTokenExpired(JwtTokenExpiredException ex) {
        Map<String, Object> error = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            "The JWT token has expired. Please login again."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Handle JWT Token Invalid Exception
     */
    @ExceptionHandler(JwtTokenInvalidException.class)
    public ResponseEntity<Map<String, Object>> handleJwtTokenInvalid(JwtTokenInvalidException ex) {
        Map<String, Object> error = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            "The JWT token is invalid or malformed."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Handle JWT Token Revoked Exception
     */
    @ExceptionHandler(JwtTokenRevokedException.class)
    public ResponseEntity<Map<String, Object>> handleJwtTokenRevoked(JwtTokenRevokedException ex) {
        Map<String, Object> error = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            "The JWT token has been revoked. Please login again."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Handle General JWT Exception
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException ex) {
        Map<String, Object> error = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            "An error occurred while processing the JWT token."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Handle Validation Errors for JWT request models
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> error = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Validation failed for JWT request",
            "Please check the request parameters."
        );
        error.put("validationErrors", validationErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(int status, String errorCode, String message, String details) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status);
        error.put("errorCode", errorCode);
        error.put("message", message);
        error.put("details", details);
        error.put("success", false);
        return error;
    }
}
