# JWT Centralized Service Migration Guide

This document outlines the complete migration from local JWT authentication to a centralized JWT service for the Student Management API.

## Overview

The Student Management API has been successfully migrated to use a centralized JWT service running on port 8091, while maintaining backward compatibility through configurable fallback mechanisms.

## Changes Made

### 1. Dependencies Updated (pom.xml)

**Added:**
- `spring-boot-starter-webflux` - For HTTP client communication with the centralized JWT service

**Location:** `pom.xml`

### 2. Enhanced JWT Configuration Properties

**File:** `src/main/java/com/example/demo/jwt/config/JwtProperties.java`

**New Features:**
- Centralized service configuration
- Connection timeout and retry settings
- Fallback mechanism configuration
- Service endpoint customization

**Key Properties:**
```properties
jwt.enable-centralized-service=true
jwt.centralized-service.base-url=http://localhost:8091
jwt.centralized-service.generate-endpoint=/api/v1/jwt/generate
jwt.centralized-service.validate-endpoint=/api/v1/jwt/validate
jwt.centralized-service.revoke-endpoint=/api/v1/jwt/revoke
jwt.centralized-service.connection-timeout=5000
jwt.centralized-service.read-timeout=10000
jwt.centralized-service.enable-fallback=true
```

### 3. RestTemplate Configuration

**File:** `src/main/java/com/example/demo/jwt/config/RestTemplateConfig.java`

**Features:**
- General purpose RestTemplate bean
- Dedicated JWT service RestTemplate with custom timeouts
- Proper timeout configuration

### 4. JWT Service Client

**File:** `src/main/java/com/example/demo/jwt/client/JwtServiceClient.java`

**Capabilities:**
- Generate JWT tokens via centralized service
- Validate JWT tokens via centralized service
- Revoke JWT tokens via centralized service
- Error handling and retry logic
- Comprehensive request/response DTOs

**Methods:**
- `generateToken(String username, String role)` - Generate new tokens
- `validateToken(String token)` - Validate existing tokens
- `revokeToken(String token)` - Revoke tokens

### 5. Enhanced Authentication Controller

**File:** `src/main/java/com/example/demo/controller/AuthController.java`

**Updates:**
- Login endpoint now uses centralized JWT service with fallback
- Token validation endpoint supports both centralized and local validation
- New token revocation endpoint (`/api/v1/auth/revoke`)
- Comprehensive error handling and response formatting

**Endpoints:**
- `POST /api/v1/auth/login` - User authentication and token generation
- `POST /api/v1/auth/validate` - Token validation
- `POST /api/v1/auth/revoke` - Token revocation (requires centralized service)

### 6. Enhanced JWT Authentication Filter

**File:** `src/main/java/com/example/demo/config/JwtAuthenticationFilter.java`

**Improvements:**
- Supports centralized JWT validation
- Fallback to local validation when centralized service is unavailable
- Enhanced error handling and logging
- Maintains Spring Security integration

### 7. Updated Application Properties

**File:** `src/main/resources/application.properties`

**New Configuration:**
```properties
# Enable centralized JWT service
jwt.enable-centralized-service=true

# Centralized JWT Service Configuration
jwt.centralized-service.base-url=http://localhost:8091
jwt.centralized-service.generate-endpoint=/api/v1/jwt/generate
jwt.centralized-service.validate-endpoint=/api/v1/jwt/validate
jwt.centralized-service.revoke-endpoint=/api/v1/jwt/revoke
jwt.centralized-service.connection-timeout=5000
jwt.centralized-service.read-timeout=10000
jwt.centralized-service.max-retries=3
jwt.centralized-service.enable-fallback=true
```

## Centralized JWT Service Integration

### Service Endpoints

The application integrates with the following centralized JWT service endpoints:

1. **Generate Token:** `POST http://localhost:8091/api/v1/jwt/generate`
   - Request: `{ "username": "user", "role": "USER", "expirationMs": 86400000, "issuer": "student-management-api" }`
   - Response: `{ "token": "jwt_token", "type": "Bearer", "expiresIn": 86400000, "message": "Token generated successfully" }`

2. **Validate Token:** `POST http://localhost:8091/api/v1/jwt/validate`
   - Request: `{ "token": "jwt_token" }`
   - Response: `{ "valid": true, "username": "user", "role": "USER", "message": "Token is valid" }`

3. **Revoke Token:** `POST http://localhost:8091/api/v1/jwt/revoke`
   - Request: `{ "token": "jwt_token" }`
   - Response: `{ "success": true, "message": "Token revoked successfully" }`

## Fallback Mechanism

The system implements a robust fallback mechanism:

1. **Primary:** Uses centralized JWT service when available
2. **Fallback:** Falls back to local JWT processing if:
   - Centralized service is unavailable
   - Network communication fails
   - Service returns errors
   - Fallback is enabled in configuration

## Configuration Options

### Enable/Disable Centralized Service
```properties
jwt.enable-centralized-service=true  # Use centralized service
jwt.enable-centralized-service=false # Use local JWT only
```

### Fallback Configuration
```properties
jwt.centralized-service.enable-fallback=true  # Enable fallback to local
jwt.centralized-service.enable-fallback=false # Fail if centralized unavailable
```

### Service URLs
```properties
jwt.centralized-service.base-url=http://localhost:8091
jwt.centralized-service.generate-endpoint=/api/v1/jwt/generate
jwt.centralized-service.validate-endpoint=/api/v1/jwt/validate
jwt.centralized-service.revoke-endpoint=/api/v1/jwt/revoke
```

### Timeouts and Retries
```properties
jwt.centralized-service.connection-timeout=5000  # 5 seconds
jwt.centralized-service.read-timeout=10000       # 10 seconds
jwt.centralized-service.max-retries=3
```

## Testing the Integration

### 1. Start the Centralized JWT Service
Ensure the centralized JWT service is running on `http://localhost:8091`

### 2. Test Token Generation
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 3. Test Token Validation
```bash
curl -X POST http://localhost:8080/api/v1/auth/validate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Test Token Revocation
```bash
curl -X POST http://localhost:8080/api/v1/auth/revoke \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Error Handling

The system provides comprehensive error handling:

1. **Service Unavailable:** Falls back to local JWT if enabled
2. **Network Errors:** Graceful degradation with proper error messages
3. **Invalid Responses:** Detailed error logging and user feedback
4. **Timeout Handling:** Configurable timeouts with retry logic

## Monitoring and Logging

Enhanced logging provides visibility into:
- JWT service communication
- Fallback mechanism activation
- Error conditions and recovery
- Token validation results

## Security Considerations

1. **Token Security:** All tokens are validated through secure channels
2. **Fallback Security:** Local validation maintains security standards
3. **Error Disclosure:** Minimal error information exposed to clients
4. **Timeout Protection:** Prevents hanging requests

## Migration Benefits

1. **Centralized Management:** Single point for JWT token lifecycle management
2. **Scalability:** Centralized service can be scaled independently
3. **Consistency:** Unified token format across multiple services
4. **Monitoring:** Centralized logging and monitoring of token operations
5. **Revocation:** Immediate token revocation across all services
6. **Flexibility:** Easy switching between centralized and local modes

## Next Steps

1. **Monitor Performance:** Track response times and error rates
2. **Scale Testing:** Test under load conditions
3. **Security Audit:** Conduct security review of the integration
4. **Documentation:** Update API documentation with new endpoints
5. **Training:** Train team on new configuration options

## Troubleshooting

### Common Issues

1. **Service Unavailable:** Check if centralized JWT service is running on port 8091
2. **Connection Timeout:** Verify network connectivity and adjust timeout settings
3. **Fallback Not Working:** Ensure `jwt.centralized-service.enable-fallback=true`
4. **Invalid Configuration:** Check application.properties for correct service URLs

### Debug Mode

Enable debug logging for detailed troubleshooting:
```properties
logging.level.com.example.demo.jwt=DEBUG
```

This migration provides a robust, scalable, and secure JWT authentication system with seamless fallback capabilities.
