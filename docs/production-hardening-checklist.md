# Production Hardening Checklist

This document provides a comprehensive checklist of settings and code that must be changed when moving from development to production.

## Critical Security Settings

### 1. Security Configuration

**Development (`SecurityConfig.java`):**
- ❌ CSRF protection disabled
- ❌ Frame options disabled (allows H2 console)
- ❌ All requests permitted without authentication
- ❌ Profile: `!test` (active in dev)

**Production (`ProductionSecurityConfig.java`):**
- ✅ CSRF protection enabled (except for API endpoints)
- ✅ Frame options set to SAMEORIGIN
- ✅ Content Security Policy (CSP) headers
- ✅ X-XSS-Protection header
- ✅ X-Content-Type-Options: nosniff
- ✅ Referrer-Policy header
- ✅ Permissions-Policy header
- ✅ Profile: `prod`

**Action Required:**
- Ensure `SPRING_PROFILES_ACTIVE=prod` is set in production environment

### 2. Database Configuration

**Development (`application.yml`):**
```yaml
datasource:
  url: jdbc:h2:file:./data/mastodon_dev
  username: sa
  password: (empty)
  driver-class-name: org.h2.Driver
h2:
  console:
    enabled: true  # ⚠️ MUST BE DISABLED IN PRODUCTION
jpa:
  hibernate:
    ddl-auto: update  # ⚠️ SHOULD BE 'validate' IN PRODUCTION
  show-sql: true  # ⚠️ SHOULD BE FALSE IN PRODUCTION
```

**Production (`application-prod.yml`):**
```yaml
datasource:
  url: ${DATABASE_URL}  # PostgreSQL required
  username: ${DATABASE_USERNAME}
  password: ${DATABASE_PASSWORD}  # ⚠️ MUST BE SET
  driver-class-name: org.postgresql.Driver
h2:
  console:
    enabled: false  # ✅ Disabled
jpa:
  hibernate:
    ddl-auto: validate  # ✅ Safe mode
  show-sql: false  # ✅ Disabled
```

**Action Required:**
- Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` environment variables
- Use PostgreSQL database (not H2)
- Run Flyway migrations before application starts

### 3. Secrets and Keys

**Development:**
- No secrets configured
- Empty passwords allowed

**Production Required:**
```bash
# Required environment variables
SECRET_KEY_BASE=<64-character-random-string>
OTP_SECRET=<64-character-random-string>
DATABASE_PASSWORD=<secure-password>
```

**Generate secrets with:**
```bash
# On Linux/Mac
openssl rand -hex 32

# On Windows PowerShell
[Convert]::ToHexString((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

### 4. Logging Configuration

**Development:**
```yaml
logging:
  level:
    org.joinmastodon: DEBUG
    org.springframework: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

**Production:**
```yaml
logging:
  level:
    root: WARN
    org.joinmastodon: INFO
    org.springframework.web: INFO
    org.springframework.security: INFO
    org.hibernate.SQL: WARN
  file:
    name: /var/log/mastodon/application.log
```

**Action Required:**
- Set appropriate log levels
- Configure log file path
- Set up log rotation

## Network and Server Settings

### 5. Instance Configuration

**Development:**
```yaml
mastodon:
  domain: localhost
  base-url: http://localhost:8080
```

**Production Required:**
```yaml
mastodon:
  domain: ${MASTODON_DOMAIN}  # e.g., social.example.com
  base-url: ${MASTODON_BASE_URL}  # e.g., https://social.example.com
```

### 6. HTTPS Configuration

**Development:**
- HTTP on port 8080
- No TLS required

**Production Required:**
- HTTPS required for all connections
- Configure reverse proxy (nginx/Caddy) with TLS certificates
- Set `server.forward-headers-strategy: native` for proxy headers

### 7. CORS Configuration

**Development:**
- Permissive CORS (all origins)

**Production (`ProductionSecurityConfig.java`):**
```java
configuration.setAllowedOriginPatterns(List.of("*"));  // ⚠️ RESTRICT IN PRODUCTION
```

**Action Required:**
- Restrict allowed origins to your domain:
```java
configuration.setAllowedOrigins(List.of("https://social.example.com"));
```

## Feature Flags

### 8. Media Configuration

**Development:**
```yaml
mastodon:
  media:
    storage-path: data/media
    transcode-enabled: false
```

**Production:**
```yaml
mastodon:
  media:
    storage-path: ${MASTODON_MEDIA_STORAGE_PATH:/var/lib/mastodon/media}
    transcode-enabled: ${MASTODON_TRANSCODE_ENABLED:false}
```

**Action Required:**
- Ensure media storage directory exists and is writable
- Configure FFmpeg path if transcoding enabled
- Set up media backup strategy

### 9. Federation Configuration

**Development:**
```yaml
mastodon:
  federation:
    enabled: true
    domain: localhost
```

**Production:**
```yaml
mastodon:
  federation:
    enabled: ${MASTODON_FEDERATION_ENABLED:true}
    domain: ${MASTODON_FEDERATION_DOMAIN}
    require-signatures: true  # ✅ Always enabled
```

**Action Required:**
- Set federation domain to your public domain
- Ensure HTTP signatures are working

### 10. Email Configuration

**Development:**
- Email not configured
- Email confirmation disabled by default

**Production Required:**
```yaml
spring:
  mail:
    host: ${SMTP_SERVER}
    port: ${SMTP_PORT:587}
    username: ${SMTP_LOGIN}
    password: ${SMTP_PASSWORD}
mastodon:
  email:
    confirmation-required: true
```

**Action Required:**
- Configure SMTP server
- Set up email templates
- Test email delivery

## Actuator Endpoints

### 11. Health and Metrics

**Development:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always  # ⚠️ TOO PERMISSIVE
```

**Production:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized  # ✅ Restricted
```

**Action Required:**
- Secure actuator endpoints
- Consider separate management port
- Set up Prometheus scraping

## Pre-Deployment Checklist

### Environment Variables
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `DATABASE_URL` set to PostgreSQL connection string
- [ ] `DATABASE_USERNAME` set
- [ ] `DATABASE_PASSWORD` set (strong password)
- [ ] `SECRET_KEY_BASE` set (64 hex characters)
- [ ] `OTP_SECRET` set (64 hex characters)
- [ ] `MASTODON_DOMAIN` set to your domain
- [ ] `MASTODON_BASE_URL` set to https://your-domain
- [ ] `SMTP_SERVER` configured (if using email)
- [ ] `SMTP_LOGIN` and `SMTP_PASSWORD` configured

### Infrastructure
- [ ] PostgreSQL database running and accessible
- [ ] Database migrations run (Flyway)
- [ ] HTTPS certificate installed
- [ ] Reverse proxy configured (nginx/Caddy)
- [ ] Media storage directory created and writable
- [ ] Log directory created and writable
- [ ] Firewall rules configured

### Security
- [ ] H2 console disabled
- [ ] Debug logging disabled
- [ ] CORS origins restricted
- [ ] Rate limiting enabled
- [ ] Security headers configured
- [ ] Admin account created
- [ ] Default passwords changed

### Monitoring
- [ ] Health endpoint accessible
- [ ] Metrics endpoint secured
- [ ] Log aggregation configured
- [ ] Alerting configured
- [ ] Backup strategy in place

### Testing
- [ ] API endpoints tested
- [ ] Federation tested with other instances
- [ ] Media upload tested
- [ ] Email delivery tested
- [ ] Rate limiting verified
- [ ] Security headers verified

## Quick Reference: Development vs Production

| Setting | Development | Production |
|---------|-------------|------------|
| Profile | default | prod |
| Database | H2 embedded | PostgreSQL |
| H2 Console | enabled | disabled |
| Security | disabled | enabled |
| CSRF | disabled | enabled |
| Logging level | DEBUG | INFO/WARN |
| Show SQL | true | false |
| DDL auto | update | validate |
| HTTPS | not required | required |
| CORS | permissive | restricted |
| Health details | always | when-authorized |
| Rate limiting | optional | required |

## Code Changes Required

### Files to Review Before Production

1. **`SecurityConfig.java`** - Ensure this is NOT active in production
   - Profile: `@Profile("!test")` - should not activate with `prod` profile
   - Consider changing to `@Profile("dev")` for clarity

2. **`ProductionSecurityConfig.java`** - Verify active in production
   - Profile: `@Profile("prod")` - correct
   - Review CORS allowed origins

3. **`application.yml`** - Development defaults
   - Should NOT be used in production
   - Ensure production profile overrides all settings

4. **`application-prod.yml`** - Production template
   - Review all placeholder values
   - Ensure all secrets use environment variables

## Recommended Code Improvements

### 1. Change SecurityConfig Profile
```java
// Current
@Profile("!test")

// Recommended
@Profile({"dev", "default"})
```
This ensures the development security config only activates in dev mode.

### 2. Add Email Confirmation Service
Currently, email confirmation fields exist but the service is not implemented.
- Create `EmailService` for sending confirmation emails
- Implement confirmation token generation
- Add confirmation endpoint

### 3. Add Approval Workflow Service
Currently, approval fields exist but the workflow is not implemented.
- Create admin approval queue
- Add approve/reject endpoints
- Send approval notification emails

### 4. Implement Secret Rotation
- Add mechanism to rotate `SECRET_KEY_BASE` and `OTP_SECRET`
- Document rotation procedure
- Add key versioning support
