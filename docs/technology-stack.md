# Technology Stack

## Core Framework
- **Spring Boot 4.0.1** - Main application framework (built on Spring Framework 7)
- **Spring Data JPA** - Database access layer
- **Spring Security** - Authentication and authorization
- **Spring Cloud** - Microservices support (optional for scaling)

**Note:** Spring Boot 4.0 is a major release with:
- Complete modularization providing smaller, more focused jars
- First-class support for Java 25 (while maintaining Java 17 compatibility)
- Portfolio-wide improvements for null safety with JSpecify
- Support for API versioning and HTTP service clients
- Built on Spring Framework 7

## ActivityPub Implementation
- **Custom ActivityPub library** - Core federation protocol
- **HTTP Signatures** - Request signing for federation
- **WebFinger** - Account discovery

## Data Layer
- **PostgreSQL** - Primary database
- **Hibernate** - ORM
- **PostgreSQL Full-Text Search** - Search and ranking (tsvector, tsquery, pg_trgm)
- **LISTEN/NOTIFY** - Pub/sub for streaming and background coordination

### PostgreSQL-Only Architecture

**This project targets Postgres-only operation:**

- ✅ In-memory caching with shared buffers
- ✅ LISTEN/NOTIFY for pub/sub messaging
- ✅ Materialized views for cached query results
- ✅ Unlogged tables for temporary high-speed data
- ✅ Session storage with proper indexing
- ✅ Rate limiting with database triggers
- ✅ Real-time updates via LISTEN/NOTIFY
- ✅ Full-text search with `tsvector`, `tsquery`, and `pg_trgm`

This approach reduces complexity and operational overhead while maintaining compatibility with upstream Mastodon behavior.

## Background Jobs
- **Spring Batch** - For batch processing
- **Spring Task Scheduling** - Background job scheduling
- Alternative: **Quartz Scheduler**

## Media Processing
- **ImageMagick** (via JNI or ProcessBuilder)
- **FFmpeg** (for video processing)
- **Thumbnailator** - Java image scaling library

## Real-time Features
- **Spring WebFlux** - Reactive streams
- **Server-Sent Events (SSE)** - Real-time updates
- **WebSocket** - Streaming API

## Testing
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Testcontainers** - Integration testing with Docker
- **Spring Boot Test** - Application testing
