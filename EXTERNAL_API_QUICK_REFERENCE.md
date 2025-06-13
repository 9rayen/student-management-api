# 🔗 External API Quick Reference

## Your API Base URL
```
http://localhost:8080/api/v1/external
```

## Authentication Required
```bash
# Get token first
POST /api/v1/auth/login
{
  "username": "user",
  "password": "password"
}

# Use token in headers
Authorization: Bearer YOUR_JWT_TOKEN
```

## 🚀 Ready-to-Use External APIs

### 1. Random Joke (No Auth Required)
```bash
GET /api/v1/external/joke
```

### 2. User Data from JSONPlaceholder
```bash
GET /api/v1/external/user/1
```

### 3. Create Test Post (Admin Only)
```bash
POST /api/v1/external/create-post
{
  "title": "Test Post",
  "body": "Content here",
  "userId": 1
}
```

### 4. Generic API Call
```bash
POST /api/v1/external/call
{
  "url": "https://api.example.com/data",
  "method": "GET",
  "headers": {"Authorization": "Bearer token"},
  "body": {"key": "value"}
}
```

## 🌐 Popular External APIs to Try

| API | URL | Method | Description |
|-----|-----|--------|-------------|
| **JSONPlaceholder** | `https://jsonplaceholder.typicode.com/posts` | GET | Test posts data |
| **Random Joke** | `https://official-joke-api.appspot.com/random_joke` | GET | Random jokes |
| **GitHub User** | `https://api.github.com/users/octocat` | GET | GitHub user info |
| **Countries** | `https://restcountries.com/v3.1/all` | GET | Country data |
| **Cat Facts** | `https://catfact.ninja/fact` | GET | Random cat facts |
| **Quotes** | `https://api.quotable.io/random` | GET | Inspirational quotes |
| **Dog Images** | `https://dog.ceo/api/breeds/image/random` | GET | Random dog photos |
| **Chuck Norris** | `https://api.chucknorris.io/jokes/random` | GET | Chuck Norris jokes |

## 🧪 Test Commands

### Test with curl:
```bash
# 1. Login
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}' \
  | jq -r '.token')

# 2. Get random joke
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/external/joke"

# 3. Call external API
curl -X POST "http://localhost:8080/api/v1/external/call" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://api.github.com/users/octocat",
    "method": "GET"
  }'
```

### Test with PowerShell:
```powershell
# 1. Login and get token
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"username": "user", "password": "password"}'

$token = $loginResponse.token

# 2. Get random joke
$headers = @{ "Authorization" = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/external/joke" `
  -Headers $headers

# 3. Call external API
$body = @{
  url = "https://api.github.com/users/octocat"
  method = "GET"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/external/call" `
  -Method Post `
  -Headers $headers `
  -ContentType "application/json" `
  -Body $body
```

## 📱 Frontend JavaScript Example

```javascript
class ExternalApiClient {
  constructor(baseUrl, token) {
    this.baseUrl = baseUrl;
    this.token = token;
  }

  async callExternalApi(url, method = 'GET', body = null) {
    const response = await fetch(`${this.baseUrl}/api/v1/external/call`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${this.token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ url, method, body })
    });
    
    return response.json();
  }

  async getRandomJoke() {
    const response = await fetch(`${this.baseUrl}/api/v1/external/joke`, {
      headers: { 'Authorization': `Bearer ${this.token}` }
    });
    return response.json();
  }
}

// Usage
const client = new ExternalApiClient('http://localhost:8080', 'YOUR_TOKEN');
const joke = await client.getRandomJoke();
const githubUser = await client.callExternalApi('https://api.github.com/users/octocat');
```

## 🎯 Quick Start Checklist

- [ ] 1. Start your Spring Boot app: `mvn spring-boot:run`
- [ ] 2. Open Swagger UI: `http://localhost:8080/swagger-ui.html`
- [ ] 3. Login to get JWT token
- [ ] 4. Test with `/api/v1/external/joke` (simplest test)
- [ ] 5. Try calling GitHub API or JSONPlaceholder
- [ ] 6. Create your own external API integrations

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Check JWT token in Authorization header |
| Connection timeout | External API might be down or slow |
| CORS error | Not applicable for server-side calls |
| SSL error | Check external API certificate |

---
**Start testing your external API integrations now!** 🚀
