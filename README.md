# Mastodon Java (mastodon_4j)

A Java-based implementation of the Mastodon federated social network with full web UI parity, built with OpenJDK 25 and Spring Boot 4.x.

**Target compatibility:** Mastodon v4.5.6 (released 2026-02-03).
**Architecture:** H2 embedded database (no Docker required!) or optional PostgreSQL.

## ✨ New: Simplified Setup (No Docker Required!)

This project now uses **H2 embedded database** by default, which means:
- ✅ No Docker installation needed
- ✅ No external database setup
- ✅ Everything runs from a single command
- ✅ Perfect for development and learning

## Quick Start

### Prerequisites

- **OpenJDK 25** (Required)
- **Maven 3.9+** (Comes with the project via mvnw)
- **That's it!** No Docker, no PostgreSQL installation needed!

### Windows 11 Setup

1. **Verify Java is installed**:
   ```powershell
   java --version
   ```
   Should show: `openjdk 25` or similar

2. **Build the project**:
   ```powershell
   mvnw.cmd clean install
   ```

3. **Run the application**:
   ```powershell
   cd mastodon-web
   ..\mvnw.cmd spring-boot:run
   ```

4. **Access the application**:
   - Main app: http://localhost:8080
   - Database console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:file:./data/mastodon_dev`
     - Username: `sa`
     - Password: (leave empty)

**That's it!** The application creates its own database file in `./data/mastodon_dev.mv.db`

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
├── data/                   # H2 database files (auto-created)
├── pom.xml                 # Parent POM
└── CLAUDE.md              # Project documentation hub
```

### Database Options

**Default: H2 (Embedded)**
- Automatically configured
- No setup required
- Database file: `./data/mastodon_dev.mv.db`
- Perfect for development

**Optional: PostgreSQL**
- For production-like testing
- Requires Docker or native PostgreSQL installation
- Run with: `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres`
- See `docker-compose.yml` for Docker setup

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

### Using with Ollama (Local AI Models)

This project is configured to work with Ollama for AI-assisted development:

1. **Start Ollama**:
   ```powershell
   ollama serve
   ```

2. **Launch Claude Code** (in a separate terminal):
   ```powershell
   claude
   ```

3. **Configuration**: 
   - Settings are in `.claude/settings.json`
   - Model: `qwen-mastodon` (or your preferred Ollama model)
   - Uses local Ollama instance at `http://localhost:11434`

### Troubleshooting

**Q: Database file too large?**
A: Delete `./data/mastodon_dev.mv.db` to reset the database

**Q: Port 8080 already in use?**
A: Change `server.port` in `application.yml` or stop the other application

**Q: Want to use PostgreSQL?**
A: Run with `-Dspring-boot.run.profiles=postgres` and start Docker: `docker-compose up -d`

## License

AGPLv3 (following original Mastodon)
