# Deployment Guide

This guide covers deploying Mastodon 4j to production environments.

## Prerequisites

### System Requirements

- **Java**: OpenJDK 25 or later
- **Database**: PostgreSQL 14+ (recommended) or H2 (development only)
- **Memory**: Minimum 2GB RAM, recommended 4GB+
- **Storage**: 10GB+ for media uploads (configurable)
- **Network**: HTTPS certificate for your domain

### External Dependencies

- PostgreSQL database
- (Optional) Reverse proxy (nginx, Caddy, or similar)
- (Optional) Object storage (S3-compatible) for media

## Configuration

### Environment Variables

Key configuration is done via environment variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/mastodon
DATABASE_USERNAME=mastodon
DATABASE_PASSWORD=your_secure_password

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Instance
MASTODON_DOMAIN=your-instance.com
MASTODON_BASE_URL=https://your-instance.com

# Secrets (generate secure random values)
SECRET_KEY_BASE=your_64_character_secret_key
OTP_SECRET=your_64_character_otp_secret

# Media Storage
MASTODON_MEDIA_STORAGE_PATH=/var/lib/mastodon/media
MASTODON_MEDIA_BASE_URL=https://your-instance.com/media

# Federation
MASTODON_FEDERATION_ENABLED=true
MASTODON_FEDERATION_DOMAIN=your-instance.com
```

### Application Configuration

Create `application-prod.yml`:

```yaml
spring:
  profiles:
    active: prod
  
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: ${SERVER_PORT:8080}
  forward-headers-strategy: native
  error:
    include-message: never
    include-stacktrace: never

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

mastodon:
  domain: ${MASTODON_DOMAIN}
  base-url: ${MASTODON_BASE_URL}
  
  media:
    storage-path: ${MASTODON_MEDIA_STORAGE_PATH}
    base-url: ${MASTODON_MEDIA_BASE_URL}
    max-upload-size: 10MB
  
  federation:
    enabled: ${MASTODON_FEDERATION_ENABLED:true}
    domain: ${MASTODON_FEDERATION_DOMAIN}
```

## Database Setup

### PostgreSQL

1. Create database and user:

```sql
CREATE USER mastodon WITH PASSWORD 'your_secure_password';
CREATE DATABASE mastodon OWNER mastodon;
GRANT ALL PRIVILEGES ON DATABASE mastodon TO mastodon;
```

2. Run migrations (automatic on startup with Flyway)

### Connection Pooling

Configure HikariCP for optimal performance:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
```

## Running the Application

### JAR Deployment

```bash
# Build
./mvnw clean package -DskipTests

# Run
java -jar mastodon-web/target/mastodon-web-0.1.0-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### Systemd Service

Create `/etc/systemd/system/mastodon.service`:

```ini
[Unit]
Description=Mastodon 4j
After=network.target postgresql.service

[Service]
Type=simple
User=mastodon
Group=mastodon
WorkingDirectory=/opt/mastodon
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="JAVA_OPTS=-Xms512m -Xmx2g"
ExecStart=/usr/bin/java $JAVA_OPTS -jar mastodon-web.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl enable mastodon
sudo systemctl start mastodon
```

### Docker Deployment

```dockerfile
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY mastodon-web/target/mastodon-web.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx2g"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

Build and run:

```bash
docker build -t mastodon-4j .
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/mastodon \
  -e DATABASE_USERNAME=mastodon \
  -e DATABASE_PASSWORD=password \
  -v /var/lib/mastodon/media:/var/lib/mastodon/media \
  mastodon-4j
```

## Reverse Proxy Configuration

### Nginx

```nginx
server {
    listen 443 ssl http2;
    server_name your-instance.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    client_max_body_size 20M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /media/ {
        alias /var/lib/mastodon/media/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

### Caddy

```
your-instance.com {
    reverse_proxy localhost:8080
    
    handle /media/* {
        root * /var/lib/mastodon
        file_server
    }
}
```

## Health Checks

### Application Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### Kubernetes Probes

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

## Monitoring

### Metrics Endpoint

```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus Integration

Add to `application-prod.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Key Metrics to Monitor

- HTTP request latency
- Database connection pool usage
- JVM memory usage
- GC pause times
- Media upload queue depth
- Federation delivery queue

## Scaling

### Horizontal Scaling

1. Use external PostgreSQL (not embedded)
2. Configure sticky sessions or token-based auth
3. Use shared media storage (S3, NFS)
4. Configure streaming service clustering

### Load Balancing

```nginx
upstream mastodon_backend {
    least_conn;
    server mastodon1:8080;
    server mastodon2:8080;
    server mastodon3:8080;
}

server {
    location / {
        proxy_pass http://mastodon_backend;
    }
}
```

## Backup and Recovery

### Database Backup

```bash
# Full backup
pg_dump -U mastodon -F c mastodon > mastodon_backup_$(date +%Y%m%d).dump

# Restore
pg_restore -U mastodon -d mastodon mastodon_backup.dump
```

### Media Backup

```bash
# Sync media to backup location
rsync -av /var/lib/mastodon/media/ backup-server:/backups/mastodon/media/
```

## Troubleshooting

### Common Issues

1. **Database connection failures**
   - Check PostgreSQL is running
   - Verify connection string and credentials
   - Check network connectivity

2. **Media upload failures**
   - Verify storage path exists and is writable
   - Check disk space
   - Verify file permissions

3. **Federation issues**
   - Verify domain configuration
   - Check SSL certificate validity
   - Verify HTTP signatures are working

### Log Analysis

```bash
# View recent logs
journalctl -u mastodon -n 100

# Follow logs
journalctl -u mastodon -f
```

### Performance Tuning

1. **JVM Options**
   ```bash
   JAVA_OPTS="-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
   ```

2. **Database**
   - Increase `shared_buffers` to 25% of RAM
   - Set `effective_cache_size` to 75% of RAM
   - Tune `work_mem` based on connections

3. **Connection Pool**
   - Set `maximum-pool-size` based on CPU cores
   - Formula: `(cores * 2) + disk_spindles`
