# External API Testing Guide

## 🎉 Your External API is Live!

Your Spring Boot Student Management API is successfully running on:
- **Base URL**: `http://localhost:8080`
- **API Documentation**: `http://localhost:8080/swagger-ui.html`
- **H2 Database Console**: `http://localhost:8080/h2-console`

## 🔐 JWT Authentication

The API uses JWT (JSON Web Token) authentication for secure access to all endpoints.

### Default Users:
- **User**: `user` / `password` (USER role)
- **Admin**: `admin` / `admin123` (ADMIN role)

### Authentication Flow:
1. **Login** → Get JWT token
2. **Use Token** → Include in Authorization header for API calls
3. **Token Expires** → Login again after 24 hours

### Authentication Endpoints:
- `POST /api/v1/auth/login` - Login and receive JWT token
- `POST /api/v1/auth/validate` - Validate JWT token

## 📋 Available Endpoints

### 1. Basic Operations
- `GET /api/v1/student/` - Get all students
- `GET /api/v1/student/{id}` - Get student by ID
- `POST /api/v1/student/` - Create new student (ADMIN only)
- `PUT /api/v1/student/{id}` - Update student (ADMIN only)
- `DELETE /api/v1/student/{id}` - Delete student (ADMIN only)

### 2. Advanced Search & Filtering
- `GET /api/v1/student/search` - Advanced search with pagination
- `GET /api/v1/student/by-age-range` - Students by age range
- `GET /api/v1/student/search-keyword` - Keyword search
- `GET /api/v1/student/by-birth-year/{year}` - Students by birth year
- `GET /api/v1/student/by-date-range` - Students by date range

### 3. Age-based Queries
- `GET /api/v1/student/older-than/{age}` - Students older than age
- `GET /api/v1/student/younger-than/{age}` - Students younger than age

### 4. Statistics & Analytics
- `GET /api/v1/student/statistics` - Comprehensive statistics
- `GET /api/v1/student/count` - Total student count

## 🧪 JWT Authentication Testing

### Step 1: Login and Get JWT Token

```powershell
# Login with user credentials
curl -X POST "http://localhost:8080/api/v1/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"username": "user", "password": "password"}'
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

### Step 2: Use JWT Token for API Calls

Extract the token from the login response and use it in the Authorization header:

```powershell
# Set your token (replace with actual token from login)
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Use token to access protected endpoints
curl -H "Authorization: Bearer $token" `
  "http://localhost:8080/api/v1/student/"
```

## 🧪 Testing Examples

### Using PowerShell/curl with JWT

#### 1. Complete Authentication Flow
```powershell
# Step 1: Login
$loginResponse = curl -X POST "http://localhost:8080/api/v1/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"username": "user", "password": "password"}' | ConvertFrom-Json

# Step 2: Extract token
$token = $loginResponse.token

# Step 3: Use token for API calls
curl -H "Authorization: Bearer $token" `
  "http://localhost:8080/api/v1/student/"
```

#### 2. Get All Students with JWT
```powershell
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  "http://localhost:8080/api/v1/student/"
```

#### 3. Search Students with Filters and JWT
```powershell
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  "http://localhost:8080/api/v1/student/search?name=john&minAge=20&maxAge=30&page=0&size=5"
```

#### 4. Create New Student (Admin only) with JWT
```powershell
# First login as admin
$adminResponse = curl -X POST "http://localhost:8080/api/v1/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"username": "admin", "password": "admin123"}' | ConvertFrom-Json

$adminToken = $adminResponse.token

# Create new student
curl -H "Authorization: Bearer $adminToken" `
  -X POST "http://localhost:8080/api/v1/student/" `
  -H "Content-Type: application/json" `
  -d '{"name":"Alice Johnson","email":"alice@example.com","dob":"1999-03-15"}'
```

#### 5. Get Statistics with JWT
```powershell
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  "http://localhost:8080/api/v1/student/statistics"
```

#### 6. Students by Age Range with JWT
```powershell
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  "http://localhost:8080/api/v1/student/by-age-range?minAge=18&maxAge=25"
```

#### 7. Update Student (Admin only) with JWT
```powershell
curl -H "Authorization: Bearer $adminToken" `
  -X PUT "http://localhost:8080/api/v1/student/1" `
  -H "Content-Type: application/json" `
  -d '{"name":"Updated Name","email":"updated@example.com","dob":"1995-05-20"}'
```

#### 8. Delete Student (Admin only) with JWT
```powershell
curl -H "Authorization: Bearer $adminToken" `
  -X DELETE "http://localhost:8080/api/v1/student/1"
```

### Using Postman with JWT

1. **Step 1**: Create a POST request to `http://localhost:8080/api/v1/auth/login`
   - Set Body to JSON: `{"username": "user", "password": "password"}`
   - Send request and copy the token from response

2. **Step 2**: For all other requests:
   - Set Authorization type to "Bearer Token"
   - Paste the JWT token in the token field
   - Set Headers: `Content-Type: application/json` for POST/PUT requests

### Using JavaScript/Fetch with JWT

```javascript
// Login and get JWT token
async function login(username, password) {
    const response = await fetch('http://localhost:8080/api/v1/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
    });
    const data = await response.json();
    return data.token;
}

// Use JWT token to fetch data
async function getStudents(token) {
    const response = await fetch('http://localhost:8080/api/v1/student/', {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    return await response.json();
}

// Complete example
async function example() {
    const token = await login('user', 'password');
    const students = await getStudents(token);
    console.log(students);
}
```

### Token Validation and Error Handling

```javascript
// Validate token
async function validateToken(token) {
    const response = await fetch('http://localhost:8080/api/v1/auth/validate', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    return response.ok;
}

// Handle token expiration
async function apiCall(token, url) {
    const response = await fetch(url, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    
    if (response.status === 401) {
        console.log('Token expired or invalid. Please login again.');
        // Redirect to login or refresh token
        return null;
    }
    
    return await response.json();
}
```
console.log(stats);
```

## 📊 Expected Response Formats

### Student Object
```json
{
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "dob": "2000-05-15",
    "age": 25
}
```

### Statistics Response
```json
{
    "totalStudents": 50,
    "averageAge": 22.5,
    "ageDistribution": {
        "Under 20": 10,
        "20-29": 25,
        "30-39": 12,
        "40+": 3
    },
    "generatedAt": "2025-06-10T18:23:40.123"
}
```

## 🎯 Key Features

✅ **Complete CRUD Operations**
✅ **Advanced Search & Filtering**
✅ **Pagination Support**
✅ **Role-based Security**
✅ **Comprehensive Statistics**
✅ **Age-based Queries**
✅ **Date Range Filtering**
✅ **Swagger Documentation**
✅ **H2 Database Console**
✅ **Error Handling**

## 🔧 Next Steps

1. **Test the API** using the examples above
2. **View Swagger UI** at `http://localhost:8080/swagger-ui.html`
3. **Check database** at `http://localhost:8080/h2-console`
4. **Customize** endpoints as needed
5. **Deploy** to production when ready

## 🛠️ Troubleshooting

- **403 Forbidden**: Check authentication credentials
- **404 Not Found**: Verify endpoint URL and student ID
- **400 Bad Request**: Check request parameters and JSON format
- **500 Internal Error**: Check application logs

Your external API is production-ready! 🚀
