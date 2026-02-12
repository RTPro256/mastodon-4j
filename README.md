# Mastodon Java (mastodon_4j)

A Java-based implementation of the Mastodon federated social network with full web UI parity, built with OpenJDK 25 and Spring Boot 4.x.

**Target compatibility:** Mastodon v4.5.6 (released 2026-02-03).
**Architecture:** Postgres-only (no Redis, no Elasticsearch).

## Quick Start

### Prerequisites

- OpenJDK 25
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16+ (via Docker)
- Node.js 20+ (for the UI build once `mastodon-ui` is added)

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
   ./mvnw clean install
   ```
   (Windows: `mvnw.cmd clean install`)

   Optional preflight:
   ```bash
   ./scripts/preflight.sh
   ```
   (Windows: `powershell -ExecutionPolicy Bypass -File .\\scripts\\preflight.ps1`)

   If tests fail to start with `java: not found`, run the preflight scripts above to verify Java is installed and on PATH.

4. **Run the application**:
   ```bash
   cd mastodon-web
   ../mvnw spring-boot:run
   ```
   (Windows: `..\\mvnw.cmd spring-boot:run`)

The application will start on `http://localhost:8080`

### Verify Setup

Check that services are running:
```bash
docker-compose ps
```

You should see:
- PostgreSQL on port 5432

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
├── mastodon-ui/            # Web UI (planned)
├── mastodon-jobs/          # Background jobs (planned)
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
- **mastodon-ui**: Web UI (planned)
- **mastodon-jobs**: Background jobs (planned)

## License

AGPLv3 (following original Mastodon)
