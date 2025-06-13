# 🌐 External API Integration Guide

## Overview

Your Student Management API includes a powerful **External API Integration** feature that allows you to call third-party APIs from your Spring Boot application. This guide covers all the endpoints, usage examples, and best practices.

## 🔗 Base URLs

- **Local Development**: `http://localhost:8080`
- **External API Endpoints**: `/api/v1/external/*`
- **API Documentation**: `http://localhost:8080/swagger-ui.html`

## 🔐 Authentication Required

All external API endpoints require JWT authentication. You must first authenticate to get a Bearer token.

### Quick Authentication
```bash
# Login to get token
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}'

# Use the token in Authorization header
Authorization: Bearer YOUR_JWT_TOKEN
```

## 📋 Available External API Endpoints

### 1. Generic External API Call
**Endpoint**: `POST /api/v1/external/call`
**Access**: USER, ADMIN
**Description**: Make any HTTP request to external APIs

**Request Body**:
```json
{
  "url": "https://api.example.com/endpoint",
  "method": "GET",
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer token"
  },
  "body": {
    "key": "value"
  }
}
```

**Example Usage**:
```bash
curl -X POST "http://localhost:8080/api/v1/external/call" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://jsonplaceholder.typicode.com/posts/1",
    "method": "GET"
  }'
```

### 2. Random Joke API
**Endpoint**: `GET /api/v1/external/joke`
**Access**: USER, ADMIN
**Description**: Get a random joke from external joke API

**Example**:
```bash
curl -X GET "http://localhost:8080/api/v1/external/joke" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "status": 200,
  "data": {
    "type": "general",
    "setup": "Why don't scientists trust atoms?",
    "punchline": "Because they make up everything!"
  }
}
```

### 3. External User Data
**Endpoint**: `GET /api/v1/external/user/{userId}`
**Access**: USER, ADMIN
**Description**: Fetch user data from JSONPlaceholder API

**Example**:
```bash
curl -X GET "http://localhost:8080/api/v1/external/user/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "name": "Leanne Graham",
    "username": "Bret",
    "email": "Sincere@april.biz"
  }
}
```

### 4. Create External Post
**Endpoint**: `POST /api/v1/external/create-post`
**Access**: ADMIN only
**Description**: Create a test post on JSONPlaceholder API

**Request Body**:
```json
{
  "title": "My Test Post",
  "body": "This is a test post created via external API",
  "userId": 1
}
```

**Example**:
```bash
curl -X POST "http://localhost:8080/api/v1/external/create-post" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Test Post",
    "body": "This is a test post",
    "userId": 1
  }'
```

### 5. Health Check
**Endpoint**: `GET /api/v1/external/health`
**Access**: No authentication required
**Description**: Check if external API service is working

**Example**:
```bash
curl -X GET "http://localhost:8080/api/v1/external/health"
```

## 🌍 Popular External APIs You Can Call

### 1. JSONPlaceholder (Test API)
- **Base URL**: `https://jsonplaceholder.typicode.com`
- **Posts**: `/posts`, `/posts/{id}`
- **Users**: `/users`, `/users/{id}`
- **Comments**: `/comments`

**Example Call**:
```json
{
  "url": "https://jsonplaceholder.typicode.com/posts",
  "method": "GET"
}
```

### 2. Random Joke API
- **URL**: `https://official-joke-api.appspot.com/random_joke`
- **Method**: GET
- **No Auth Required**

### 3. REST Countries API
- **Base URL**: `https://restcountries.com/v3.1`
- **All Countries**: `/all`
- **By Name**: `/name/{name}`

**Example Call**:
```json
{
  "url": "https://restcountries.com/v3.1/name/france",
  "method": "GET"
}
```

### 4. GitHub API
- **Base URL**: `https://api.github.com`
- **User Info**: `/users/{username}`
- **Repos**: `/users/{username}/repos`

**Example Call**:
```json
{
  "url": "https://api.github.com/users/octocat",
  "method": "GET"
}
```

### 5. OpenWeatherMap API (Requires API Key)
- **Base URL**: `https://api.openweathermap.org/data/2.5`
- **Current Weather**: `/weather?q={city}&appid={API_KEY}`

**Example Call**:
```json
{
  "url": "https://api.openweathermap.org/data/2.5/weather?q=London&appid=YOUR_API_KEY",
  "method": "GET"
}
```

## 🛠️ Advanced Usage Examples

### Example 1: Weather Data
```bash
curl -X POST "http://localhost:8080/api/v1/external/call" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://api.openweathermap.org/data/2.5/weather?q=London&appid=YOUR_API_KEY",
    "method": "GET"
  }'
```

### Example 2: GitHub Repository Information
```bash
curl -X POST "http://localhost:8080/api/v1/external/call" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://api.github.com/repos/spring-projects/spring-boot",
    "method": "GET"
  }'
```

### Example 3: POST Request with Authentication
```bash
curl -X POST "http://localhost:8080/api/v1/external/call" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://jsonplaceholder.typicode.com/posts",
    "method": "POST",
    "headers": {
      "Content-Type": "application/json"
    },
    "body": {
      "title": "New Post",
      "body": "This is a new post",
      "userId": 1
    }
  }'
```

## 📊 Response Format

All external API responses follow this format:

```json
{
  "status": 200,
  "headers": {
    "content-type": "application/json",
    "server": "nginx"
  },
  "data": {
    // External API response data
  }
}
```

### Error Response Format
```json
{
  "error": true,
  "status": 404,
  "message": "External API error message",
  "body": "External API error details"
}
```

## 🔒 Security Considerations

### 1. Rate Limiting
- Be mindful of external API rate limits
- Implement caching for frequently accessed data
- Consider using API keys responsibly

### 2. Data Validation
- Always validate external API responses
- Handle network timeouts and errors gracefully
- Don't expose sensitive API keys in requests

### 3. Authentication
- Store API keys securely (environment variables)
- Use HTTPS for all external API calls
- Validate SSL certificates

## ⚙️ Configuration

### Timeout Settings
The external API service is configured with:
- **Connection Timeout**: 10 seconds
- **Read Timeout**: 30 seconds
- **Retry Policy**: 3 attempts

### Headers
Default headers included in all requests:
- `Content-Type: application/json`
- `User-Agent: Student-Management-API/1.0`

## 🧪 Testing with Postman

### 1. Import Collection
Create a Postman collection with these requests:

### 2. Set Environment Variables
```
base_url = http://localhost:8080
jwt_token = YOUR_JWT_TOKEN_HERE
```

### 3. Test Sequence
1. Login and get JWT token
2. Test health check
3. Get random joke
4. Call external user API
5. Create external post (admin only)

## 🚀 Production Deployment URLs

When deploying to production, update these URLs:

### Heroku
```
https://your-app-name.herokuapp.com/api/v1/external/
```

### AWS
```
https://your-domain.amazonaws.com/api/v1/external/
```

### Custom Domain
```
https://api.yourdomain.com/api/v1/external/
```

## 📝 Best Practices

### 1. Error Handling
```javascript
// Frontend example
try {
  const response = await fetch('/api/v1/external/call', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(request)
  });
  
  if (response.ok) {
    const data = await response.json();
    // Handle success
  } else {
    // Handle HTTP errors
  }
} catch (error) {
  // Handle network errors
}
```

### 2. Caching Strategy
- Cache static data (countries, currencies)
- Use short-term cache for dynamic data
- Implement cache invalidation

### 3. Monitoring
- Log all external API calls
- Monitor response times
- Track error rates

## 🔍 Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check JWT token validity
   - Ensure proper Authorization header

2. **Timeout Errors**
   - External API might be slow
   - Check network connectivity

3. **CORS Issues**
   - Add appropriate CORS headers
   - Check external API CORS policy

4. **SSL Certificate Errors**
   - Verify external API SSL certificate
   - Check system time/date

### Debug Mode
Enable debug logging in `application.properties`:
```properties
logging.level.com.example.demo.service.ExternalApiService=DEBUG
```

## 📞 Support

For issues or questions:
1. Check the Swagger documentation at `/swagger-ui.html`
2. Review server logs for error details
3. Test with basic curl commands first
4. Verify external API status independently

## 🎯 Next Steps

1. **Add More External APIs**: Extend the service to support more APIs
2. **Implement Caching**: Add Redis for response caching
3. **Add Rate Limiting**: Implement request rate limiting
4. **Add Metrics**: Monitor API usage and performance
5. **Add Webhooks**: Support webhook endpoints for real-time data

---

**Happy API Integration!** 🚀
