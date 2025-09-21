# Booking System - Spring Boot Application

A comprehensive booking management system built with Spring Boot 3.2.0, featuring user authentication, book management, and caching capabilities.

## Quick Start with Docker

### Prerequisites

- **Docker Desktop** (Windows/Mac) or **Docker Engine** (Linux)
- **Docker Compose** v2.0+
- **Git** for cloning the repository

### Running with Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd booking.system
   ```

2. **Start all services**
   ```bash
   docker-compose up -d
   ```

3. **Verify services are running**
   ```bash
   docker-compose ps
   ```

4. **Access the application**
   - **Application**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
   - **API Docs**: http://localhost:8080/v3/api-docs

### Services Overview

| Service | Port | Description |
|---------|------|-------------|
| **Spring Boot App** | 8080 | Main application server |
| **MySQL Database** | 3307 | Primary database (external port) |
| **Redis Cache** | 6380 | Caching layer (external port) |

### Docker Commands

```bash
# Start services in background
docker-compose up -d

# View logs
docker-compose logs -f
docker-compose logs -f app    # Application logs only

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up -d --build

# Remove all data (⚠️ destructive)
docker-compose down -v
```

## Local Development Setup

### Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis 7+**

### Setup Steps

1. **Configure Database**
   ```sql
   CREATE DATABASE booking_system;
   CREATE USER 'booking_user'@'localhost' IDENTIFIED BY 'booking_pass';
   GRANT ALL PRIVILEGES ON booking_system.* TO 'booking_user'@'localhost';
   ```

2. **Start Redis**
   ```bash
   redis-server
   ```

3. **Run Application**
   ```bash
   ./mvnw spring-boot:run
   ```

## API Documentation

### Swagger UI
Access interactive API documentation at: http://localhost:8080/swagger-ui.html

### Key Endpoints

- **Authentication**: `/api/auth/*`
- **Books**: `/api/books/*`
- **Users**: `/api/users/*`
- **Health Check**: `/actuator/health`

## Authentication

The application uses JWT-based authentication:

### Default Admin Credentials
```
Username: admin@gmail.com
Password: 123456789
```

### API Endpoints
1. **Register**: `POST /api/auth/register`
2. **Login**: `POST /api/auth/login`
3. **Use Token**: Include `Authorization: Bearer <token>` header

### Login Example
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@booking.com",
    "password": "admin123"
  }'
```

## Database

### Schema Initialization
- Database schema is automatically created on startup
- Initial data is loaded from `src/main/resources/data.sql`
- DDL updates are handled by Hibernate (`spring.jpa.hibernate.ddl-auto=update`)

### Connection Details (Docker)
```properties
Host: localhost
Port: 3307
Database: booking_system
Username: root
Password: root
```

## Caching

Redis is used for caching:
- **Available Books**: 5 minutes TTL
- **Borrowed Books**: 10 minutes TTL
- **Book Details**: 30 minutes TTL

## Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest="UserPrincipalTest"

# Run security tests only
./mvnw test -Dtest="com.swl.booking.system.security.**"
```

## Monitoring & Health Checks

- **Application Health**: http://localhost:8080/actuator/health
- **Database Health**: Included in health endpoint
- **Redis Health**: Included in health endpoint

## Troubleshooting

### Common Issues

1. **Port Conflicts**
   ```bash
   # Check what's using the ports
   netstat -an | findstr :8080
   netstat -an | findstr :3307
   netstat -an | findstr :6380
   ```

2. **Database Connection Issues**
   ```bash
   # Check MySQL container logs
   docker-compose logs mysql
   
   # Connect to MySQL directly
   docker exec -it booking-mysql mysql -u root -p
   ```

3. **Application Won't Start**
   ```bash
   # Check application logs
   docker-compose logs app
   
   # Rebuild containers
   docker-compose down
   docker-compose up -d --build
   ```

4. **Swagger UI Not Loading**
   - Ensure application is fully started
   - Check logs for SpringDoc configuration errors
   - Verify URL: http://localhost:8080/swagger-ui.html

### Reset Everything

```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v
docker system prune -f

# Restart fresh
docker-compose up -d --build
```

## Project Structure

```
booking.system/
├── src/main/java/com/swl/booking/system/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── entity/          # JPA entities
│   ├── repository/      # Data repositories
│   ├── security/        # Security configuration
│   └── service/         # Business logic
├── src/main/resources/
│   ├── application.properties
│   ├── application-docker.properties
│   └── db/              # Database scripts
├── docker-compose.yml   # Docker services
├── Dockerfile          # Application container
└── README.md           # This file
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./mvnw test`
5. Submit a pull request
