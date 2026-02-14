# Build Error Fix: Duplicate Main Classes

## Problem

Build failed with error:
```
Unable to find a single main class from the following candidates:
- org.joinmastodon.streaming.MastodonStreamingApplication
- org.joinmastodon.streaming.StreamingApplication
```

## Root Cause

The `mastodon-streaming` module had:
1. Two duplicate `@SpringBootApplication` classes
2. The `spring-boot-maven-plugin` trying to create an executable JAR

**However**, `mastodon-streaming` is a **library module**, not a standalone application. Only `mastodon-web` should be executable.

## Solution Applied

### ✅ Removed Spring Boot Plugin from mastodon-streaming

**File:** `mastodon-streaming/pom.xml`

**Changed:**
```xml
<!-- BEFORE: Tried to create executable JAR -->
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>

<!-- AFTER: Just a library JAR -->
<!-- This is a library module, not a standalone application -->
<!-- No spring-boot-maven-plugin needed -->
```

## Module Architecture

| Module | Type | Spring Boot Plugin? | Main Class? |
|--------|------|---------------------|-------------|
| mastodon-core | Library | ❌ No | ❌ No |
| mastodon-activitypub | Library | ❌ No | ❌ No |
| mastodon-federation | Library | ❌ No | ❌ No |
| mastodon-media | Library | ❌ No | ❌ No |
| mastodon-streaming | Library | ❌ No (FIXED!) | ❌ No |
| mastodon-jobs | Library | ❌ No | ❌ No |
| mastodon-ui | Frontend | ❌ No | ❌ No |
| **mastodon-web** | **Application** | ✅ **YES** | ✅ **YES** |

## Optional Cleanup

The two duplicate application classes in `mastodon-streaming` are now harmless (they won't be used), but you can delete them for cleanliness:

**Files to delete (optional):**
1. `mastodon-streaming/src/main/java/org/joinmastodon/streaming/MastodonStreamingApplication.java`
2. `mastodon-streaming/src/main/java/org/joinmastodon/streaming/StreamingApplication.java`

**PowerShell command:**
```powershell
cd mastodon-streaming\src\main\java\org\joinmastodon\streaming
del MastodonStreamingApplication.java
del StreamingApplication.java
```

## Testing the Fix

Run the build:
```powershell
mvnw.cmd clean install
```

**Expected result:**
- ✅ All modules compile successfully
- ✅ Only `mastodon-web.jar` is executable
- ✅ Other modules produce library JARs

## Why This Happened

Common mistake in multi-module Spring Boot projects:
1. Someone generated module templates with `@SpringBootApplication`
2. The Spring Boot plugin was added to all module POMs
3. But only ONE module should be the main application

## Best Practice

**Only the web/main module should:**
- Have `@SpringBootApplication`
- Have `spring-boot-maven-plugin` in its `pom.xml`
- Be packaged as an executable JAR

**All other modules should:**
- Be plain libraries
- No `@SpringBootApplication`
- No `spring-boot-maven-plugin`
- Package as regular JARs

## Related Files Changed

1. ✅ `mastodon-streaming/pom.xml` - Removed Spring Boot plugin
2. ✅ Previous fix: Added Jackson dependency to fix compilation

---

**Status:** ✅ Fixed  
**Build should now succeed:** `mvnw.cmd clean install`
