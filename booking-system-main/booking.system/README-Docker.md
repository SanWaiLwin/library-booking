# Booking System - Docker Setup

This guide explains how to run the Booking System application using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 2GB of available RAM
- Ports 8080, 3306, and 6379 available on your host machine

## Quick Start

1. **Clone the repository and navigate to the project directory:**
   ```bash
   cd booking.system
   ```

2. **Start all services:**
   ```bash
   docker-compose up -d
   ```

3. **Wait for all services to be healthy:**
   ```bash
   docker-compose ps
   ```

4. **Access the application:**
   - Application: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

## Services

### Application (booking-app)
- **Port:** 8080
- **Health Check:** http://localhost:8080/actuator/health
- **Profile:** docker

### MySQL Database (booking-mysql)
- **Port:** 3306
- **Database:** booking_system
- **Username:** root
- **Password:** root
- **Data Volume:** mysql_data

### Redis Cache (booking-redis)
- **Port:** 6379
- **Data Volume:** redis_data
- **No authentication required

## Environment Variables

You can customize the application by setting environment variables in the `docker-compose.yml` file:

### Database Configuration
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

### Redis Configuration
- `REDIS_HOST`: Redis server hostname
- `REDIS_PORT`: Redis server port
- `REDIS_PASSWORD`: Redis password (optional)

### Cache Configuration
- `CACHE_AVAILABLE_BOOKS_TTL`: TTL for available books cache (seconds)
- `CACHE_BORROWED_BOOKS_TTL`: TTL for borrowed books cache (seconds)
- `CACHE_BOOK_DETAIL_TTL`: TTL for book detail cache (seconds)

### JWT Configuration
- `JWT_SECRET`: JWT signing secret
- `JWT_EXPIRATION`: JWT expiration time (hours)

## Docker Commands

### Start Services
```bash
# Start all services in detached mode
docker-compose up -d

# Start specific service
docker-compose up -d mysql

# Start with build (if you made code changes)
docker-compose up -d --build
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker-compose down -v
```

### View Logs
```bash
# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f app
docker-compose logs -f mysql
docker-compose logs -f redis
```

### Check Service Status
```bash
# Check status of all services
docker-compose ps

# Check health of services
docker-compose exec app curl -f http://localhost:8080/actuator/health
```

### Database Operations
```bash
# Connect to MySQL
docker-compose exec mysql mysql -u root -p booking_system

# Connect to Redis
docker-compose exec redis redis-cli
```

## Development

### Building the Application
```bash
# Build only the application image
docker-compose build app

# Build with no cache
docker-compose build --no-cache app
```

### Running Tests
```bash
# Run tests in container
docker-compose exec app ./mvnw test
```

### Debugging
```bash
# Access application container shell
docker-compose exec app sh

# View application logs in real-time
docker-compose logs -f app
```

## Data Persistence

Data is persisted using Docker volumes:
- `mysql_data`: MySQL database files
- `redis_data`: Redis data files

To backup data:
```bash
# Backup MySQL
docker-compose exec mysql mysqldump -u root -p booking_system > backup.sql

# Backup Redis
docker-compose exec redis redis-cli BGSAVE
```

## Troubleshooting

### Common Issues

1. **Port conflicts:**
   ```bash
   # Check what's using the ports
   netstat -tulpn | grep :8080
   netstat -tulpn | grep :3306
   netstat -tulpn | grep :6379
   ```

2. **Services not starting:**
   ```bash
   # Check logs
   docker-compose logs
   
   # Restart services
   docker-compose restart
   ```

3. **Database connection issues:**
   ```bash
   # Check MySQL health
   docker-compose exec mysql mysqladmin ping -h localhost
   
   # Verify database exists
   docker-compose exec mysql mysql -u root -p -e "SHOW DATABASES;"
   ```

4. **Redis connection issues:**
   ```bash
   # Check Redis health
   docker-compose exec redis redis-cli ping
   ```

### Clean Start
If you encounter persistent issues:
```bash
# Stop everything and remove volumes
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Start fresh
docker-compose up -d --build
```

## Production Considerations

For production deployment:

1. **Security:**
   - Change default passwords
   - Use environment files for secrets
   - Enable SSL/TLS
   - Configure firewall rules

2. **Performance:**
   - Adjust JVM heap size in Dockerfile
   - Configure MySQL performance settings
   - Set up Redis persistence

3. **Monitoring:**
   - Set up log aggregation
   - Configure health check endpoints
   - Monitor resource usage

4. **Backup:**
   - Implement automated database backups
   - Set up volume backup strategies

## API Documentation

Once the application is running, you can access:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Support

For issues and questions:
1. Check the application logs: `docker-compose logs app`
2. Verify service health: `docker-compose ps`
3. Check the troubleshooting section above