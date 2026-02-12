# Resources and References

## Official Documentation

### Mastodon
- [Mastodon API Documentation](https://docs.joinmastodon.org/api/)
- [Mastodon GitHub Repository](https://github.com/mastodon/mastodon)
- [Mastodon Developer Documentation](https://docs.joinmastodon.org/dev/overview/)

### ActivityPub & Federation
- [ActivityPub Specification](https://www.w3.org/TR/activitypub/)
- [ActivityStreams 2.0](https://www.w3.org/TR/activitystreams-core/)
- [WebFinger RFC 7033](https://tools.ietf.org/html/rfc7033)
- [HTTP Signatures](https://datatracker.ietf.org/doc/html/draft-cavage-http-signatures)

### Spring Boot & Java
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [OpenJDK 25 Documentation](https://openjdk.org/projects/jdk/25/)

## Original Codebase Study

When implementing features, refer to the original Ruby on Rails codebase:

### Key Directories to Study
- `app/models/` - Domain models and business logic
- `app/controllers/api/` - API controllers
- `app/lib/activitypub/` - ActivityPub protocol implementation
- `app/workers/` - Background job patterns
- `app/services/` - Service layer implementations
- `db/schema.rb` - Database schema definition
- `db/migrate/` - Database migrations

### Important Files
- `config/routes.rb` - API routing configuration
- `app/serializers/` - JSON serialization logic
- `app/validators/` - Custom validation rules
- `streaming/index.js` - WebSocket streaming server

## Development Tools

### Build Tools
- [Maven Documentation](https://maven.apache.org/guides/)
- [Gradle Documentation](https://docs.gradle.org/)

### Database
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Flyway Database Migration](https://flywaydb.org/documentation/)
- [Liquibase Documentation](https://docs.liquibase.com/)

### Testing
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Testcontainers Documentation](https://www.testcontainers.org/)

### Media Processing
- [ImageMagick Documentation](https://imagemagick.org/index.php)
- [FFmpeg Documentation](https://ffmpeg.org/documentation.html)
- [Thumbnailator GitHub](https://github.com/coobird/thumbnailator)

## Learning Resources

### ActivityPub & Federation
- [A highly opinionated guide to learning about ActivityPub](https://tinysubversions.com/notes/reading-activitypub/)
- [ActivityPub Rocks](https://activitypub.rocks/)
- [Guide for new ActivityPub implementers](https://socialhub.activitypub.rocks/t/guide-for-new-activitypub-implementers/479)

### Spring Boot
- [Spring Boot Guides](https://spring.io/guides)
- [Baeldung Spring Tutorials](https://www.baeldung.com/spring-tutorial)
- [Spring Boot Best Practices](https://www.marcobehler.com/guides/spring-boot)

### OAuth 2.0
- [OAuth 2.0 Simplified](https://www.oauth.com/)
- [RFC 6749 - OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749)

## Community & Support

### Forums & Chat
- [Mastodon Development Discord](https://discord.gg/mastodon)
- [Spring Community Forums](https://spring.io/community)
- [Stack Overflow - mastodon tag](https://stackoverflow.com/questions/tagged/mastodon)
- [Stack Overflow - spring-boot tag](https://stackoverflow.com/questions/tagged/spring-boot)

### Related Projects
- [Pleroma](https://pleroma.social/) - Another ActivityPub implementation
- [Misskey](https://misskey-hub.net/) - Alternative federated platform
- [Pixelfed](https://pixelfed.org/) - Image-focused ActivityPub platform

## Style Guides & Best Practices

### Java
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Effective Java by Joshua Bloch](https://www.oreilly.com/library/view/effective-java/9780134686097/)

### Security
- [OWASP Top Ten](https://owasp.org/www-project-top-ten/)
- [OWASP API Security Top 10](https://owasp.org/www-project-api-security/)
- [Spring Security Best Practices](https://docs.spring.io/spring-security/reference/servlet/appendix/faq.html)

### Database
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)
- [Database Indexing Best Practices](https://use-the-index-luke.com/)

## Useful Libraries

### ActivityPub & Federation
- Consider implementing from scratch for learning, but review:
  - [activitypub4j](https://github.com/marianobarrios/activitypub4j) - Java ActivityPub library

### HTTP & REST
- [OkHttp](https://square.github.io/okhttp/) - HTTP client
- [RestTemplate/WebClient](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#webmvc-client) - Spring's HTTP clients

### JSON Processing
- [Jackson](https://github.com/FasterXML/jackson) - JSON library (included with Spring Boot)
- [JSON-LD Java](https://github.com/jsonld-java/jsonld-java) - For ActivityPub JSON-LD

### Validation
- [Hibernate Validator](https://hibernate.org/validator/) - Bean validation implementation
- [Jakarta Bean Validation](https://beanvalidation.org/)

## Performance & Monitoring

### Monitoring
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/) - Metrics collection
- [Prometheus](https://prometheus.io/) - Monitoring system

### Caching
- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Caffeine Cache](https://github.com/ben-manes/caffeine)

## Additional Reading

### Distributed Systems
- "Designing Data-Intensive Applications" by Martin Kleppmann
- "Building Microservices" by Sam Newman

### Federation & Decentralization
- [How the Fediverse Works](https://github.com/BasixKOR/How-the-Fediverse-Works)
- [Fediverse Party](https://fediverse.party/) - Overview of federated platforms
