# JWT Authentication Implementation Guide

## Overview

This Student Management API uses JWT (JSON Web Token) authentication for secure, stateless user authentication. This guide explains how JWT authentication works in the system and how to use it effectively.

## What is JWT?

JWT is a compact, URL-safe means of representing claims to be transferred between two parties. In our API:
- **Stateless**: No server-side session storage required
- **Secure**: Tokens are digitally signed using HMAC-SHA256
- **Self-contained**: Tokens contain all necessary user information
- **Expirable**: Tokens automatically expire after 24 hours

## JWT Token Structure

Our JWT tokens contain:
```json
{
  "sub": "username",
  "roles": ["USER"] or ["ADMIN"],
  "iat": 1705123456,
  "exp": 1705209856
}
```

## Authentication Flow

### 1. Login Process
```
Client → POST /api/v1/auth/login
       ← JWT Token Response
```

**Request:**
```json
{
  "username": "user",
  "password": "password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "user",
  "roles": ["USER"],
  "expiresAt": "2024-01-16T12:00:00Z"
}
```

### 2. API Access Process
```
Client → API Request with Bearer Token
       ← API Response or 401 Unauthorized
```

**Request Header:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Implementation Details

### JWT Configuration
Located in `application.properties`:
```properties
jwt.secret=mySecretKey
jwt.expiration=86400000
```

### Key Components

#### 1. JwtUtil Class
- **Location**: `src/main/java/com/example/demo/config/JwtUtil.java`
- **Purpose**: Token generation, validation, and claims extraction
- **Key Methods**:
  - `generateToken(UserDetails userDetails)` - Creates JWT token
  - `validateToken(String token, UserDetails userDetails)` - Validates token
  - `extractUsername(String token)` - Extracts username from token

#### 2. JwtAuthenticationFilter
- **Location**: `src/main/java/com/example/demo/config/JwtAuthenticationFilter.java`
- **Purpose**: Intercepts HTTP requests and validates JWT tokens
- **Functionality**:
  - Extracts Bearer token from Authorization header
  - Validates token and sets authentication context
  - Allows request to proceed if valid

#### 3. AuthController
- **Location**: `src/main/java/com/example/demo/controller/AuthController.java`
- **Endpoints**:
  - `POST /api/v1/auth/login` - User login
  - `POST /api/v1/auth/validate` - Token validation

#### 4. SecurityConfig
- **Location**: `src/main/java/com/example/demo/config/SecurityConfig.java`
- **Configuration**:
  - Stateless session management
  - JWT filter integration
  - Public endpoint configuration

## User Roles and Permissions

### USER Role
- **Access**: All GET endpoints
- **Restrictions**: Cannot create, update, or delete students

### ADMIN Role
- **Access**: All endpoints (GET, POST, PUT, DELETE)
- **Full Control**: Complete student management capabilities

## Security Features

### 1. Token Expiration
- **Duration**: 24 hours (86400000 milliseconds)
- **Behavior**: Tokens automatically expire and require re-authentication
- **Benefit**: Limits exposure time if token is compromised

### 2. Digital Signature
- **Algorithm**: HMAC-SHA256
- **Secret Key**: Configured in application properties
- **Protection**: Prevents token tampering

### 3. Stateless Authentication
- **No Sessions**: Server doesn't store user state
- **Scalability**: Easy horizontal scaling
- **Performance**: Reduced server memory usage

## Error Handling

### Common HTTP Status Codes

| Status | Meaning | Cause |
|--------|---------|--------|
| 200 | OK | Successful request with valid token |
| 401 | Unauthorized | Missing, invalid, or expired token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |

### Error Response Format
```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is expired",
  "path": "/api/v1/student/"
}
```

## Best Practices

### 1. Token Storage (Client-side)
- **Recommended**: Store in memory for single-page applications
- **Alternative**: HttpOnly cookies for web applications
- **Avoid**: localStorage for sensitive applications

### 2. Token Refresh Strategy
- **Current**: Manual re-login after expiration
- **Future Enhancement**: Implement refresh token mechanism

### 3. Error Handling
- **Always check**: HTTP status codes in responses
- **Implement**: Automatic re-authentication on 401 errors
- **User Experience**: Clear error messages for authentication failures

### 4. Security Considerations
- **HTTPS Only**: Always use HTTPS in production
- **Secret Key**: Use strong, random secret keys
- **Token Validation**: Validate tokens on every request

## Testing JWT Authentication

### Using curl
```bash
# 1. Login
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}' \
  | jq -r '.token')

# 2. Use token
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/student/"
```
### Using Postman
1. **Login Request**:
   - Method: POST
   - URL: `http://localhost:8080/api/v1/auth/login`
   - Body: JSON with username/password
   - Copy token from response

2. **API Requests**:
   - Authorization: Bearer Token
   - Token: Paste copied token
   - Headers: Content-Type: application/json (for POST/PUT)

### Using JavaScript
```javascript
class ApiClient {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
        this.token = null;
    }

    async login(username, password) {
        const response = await fetch(`${this.baseUrl}/api/v1/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            this.token = data.token;
            return data;
        }
        throw new Error('Login failed');
    }

    async apiCall(endpoint, options = {}) {
        const response = await fetch(`${this.baseUrl}${endpoint}`, {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${this.token}`
            }
        });

        if (response.status === 401) {
            this.token = null;
            throw new Error('Token expired');
        }

        return response.json();
    }
}
```

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - **Cause**: Missing or invalid token
   - **Solution**: Check Authorization header format

2. **Token Expired**
   - **Cause**: Token older than 24 hours
   - **Solution**: Login again to get new token

3. **403 Forbidden**
   - **Cause**: Valid token but insufficient role permissions
   - **Solution**: Use admin credentials for admin-only endpoints

4. **Token Format Error**
   - **Cause**: Malformed Authorization header
   - **Solution**: Ensure format is "Bearer <token>"

### Debug Tips

1. **Check Token Claims**:
   ```bash
   # Decode JWT token (without verification)
   echo "YOUR_TOKEN" | cut -d. -f2 | base64 -d
   ```

2. **Validate Token**:
   ```bash
   curl -X POST "http://localhost:8080/api/v1/auth/validate" \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

3. **Check Server Logs**:
   - Look for authentication-related errors
   - Verify JWT filter is processing requests

## Production Considerations

### Security Enhancements
1. **Environment Variables**: Store JWT secret in environment variables
2. **Key Rotation**: Implement periodic secret key rotation
3. **Rate Limiting**: Add rate limiting to login endpoints
4. **Audit Logging**: Log authentication events

### Performance Optimizations
1. **Token Caching**: Cache validated tokens briefly
2. **Async Processing**: Use async authentication where possible
3. **Connection Pooling**: Optimize database connections

### Monitoring
1. **Authentication Metrics**: Track login success/failure rates
2. **Token Usage**: Monitor token validation patterns
3. **Security Events**: Alert on suspicious authentication activity

## Conclusion

This JWT implementation provides a robust, scalable authentication system for the Student Management API. The stateless nature of JWT tokens makes the system highly scalable while maintaining security through digital signatures and automatic expiration.

For any questions or issues with JWT authentication, refer to the API documentation at `http://localhost:8080/swagger-ui.html` or check the server logs for detailed error messages.
