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
- **Redis** - Optional high-performance caching layer (see notes below)
- **Spring Data Redis** - Redis integration (if using Redis)
- **Hibernate** - ORM

### Redis vs PostgreSQL-Only Architecture

**Modern PostgreSQL (versions 16-18) can handle many use cases traditionally served by Redis:**

**PostgreSQL Can Do:**
- ✅ In-memory caching with shared buffers
- ✅ LISTEN/NOTIFY for pub/sub messaging
- ✅ Materialized views for cached query results
- ✅ Unlogged tables for temporary high-speed data
- ✅ Session storage with proper indexing
- ✅ Rate limiting with database triggers
- ✅ Real-time updates via LISTEN/NOTIFY

**When Redis Still Makes Sense:**
- ⚡ **Sub-millisecond latency requirements** - Redis keeps data in RAM with simpler data structures
- 🔄 **High-frequency writes** - Timeline feeds, streaming counters, leaderboards
- 📊 **Specialized data structures** - HyperLogLog for cardinality, sorted sets for rankings
- 🌊 **Streaming workloads** - Redis Streams for event processing
- 💾 **Cache eviction policies** - LRU, LFU built-in
- 🚀 **Horizontal scaling** - Redis Cluster for distributed caching

**Recommendation for this Project:**
Start **without Redis** and use PostgreSQL for everything:
- Use PostgreSQL LISTEN/NOTIFY for real-time streaming
- Use materialized views for timeline caching
- Monitor performance metrics

**Add Redis later if you observe:**
- Timeline generation taking >100ms
- Database connection pool exhaustion under load
- Need for more sophisticated caching strategies
- Streaming API struggling with concurrent connections

This approach reduces complexity and operational overhead while you're learning and building the core features.

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
