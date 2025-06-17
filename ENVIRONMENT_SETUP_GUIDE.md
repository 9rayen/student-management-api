# Environment Setup Guide

This project now supports two distinct environments: **Development** and **Production**.

## Environment Overview

| Feature | Development | Production |
|---------|-------------|------------|
| **Database** | H2 In-Memory (jdbc:h2:mem:devdb) | H2 File-based (jdbc:h2:file:./data/proddb) |
| **Data Persistence** | Lost on restart | Persisted to disk |
| **H2 Console** | Always enabled | Disabled by default |
| **DDL Auto** | create-drop | validate |
| **SQL Logging** | Enabled | Disabled |
| **Debug Logging** | Enabled | Minimal |
| **Actuator Endpoints** | All exposed | Limited (health, info, metrics) |
| **JWT Expiration** | 24 hours | 1 hour |
| **CORS** | Permissive | Restrictive |

## Quick Start

### Development Environment

```bash
# Using batch file (Windows)
run-dev.bat

# Using Maven directly
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Using PowerShell
$env:SPRING_PROFILES_ACTIVE="dev"; mvn spring-boot:run
```

**Access Points:**
- Application: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:devdb`
  - Username: `sa`
  - Password: (empty)
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator Health: http://localhost:8080/actuator/health

### Production Environment

```bash
# Setup directories first
setup-prod-dirs.bat

# Using batch file (Windows)
run-prod.bat

# Using Maven directly
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Using Docker
docker-compose up --build
```

**Access Points:**
- Application: http://localhost:8080
- H2 Console: Disabled by default
- Actuator Health: http://localhost:8080/actuator/health
- Actuator Info: http://localhost:8080/actuator/info

## Environment Configuration Files

### Development (`application-dev.properties`)
- H2 in-memory database
- H2 console enabled
- Detailed logging
- All actuator endpoints exposed
- Permissive CORS
- Extended JWT expiration (24 hours)

### Production (`application-prod.properties`)
- H2 file-based database (persistent)
- H2 console disabled by default
- Minimal logging with file output
- Limited actuator endpoints
- Restrictive CORS
- Shorter JWT expiration (1 hour)

## Environment-Specific Security

### Development Security
- H2 console accessible to all
- All actuator endpoints exposed
- Swagger UI enabled
- Detailed error messages

### Production Security
- H2 console restricted to ADMIN role (if enabled)
- Limited actuator endpoints
- No Swagger UI
- Minimal error details

## Database Management

### Development
- Database recreated on each restart
- Initial data loaded via Flyway migrations
- No backup needed (in-memory)

### Production
- Database persisted in `./data/proddb.mv.db`
- Flyway validates schema on startup
- Regular backups recommended

**Backup Production Database:**
```bash
backup-h2-db.bat
```

## Environment Variables

### Production Environment Variables
```bash
# Database credentials
DB_USERNAME=sa
DB_PASSWORD=your_secure_password

# JWT configuration
JWT_SECRET=your_production_jwt_secret
JWT_EXPIRATION=3600000

# H2 Console (optional)
H2_CONSOLE_ENABLED=false

# CORS origins
ALLOWED_ORIGINS=https://yourdomain.com

# Server port
PORT=8080
```

## Switching Between Environments

### Method 1: Using Batch Files
```bash
# Development
run-dev.bat

# Production
run-prod.bat
```

### Method 2: Using Maven Profiles
```bash
# Development
mvn spring-boot:run -Pdev

# Production
mvn spring-boot:run -Pprod
```

### Method 3: Using Environment Variables
```bash
# Development
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run

# Production
$env:SPRING_PROFILES_ACTIVE="prod"
mvn spring-boot:run
```

## Docker Deployment

### Build and Run with Docker Compose
```bash
# Build and start
docker-compose up --build

# Run in background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

### Build Docker Image Manually
```bash
# Build JAR
mvn clean package

# Build Docker image
docker build -t student-management-api .

# Run container
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod student-management-api
```

## Troubleshooting

### Common Issues

1. **H2 Database Lock Error**
   - Stop all running instances
   - Delete `./data/proddb.mv.db.lock` file
   - Restart application

2. **Port Already in Use**
   - Change port in environment-specific properties
   - Or set `PORT` environment variable

3. **Flyway Migration Errors**
   - Check migration scripts in `src/main/resources/db/migration/`
   - Ensure proper naming convention: `V{version}__{description}.sql`

4. **JWT Token Issues**
   - Verify `JWT_SECRET` is set for production
   - Check token expiration settings

### Logging Locations

**Development:**
- Console output only
- Debug level logging

**Production:**
- File: `./logs/student-management-api.log`
- Console: INFO level and above
- File rotation: 10MB max, 30 days retention

## Health Checks

### Development Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Production Health Check
```bash
curl http://localhost:8080/actuator/health
```

Both environments include a custom H2 health indicator that monitors database connectivity.

## Best Practices

1. **Development**
   - Use dev profile for local development
   - Keep H2 console enabled for debugging
   - Use extended JWT expiration for convenience

2. **Production**
   - Always use prod profile in production
   - Set strong JWT secret
   - Monitor application logs
   - Regular database backups
   - Restrict H2 console access

3. **Security**
   - Never commit production secrets to version control
   - Use environment variables for sensitive configuration
   - Regular security updates

## API Testing

The API endpoints work the same in both environments:

### Authentication
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Register  
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"password123"}'
```

### Student Management
```bash
# Get all students (requires authentication)
curl -X GET http://localhost:8080/api/v1/student \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Create student (requires ADMIN role)
curl -X POST http://localhost:8080/api/v1/student \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","age":25}'
```

For detailed API documentation, visit the Swagger UI (development only): http://localhost:8080/swagger-ui.html
