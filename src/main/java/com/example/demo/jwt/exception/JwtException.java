package com.example.demo.jwt.exception;

/**
 * Base JWT Exception class
 * Parent class for all JWT-related exceptions
 */
public class JwtException extends RuntimeException {
    
    private final String errorCode;
    
    public JwtException(String message) {
        super(message);
        this.errorCode = "JWT_ERROR";
    }
    
    public JwtException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "JWT_ERROR";
    }
    
    public JwtException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
