# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build Commands
```bash
mvnw.cmd clean install          # Build all modules (tests skipped by default)
mvnw.cmd clean install -DskipTests=false    # Build with tests
mvnw.cmd spring-boot:run -pl mastodon-web   # Run main application
```

## Single Test Execution
```bash
mvnw.cmd test -pl mastodon-web -Dtest=ApiIntegrationTest -DskipTests=false
```

## Architecture Notes
- **H2 embedded database** by default (no Docker required for dev)
- **Postgres-only architecture** - no Redis/Elasticsearch; PostgreSQL handles caching and pub/sub
- **API target**: Mastodon v4.5.6 compatibility

## Module Dependencies
```
mastodon-web → mastodon-core, mastodon-media, mastodon-streaming
mastodon-federation → mastodon-activitypub → mastodon-core
mastodon-jobs → mastodon-core
mastodon-streaming → mastodon-core (Postgres LISTEN/NOTIFY for real-time)
```

## Key Patterns
- IDs are Long internally, String in API responses (use `parseId()` in controllers)
- Visibility filtering via `StatusVisibilityService.canView(status, viewer)`
- Streaming notifications via `StreamingNotifier.notifyStatus(status)`
- HTTP signatures for federation in `mastodon-activitypub/signature/`

## Test Profile
- Tests use Testcontainers with PostgreSQL
- Dev profile uses H2 embedded database
- Database console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/mastodon_dev`)

## Federation Guardrails (from docs/guardrails.md)
- Preserve HTTP headers exactly where required
- Do not change TLS, cipher, or signature defaults
- Do not upgrade crypto providers unless forced
