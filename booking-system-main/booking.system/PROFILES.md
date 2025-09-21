# Spring Boot Profiles Configuration

This application supports multiple Spring Boot profiles for different environments with senior-level configurations.

## Available Profiles

### 1. Development Profile (`dev`)
**Default Profile** - Optimized for local development

#### Key Features:
- **Database**: H2 in-memory database with console access
- **Logging**: Verbose logging with SQL queries and debug information
- **Security**: Relaxed security settings for development
- **Performance**: Development tools enabled (live reload, restart)
- **Error Handling**: Detailed error messages and stack traces
- **Caching**: Simple in-memory caching

#### Configuration Highlights:
```properties
# H2 Database with console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Verbose logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.springframework.security=DEBUG

# Development tools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
```

### 2. Production Profile (`prod`)
**Enterprise-grade** - Optimized for production deployment

#### Key Features:
- **Database**: MySQL with optimized connection pooling (HikariCP)
- **Logging**: Structured JSON logging with file rotation
- **Security**: Hardened security with secure headers and session management
- **Performance**: Optimized JPA/Hibernate settings and caching
- **Monitoring**: Comprehensive metrics and health checks
- **Scalability**: Thread pool configuration and graceful shutdown

#### Configuration Highlights:
```properties
# MySQL with HikariCP
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.leak-detection-threshold=60000

# Redis caching
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000

# Security headers
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict

# Performance tuning
server.tomcat.max-threads=200
server.compression.enabled=true
```

## How to Use Profiles

### 1. Setting Active Profile

#### Via Environment Variable (Recommended)
```bash
export SPRING_PROFILES_ACTIVE=prod
```

#### Via Application Arguments
```bash
java -jar booking-system.jar --spring.profiles.active=prod
```

#### Via Maven
```bash
# Development
mvn spring-boot:run

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Via IDE
Set VM options: `-Dspring.profiles.active=prod`

### 2. Environment Variables

#### Development Environment
```bash
# Optional overrides
export SERVER_PORT=8080
export JWT_SECRET=myDevSecret
```

#### Production Environment
```bash
# Required for production
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://prod-db:3306/booking_system
export DB_USERNAME=booking_user
export DB_PASSWORD=secure_password_123
export JWT_SECRET=myProductionSecretKeyThatIsVeryLongAndSecure123456789
export REDIS_HOST=redis-cluster
export REDIS_PASSWORD=redis_password
```

## Database Setup

### Development (H2)
No setup required - uses in-memory database
- Console: http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:devdb`
- Username: `sa`
- Password: (empty)

### Production (MySQL)
```sql
-- Create database and user
CREATE DATABASE booking_system;
CREATE USER 'booking_user'@'%' IDENTIFIED BY 'secure_password_123';
GRANT ALL PRIVILEGES ON booking_system.* TO 'booking_user'@'%';
FLUSH PRIVILEGES;

-- Run schema initialization
source booking_system_initial_db.sql;
```

## Logging Configuration

### Development
- **Console**: Colorized output with debug information
- **File**: `./logs/application.log`
- **Level**: DEBUG for application, TRACE for SQL binding

### Production
- **Console**: Structured JSON format
- **Files**: 
  - Application: `/var/log/booking-system/application.log`
  - Errors: `/var/log/booking-system/error.log`
  - Audit: `/var/log/booking-system/audit.log`
- **Rotation**: 100MB max size, 30 days retention
- **Level**: INFO for application, WARN for frameworks

## Monitoring and Health Checks

### Development
- All actuator endpoints exposed
- Health details always shown
- Metrics and Prometheus enabled

### Production
- Restricted endpoints: `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`
- Health details only for authorized users
- Distributed tracing with 10% sampling

## Security Configuration

### Development
- Detailed error messages
- H2 console accessible
- Relaxed CORS settings
- Default admin user: `admin/admin123`

### Production
- No error details in responses
- Secure session cookies
- HTTPS enforcement
- Environment-based secrets
- Rate limiting and security headers

## Performance Optimizations

### Production Features
- **Connection Pooling**: HikariCP with 20 max connections
- **Caching**: Redis-based with TTL
- **Compression**: Gzip for responses > 1KB
- **HTTP/2**: Enabled for better performance
- **Batch Processing**: Hibernate batch operations
- **Thread Pools**: Optimized for concurrent requests
- **Graceful Shutdown**: 30-second timeout

## Troubleshooting

### Common Issues

1. **Profile not loading**
   ```bash
   # Check active profile
   curl http://localhost:8080/api/actuator/info
   ```

2. **Database connection issues**
   ```bash
   # Check health endpoint
   curl http://localhost:8080/api/actuator/health
   ```

3. **Logging not working**
   - Ensure log directory exists and is writable
   - Check `logback-spring.xml` configuration

### Profile Verification
```bash
# Check which profile is active
grep "The following profiles are active" logs/application.log

# Or via actuator
curl http://localhost:8080/api/actuator/env | jq '.propertySources[0].properties."spring.profiles.active"'
```

## Best Practices

1. **Never commit secrets** - Use environment variables
2. **Test profile switching** - Verify configurations work
3. **Monitor logs** - Set up log aggregation in production
4. **Use health checks** - Implement custom health indicators
5. **Backup configurations** - Version control all config files
6. **Document changes** - Update this file when adding new profiles

## Additional Profiles

You can create additional profiles by adding `application-{profile}.properties` files:
- `application-test.properties` - For integration testing
- `application-staging.properties` - For staging environment
- `application-docker.properties` - For containerized deployment

Each profile inherits from the base `application.properties` and can override specific settings.