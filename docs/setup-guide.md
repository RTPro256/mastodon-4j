# Initial Setup Guide

## 1. Create Parent POM

Create `./pom.xml` in the project root:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.1</version>
        <relativePath/>
    </parent>

    <groupId>org.joinmastodon</groupId>
    <artifactId>mastodon-parent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <java.version>25</java.version>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <modules>
        <module>mastodon-core</module>
        <module>mastodon-web</module>
        <module>mastodon-activitypub</module>
        <module>mastodon-federation</module>
        <module>mastodon-media</module>
        <module>mastodon-streaming</module>
    </modules>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

## 2. Set Up Development Environment

```bash
# Clone this repository
git clone <your-repo-url> mastodon-java
cd mastodon-java

# Verify Java version
java --version  # Should show OpenJDK 25

# Start PostgreSQL with Docker (Redis is optional)
docker-compose up -d postgres

# Build the project
mvn clean install

# Run the application
cd mastodon-web
mvn spring-boot:run
```

**Note:** The original setup included Redis, but modern PostgreSQL can handle caching and pub/sub messaging. Start with PostgreSQL-only and add Redis later if performance testing shows it's beneficial.

## 3. Database Configuration

Create `application.yml` in `./mastodon-web/src/main/resources/`:

```yaml
spring:
  application:
    name: mastodon-java
  
  datasource:
    url: jdbc:postgresql://localhost:5432/mastodon_development
    username: mastodon
    password: mastodon
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  # Redis configuration (optional - comment out if not using)
  # data:
  #   redis:
  #     host: localhost
  #     port: 6379
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080

server:
  port: 8080
  
logging:
  level:
    org.joinmastodon: DEBUG
    org.springframework.web: INFO
```

**Alternative: Using PostgreSQL for Caching**

If not using Redis, you can use PostgreSQL features:
- **LISTEN/NOTIFY** for pub/sub messaging
- **Materialized views** for caching query results
- **Unlogged tables** for session storage (faster, not crash-safe)

Example session table:
```sql
CREATE UNLOGGED TABLE sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT,
    data JSONB,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_sessions_user ON sessions(user_id);
CREATE INDEX idx_sessions_expires ON sessions(expires_at);
```

## 4. Docker Compose Configuration

Create `./docker-compose.yml` in the project root:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_USER: mastodon
      POSTGRES_PASSWORD: mastodon
      POSTGRES_DB: mastodon_development
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
  
  elasticsearch:
    image: elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

volumes:
  postgres_data:
  redis_data:
  elasticsearch_data:
```
