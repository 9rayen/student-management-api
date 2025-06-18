# Centralized JWT System Deployment Guide

## Quick Start

### 1. Build and Run the Application

```bash
# Clean and build the project
mvn clean install

# Run in development mode (default)
mvn spring-boot:run

# OR use the provided scripts
./run-dev.ps1    # Windows PowerShell
./run-dev.bat    # Windows Batch
```

### 2. Test the API Endpoints

```bash
# Run the automated test script
./test-jwt-api.ps1   # Windows PowerShell
./test-jwt-api.sh    # Linux/Mac (if available)
```

### 3. Access API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## Available Endpoints

### Core JWT Management
- `POST /api/v1/jwt/generate` - Generate JWT token (login)
- `POST /api/v1/jwt/validate` - Validate JWT token
- `GET /api/v1/jwt/validate` - Validate via Authorization header
- `POST /api/v1/jwt/revoke` - Revoke JWT token(s)
- `GET /api/v1/jwt/status` - Service status
- `GET /api/v1/jwt/user/{username}/stats` - User token statistics

### Legacy Authentication (Backward Compatibility)
- `POST /api/v1/auth/login` - Legacy login endpoint
- `POST /api/v1/auth/validate` - Legacy validation endpoint

## Default Credentials

For testing purposes, you can use these default credentials:
- **Username**: `admin`
- **Password**: `admin123`

## Configuration

### Development Mode (Default)
- Uses in-memory token storage
- Redis not required
- Automatic fallback to memory storage

### Production Mode
- Requires Redis server
- Set `jwt.redis.enabled=true` in `application-prod.properties`
- Configure Redis connection details

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │    │  Spring Boot    │    │  JWT Service    │
│                 │    │   API Gateway   │    │   (Memory)      │
│   POST /login   │───▶│                 │───▶│                 │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        ▲                       │                       │
        │                       ▼                       ▼
        │              ┌─────────────────┐    ┌─────────────────┐
        │              │  JWT Controller │    │  Token Storage  │
        └──────────────│  /api/v1/jwt/*  │    │   (In-Memory)   │
                       │                 │    │                 │
                       └─────────────────┘    └─────────────────┘
```

## Key Features Implemented

### ✅ Token Management
- ✅ JWT token generation with user authentication
- ✅ Real-time token validation
- ✅ Immediate token revocation (single or all user tokens)
- ✅ Token blacklisting support

### ✅ Storage Options
- ✅ In-memory storage (development)
- ✅ Redis integration ready (production)
- ✅ Automatic fallback mechanism

### ✅ API Endpoints
- ✅ RESTful JWT management APIs
- ✅ Comprehensive request/response models
- ✅ Swagger documentation
- ✅ Error handling and validation

### ✅ Security
- ✅ Structured exception handling
- ✅ Input validation
- ✅ Security logging
- ✅ CORS configuration

### ✅ Monitoring
- ✅ Service health checks
- ✅ User token statistics
- ✅ Performance metrics ready

### ✅ Testing
- ✅ Unit tests for services
- ✅ API testing scripts
- ✅ Integration test support

## Next Steps for Production

### 1. Redis Setup
```bash
# Install Redis
# Ubuntu/Debian:
sudo apt-get install redis-server

# Start Redis
sudo systemctl start redis-server
```

### 2. Update Configuration
```properties
# application-prod.properties
jwt.redis.enabled=true
jwt.redis.host=your-redis-host
jwt.redis.port=6379
jwt.redis.password=your-redis-password
```

### 3. Enable Production Profile
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

## Troubleshooting

### Common Issues

1. **Service Won't Start**
   - Check Java version (requires Java 21)
   - Verify Maven dependencies
   - Check port 8080 availability

2. **JWT Errors**
   - Verify JWT secret configuration
   - Check token format and expiration
   - Review authentication credentials

3. **Redis Connection Issues**
   - Verify Redis server is running
   - Check connection configuration
   - System automatically falls back to memory storage

### Logs
Check application logs for detailed error information:
```bash
tail -f logs/student-management-api.log
```

## Performance Notes

### Memory Storage
- ✅ Fast performance
- ❌ Lost on restart
- ❌ Not suitable for clustering

### Redis Storage
- ✅ Persistent storage
- ✅ Clustering support
- ✅ Production ready
- ❌ Additional infrastructure requirement

---

**Deployment Status**: ✅ Ready for Development Testing  
**Production Ready**: ⚠️ Requires Redis Configuration  
**Last Updated**: June 18, 2025
