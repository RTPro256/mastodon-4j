# Security Considerations

This document outlines security considerations and best practices for Mastodon 4j.

## Authentication

### OAuth 2.0 Implementation

Mastodon 4j implements OAuth 2.0 for authentication:

- **Authorization Code Grant**: For third-party applications
- **Password Grant**: For first-party clients (deprecated but supported)
- **Client Credentials Grant**: For application-level access

### Token Security

- Access tokens are random 64-character strings
- Tokens are stored hashed in the database
- Token expiration is configurable
- Revocation is immediate upon logout

### Best Practices

1. **Use HTTPS in production** - Never transmit tokens over HTTP
2. **Short token lifetimes** - Configure `mastodon.oauth.access-token-ttl` appropriately
3. **Secure token storage** - Clients should store tokens securely
4. **Token rotation** - Use refresh tokens for long-lived sessions

## Authorization

### Scope System

OAuth scopes control access:

| Scope | Description |
|-------|-------------|
| `read` | Read access to all resources |
| `write` | Write access to all resources |
| `follow` | Follow/unfollow accounts |
| `push` | Receive push notifications |
| `admin:read` | Admin read access |
| `admin:write` | Admin write access |

### Granular Scopes

More specific scopes are available:

- `read:accounts`, `write:accounts`
- `read:statuses`, `write:statuses`
- `read:notifications`, `write:notifications`
- etc.

### Role-Based Access

User roles control administrative access:

| Role | Permissions |
|------|-------------|
| `USER` | Standard user |
| `MODERATOR` | Content moderation |
| `ADMIN` | Full administrative access |

## Input Validation

### Request Validation

All API endpoints validate:

1. **Content-Type** - Must match expected type
2. **Request size** - Limited by configuration
3. **Parameter types** - Numeric IDs, valid enums
4. **Required fields** - Missing required fields rejected

### SQL Injection Prevention

- All database queries use parameterized statements
- JPA/Hibernate automatically escapes parameters
- Native queries use named parameters

### XSS Prevention

- All user content is HTML-escaped in responses
- Content-Security-Policy header restricts script execution
- X-XSS-Protection header enabled

### Path Traversal Prevention

- File paths are validated against base directories
- No user input in file paths without sanitization
- Media storage uses generated keys, not user filenames

## Rate Limiting

### Default Limits

| Endpoint Category | Requests/Minute |
|-------------------|-----------------|
| Anonymous | 300 |
| Authenticated | 600 |
| Write operations | 300 |
| Auth endpoints | 30 |

### Implementation

Rate limiting uses a sliding window algorithm:

1. Request count tracked per IP/user
2. `X-RateLimit-Limit` header shows limit
3. `X-RateLimit-Remaining` shows remaining
4. `X-RateLimit-Reset` shows reset time
5. HTTP 429 returned when exceeded

### Best Practices

1. **Handle 429 responses** - Clients should back off
2. **Use Retry-After header** - Wait before retrying
3. **Implement exponential backoff** - For repeated failures

## Federation Security

### HTTP Signatures

All federation requests are signed:

1. **Outgoing** - Requests signed with instance private key
2. **Incoming** - Signatures verified against remote public key
3. **Digest** - Request body hash verified

### Signature Format

```
Signature: keyId="https://example.com/users/alice#main-key",
           algorithm="rsa-sha256",
           headers="(request-target) host date digest",
           signature="base64-encoded-signature"
```

### Key Management

- RSA 2048-bit keys generated per account
- Public keys exposed via ActivityPub actor
- Private keys never transmitted

### Instance-Level Security

1. **Domain allowlisting** - Optionally restrict federation
2. **Domain blocklisting** - Block problematic instances
3. **Secure media proxying** - Validate remote media

## Data Protection

### Sensitive Data

The following data is considered sensitive:

- Email addresses
- IP addresses
- Password hashes
- OAuth tokens
- Private posts
- Direct messages

### Data Retention

| Data Type | Retention Period |
|-----------|------------------|
| Account data | Until deletion |
| Posts | Until deletion |
| Media | Until orphaned + 7 days |
| Logs | 30 days |
| Audit logs | 1 year |

### Right to be Forgotten

Account deletion:

1. Immediate account deactivation
2. Data deletion within 30 days
3. Federation deletion requests sent
4. Backups purged on schedule

## Security Headers

### Production Headers

```
X-Content-Type-Options: nosniff
X-Frame-Options: SAMEORIGIN
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Content-Security-Policy: default-src 'self'; ...
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

### Content Security Policy

Default CSP policy:

```
default-src 'self';
script-src 'self' 'unsafe-inline';
style-src 'self' 'unsafe-inline';
img-src 'self' data: https:;
media-src 'self' https:;
connect-src 'self' wss: https:;
frame-ancestors 'self';
form-action 'self';
```

## Reporting Security Issues

### Responsible Disclosure

If you discover a security vulnerability:

1. **Do not** open a public issue
2. Email security@your-instance.com
3. Include detailed reproduction steps
4. Allow 90 days for response before disclosure

### Security Contact

Configure a security contact email:

```yaml
mastodon:
  security:
    contact-email: security@example.com
```

## Security Checklist

### Pre-Deployment

- [ ] HTTPS enabled with valid certificate
- [ ] Production profile active
- [ ] Database credentials secured
- [ ] Secret keys generated (not default)
- [ ] Rate limiting enabled
- [ ] Security headers enabled
- [ ] Admin account created
- [ ] Default passwords changed
- [ ] Debug endpoints disabled
- [ ] Error messages sanitized

### Ongoing

- [ ] Regular security updates applied
- [ ] Access logs monitored
- [ ] Rate limit violations reviewed
- [ ] Failed authentication attempts monitored
- [ ] Federation audit logs reviewed
- [ ] Backup integrity verified

## Known Limitations

1. **No end-to-end encryption** - Direct messages are stored in plaintext
2. **Instance admin access** - Admins can view all content
3. **Federation trust** - Remote instances may not honor deletion requests
4. **Media metadata** - EXIF data may be preserved in images

## Compliance Considerations

### GDPR

- Data portability via API
- Right to erasure supported
- Consent management for email
- Data processing records

### COPPA

- No users under 13
- Age verification recommended

### DMCA

- Report mechanism for copyrighted content
- Takedown procedure documented
