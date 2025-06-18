package com.example.demo.jwt.exception;

/**
 * JWT Token Expired Exception
 * Thrown when a JWT token has expired
 */
public class JwtTokenExpiredException extends JwtException {
    
    public JwtTokenExpiredException(String message) {
        super(message, "JWT_TOKEN_EXPIRED");
    }
    
    public JwtTokenExpiredException(String message, Throwable cause) {
        super(message, "JWT_TOKEN_EXPIRED", cause);
    }
}
