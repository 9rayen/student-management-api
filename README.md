# Student Management API

A comprehensive RESTful API for student management built with Spring Boot, featuring JWT authentication, advanced search capabilities, and comprehensive documentation.

## 🚀 Features

- **JWT Authentication**: Secure token-based authentication with role-based access control
- **Complete CRUD Operations**: Create, read, update, and delete students
- **Advanced Search & Filtering**: Multiple search endpoints with pagination and sorting
- **External API Integration**: Call third-party APIs through your secure endpoints
- **Role-Based Security**: USER and ADMIN roles with different permissions
- **Statistics & Analytics**: Comprehensive student statistics and age analytics
- **Interactive API Documentation**: Swagger/OpenAPI integration
- **Database Integration**: H2 database with Flyway migrations
- **Professional Architecture**: Clean, maintainable code structure

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.1
- **Security**: Spring Security with JWT
- **Database**: H2 (development), Flyway for migrations
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Java Version**: 17+

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/9rayen/student-management-api.git
cd student-management-api
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

### 3. Access the API
- **API Base URL**: `http://localhost:8080`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
- **H2 Database Console**: `http://localhost:8080/h2-console`

## 🔐 Authentication

The API uses JWT (JSON Web Token) authentication. All endpoints except `/api/v1/auth/login` require a valid JWT token.

### Default Users
- **User**: `user` / `password` (USER role)
- **Admin**: `admin` / `admin123` (ADMIN role)

### Authentication Flow
1. **Login**: POST to `/api/v1/auth/login` with credentials
2. **Get Token**: Extract JWT token from response
3. **Use Token**: Include token in Authorization header as `Bearer <token>`

### Quick Authentication Example
```bash
# 1. Login and get token
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}'

# 2. Use token in API calls
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/v1/student/"
```

## 📚 API Endpoints

### Authentication
- `POST /api/v1/auth/login` - Login and get JWT token
- `POST /api/v1/auth/validate` - Validate JWT token

### Student Management
- `GET /api/v1/student/` - Get all students
- `GET /api/v1/student/{id}` - Get student by ID
- `POST /api/v1/student/` - Create new student (ADMIN only)
- `PUT /api/v1/student/{id}` - Update student (ADMIN only)
- `DELETE /api/v1/student/{id}` - Delete student (ADMIN only)

### Advanced Search
- `GET /api/v1/student/search` - Advanced search with filtering and pagination
- `GET /api/v1/student/by-age-range` - Students by age range
- `GET /api/v1/student/search-keyword` - Keyword search across all fields
- `GET /api/v1/student/by-birth-year/{year}` - Students by birth year
- `GET /api/v1/student/by-date-range` - Students by date range

### Age-based Queries
- `GET /api/v1/student/older-than/{age}` - Students older than specified age
- `GET /api/v1/student/younger-than/{age}` - Students younger than specified age

### Statistics
- `GET /api/v1/student/statistics` - Comprehensive student statistics
- `GET /api/v1/student/count` - Total student count

### External API Integration
- `POST /api/v1/external/call` - Generic external API caller
- `GET /api/v1/external/joke` - Get random jokes from external API
- `GET /api/v1/external/user/{id}` - Get user data from JSONPlaceholder
- `POST /api/v1/external/create-post` - Create test posts on external API
- `GET /api/v1/external/health` - External API service health check

## 🔍 Example Usage

### Get All Students
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/v1/student/"
```

### Advanced Search with Filters
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/v1/student/search?name=john&minAge=20&maxAge=30&page=0&size=5&sortBy=name&sortDirection=asc"
```

### Create New Student (Admin Only)
```bash
curl -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -X POST "http://localhost:8080/api/v1/student/" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Johnson","email":"alice@example.com","dob":"1999-03-15"}'
```

### Get Statistics
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "http://localhost:8080/api/v1/student/statistics"
```

## 📊 Sample Data

The application comes with pre-loaded sample data:
- 8 students with diverse information
- Various ages and birth dates for testing filters
- Sample emails and names for search testing

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── config/          # JWT and Security configuration
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic and external API services
│   │   ├── student/         # Student domain (Entity, Repository, Service, Controller)
│   │   └── DemoApplication.java
│   └── resources/
│       ├── db/migration/    # Flyway database migrations
│       └── application.properties
├── docs/                    # API documentation
├── API_ENDPOINTS.md         # Detailed API documentation
├── API_TESTING_GUIDE.md     # Testing guide with examples
├── JWT_AUTHENTICATION_GUIDE.md # JWT implementation guide
├── EXTERNAL_API_INTEGRATION_GUIDE.md # External API integration guide
├── EXTERNAL_API_QUICK_REFERENCE.md  # Quick reference for external APIs
└── README.md               # This file
```

## 🌐 External API Integration

This API includes powerful external API integration capabilities that allow you to call third-party APIs securely through your authenticated endpoints.

### Features
- **Generic API Caller**: Call any external REST API
- **Pre-built Integrations**: Ready-to-use integrations with popular APIs
- **Secure Proxy**: All external calls go through your authenticated API
- **Error Handling**: Comprehensive error handling and timeout management

### Quick Example
```bash
# Call GitHub API through your API
curl -X POST "http://localhost:8080/api/v1/external/call" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://api.github.com/users/octocat",
    "method": "GET"
  }'
```

### 📖 External API Documentation
- **[Complete Integration Guide](EXTERNAL_API_INTEGRATION_GUIDE.md)** - Comprehensive guide with all endpoints and examples
- **[Quick Reference](EXTERNAL_API_QUICK_REFERENCE.md)** - Quick start guide with popular APIs and test commands

### Available External Endpoints
- `POST /api/v1/external/call` - Generic external API caller
- `GET /api/v1/external/joke` - Get random jokes
- `GET /api/v1/external/user/{id}` - Get user data from JSONPlaceholder
- `POST /api/v1/external/create-post` - Create test posts
- `GET /api/v1/external/health` - Service health check

## 🔧 Configuration

### Application Properties
```properties
# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=validate

# JWT Configuration
jwt.secret=mySecretKey
jwt.expiration=86400000

# API Documentation
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Security Configuration
- JWT token expiration: 24 hours
- HMAC-SHA256 signing algorithm
- Stateless session management
- Role-based method security

## 📖 Documentation

- **[API Endpoints](API_ENDPOINTS.md)**: Detailed endpoint documentation
- **[Testing Guide](API_TESTING_GUIDE.md)**: Comprehensive testing examples
- **[JWT Guide](JWT_AUTHENTICATION_GUIDE.md)**: JWT implementation details
- **[Swagger UI](http://localhost:8080/swagger-ui.html)**: Interactive API documentation

## 🧪 Testing

### Using curl
```bash
# Complete authentication flow
TOKEN=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}' \
  | jq -r '.token')

curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/student/"
```

### Using Postman
1. Import the API endpoints from Swagger documentation
2. Set up JWT authentication in Authorization tab
3. Use the provided examples for testing

### Using Browser
Visit `http://localhost:8080/swagger-ui.html` for interactive testing with the Swagger UI.

## 🚀 Deployment

### Development
```bash
mvn spring-boot:run
```

### Production Build
```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Docker (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔒 Security Features

- **JWT Authentication**: Secure, stateless authentication
- **Role-Based Access Control**: USER and ADMIN roles
- **Password Protection**: Secure password handling
- **CORS Configuration**: Configurable cross-origin requests
- **Input Validation**: Request validation and sanitization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Rayen**
- GitHub: [@9rayen](https://github.com/9rayen)
- Repository: [student-management-api](https://github.com/9rayen/student-management-api)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Spring Security team for robust security features
- H2 Database for the lightweight development database
- Swagger/OpenAPI for comprehensive API documentation

## 📞 Support

If you have any questions or need help with the API:

1. Check the [API Documentation](API_ENDPOINTS.md)
2. Review the [Testing Guide](API_TESTING_GUIDE.md)
3. Consult the [JWT Authentication Guide](JWT_AUTHENTICATION_GUIDE.md)
4. Open an issue on GitHub
5. Check the Swagger documentation at `http://localhost:8080/swagger-ui.html`

---

**Happy Coding! 🎉**