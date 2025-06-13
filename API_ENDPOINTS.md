# Student Management API - Endpoints Documentation

This document describes all the endpoints available in your Spring Boot Student Management API.

## Authentication
The API uses JWT (JSON Web Token) authentication. All endpoints except `/api/v1/auth/login` require a valid JWT token.

### Authentication Endpoints
- `POST /api/v1/auth/login` - Login and get JWT token
- `POST /api/v1/auth/validate` - Validate JWT token

### How to Use Authentication
1. **Login**: Send POST request to `/api/v1/auth/login` with username/password
2. **Get Token**: Extract the JWT token from the response
3. **Use Token**: Include the token in the `Authorization` header as `Bearer <token>`

### Default Users
- **User**: `user` / `password` (USER role)
- **Admin**: `admin` / `admin123` (ADMIN role)

## Base URL
`http://localhost:8080/api/v1/student`

## Authentication Examples

### Login Request
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}'
```

### Using JWT Token in Requests
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/v1/student/"
```

## Complex GET Endpoints

### 1. Advanced Search with Filtering and Pagination
**Endpoint:** `GET /search`

**Parameters:**
- `name` (optional) - Filter by student name (partial match, case-insensitive)
- `email` (optional) - Filter by email (partial match, case-insensitive)
- `minAge` (optional) - Minimum age filter
- `maxAge` (optional) - Maximum age filter
- `page` (default: 0) - Page number for pagination
- `size` (default: 10) - Number of items per page
- `sortBy` (default: "name") - Field to sort by (name, email, dob)
- `sortDirection` (default: "asc") - Sort direction (asc/desc)

**Example:**
```
GET /search?name=john&minAge=20&maxAge=30&page=0&size=5&sortBy=name&sortDirection=asc
```

### 2. Students by Age Range
**Endpoint:** `GET /by-age-range`

**Parameters:**
- `minAge` (required) - Minimum age
- `maxAge` (required) - Maximum age

**Example:**
```
GET /by-age-range?minAge=18&maxAge=25
```

### 3. Student Statistics
**Endpoint:** `GET /statistics`

**Returns:**
- Total number of students
- Average age
- Age distribution (grouped by ranges)
- Generation timestamp

**Example Response:**
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
  "generatedAt": "2025-06-10T18:12:40.123"
}
```

### 4. Students by Birth Year
**Endpoint:** `GET /by-birth-year/{year}`

**Parameters:**
- `year` (path variable) - Birth year (e.g., 1995, 2000)

**Example:**
```
GET /by-birth-year/2000
```

### 5. Keyword Search
**Endpoint:** `GET /search-keyword`

**Parameters:**
- `keyword` (required) - Search term to match in name or email

**Example:**
```
GET /search-keyword?keyword=smith
```

### 6. Student Count
**Endpoint:** `GET /count`

**Returns:**
```json
{
  "totalStudents": 50
}
```

### 7. Students Older Than Specific Age
**Endpoint:** `GET /older-than/{age}`

**Parameters:**
- `age` (path variable) - Age threshold

**Example:**
```
GET /older-than/25
```

### 8. Students Younger Than Specific Age
**Endpoint:** `GET /younger-than/{age}`

**Parameters:**
- `age` (path variable) - Age threshold

**Example:**
```
GET /younger-than/20
```

### 9. Students by Date of Birth Range
**Endpoint:** `GET /by-date-range`

**Parameters:**
- `startDate` (required) - Start date in YYYY-MM-DD format
- `endDate` (required) - End date in YYYY-MM-DD format

**Example:**
```
GET /by-date-range?startDate=1995-01-01&endDate=2000-12-31
```

## Existing Basic Endpoints

### 10. Get All Students
**Endpoint:** `GET /`

### 11. Get Student by ID
**Endpoint:** `GET /{studentId}`

## Response Formats

### Success Response
- **Status Code:** 200 OK
- **Body:** JSON array of students or single student object

### Error Responses
- **400 Bad Request:** Invalid parameters
- **404 Not Found:** Student not found
- **500 Internal Server Error:** Server error

## Example Student Object
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "dob": "2000-05-15",
  "age": 25
}
```

## Usage Tips

1. **Pagination:** Use `page` and `size` parameters for large datasets
2. **Sorting:** Combine `sortBy` and `sortDirection` for custom ordering
3. **Filtering:** Combine multiple filters for precise searches
4. **Date Format:** Always use YYYY-MM-DD format for dates
5. **Case Sensitivity:** Name and email searches are case-insensitive
6. **Partial Matching:** Search endpoints support partial text matching

## Authentication Examples

### Using Basic Auth (Postman/curl)
```bash
curl -u "user:password" "http://localhost:8080/api/v1/student/statistics"
```

### Using Bearer Token
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" "http://localhost:8080/api/v1/student/search?name=john"
```
