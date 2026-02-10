# Mastodon Java (mastodon_4j)

A Java-based implementation of the Mastodon federated social network, built with OpenJDK 25 and Spring Boot 4.0.

## Quick Start

### Prerequisites

- OpenJDK 25
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16+ (via Docker)

### Setup

1. **Run the PowerShell script to create directory structure** (if not already done):
   ```powershell
   .\create-structure.ps1
   ```

2. **Start the required services**:
   ```bash
   docker-compose up -d
   ```

3. **Build the project**:
   ```bash
   mvn clean install
   ```

4. **Run the application**:
   ```bash
   cd mastodon-web
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

### Verify Setup

Check that services are running:
```bash
docker-compose ps
```

You should see:
- PostgreSQL on port 5432
- Elasticsearch on port 9200

### Project Structure

```
.
├── docs/                   # Documentation
├── mastodon-core/          # Domain models & repositories
├── mastodon-web/           # REST API & web layer
├── mastodon-activitypub/   # ActivityPub protocol
├── mastodon-federation/    # Federation logic
├── mastodon-media/         # Media processing
├── mastodon-streaming/     # Real-time streaming
├── docker-compose.yml      # Docker services
├── pom.xml                 # Parent POM
└── CLAUDE.md              # Project documentation hub
```

### Development

See [CLAUDE.md](./CLAUDE.md) for comprehensive documentation including:
- [Technology Stack](./docs/technology-stack.md)
- [Setup Guide](./docs/setup-guide.md)
- [Migration Strategy](./docs/migration-strategy.md)
- [Domain Models](./docs/domain-models.md)
- [API Endpoints](./docs/api-endpoints.md)

### Modules

- **mastodon-core**: Core domain entities, repositories, and services
- **mastodon-web**: REST API controllers and main application
- **mastodon-activitypub**: ActivityPub protocol implementation
- **mastodon-federation**: Inter-instance federation
- **mastodon-media**: Image and video processing
- **mastodon-streaming**: Real-time updates via WebSockets/SSE

## License

AGPLv3 (following original Mastodon)
