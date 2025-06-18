package com.example.demo.jwt.service;

import com.example.demo.config.JwtUtil;
import com.example.demo.jwt.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InMemoryJwtService
 */
@ExtendWith(MockitoExtension.class)
class InMemoryJwtServiceTest {
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private InMemoryJwtService jwtService;
    
    private final String testUsername = "testuser";
    private final String testRole = "ROLE_USER";
    private final String testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    
    @BeforeEach
    void setUp() {
        // Mock JwtUtil behavior
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(testToken);
        when(jwtUtil.extractUsername(testToken)).thenReturn(testUsername);
        when(jwtUtil.extractRole(testToken)).thenReturn(testRole);
        when(jwtUtil.validateToken(testToken, testUsername)).thenReturn(true);
        when(jwtUtil.extractExpiration(testToken)).thenReturn(new java.util.Date(System.currentTimeMillis() + 86400000));
    }
    
    @Test
    void testGenerateToken_Success() {
        // Act
        JwtResponse response = jwtService.generateToken(testUsername, testRole);
        
        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(testUsername, response.getUsername());
        assertEquals(testRole, response.getRole());
        assertEquals("Bearer", response.getType());
        
        // Verify JwtUtil was called
        verify(jwtUtil).generateToken(testUsername, testRole);
    }
    
    @Test
    void testValidateToken_ValidToken_Success() {
        // Arrange
        jwtService.generateToken(testUsername, testRole); // Generate token first
        TokenValidationRequest request = new TokenValidationRequest(testToken);
        
        // Act
        TokenValidationResponse response = jwtService.validateToken(request);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals(testUsername, response.getUsername());
        assertEquals(testRole, response.getRole());
        assertEquals("Token is valid", response.getMessage());
        
        // Verify JwtUtil was called
        verify(jwtUtil).extractUsername(testToken);
        verify(jwtUtil).validateToken(testToken, testUsername);
    }
    
    @Test
    void testValidateToken_InvalidToken_Failure() {
        // Arrange
        String invalidToken = "invalid.token";
        when(jwtUtil.extractUsername(invalidToken)).thenThrow(new RuntimeException("Invalid token"));
        TokenValidationRequest request = new TokenValidationRequest(invalidToken);
        
        // Act
        TokenValidationResponse response = jwtService.validateToken(request);
        
        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
        assertTrue(response.getMessage().contains("Token validation error"));
    }
    
    @Test
    void testValidateToken_RevokedToken_Failure() {
        // Arrange
        jwtService.generateToken(testUsername, testRole); // Generate token first
        TokenRevocationRequest revokeRequest = new TokenRevocationRequest(testToken);
        jwtService.revokeToken(revokeRequest); // Revoke the token
        
        TokenValidationRequest validateRequest = new TokenValidationRequest(testToken);
        
        // Act
        TokenValidationResponse response = jwtService.validateToken(validateRequest);
        
        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals("Token has been revoked", response.getMessage());
    }
    
    @Test
    void testRevokeToken_SingleToken_Success() {
        // Arrange
        jwtService.generateToken(testUsername, testRole); // Generate token first
        TokenRevocationRequest request = new TokenRevocationRequest(testToken, "User logout");
        
        // Act
        TokenRevocationResponse response = jwtService.revokeToken(request);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.isRevoked());
        assertEquals(testUsername, response.getUsername());
        assertEquals(1, response.getTokensRevoked());
        assertTrue(response.getMessage().contains("Token revoked successfully"));
    }
    
    @Test
    void testRevokeToken_AllUserTokens_Success() {
        // Arrange
        String token1 = testToken;
        String token2 = "second.test.token";
        
        // Mock second token
        when(jwtUtil.generateToken(testUsername, testRole))
            .thenReturn(token1)
            .thenReturn(token2);
        when(jwtUtil.extractUsername(token2)).thenReturn(testUsername);
        
        // Generate multiple tokens for the same user
        jwtService.generateToken(testUsername, testRole);
        jwtService.generateToken(testUsername, testRole);
        
        TokenRevocationRequest request = new TokenRevocationRequest(token1);
        request.setRevokeAllUserTokens(true);
        
        // Act
        TokenRevocationResponse response = jwtService.revokeToken(request);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.isRevoked());
        assertEquals(testUsername, response.getUsername());
        assertEquals(2, response.getTokensRevoked());
        assertTrue(response.getMessage().contains("All tokens revoked successfully"));
    }
    
    @Test
    void testIsTokenValidAndActive_ValidToken_ReturnsTrue() {
        // Arrange
        jwtService.generateToken(testUsername, testRole); // Generate token first
        
        // Act
        boolean isValid = jwtService.isTokenValidAndActive(testToken);
        
        // Assert
        assertTrue(isValid);
        verify(jwtUtil).extractUsername(testToken);
        verify(jwtUtil).validateToken(testToken, testUsername);
    }
    
    @Test
    void testIsTokenValidAndActive_RevokedToken_ReturnsFalse() {
        // Arrange
        jwtService.generateToken(testUsername, testRole); // Generate token first
        TokenRevocationRequest revokeRequest = new TokenRevocationRequest(testToken);
        jwtService.revokeToken(revokeRequest); // Revoke the token
        
        // Act
        boolean isValid = jwtService.isTokenValidAndActive(testToken);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    void testGetUserActiveTokenCount_WithTokens_ReturnsCorrectCount() {
        // Arrange
        String token1 = testToken;
        String token2 = "second.test.token";
        
        // Mock second token
        when(jwtUtil.generateToken(testUsername, testRole))
            .thenReturn(token1)
            .thenReturn(token2);
        when(jwtUtil.extractUsername(token2)).thenReturn(testUsername);
        
        // Generate multiple tokens for the same user
        jwtService.generateToken(testUsername, testRole);
        jwtService.generateToken(testUsername, testRole);
        
        // Act
        long count = jwtService.getUserActiveTokenCount(testUsername);
        
        // Assert
        assertEquals(2, count);
    }
    
    @Test
    void testGetUserActiveTokenCount_NoTokens_ReturnsZero() {
        // Act
        long count = jwtService.getUserActiveTokenCount("nonexistentuser");
        
        // Assert
        assertEquals(0, count);
    }
}
