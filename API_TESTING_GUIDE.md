# External API Testing Guide

## 🎉 Your External API is Live!

Your Spring Boot Student Management API is successfully running on:
- **Base URL**: `http://localhost:8080`
- **API Documentation**: `http://localhost:8080/swagger-ui.html`
- **H2 Database Console**: `http://localhost:8080/h2-console`

## 🔐 Authentication

All endpoints require authentication. Default users:
- **User**: `user` / `password` (USER role)
- **Admin**: `admin` / `password` (ADMIN role)

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

## 🧪 Testing Examples

### Using PowerShell/curl

#### 1. Get All Students
```powershell
curl -u "user:password" "http://localhost:8080/api/v1/student/"
```

#### 2. Search Students with Filters
```powershell
curl -u "user:password" "http://localhost:8080/api/v1/student/search?name=john&minAge=20&maxAge=30&page=0&size=5"
```

#### 3. Get Statistics
```powershell
curl -u "user:password" "http://localhost:8080/api/v1/student/statistics"
```

#### 4. Students by Age Range
```powershell
curl -u "user:password" "http://localhost:8080/api/v1/student/by-age-range?minAge=18&maxAge=25"
```

#### 5. Create New Student (Admin only)
```powershell
curl -u "admin:password" -X POST "http://localhost:8080/api/v1/student/" -H "Content-Type: application/json" -d '{"name":"Alice Johnson","email":"alice@example.com","dob":"1999-03-15"}'
```

### Using Postman

1. **Set Authentication**: Basic Auth with username/password
2. **Set Headers**: `Content-Type: application/json` for POST/PUT requests
3. **Use the endpoints** listed above with the full URLs

### Using JavaScript/Fetch

```javascript
// Get statistics with basic auth
const response = await fetch('http://localhost:8080/api/v1/student/statistics', {
    headers: {
        'Authorization': 'Basic ' + btoa('user:password')
    }
});
const stats = await response.json();
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
