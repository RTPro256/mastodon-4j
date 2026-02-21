# Admin Setup Experience Plan

## Overview

This document outlines the plan for creating a comprehensive admin setup experience that guides administrators through configuring all settings and features, with prompts for missing or incorrect settings.

## Goals

1. Provide a guided setup wizard for new installations
2. Validate all configuration settings
3. Alert administrators to missing or incorrect settings
4. Provide clear documentation for each setting
5. Enable easy configuration through a web interface

## Components

### 1. Setup Wizard (New Installations)

**Location:** `/admin/setup`

**Flow:**
1. Welcome screen with prerequisites check
2. Database configuration
3. Instance settings
4. Email configuration
5. Federation settings
6. Security settings
7. Review and confirm

### 2. Configuration Dashboard

**Location:** `/admin/settings`

**Features:**
- Overview of all configuration categories
- Status indicators (✅ configured, ⚠️ needs attention, ❌ missing)
- Quick links to edit each category
- Validation warnings

### 3. Configuration Validation Service

**Backend Service:** `ConfigurationValidationService`

Validates:
- Required environment variables
- Database connectivity
- Email server connectivity
- Federation settings
- Security settings
- Media storage

### 4. Setup Prompts

**Implementation:** Startup checks and admin dashboard alerts

---

## Implementation Plan

### Phase 1: Configuration Validation Service

Create a service that validates all configuration settings:

```java
@Service
public class ConfigurationValidationService {
    
    public ValidationResult validateAll() {
        // Validate each category
    }
    
    public ValidationResult validateDatabase() { }
    public ValidationResult validateEmail() { }
    public ValidationResult validateFederation() { }
    public ValidationResult validateSecurity() { }
    public ValidationResult validateMedia() { }
}
```

### Phase 2: Admin Configuration API

Create REST endpoints for configuration management:

```
GET  /api/v1/admin/config/status       - Get configuration status
GET  /api/v1/admin/config/validate     - Validate all settings
GET  /api/v1/admin/config/categories   - List configuration categories
GET  /api/v1/admin/config/{category}   - Get category details
POST /api/v1/admin/config/{category}   - Update category settings
```

### Phase 3: Admin UI Dashboard

Create admin dashboard with:
- Configuration status overview
- Validation warnings
- Quick actions to fix issues

### Phase 4: Setup Wizard

Create guided setup wizard for new installations.

---

## Configuration Categories

### 1. Database Configuration

**Settings:**
| Setting | Required | Validation |
|---------|----------|------------|
| DATABASE_URL | Yes | Valid JDBC URL |
| DATABASE_USERNAME | Yes | Non-empty |
| DATABASE_PASSWORD | Yes | Non-empty (production) |
| Connection pool size | No | Positive integer |

**Validation:**
- Test database connection
- Verify Flyway migrations are applied
- Check PostgreSQL version (14+)

**Prompts:**
- "Database URL is missing. Please set DATABASE_URL environment variable."
- "Cannot connect to database. Check credentials and network connectivity."
- "Database migrations pending. Run Flyway migrations."

### 2. Instance Configuration

**Settings:**
| Setting | Required | Validation |
|---------|----------|------------|
| MASTODON_DOMAIN | Yes | Valid domain name |
| MASTODON_BASE_URL | Yes | Valid HTTPS URL (production) |
| Instance name | No | - |
| Instance description | No | - |
| Admin email | Recommended | Valid email format |

**Validation:**
- Domain resolves to this server
- HTTPS certificate valid (production)
- Base URL matches domain

**Prompts:**
- "Instance domain is not configured. Set MASTODON_DOMAIN."
- "HTTPS is required for production. Configure TLS certificate."
- "Admin email is recommended for user support."

### 3. Email Configuration

**Settings:**
| Setting | Required | Validation |
|---------|----------|------------|
| SMTP_SERVER | Conditional* | Valid hostname |
| SMTP_PORT | No | Valid port (1-65535) |
| SMTP_LOGIN | Conditional* | Non-empty |
| SMTP_PASSWORD | Conditional* | Non-empty |

*Required if email confirmation is enabled

**Validation:**
- Test SMTP connection
- Send test email
- Verify sender address

**Prompts:**
- "Email confirmation is enabled but SMTP is not configured."
- "Cannot connect to SMTP server. Check host and port."
- "SMTP authentication failed. Check credentials."

### 4. Federation Configuration

**Settings:**
| Setting | Required | Validation |
|---------|----------|------------|
| Federation enabled | No | Boolean |
| Federation domain | Conditional* | Valid domain |
| HTTP signatures | Recommended | Enabled |

*Required if federation is enabled

**Validation:**
- Test WebFinger endpoint
- Test inbox/outbox endpoints
- Verify HTTP signature generation

**Prompts:**
- "Federation is enabled but domain is not configured."
- "WebFinger endpoint not accessible. Check reverse proxy."
- "HTTP signatures should be enabled for security."

### 5. Security Configuration

**Settings:**
| Setting | Required | Validation |
|---------|----------|------------|
| SECRET_KEY_BASE | Yes | 64 hex characters |
| OTP_SECRET | Yes | 64 hex characters |
| Rate limiting | Recommended | Enabled |
| CORS origins | Recommended | Valid URLs |

**Validation:**
- Secrets are strong (64+ characters)
- Secrets are not default values
- Rate limiting is configured
- CORS is restricted (production)

**Prompts:**
- "SECRET_KEY_BASE is missing. Generate with: openssl rand -hex 32"
- "OTP_SECRET is missing. Generate with: openssl rand -hex 32"
- "Secrets should not be default values in production."
- "CORS allows all origins. Restrict to your domain."

### 6. Media Configuration

**Settings:**
| Setting | Required | Validation |
|---------|----------|------------|
| Storage path | Yes | Writable directory |
| Max upload size | No | Valid size format |
| FFmpeg path | Conditional* | Executable exists |

*Required if transcoding is enabled

**Validation:**
- Directory exists and is writable
- Sufficient disk space
- FFmpeg available (if transcoding)

**Prompts:**
- "Media storage path does not exist. Create directory."
- "Media storage is not writable. Check permissions."
- "FFmpeg not found. Install for video transcoding."

---

## Validation Result Structure

```java
public class ValidationResult {
    private String category;
    private ValidationStatus status; // OK, WARNING, ERROR
    private List<ValidationIssue> issues;
    private List<ValidationSuggestion> suggestions;
}

public class ValidationIssue {
    private String setting;
    private IssueSeverity severity; // ERROR, WARNING, INFO
    private String message;
    private String documentation;
    private String fixCommand; // e.g., "openssl rand -hex 32"
}

public class ValidationSuggestion {
    private String setting;
    private String currentValue;
    private String suggestedValue;
    private String reason;
}
```

---

## API Response Example

```json
{
  "status": "WARNING",
  "categories": [
    {
      "name": "database",
      "status": "OK",
      "issues": []
    },
    {
      "name": "instance",
      "status": "OK",
      "issues": []
    },
    {
      "name": "email",
      "status": "WARNING",
      "issues": [
        {
          "setting": "SMTP_SERVER",
          "severity": "WARNING",
          "message": "Email confirmation is enabled but SMTP is not configured",
          "documentation": "/docs/configuration#email",
          "fixHint": "Set SMTP_SERVER environment variable"
        }
      ]
    },
    {
      "name": "security",
      "status": "ERROR",
      "issues": [
        {
          "setting": "SECRET_KEY_BASE",
          "severity": "ERROR",
          "message": "Secret key is missing",
          "documentation": "/docs/configuration#secrets",
          "fixCommand": "openssl rand -hex 32"
        }
      ]
    }
  ],
  "missingRequiredSettings": [
    "SECRET_KEY_BASE",
    "OTP_SECRET"
  ],
  "recommendations": [
    {
      "setting": "MASTODON_ADMIN_EMAIL",
      "message": "Admin email is recommended for user support"
    }
  ]
}
```

---

## Startup Validation

On application startup, validate configuration and log warnings:

```java
@Component
public class ConfigurationStartupValidator implements ApplicationRunner {
    
    @Override
    public void run(ApplicationArguments args) {
        ValidationResult result = validationService.validateAll();
        
        if (result.hasErrors()) {
            log.error("═══════════════════════════════════════════════════════════");
            log.error("CONFIGURATION ERRORS DETECTED");
            log.error("═══════════════════════════════════════════════════════════");
            for (ValidationIssue issue : result.getErrors()) {
                log.error("  ❌ {}: {}", issue.getSetting(), issue.getMessage());
                if (issue.getFixCommand() != null) {
                    log.error("     Fix: {}", issue.getFixCommand());
                }
            }
            log.error("═══════════════════════════════════════════════════════════");
        }
        
        if (result.hasWarnings()) {
            log.warn("Configuration warnings detected. Check admin dashboard.");
        }
    }
}
```

---

## Admin Dashboard UI Mockup

```
┌─────────────────────────────────────────────────────────────────┐
│  Admin Dashboard                                    [Setup Mode] │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Configuration Status                                           │
│  ─────────────────────────────────────────────────────────────  │
│                                                                 │
│  ✅ Database        Connected to PostgreSQL 16                  │
│  ✅ Instance        social.example.com                          │
│  ⚠️ Email           SMTP not configured                         │
│  ❌ Security        Missing required secrets                     │
│  ✅ Federation      Enabled and working                          │
│  ✅ Media           Storage configured                           │
│                                                                 │
│  ─────────────────────────────────────────────────────────────  │
│                                                                 │
│  ⚠️ Action Required: 2 issues need attention                    │
│                                                                 │
│  ❌ SECRET_KEY_BASE is missing                                  │
│     Generate a secret key:                                      │
│     [Copy] openssl rand -hex 32                                 │
│                                                                 │
│  ❌ OTP_SECRET is missing                                       │
│     Generate an OTP secret:                                     │
│     [Copy] openssl rand -hex 32                                 │
│                                                                 │
│  ⚠️ SMTP_SERVER is not configured                               │
│     Email confirmation will not work. Configure SMTP or         │
│     disable email confirmation.                                 │
│     [Configure SMTP] [Disable Email Confirmation]               │
│                                                                 │
│  ─────────────────────────────────────────────────────────────  │
│                                                                 │
│  [Run Setup Wizard]  [Validate Configuration]  [View All Settings] │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Setup Wizard Flow

### Step 1: Welcome
```
┌─────────────────────────────────────────────────────────────────┐
│  Welcome to Mastodon 4j Setup                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  This wizard will guide you through setting up your instance.   │
│                                                                 │
│  Prerequisites:                                                 │
│  ✅ Java 25 installed                                           │
│  ✅ PostgreSQL 14+ available                                    │
│  ✅ Domain name configured                                      │
│  ✅ HTTPS certificate (recommended)                             │
│                                                                 ││
│  [Start Setup]                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Step 2: Database
```
┌─────────────────────────────────────────────────────────────────┐
│  Database Configuration                                    1/5  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Database URL *                                                 │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ jdbc:postgresql://localhost:5432/mastodon                   ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
│  Username *                                                     │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ mastodon                                                     ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
│  Password *                                                     │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ ••••••••••••                                                 ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
│  [Test Connection]  Status: Not tested                          │
│                                                                 │
│                                        [Back]  [Next: Instance]  │
└─────────────────────────────────────────────────────────────────┘
```

### Step 3: Instance
```
┌─────────────────────────────────────────────────────────────────┐
│  Instance Configuration                                    2/5  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Domain *                                                       │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ social.example.com                                          ││
│  └─────────────────────────────────────────────────────────────┘│
│  Your instance's public domain (without https://)               │
│                                                                 │
│  Base URL *                                                     │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ https://social.example.com                                  ││
│  └─────────────────────────────────────────────────────────────┘│
│  Full URL including https://                                    │
│                                                                 │
│  Instance Name                                                  │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ My Mastodon Instance                                        ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
│  Admin Email (recommended)                                      │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ admin@example.com                                           ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
│                                     [Back]  [Next: Email Setup]  │
└─────────────────────────────────────────────────────────────────┘
```

### Step 4: Email (Optional)
```
┌─────────────────────────────────────────────────────────────────┐
│  Email Configuration                                       3/5  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Email Confirmation: [Enabled] [Disabled]                        │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ SMTP Configuration                                          ││
│  ├─────────────────────────────────────────────────────────────┤│
│  │ SMTP Server: smtp.example.com                               ││
│  │ Port:       587                                             ││
│  │ Username:   your_username                                   ││
│  │ Password:   ••••••••••••                                     ││
│  │                                                             ││
│  │ [Test SMTP Connection]  Status: Not tested                  ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
│  ⚠️ If email is disabled, users cannot confirm their email.     │
│                                                                 │
│                                   [Back]  [Next: Security Setup] │
└─────────────────────────────────────────────────────────────────┘
```

### Step 5: Security
```
┌─────────────────────────────────────────────────────────────────┐
│  Security Configuration                                    4/5  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Secret Key Base *                                              │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ [Generate]  a1b2c3d4e5f6...                                 ││
│  └─────────────────────────────────────────────────────────────┘│
│  Used for encrypting session data. Keep this secret!            │
│                                                                 │
│  OTP Secret *                                                   │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ [Generate]  f6e5d4c3b2a1...                                 ││
│  └─────────────────────────────────────────────────────────────┘│
│  Used for two-factor authentication. Keep this secret!          │
│                                                                 │
│  Rate Limiting: [Enabled]                                       │
│  Requests per minute: [300]                                     │
│                                                                 │
│  CORS Origins (comma-separated)                                 │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ https://social.example.com                                  ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                 │
│                                   [Back]  [Next: Review Setup]   │
└─────────────────────────────────────────────────────────────────┘
```

### Step 6: Review
```
┌─────────────────────────────────────────────────────────────────┐
│  Review Configuration                                      5/5  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Database                                                       │
│  ✅ PostgreSQL at localhost:5432/mastodon                       │
│                                                                 │
│  Instance                                                       │
│  ✅ Domain: social.example.com                                  │
│  ✅ URL: https://social.example.com                             │
│                                                                 │
│  Email                                                          │
│  ✅ SMTP configured at smtp.example.com:587                     │
│  ✅ Email confirmation enabled                                  │
│                                                                 │
│  Security                                                       │
│  ✅ Secrets generated                                           │
│  ✅ Rate limiting enabled (300 req/min)                         │
│  ✅ CORS restricted to your domain                              │
│                                                                 │
│  ─────────────────────────────────────────────────────────────  │
│                                                                 │
│  Ready to create configuration!                                 │
│                                                                 │
│  [Download .env file]  [Apply to Environment]                   │
│                                                                 │
│  [Back]  [Complete Setup]                                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## Implementation Checklist

### Backend
- [ ] Create `ConfigurationValidationService`
- [ ] Create `ValidationResult` and related DTOs
- [ ] Create `ConfigurationStartupValidator`
- [ ] Create admin configuration API endpoints
- [ ] Add configuration validation tests

### Frontend
- [ ] Create admin configuration dashboard
- [ ] Create setup wizard component
- [ ] Create configuration status indicators
- [ ] Create validation warning displays

### Documentation
- [ ] Document all configuration settings
- [ ] Create troubleshooting guide
- [ ] Add configuration examples

---

## Files to Create

1. `mastodon-core/src/main/java/org/joinmastodon/core/service/ConfigurationValidationService.java`
2. `mastodon-core/src/main/java/org/joinmastodon/core/dto/ValidationResult.java`
3. `mastodon-core/src/main/java/org/joinmastodon/core/dto/ValidationIssue.java`
4. `mastodon-web/src/main/java/org/joinmastodon/web/api/admin/AdminConfigController.java`
5. `mastodon-web/src/main/java/org/joinmastodon/web/config/ConfigurationStartupValidator.java`
6. `mastodon-ui/src/components/admin/ConfigDashboard.tsx`
7. `mastodon-ui/src/components/admin/SetupWizard.tsx`
