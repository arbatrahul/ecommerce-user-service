# User Management Service

Independent microservice for user management, authentication, and authorization in the Ecommerce Platform.

## Overview

This is a standalone Spring Boot microservice that handles:
- User registration and authentication
- JWT-based security
- Social media authentication (Google, Facebook, etc.)
- Password reset functionality
- User profile management
- Role-based authorization

## Features

- ✅ **Independent Deployment**: Runs as a standalone service
- ✅ **JWT Authentication**: Secure token-based authentication
- ✅ **Social Media Login**: OAuth2 integration for social platforms
- ✅ **Password Management**: Reset and change password functionality
- ✅ **Role-Based Access**: USER and ADMIN roles
- ✅ **Database Integration**: MySQL for production, H2 for local development
- ✅ **Event Publishing**: Kafka integration for user events
- ✅ **Health Monitoring**: Actuator endpoints for monitoring
- ✅ **Comprehensive Testing**: Unit and integration tests

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running Locally

1. **Simple Run** (recommended for development):
   ```bash
   ./run-local.sh
   ```

2. **Manual Run**:
   ```bash
   # Compile the service
   mvn clean compile
   
   # Run with local profile
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. **JAR Run**:
   ```bash
   # Package the application
   mvn clean package -DskipTests
   
   # Run the JAR
   java -jar target/user-service-1.0.0.jar --spring.profiles.active=local
   ```

### Service Endpoints

Once running, the service will be available at `http://localhost:8080`

#### Health Check
```bash
curl http://localhost:8080/actuator/health
```

#### API Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/register/social` - Social media registration
- `POST /api/auth/login/social` - Social media login
- `GET /api/auth/check-username?username=test` - Check username availability
- `GET /api/auth/check-email?email=test@example.com` - Check email availability
- `GET /api/users/profile` - Get user profile (authenticated)
- `PUT /api/users/profile` - Update user profile (authenticated)

#### Database Console (Local Development)
- H2 Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:userdb`
- Username: `sa`
- Password: `password`

## Configuration

### Local Development (application-local.yml)
- **Database**: H2 in-memory database
- **Port**: 8080
- **Eureka**: Disabled
- **Kafka**: Optional (localhost:9092)
- **JWT Secret**: Development key
- **Logging**: Debug level enabled

### Production Configuration
Update `application.yml` for production:
- MySQL database connection
- Eureka service discovery
- Kafka message broker
- Secure JWT secret
- Email service configuration

## API Usage Examples

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

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Classes
```bash
# Service layer tests
mvn test -Dtest=UserServiceTest

# Controller tests (note: may need security config fixes)
mvn test -Dtest=AuthControllerTest
```

### Test Coverage
- **Service Tests**: ✅ 12/12 passing
- **Controller Tests**: ⚠️ Security configuration needed for full testing

## Development

### Project Structure
```
src/
├── main/
│   ├── java/org/example/user/
│   │   ├── config/          # Security configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── entity/         # JPA entities
│   │   ├── repository/     # Data repositories
│   │   ├── security/       # JWT and security components
│   │   ├── service/        # Business logic
│   │   └── consumer/       # Kafka consumers
│   └── resources/
│       ├── application.yml      # Default configuration
│       └── application-local.yml # Local development config
└── test/                   # Unit and integration tests
```

### Key Components

1. **UserService**: Core business logic for user management
2. **AuthController**: Authentication and registration endpoints
3. **UserController**: User profile management
4. **JwtTokenProvider**: JWT token generation and validation
5. **SecurityConfig**: Spring Security configuration
6. **UserEventConsumer**: Kafka event processing

## Deployment

### Docker
```bash
# Build image
docker build -t ecommerce/user-service .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  ecommerce/user-service
```

### Production Considerations

1. **Database**: Configure MySQL connection
2. **Security**: 
   - Use strong JWT secret
   - Enable HTTPS
   - Configure CORS properly
3. **Monitoring**: Enable metrics and logging
4. **Scaling**: Configure for horizontal scaling
5. **Service Discovery**: Enable Eureka client
6. **Message Broker**: Configure Kafka connection

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
   - Check H2 console at `/h2-console`
   - Verify JDBC URL: `jdbc:h2:mem:userdb`

3. **JWT Token Issues**:
   - Check JWT secret configuration
   - Verify token expiration settings

4. **Compilation Errors**:
   ```bash
   # Clean and recompile
   mvn clean compile
   ```

### Logs
Check application logs for detailed error information:
```bash
# If running with Maven
# Logs appear in console

# If running JAR
java -jar target/user-service-1.0.0.jar --spring.profiles.active=local --logging.level.com.ecommerce.user=DEBUG
```

## Contributing

1. Follow Spring Boot best practices
2. Write comprehensive tests
3. Update documentation
4. Use proper logging levels
5. Handle errors gracefully

## Dependencies

- Spring Boot 3.2.0
- Spring Security 6.x
- Spring Data JPA
- JWT (io.jsonwebtoken)
- H2 Database (local)
- MySQL Connector (production)
- Spring Cloud (service discovery)
- Apache Kafka (messaging)
- Spring Boot Test (testing)
