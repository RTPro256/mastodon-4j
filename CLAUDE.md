# Mastodon to OpenJDK 25 Migration Project

## Project Overview

This project aims to migrate [Mastodon](https://github.com/mastodon/mastodon) (currently built with Ruby on Rails) to OpenJDK 25 using the Spring Boot framework. This is a large-scale architectural migration that will require reimplementing the ActivityPub-based federated social network in Java.

**Original Stack:** Ruby on Rails, PostgreSQL, Redis, Sidekiq  
**Target Stack:** OpenJDK 25, Spring Boot 4.0.1, PostgreSQL, (Optional: Redis), Spring Cloud

**Note:** Modern PostgreSQL can handle many caching and real-time use cases. We recommend starting without Redis to reduce complexity, then adding it only if performance metrics indicate it's needed. See [Technology Stack](./docs/technology-stack.md) for details.

## Documentation Structure

This project documentation is organized into separate files for easier navigation:

- **[Technology Stack](./docs/technology-stack.md)** - Detailed breakdown of all technologies and frameworks
- **[Setup Guide](./docs/setup-guide.md)** - Initial setup steps, configurations, and environment setup
- **[Migration Strategy](./docs/migration-strategy.md)** - 24-week phased migration plan
- **[Domain Models](./docs/domain-models.md)** - Core entity classes and data structures
- **[API Endpoints](./docs/api-endpoints.md)** - Complete REST API reference
- **[Resources](./docs/resources.md)** - Links to documentation, tutorials, and helpful resources

## Prerequisites

- OpenJDK 25 (Early Access builds available from [jdk.java.net](https://jdk.java.net/25/))
- Maven 3.9+ or Gradle 8.5+
- PostgreSQL 14+ (16+ recommended for best performance)
- Docker and Docker Compose (for development environment)
- Git
- IDE (IntelliJ IDEA recommended for Spring Boot development)

**Optional:**
- Redis 7+ (only add if performance testing shows it's needed)

## Quick Start

```bash
# Clone this repository
git clone <your-repo-url> mastodon-java
cd mastodon-java

# Verify Java version
java --version  # Should show OpenJDK 25

# Start required services with Docker
docker-compose up -d

# Build the project
mvn clean install

# Run the application
cd mastodon-web
mvn spring-boot:run
```

For detailed setup instructions, see [Setup Guide](./docs/setup-guide.md).

## Project Structure

```
./                          # Project root (current directory)
├── docs/
│   ├── technology-stack.md
│   ├── setup-guide.md
│   ├── migration-strategy.md
│   ├── domain-models.md
│   ├── api-endpoints.md
│   └── resources.md
├── mastodon-core/
│   ├── src/main/java/org/joinmastodon/core/
│   ├── src/main/resources/
│   └── pom.xml
├── mastodon-web/
│   ├── src/main/java/org/joinmastodon/web/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── static/
│   └── pom.xml
├── mastodon-activitypub/
│   ├── src/main/java/org/joinmastodon/activitypub/
│   └── pom.xml
├── mastodon-federation/
│   ├── src/main/java/org/joinmastodon/federation/
│   └── pom.xml
├── mastodon-media/
│   ├── src/main/java/org/joinmastodon/media/
│   └── pom.xml
├── mastodon-streaming/
│   ├── src/main/java/org/joinmastodon/streaming/
│   └── pom.xml
├── docker-compose.yml
├── pom.xml                 # Parent POM
├── CLAUDE.md               # This file
└── README.md
```
├── CLAUDE.md (this file)
└── README.md
```

## Development Workflow

### Phase 1: Core Infrastructure (Current Focus)
Start with the foundation:
1. Review [Technology Stack](./docs/technology-stack.md) to understand the frameworks
2. Follow [Setup Guide](./docs/setup-guide.md) to initialize your environment
3. Study [Domain Models](./docs/domain-models.md) to understand the data structure
4. Begin implementing basic entities and repositories

### Migration Phases
See [Migration Strategy](./docs/migration-strategy.md) for the complete 24-week roadmap.

## Development Guidelines

1. **Code Style**: Follow Google Java Style Guide
2. **Testing**: Aim for 80%+ code coverage
3. **Documentation**: JavaDoc for all public APIs
4. **Git Workflow**: Feature branches, pull requests, code reviews
5. **Database**: Use Flyway or Liquibase for migrations
6. **Security**: Follow OWASP guidelines, regular dependency updates

## Key Technologies

- **Spring Boot 4.0.1** - Main application framework (built on Spring Framework 7)
- **OpenJDK 25** - Java platform with latest features
- **PostgreSQL 16+** - Primary database (handles storage, caching, and pub/sub)
- **ActivityPub** - Federation protocol

For complete details, see [Technology Stack](./docs/technology-stack.md).

## Next Steps

1. ✅ Set up development environment ([Setup Guide](./docs/setup-guide.md))
2. 📖 Study the original Mastodon codebase for business logic understanding
3. 🗄️ Create database schema based on Mastodon's PostgreSQL schema
4. 🏗️ Implement core domain models ([Domain Models](./docs/domain-models.md))
5. 🔌 Build REST API layer ([API Endpoints](./docs/api-endpoints.md))
6. 🌐 Implement ActivityPub protocol handlers

## Important Notes

⚠️ **This is a major undertaking** requiring deep understanding of:
- ActivityPub protocol
- Federation concepts
- Distributed systems
- OAuth 2.0
- Spring Boot ecosystem

💡 **Start Small**: Consider beginning with a minimal viable implementation focusing on:
- Local posting and timelines
- Basic following/followers
- Simple ActivityPub federation

📚 **Learn as You Go**: The original Mastodon has thousands of features built over years. Prioritize core functionality first and reference the [Resources](./docs/resources.md) guide for learning materials.

## Contributing

This is a learning project. Contributions welcome! Please:
- Open issues for bugs or feature requests
- Submit pull requests with tests
- Follow the code style guidelines
- Update documentation

## Resources

For comprehensive links to documentation, tutorials, and community resources, see [Resources](./docs/resources.md).

## License

Follow the original Mastodon license (AGPLv3) for derivative works.

