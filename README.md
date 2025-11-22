# Ecommerce User Service

Microservice for user management, authentication, and authorization in the Ecommerce Platform.

## Overview

The User Service handles all user-related operations including:
- User registration and authentication
- JWT-based security
- Social media authentication (Google, Facebook, etc.)
- Password reset functionality
- User profile management
- Role-based authorization

## Features

- ✅ **User Management**: Full CRUD operations for users
- ✅ **JWT Authentication**: Secure token-based authentication
- ✅ **Social Media Login**: OAuth2 integration for social platforms
- ✅ **Password Management**: Reset and change password functionality
- ✅ **Role-Based Access**: USER and ADMIN roles
- ✅ **Database Integration**: MySQL for production, H2 for local development
- ✅ **Kafka Integration**: Event-driven architecture
- ✅ **Service Discovery**: Eureka client integration
- ✅ **Health Monitoring**: Actuator endpoints for monitoring
- ✅ **RESTful API**: Comprehensive REST endpoints

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0+ (for production)
- Kafka (Port 9092, optional for local)
- Eureka Server (Port 8761, optional for local)

### Database Setup

1. **For Production - Create MySQL Database**:
   ```sql
   CREATE DATABASE user_service_db;
   ```

2. **For Local Development**:
   - H2 in-memory database is used automatically
   - No setup required

### Running Locally

1. **Simple Run** (recommended for development):
   ```bash
   ./run-local.sh
   ```

2. **Build the project**:
   ```bash
   mvn clean package
   ```

3. **Run the application**:
   ```bash
   java -jar target/user-service-1.0.0.jar --spring.profiles.active=local
   ```

4. **Or use Maven**:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

The service will start on `http://localhost:8080`

## API Endpoints

### Authentication Endpoints

#### User Registration
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### User Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "password123"
}
```

#### Social Media Registration
```http
POST /api/auth/register/social
Content-Type: application/json

{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "provider": "google",
  "providerId": "google123456",
  "profileImageUrl": "https://example.com/profile.jpg"
}
```

#### Social Media Login
```http
POST /api/auth/login/social
Content-Type: application/json

{
  "email": "john@example.com",
  "provider": "google",
  "providerId": "google123456"
}
```

#### Check Username Availability
```http
GET /api/auth/check-username?username=johndoe
```

#### Check Email Availability
```http
GET /api/auth/check-email?email=john@example.com
```

### User Profile Endpoints

#### Get User Profile (Authenticated)
```http
GET /api/users/profile
Authorization: Bearer <JWT_TOKEN>
```

#### Update User Profile (Authenticated)
```http
PUT /api/users/profile
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

## Configuration

### Application Configuration (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:mysql://localhost:3306/user_service_db
    username: root
    password: password
  kafka:
    bootstrap-servers: localhost:9092
```

### Local Development (application-local.yml)

- **Database**: H2 in-memory database
- **Port**: 8080
- **Eureka**: Disabled
- **Kafka**: Optional (localhost:9092)
- **JWT Secret**: Development key
- **Logging**: Debug level enabled

### JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration: 86400000  # 24 hours in milliseconds
```

### Database Console (Local Development)

- **H2 Console**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:userdb`
- **Username**: `sa`
- **Password**: `password`

## Usage Examples

### Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "password123"
  }'
```

### Social Media Registration

```bash
curl -X POST http://localhost:8080/api/auth/register/social \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "provider": "google",
    "providerId": "google123456",
    "profileImageUrl": "https://example.com/profile.jpg"
  }'
```

### Check Username Availability

```bash
curl "http://localhost:8080/api/auth/check-username?username=johndoe"
```

### Get User Profile

```bash
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### Update User Profile

```bash
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe Updated",
    "phone": "+1234567890"
  }'
```

## Architecture

### Authentication Flow

1. **Registration/Login** → User provides credentials
2. **Validation** → Validate credentials against database
3. **JWT Generation** → Generate JWT token with user details
4. **Token Return** → Return token to client
5. **Event Publishing** → Publish user event to Kafka

### JWT Token Structure

- **Header**: Algorithm and token type
- **Payload**: User ID, username, email, roles, expiration
- **Signature**: HMAC SHA256 with secret key

### Components

1. **AuthController**: Authentication and registration endpoints
2. **UserController**: User profile management
3. **UserService**: Core business logic for user management
4. **JwtTokenProvider**: JWT token generation and validation
5. **SecurityConfig**: Spring Security configuration
6. **UserEventConsumer**: Kafka event processing

## Kafka Integration

### Topics Produced

- `user-events`: User registration and profile updates

### Event Types

- `USER_REGISTERED` → User registration event
- `USER_UPDATED` → User profile update event
- `PASSWORD_RESET_REQUESTED` → Password reset request event

### Event Consumers

- **Notification Service**: Listens to user events for sending welcome emails

## Database Schema

### User Entity

- `id` - Primary key
- `username` - Unique username
- `email` - Unique email address
- `password` - Encrypted password
- `firstName` - First name
- `lastName` - Last name
- `phone` - Phone number
- `role` - User role (USER, ADMIN)
- `enabled` - Account enabled status
- `createdAt` - Creation timestamp
- `updatedAt` - Update timestamp

### Social Auth Entity (if applicable)

- `id` - Primary key
- `userId` - Foreign key to User
- `provider` - Social provider (google, facebook, etc.)
- `providerId` - Provider-specific user ID
- `profileImageUrl` - Profile image URL

## Testing

### Run Tests

```bash
mvn test
```

### Run Specific Test Classes

```bash
# Service layer tests
mvn test -Dtest=UserServiceTest

# Controller tests
mvn test -Dtest=AuthControllerTest
```

### Test Coverage

- **Service Tests**: ✅ Comprehensive coverage
- **Controller Tests**: ✅ REST endpoint tests
- **Security Tests**: ✅ JWT validation tests

## Deployment

### Docker

```bash
# Build image
docker build -t ecommerce/user-service .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-user:3306/user_service_db \
  -e JWT_SECRET=your-secret-key \
  ecommerce/user-service
```

### Production Considerations

1. **Database**:
   - Use MySQL for production
   - Set up connection pooling
   - Configure read replicas
   - Set up backups
2. **Security**:
   - Use strong JWT secret (environment variable)
   - Enable HTTPS
   - Configure CORS properly
   - Implement rate limiting
   - Use password encryption (BCrypt)
3. **Monitoring**:
   - Enable metrics and logging
   - Set up health checks
   - Monitor authentication failures
4. **Scaling**:
   - Configure for horizontal scaling
   - Use stateless JWT tokens
   - Share JWT secret across instances
5. **Service Discovery**:
   - Enable Eureka client
   - Register with service registry
6. **Message Broker**:
   - Configure Kafka connection
   - Set up proper topics

## Troubleshooting

### Common Issues

1. **Port Already in Use**:
   ```bash
   # Find process using port 8080
   lsof -i :8080
   # Kill the process
   kill -9 <PID>
   ```

2. **Database Connection Issues**:
   - Check H2 console at `/h2-console` (local)
   - Verify MySQL connection (production)
   - Verify JDBC URL: `jdbc:h2:mem:userdb` (local)

3. **JWT Token Issues**:
   - Check JWT secret configuration
   - Verify token expiration settings
   - Ensure token is included in Authorization header

4. **Authentication Fails**:
   - Verify user exists in database
   - Check password encryption
   - Verify JWT secret matches

5. **Compilation Errors**:
   ```bash
   # Clean and recompile
   mvn clean compile
   ```

### Logs

Check application logs for detailed error information:

```bash
# Enable debug logging
java -jar target/user-service-1.0.0.jar \
  --spring.profiles.active=local \
  --logging.level.org.example.user=DEBUG \
  --logging.level.org.springframework.security=DEBUG
```

## Dependencies

- Spring Boot 3.2.0
- Spring Security 6.x
- Spring Data JPA
- Spring Cloud Netflix Eureka Client
- Spring Kafka
- JWT (io.jsonwebtoken)
- H2 Database (local development)
- MySQL Connector (production)
- Spring Boot Actuator
- Validation API

## Project Structure

```
src/
├── main/
│   ├── java/org/example/user/
│   │   ├── UserServiceApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── UserController.java
│   │   ├── service/
│   │   │   └── UserService.java
│   │   ├── entity/
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── security/
│   │   │   └── JwtTokenProvider.java
│   │   ├── dto/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── UserProfileDto.java
│   │   └── consumer/
│   │       └── UserEventConsumer.java
│   └── resources/
│       ├── application.yml
│       └── application-local.yml
└── test/
    └── java/org/example/user/
        ├── service/
        │   └── UserServiceTest.java
        └── controller/
            └── AuthControllerTest.java
```

## Contributing

1. Follow Spring Boot best practices
2. Write comprehensive tests
3. Update documentation
4. Use proper logging levels
5. Handle errors gracefully
6. Ensure security best practices

## Security Notes

⚠️ **Important Security Considerations**:

1. **Password Storage**: Always use BCrypt or similar hashing
2. **JWT Secret**: Use strong, randomly generated secrets
3. **HTTPS**: Always use HTTPS in production
4. **Token Expiration**: Set appropriate token expiration times
5. **Rate Limiting**: Implement rate limiting for authentication endpoints
6. **Input Validation**: Validate all user inputs
7. **SQL Injection**: Use parameterized queries (JPA handles this)

## License

This project is part of the Ecommerce Microservices Platform.
