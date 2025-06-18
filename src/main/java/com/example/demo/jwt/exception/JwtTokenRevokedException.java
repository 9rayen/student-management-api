package com.example.demo.jwt.exception;

/**
 * JWT Token Revoked Exception
 * Thrown when a JWT token has been revoked/blacklisted
 */
public class JwtTokenRevokedException extends JwtException {
    
    public JwtTokenRevokedException(String message) {
        super(message, "JWT_TOKEN_REVOKED");
    }
    
    public JwtTokenRevokedException(String message, Throwable cause) {
        super(message, "JWT_TOKEN_REVOKED", cause);
    }
}
