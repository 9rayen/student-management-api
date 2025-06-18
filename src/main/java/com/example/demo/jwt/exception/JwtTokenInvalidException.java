package com.example.demo.jwt.exception;

/**
 * JWT Token Invalid Exception
 * Thrown when a JWT token is invalid or malformed
 */
public class JwtTokenInvalidException extends JwtException {
    
    public JwtTokenInvalidException(String message) {
        super(message, "JWT_TOKEN_INVALID");
    }
    
    public JwtTokenInvalidException(String message, Throwable cause) {
        super(message, "JWT_TOKEN_INVALID", cause);
    }
}
