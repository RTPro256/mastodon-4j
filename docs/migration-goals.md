# Migration Goals

## Required
- Java runtime: Java 25
- Spring Boot: Latest stable release
- All tests passing
- Clean startup with no illegal-access warnings

## Constraints
- No public API changes
- No schema changes unless required by framework upgrades
- Incremental commits preferred over large rewrites

## Strategy
- Prefer official Spring migration paths
- Follow deprecation warnings, not guesswork
