# Coding Standards

## General Principles

- Prefer minimal diffs
- Avoid stylistic reformatting
- Do not reorder imports unless required
- Preserve existing naming conventions
- Add comments only when behavior changes are unavoidable

## Code Quality

### Unused Imports
- **Remove all unused imports** before committing code
- IDE warnings about unused imports should be resolved
- Run IDE "Organize Imports" or equivalent before committing

### Deprecated Code
- **Do not use deprecated APIs** unless absolutely necessary
- When a deprecated API must be used, add `@SuppressWarnings("deprecation")` with a comment explaining why
- Prefer modern replacements for deprecated methods:
  - Use `HttpStatusCode` instead of deprecated `HttpStatus` fields
  - Use `JacksonJsonHttpMessageConverter` instead of `MappingJackson2HttpMessageConverter`
  - Use module-specific Testcontainers classes (e.g., `org.testcontainers.postgresql.PostgreSQLContainer`)

### Resource Management
- Always close resources properly (use try-with-resources or ensure cleanup)
- Suppress resource leak warnings only when lifecycle is managed externally (e.g., Testcontainers)

## Code Style

### Imports
```java
// Good - only used imports
import org.springframework.stereotype.Service;
import org.joinmastodon.core.entity.User;

// Bad - unused import
import java.time.Instant;  // Never used - REMOVE
```

### Deprecated API Usage
```java
// Good - use modern API
return HttpStatusCode.valueOf(422);

// Bad - deprecated API
return HttpStatus.UNPROCESSABLE_ENTITY;  // Deprecated

// Acceptable - with suppression and explanation
@SuppressWarnings("deprecation") // Required until migration to new API is complete
public void legacyMethod() {
    // ...
}
```

### Resource Management
```java
// Good - try-with-resources
try (InputStream is = new FileInputStream(file)) {
    // use resource
}

// Good - managed lifecycle with documentation
@SuppressWarnings("resource") // Container lifecycle managed by Testcontainers JUnit extension
private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");
```

## IDE Configuration

Configure your IDE to:
1. Show warnings for unused imports
2. Show warnings for deprecated API usage
3. Show warnings for resource leaks
4. Enable "Save Actions" to automatically remove unused imports

### VS Code Settings
```json
{
    "java.cleanup.actions": ["qualifyMembers", "qualifyStaticMembers"],
    "java.saveActions.organizeImports": true,
    "java.compiler.annotation.nullanalysis.mode": "disabled"
}
```

### IntelliJ IDEA Settings
- Editor → General → Auto Import → Optimize imports on the fly
- Editor → Inspections → Java → Code style → Unused import
- Editor → Inspections → Java → Code maturity → Deprecated API usage
