# Configuration Reference

This document describes all configurable options for Mastodon 4j.

## Configuration Methods

Configuration can be provided via:

1. **Environment variables** (recommended for production)
2. **application.yml** files
3. **Command-line arguments**

Priority: Command-line args > Environment variables > application.yml

## Core Configuration

### Server Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `server.port` | `SERVER_PORT` | `8080` | HTTP server port |
| `server.address` | `SERVER_ADDRESS` | `0.0.0.0` | Bind address |
| `spring.profiles.active` | `SPRING_PROFILES_ACTIVE` | `dev` | Active profile (dev/test/prod) |

### Database Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `spring.datasource.url` | `DATABASE_URL` | `jdbc:h2:file:./data/mastodon_dev` | Database JDBC URL |
| `spring.datasource.username` | `DATABASE_USERNAME` | `sa` | Database username |
| `spring.datasource.password` | `DATABASE_PASSWORD` | | Database password |
| `spring.datasource.driver-class-name` | | `org.h2.Driver` | JDBC driver class |
| `spring.jpa.hibernate.ddl-auto` | | `validate` | DDL mode |
| `spring.flyway.enabled` | | `true` | Enable Flyway migrations |

### Connection Pool (HikariCP)

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.hikari.maximum-pool-size` | `10` | Maximum connections |
| `spring.datasource.hikari.minimum-idle` | `5` | Minimum idle connections |
| `spring.datasource.hikari.idle-timeout` | `300000` | Idle timeout (ms) |
| `spring.datasource.hikari.connection-timeout` | `20000` | Connection timeout (ms) |

## Instance Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `mastodon.domain` | `MASTODON_DOMAIN` | `localhost` | Instance domain |
| `mastodon.base-url` | `MASTODON_BASE_URL` | `http://localhost:8080` | Full instance URL |
| `mastodon.instance-name` | `MASTODON_INSTANCE_NAME` | `Mastodon` | Instance display name |
| `mastodon.instance-description` | `MASTODON_INSTANCE_DESCRIPTION` | | Instance description |
| `mastodon.admin-email` | `MASTODON_ADMIN_EMAIL` | | Admin contact email |

## Media Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `mastodon.media.storage-path` | `MASTODON_MEDIA_STORAGE_PATH` | `data/media` | Media storage directory |
| `mastodon.media.base-url` | `MASTODON_MEDIA_BASE_URL` | | Base URL for media |
| `mastodon.media.max-upload-size` | | `10MB` | Maximum upload size |
| `mastodon.media.allowed-types` | | See below | Allowed MIME types |
| `mastodon.media.preview-max-width` | | `400` | Preview max width |
| `mastodon.media.preview-max-height` | | `400` | Preview max height |

### Default Allowed Media Types

```
image/jpeg
image/png
image/gif
image/webp
video/mp4
video/webm
audio/mpeg
audio/ogg
audio/wav
```

### Media Processing

| Property | Default | Description |
|----------|---------|-------------|
| `mastodon.media.ffmpeg-path` | `ffmpeg` | FFmpeg binary path |
| `mastodon.media.ffprobe-path` | `ffprobe` | FFprobe binary path |
| `mastodon.media.transcode-enabled` | `false` | Enable video transcoding |

### Media Cleanup

| Property | Default | Description |
|----------|---------|-------------|
| `mastodon.media.cleanup-retention` | `7d` | Retention period for orphaned media |
| `mastodon.media.cleanup-batch-size` | `100` | Cleanup batch size |
| `mastodon.media.processing-lock-timeout` | `5m` | Processing lock timeout |
| `mastodon.media.processing-batch-size` | `5` | Processing batch size |
| `mastodon.media.processing-poll-interval` | `5s` | Processing poll interval |

## Federation Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `mastodon.federation.enabled` | `MASTODON_FEDERATION_ENABLED` | `true` | Enable federation |
| `mastodon.federation.domain` | `MASTODON_FEDERATION_DOMAIN` | | Federation domain |
| `mastodon.federation.base-url` | `MASTODON_FEDERATION_BASE_URL` | | Federation base URL |
| `mastodon.federation.require-signatures` | | `true` | Require HTTP signatures |

### Federation Delivery

| Property | Default | Description |
|----------|---------|-------------|
| `mastodon.federation.delivery-timeout` | `30s` | HTTP timeout for delivery |
| `mastodon.federation.delivery-retries` | `3` | Number of delivery retries |
| `mastodon.federation.delivery-threads` | `5` | Delivery thread pool size |

## OAuth Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `mastodon.oauth.access-token-ttl` | | `2h` | Access token lifetime |
| `mastodon.oauth.refresh-token-ttl` | | `30d` | Refresh token lifetime |
| `mastodon.oauth.authorization-code-ttl` | | `10m` | Authorization code lifetime |

## Security Configuration

### Rate Limiting

| Property | Default | Description |
|----------|---------|-------------|
| `mastodon.rate-limit.enabled` | `true` | Enable rate limiting |
| `mastodon.rate-limit.requests-per-minute` | `300` | Requests per minute |
| `mastodon.rate-limit.authenticated-multiplier` | `2` | Multiplier for authenticated users |

### Security Headers

Configured automatically in production profile:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: SAMEORIGIN`
- `X-XSS-Protection: 1; mode=block`
- `Referrer-Policy: strict-origin-when-cross-origin`
- Content-Security-Policy

## Logging Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `logging.level.root` | `INFO` | Root log level |
| `logging.level.org.joinmastodon` | `DEBUG` | Application log level |
| `logging.file.name` | | Log file path |
| `logging.pattern.console` | | Console log pattern |
| `logging.pattern.file` | | File log pattern |

### Production Logging

```yaml
logging:
  level:
    root: WARN
    org.joinmastodon: INFO
    org.springframework.web: INFO
  file:
    name: /var/log/mastodon/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## Actuator Endpoints

| Property | Default | Description |
|----------|---------|-------------|
| `management.endpoints.web.exposure.include` | `health,info` | Exposed endpoints |
| `management.endpoint.health.show-details` | `never` | Health details visibility |
| `management.server.port` | | Separate management port |

### Available Endpoints

- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics
- `/actuator/prometheus` - Prometheus metrics (if enabled)

## Profile-Specific Configuration

### Development Profile (`dev`)

```yaml
spring:
  h2:
    console:
      enabled: true
  jpa:
    show-sql: true
logging:
  level:
    org.joinmastodon: DEBUG
```

### Test Profile (`test`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Production Profile (`prod`)

```yaml
spring:
  jpa:
    show-sql: false
  flyway:
    enabled: true
logging:
  level:
    root: WARN
    org.joinmastodon: INFO
```

## Environment-Specific Examples

### Docker Compose

```yaml
services:
  mastodon:
    image: mastodon-4j:latest
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://db:5432/mastodon
      DATABASE_USERNAME: mastodon
      DATABASE_PASSWORD: ${DB_PASSWORD}
      MASTODON_DOMAIN: social.example.com
      MASTODON_BASE_URL: https://social.example.com
```

### Kubernetes ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mastodon-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
  MASTODON_DOMAIN: "social.example.com"
  MASTODON_BASE_URL: "https://social.example.com"
---
apiVersion: v1
kind: Secret
metadata:
  name: mastodon-secrets
type: Opaque
stringData:
  DATABASE_PASSWORD: "your_secure_password"
  SECRET_KEY_BASE: "your_64_char_secret"
```

## Validation

Configuration is validated on startup. Invalid configuration will prevent application start with clear error messages.

To validate configuration without starting:

```bash
java -jar mastodon-web.jar --spring.main.allow-bean-definition-overriding=true --spring.main.web-application-type=none
```
