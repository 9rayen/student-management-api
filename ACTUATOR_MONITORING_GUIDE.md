# 📊 Actuator Monitoring Guide

This document explains how to monitor your Student Management API using Spring Boot Actuator.

## 🚀 Getting Started

Actuator endpoints are available on port **8081** (separate from your main API on port 8080).

## 📋 Available Endpoints

### Public Endpoints (No Authentication Required)

#### Health Check
- **URL**: `http://localhost:8081/actuator/health`
- **Purpose**: Overall application health status
- **Example**:
```bash
curl http://localhost:8081/actuator/health
```

#### External API Health
- **URL**: `http://localhost:8081/actuator/health/externalApi`
- **Purpose**: Check external API connectivity
- **Example**:
```bash
curl http://localhost:8081/actuator/health/externalApi
```

#### Application Info
- **URL**: `http://localhost:8081/actuator/info`
- **Purpose**: Application metadata and version information
- **Example**:
```bash
curl http://localhost:8081/actuator/info
```

### Admin-Only Endpoints (Requires Admin JWT Token)

#### All Metrics
- **URL**: `http://localhost:8081/actuator/metrics`
- **Purpose**: List all available metrics
- **Example**:
```bash
curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/metrics
```

#### Custom Application Metrics
- **Students Created**: `http://localhost:8081/actuator/metrics/students.created`
- **Students Updated**: `http://localhost:8081/actuator/metrics/students.updated`
- **Students Deleted**: `http://localhost:8081/actuator/metrics/students.deleted`
- **Authentication Attempts**: `http://localhost:8081/actuator/metrics/auth.attempts`
- **Authentication Successes**: `http://localhost:8081/actuator/metrics/auth.successes`
- **Authentication Failures**: `http://localhost:8081/actuator/metrics/auth.failures`
- **External API Calls**: `http://localhost:8081/actuator/metrics/external.api.calls`
- **External API Errors**: `http://localhost:8081/actuator/metrics/external.api.errors`
- **External API Response Time**: `http://localhost:8081/actuator/metrics/external.api.response.time`

**Examples**:
```bash
# External API metrics
curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/metrics/external.api.calls

curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/metrics/external.api.response.time

# Student operations metrics
curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/metrics/students.created
```

#### Environment Variables
- **URL**: `http://localhost:8081/actuator/env`
- **Purpose**: Application configuration and environment variables
- **Example**:
```bash
curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/env
```

#### Configuration Properties
- **URL**: `http://localhost:8081/actuator/configprops`
- **Purpose**: All configuration properties
- **Example**:
```bash
curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/configprops
```

#### Application Beans
- **URL**: `http://localhost:8081/actuator/beans`
- **Purpose**: All Spring beans in the application context
- **Example**:
```bash
curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/beans
```

#### Request Mappings
- **URL**: `http://localhost:8081/actuator/mappings`
- **Purpose**: All HTTP request mappings
- **Example**:
```bash
curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/mappings
```

## 🔐 Getting Admin JWT Token

First, authenticate as admin to get a JWT token:

```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Use the returned token in subsequent requests to admin-protected actuator endpoints.

## 📊 Prometheus Integration

For production monitoring with Prometheus:

- **URL**: `http://localhost:8081/actuator/prometheus`
- **Purpose**: Metrics in Prometheus format
- **Example**:
```bash
curl -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  http://localhost:8081/actuator/prometheus
```

## 🔍 Monitoring Examples

### Basic Health Check
```bash
# Quick health check
curl http://localhost:8081/actuator/health

# Expected response:
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "externalApi": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### Monitor External API Performance
```bash
# Get external API call count
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8081/actuator/metrics/external.api.calls

# Get external API response times
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8081/actuator/metrics/external.api.response.time
```

### Monitor Authentication Activity
```bash
# Get authentication attempts
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8081/actuator/metrics/auth.attempts

# Get authentication success rate
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8081/actuator/metrics/auth.successes
```

## 🛠️ Troubleshooting

### Common Issues

1. **Actuator endpoints not accessible**
   - Check if application is running on port 8081
   - Verify management port configuration in `application.properties`

2. **403 Forbidden on admin endpoints**
   - Ensure you're using a valid admin JWT token
   - Check if token is included in Authorization header

3. **External API health check failing**
   - Check internet connectivity
   - Verify external API endpoints are accessible

### Configuration Verification

Check current actuator configuration:
```bash
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8081/actuator/env | grep management
```

## 🚀 Production Considerations

1. **Security**: In production, consider restricting actuator endpoints to internal networks
2. **Monitoring**: Integrate with monitoring tools like Prometheus + Grafana
3. **Alerting**: Set up alerts for critical metrics (external API failures, high response times)
4. **Custom Metrics**: Add more business-specific metrics as needed

## 📈 Next Steps

1. Set up Grafana dashboards for visualization
2. Configure alerts for critical thresholds
3. Add custom business metrics
4. Integrate with centralized logging (ELK stack)
