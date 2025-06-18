# Centralized JWT Management System

## Overview

This project implements a **Centralized JWT Management System** for the Student Management API. The system provides a unified approach to JWT token generation, validation, and revocation across the entire application.

## Features

### 🔐 Core JWT Operations
- **Token Generation**: Secure JWT token creation with user authentication
- **Token Validation**: Real-time token validation with detailed response
- **Token Revocation**: Immediate token invalidation (single or all user tokens)
- **Token Blacklisting**: Centralized blacklist management

### 🏗️ Architecture Benefits
- **Single Source of Truth**: All JWT operations centralized
- **Immediate Revocation**: Tokens can be revoked instantly
- **Scalable Storage**: Redis-based with in-memory fallback
- **Comprehensive Monitoring**: Token statistics and health checks
- **Exception Handling**: Robust error handling and user feedback

### 🔄 Storage Options
- **Redis**: Production-ready distributed storage
- **In-Memory**: Development fallback (automatic detection)

## API Endpoints

### 🔑 Authentication & Token Management

#### 1. Generate JWT Token (Login)
```http
POST /api/v1/jwt/generate
Content-Type: application/json

{
    "username": "john.doe",
    "password": "password123"
}
```

**Response:**
```json
{
    "success": true,
    "message": "JWT token generated successfully",
    "timestamp": "2025-06-18T10:30:00",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "type": "Bearer",
        "username": "john.doe",
        "role": "ROLE_USER",
        "expiresIn": 86400000,
        "issuedAt": "2025-06-18T10:30:00",
        "expiresAt": "2025-06-19T10:30:00"
    }
}
```

#### 2. Validate JWT Token (POST)
```http
POST /api/v1/jwt/validate
Content-Type: application/json

{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "john.doe"  // optional
}
```

#### 3. Validate JWT Token (GET with Header)
```http
GET /api/v1/jwt/validate
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
    "success": true,
    "message": "Token validation completed",
    "timestamp": "2025-06-18T10:30:00",
    "data": {
        "valid": true,
        "username": "john.doe",
        "role": "ROLE_USER",
        "message": "Token is valid",
        "validatedAt": "2025-06-18T10:30:00",
        "expiresAt": "2025-06-19T10:30:00",
        "remainingTimeMs": 82800000
    }
}
```

#### 4. Revoke JWT Token
```http
POST /api/v1/jwt/revoke
Content-Type: application/json

{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "reason": "User logout",  // optional
    "revokeAllUserTokens": false  // true to revoke all user tokens
}
```

**Response:**
```json
{
    "success": true,
    "message": "Token revocation completed",
    "timestamp": "2025-06-18T10:30:00",
    "data": {
        "revoked": true,
        "message": "Token revoked successfully",
        "username": "john.doe",
        "tokensRevoked": 1,
        "revokedAt": "2025-06-18T10:30:00"
    }
}
```

### 📊 Monitoring & Statistics

#### 5. Service Status
```http
GET /api/v1/jwt/status
```

**Response:**
```json
{
    "success": true,
    "message": "JWT service status retrieved successfully",
    "timestamp": "2025-06-18T10:30:00",
    "data": {
        "service": "Centralized JWT Service",
        "status": "RUNNING",
        "timestamp": "2025-06-18T10:30:00",
        "version": "1.0.0",
        "storageType": "IN_MEMORY"
    }
}
```

#### 6. User Token Statistics
```http
GET /api/v1/jwt/user/{username}/stats
```

**Response:**
```json
{
    "success": true,
    "message": "User token statistics retrieved successfully",
    "timestamp": "2025-06-18T10:30:00",
    "data": {
        "username": "john.doe",
        "activeTokenCount": 2,
        "timestamp": "2025-06-18T10:30:00"
    }
}
```

## Configuration

### Application Properties

```properties
# Centralized JWT Configuration
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidationInStudentManagementAPI2025
jwt.expiration=86400000
jwt.issuer=student-management-api
jwt.enable-revocation=true
jwt.fallback-to-memory=true

# Redis Configuration for JWT (optional)
jwt.redis.enabled=false
jwt.redis.host=localhost
jwt.redis.port=6379
jwt.redis.database=0
jwt.redis.timeout=2000
```

### Environment-Specific Configuration

**Development (`application-dev.properties`):**
```properties
jwt.redis.enabled=false
jwt.fallback-to-memory=true
```

**Production (`application-prod.properties`):**
```properties
jwt.redis.enabled=true
jwt.redis.host=your-redis-host
jwt.redis.password=your-redis-password
jwt.fallback-to-memory=false
```

## Project Structure

```
src/main/java/com/example/demo/jwt/
├── config/
│   └── JwtProperties.java              # JWT configuration properties
├── controller/
│   └── CentralizedJwtController.java   # REST API endpoints
├── exception/
│   ├── JwtException.java               # Base JWT exception
│   ├── JwtTokenExpiredException.java   # Token expired exception
│   ├── JwtTokenInvalidException.java   # Token invalid exception
│   ├── JwtTokenRevokedException.java   # Token revoked exception
│   └── JwtExceptionHandler.java        # Global exception handler
├── model/
│   ├── JwtRequest.java                 # Login request model
│   ├── JwtResponse.java                # Token response model
│   ├── TokenValidationRequest.java     # Validation request model
│   ├── TokenValidationResponse.java    # Validation response model
│   ├── TokenRevocationRequest.java     # Revocation request model
│   └── TokenRevocationResponse.java    # Revocation response model
└── service/
    ├── CentralizedJwtService.java      # Redis-based JWT service
    └── InMemoryJwtService.java         # In-memory fallback service
```

## Usage Examples

### 1. Login and Get Token
```bash
curl -X POST http://localhost:8080/api/v1/jwt/generate \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 2. Validate Token
```bash
curl -X GET http://localhost:8080/api/v1/jwt/validate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Revoke Token
```bash
curl -X POST http://localhost:8080/api/v1/jwt/revoke \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_JWT_TOKEN",
    "reason": "User logout"
  }'
```

### 4. Revoke All User Tokens
```bash
curl -X POST http://localhost:8080/api/v1/jwt/revoke \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_JWT_TOKEN",
    "revokeAllUserTokens": true,
    "reason": "Security breach"
  }'
```

## Security Features

### 🔒 Token Security
- **HMAC SHA-256**: Secure token signing algorithm
- **Configurable Secret**: Environment-specific JWT secrets
- **Expiration Control**: Configurable token lifetime
- **Immediate Revocation**: Real-time token invalidation

### 🛡️ Error Handling
- **Structured Responses**: Consistent error response format
- **Detailed Messages**: Clear error descriptions
- **Security Logging**: Comprehensive audit trail
- **Input Validation**: Request parameter validation

### 🔍 Monitoring
- **Token Statistics**: Per-user token counts
- **Service Health**: Real-time service status
- **Performance Metrics**: Token operation timing
- **Security Events**: Authentication and authorization logs

## Integration

### With Existing Security
The centralized JWT system integrates with your existing:
- **SecurityConfig**: Spring Security configuration
- **JwtAuthenticationFilter**: Request filtering
- **JwtUtil**: Token utilities
- **AuthController**: Authentication endpoints

### Service Auto-Detection
The system automatically detects available storage:
1. **Redis Available**: Uses `CentralizedJwtService`
2. **Redis Unavailable**: Falls back to `InMemoryJwtService`
3. **Development Mode**: Defaults to in-memory storage

## Production Deployment

### Redis Setup
1. Install and configure Redis server
2. Update `application-prod.properties`
3. Set `jwt.redis.enabled=true`
4. Configure Redis connection details

### Security Recommendations
- Use strong JWT secrets (minimum 256 bits)
- Enable HTTPS in production
- Configure proper Redis security
- Set appropriate token expiration times
- Monitor token usage patterns

## Testing

### Unit Tests
Run JWT service tests:
```bash
mvn test -Dtest=*JwtService*
```

### Integration Tests
Test JWT controller endpoints:
```bash
mvn test -Dtest=*JwtController*
```

### Manual Testing
Use the provided API endpoints with tools like:
- **Postman**: Import API collection
- **curl**: Command-line testing
- **Swagger UI**: Interactive API documentation

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   - Solution: Check Redis server status and connection settings
   - Fallback: System automatically uses in-memory storage

2. **Token Validation Errors**
   - Check token format and expiration
   - Verify JWT secret configuration
   - Check for token blacklisting

3. **Authentication Failures**
   - Verify user credentials
   - Check user database connection
   - Review Spring Security configuration

### Logs
Check application logs for detailed error information:
```bash
tail -f logs/student-management-api.log
```

## Performance Considerations

### Optimization Tips
- **Redis Connection Pooling**: Configure connection pools
- **Token Caching**: Use appropriate cache TTL
- **Batch Operations**: Group token operations when possible
- **Database Indexing**: Optimize user lookup queries

### Monitoring Metrics
- Token generation rate
- Validation response time
- Redis connection health
- Memory usage (in-memory fallback)

---

**Version**: 1.0.0  
**Last Updated**: June 18, 2025  
**Compatibility**: Spring Boot 3.5.0, Java 21
