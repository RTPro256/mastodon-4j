# UI Contracts (Draft)

This document defines field-level data contracts and example payloads used by the web UI. It is a draft aligned to the current milestone plan and API endpoint list. Contracts follow Mastodon-style JSON naming where applicable and should not change without coordination with federation and API compatibility goals.

Conventions:
- `string` uses UTF-8 text.
- `id` is stringified to avoid precision issues.
- Timestamps are ISO-8601 UTC strings.
- List endpoints return arrays with pagination via `Link` headers. The UI client can wrap them into `PagedResponse<T>` for convenience.

## InstanceInfo
Used by: `GET /api/v1/instance`

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `uri` | string | yes | Instance domain, no scheme |
| `title` | string | yes | Instance title |
| `description` | string | yes | HTML or plain text |
| `short_description` | string | no | Short summary |
| `email` | string | no | Contact email |
| `version` | string | yes | Server version string |
| `urls` | object | no | Additional URLs |
| `stats` | object | yes | Usage statistics |
| `stats.user_count` | number | yes | Total users |
| `stats.status_count` | number | yes | Total statuses |
| `stats.domain_count` | number | yes | Total known domains |
| `configuration` | object | no | Feature flags and limits |

Example:
```json
{
  "uri": "example.social",
  "title": "Example Social",
  "description": "A friendly instance.",
  "short_description": "Be kind.",
  "email": "admin@example.social",
  "version": "4.5.6",
  "urls": {
    "streaming_api": "wss://example.social"
  },
  "stats": {
    "user_count": 1234,
    "status_count": 56789,
    "domain_count": 210
  },
  "configuration": {
    "statuses": {
      "max_characters": 500
    }
  }
}
```

## ErrorResponse
Used by: all endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `error` | string | yes | Human-readable error |
| `error_description` | string | no | Additional detail |
| `details` | object | no | Field-level errors |

Example:
```json
{
  "error": "Validation failed",
  "error_description": "status text is too long",
  "details": {
    "text": "must be 500 characters or fewer"
  }
}
```

## OAuthApp
Used by: `POST /api/v1/apps`

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | App ID |
| `name` | string | yes | App name |
| `website` | string | no | App website |
| `client_id` | string | yes | OAuth client id |
| `client_secret` | string | yes | OAuth client secret |
| `redirect_uri` | string | yes | Redirect URI |

Example:
```json
{
  "id": "42",
  "name": "Example Client",
  "website": "https://client.example",
  "client_id": "abc123",
  "client_secret": "def456",
  "redirect_uri": "https://client.example/callback"
}
```

## TokenResponse
Used by: `POST /oauth/token`

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `access_token` | string | yes | Bearer token |
| `token_type` | string | yes | Usually `Bearer` |
| `scope` | string | yes | Space-delimited scopes |
| `created_at` | number | yes | Epoch seconds |

Example:
```json
{
  "access_token": "access123",
  "token_type": "Bearer",
  "scope": "read write follow",
  "created_at": 1739210000
}
```

## Account
Used by: account endpoints, statuses, notifications

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | Account id |
| `username` | string | yes | Local username |
| `acct` | string | yes | Username with domain for remote |
| `display_name` | string | no | Display name |
| `locked` | boolean | yes | Requires approval |
| `bot` | boolean | yes | Bot flag |
| `created_at` | string | yes | ISO-8601 |
| `note` | string | no | Bio |
| `url` | string | no | Profile URL |
| `avatar` | string | no | Avatar URL |
| `header` | string | no | Header URL |
| `followers_count` | number | yes | Count |
| `following_count` | number | yes | Count |
| `statuses_count` | number | yes | Count |
| `fields` | array | no | Profile fields |

Example:
```json
{
  "id": "100",
  "username": "alice",
  "acct": "alice@example.social",
  "display_name": "Alice",
  "locked": false,
  "bot": false,
  "created_at": "2026-02-10T12:00:00Z",
  "note": "Hello world",
  "url": "https://example.social/@alice",
  "avatar": "https://example.social/media/avatars/1.png",
  "header": "https://example.social/media/headers/1.png",
  "followers_count": 10,
  "following_count": 5,
  "statuses_count": 123,
  "fields": [
    {"name": "Website", "value": "https://alice.example", "verified_at": null}
  ]
}
```

## Relationship
Used by: account relationship endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | Account id |
| `following` | boolean | yes | Following state |
| `followed_by` | boolean | yes | Followed by |
| `blocking` | boolean | yes | Block state |
| `muting` | boolean | yes | Mute state |
| `requested` | boolean | yes | Follow requested |
| `domain_blocking` | boolean | no | Domain block |

Example:
```json
{
  "id": "100",
  "following": true,
  "followed_by": false,
  "blocking": false,
  "muting": false,
  "requested": false,
  "domain_blocking": false
}
```

## MediaAttachment
Used by: status/media endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | Media id |
| `type` | string | yes | `image`, `video`, `gifv`, `audio` |
| `url` | string | no | Full-size URL |
| `preview_url` | string | no | Preview URL |
| `remote_url` | string | no | Remote URL |
| `meta` | object | no | Dimensions, duration, focus |
| `description` | string | no | Alt text |
| `blurhash` | string | no | Blurhash |

Example:
```json
{
  "id": "900",
  "type": "image",
  "url": "https://example.social/media/900.png",
  "preview_url": "https://example.social/media/900-preview.png",
  "remote_url": null,
  "meta": {
    "original": {"width": 800, "height": 600, "size": "800x600"},
    "small": {"width": 400, "height": 300, "size": "400x300"}
  },
  "description": "A sunset",
  "blurhash": "LEHV6nWB2yk8pyo0adR*.7kCMdnj"
}
```

## Poll
Used by: status and poll endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | Poll id |
| `expires_at` | string | no | ISO-8601 or null |
| `expired` | boolean | yes | Expired |
| `multiple` | boolean | yes | Multi-select |
| `votes_count` | number | yes | Total votes |
| `voters_count` | number | no | Distinct voters |
| `voted` | boolean | no | Current user voted |
| `options` | array | yes | Poll options |

Example:
```json
{
  "id": "55",
  "expires_at": "2026-02-12T12:00:00Z",
  "expired": false,
  "multiple": false,
  "votes_count": 10,
  "voters_count": 9,
  "voted": false,
  "options": [
    {"title": "Yes", "votes_count": 7},
    {"title": "No", "votes_count": 3}
  ]
}
```

## Status
Used by: timeline/status endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | Status id |
| `created_at` | string | yes | ISO-8601 |
| `in_reply_to_id` | string | no | Parent status id |
| `in_reply_to_account_id` | string | no | Parent account id |
| `sensitive` | boolean | yes | Content warning |
| `spoiler_text` | string | no | CW text |
| `visibility` | string | yes | `public`, `unlisted`, `private`, `direct` |
| `language` | string | no | BCP-47 language code |
| `uri` | string | no | Canonical URI |
| `url` | string | no | URL |
| `content` | string | yes | HTML content |
| `account` | Account | yes | Author |
| `media_attachments` | array | yes | MediaAttachment[] |
| `mentions` | array | no | Mention[] |
| `tags` | array | no | Tag[] |
| `poll` | Poll | no | Poll object |
| `reblog` | Status | no | Boosted status |

Example:
```json
{
  "id": "200",
  "created_at": "2026-02-11T10:00:00Z",
  "in_reply_to_id": null,
  "in_reply_to_account_id": null,
  "sensitive": false,
  "spoiler_text": "",
  "visibility": "public",
  "language": "en",
  "uri": "https://example.social/@alice/200",
  "url": "https://example.social/@alice/200",
  "content": "<p>Hello</p>",
  "account": {"id": "100", "username": "alice", "acct": "alice@example.social", "display_name": "Alice", "locked": false, "bot": false, "created_at": "2026-02-10T12:00:00Z", "note": "", "url": "https://example.social/@alice", "avatar": "", "header": "", "followers_count": 10, "following_count": 5, "statuses_count": 123, "fields": []},
  "media_attachments": [],
  "mentions": [],
  "tags": [],
  "poll": null,
  "reblog": null
}
```

## Notification
Used by: notification endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | Notification id |
| `type` | string | yes | `mention`, `status`, `reblog`, `follow`, `favourite`, `poll`, `update` |
| `created_at` | string | yes | ISO-8601 |
| `account` | Account | yes | Actor |
| `status` | Status | no | Related status |

Example:
```json
{
  "id": "777",
  "type": "mention",
  "created_at": "2026-02-11T10:05:00Z",
  "account": {"id": "101", "username": "bob", "acct": "bob@example.social", "display_name": "Bob", "locked": false, "bot": false, "created_at": "2026-02-09T12:00:00Z", "note": "", "url": "https://example.social/@bob", "avatar": "", "header": "", "followers_count": 2, "following_count": 3, "statuses_count": 7, "fields": []},
  "status": {"id": "201", "created_at": "2026-02-11T10:04:00Z", "in_reply_to_id": "200", "in_reply_to_account_id": "100", "sensitive": false, "spoiler_text": "", "visibility": "public", "language": "en", "uri": "https://example.social/@bob/201", "url": "https://example.social/@bob/201", "content": "<p>@alice hi</p>", "account": {"id": "101", "username": "bob", "acct": "bob@example.social", "display_name": "Bob", "locked": false, "bot": false, "created_at": "2026-02-09T12:00:00Z", "note": "", "url": "https://example.social/@bob", "avatar": "", "header": "", "followers_count": 2, "following_count": 3, "statuses_count": 7, "fields": []}, "media_attachments": [], "mentions": [], "tags": [], "poll": null, "reblog": null}
}
```

## SearchResults
Used by: search endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `accounts` | array | yes | Account[] |
| `statuses` | array | yes | Status[] |
| `hashtags` | array | yes | Tag[] |

Example:
```json
{
  "accounts": [],
  "statuses": [],
  "hashtags": [
    {"name": "java", "url": "https://example.social/tags/java"}
  ]
}
```

## StatusCreateRequest
Used by: `POST /api/v1/statuses`

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `status` | string | yes | Text content |
| `visibility` | string | no | `public`, `unlisted`, `private`, `direct` |
| `sensitive` | boolean | no | CW flag |
| `spoiler_text` | string | no | CW text |
| `media_ids` | array | no | Media ids |
| `poll` | object | no | Poll options |

Example:
```json
{
  "status": "Hello from Java",
  "visibility": "public",
  "sensitive": false,
  "spoiler_text": "",
  "media_ids": ["900"],
  "poll": {
    "options": ["Yes", "No"],
    "expires_in": 3600,
    "multiple": false
  }
}
```

## AccountUpdateRequest
Used by: `PATCH /api/v1/accounts/update_credentials`

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `display_name` | string | no | Display name |
| `note` | string | no | Bio |
| `avatar` | string | no | Base64 or media id |
| `header` | string | no | Base64 or media id |
| `locked` | boolean | no | Requires approval |
| `fields_attributes` | array | no | Profile fields |

Example:
```json
{
  "display_name": "Alice",
  "note": "Hello world",
  "locked": false,
  "fields_attributes": [
    {"name": "Website", "value": "https://alice.example"}
  ]
}
```

## PagedResponse<T>
Client-side wrapper used by the UI for array endpoints.

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `items` | array | yes | Result list |
| `next` | string | no | Next cursor id |
| `prev` | string | no | Previous cursor id |

Example:
```json
{
  "items": [
    {"id": "200", "created_at": "2026-02-11T10:00:00Z", "sensitive": false, "spoiler_text": "", "visibility": "public", "content": "<p>Hello</p>", "account": {"id": "100", "username": "alice", "acct": "alice@example.social", "display_name": "Alice", "locked": false, "bot": false, "created_at": "2026-02-10T12:00:00Z", "note": "", "url": "https://example.social/@alice", "avatar": "", "header": "", "followers_count": 10, "following_count": 5, "statuses_count": 123, "fields": []}, "media_attachments": []}
  ],
  "next": "199",
  "prev": null
}
```

## RateLimitInfo
Client-side view of rate limit headers.

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `limit` | number | yes | Max requests |
| `remaining` | number | yes | Remaining requests |
| `reset` | number | yes | Epoch seconds |

Example:
```json
{
  "limit": 300,
  "remaining": 299,
  "reset": 1739210500
}
```

## StreamEvent
Used by: streaming endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `event` | string | yes | `update`, `notification`, `delete` |
| `payload` | object | yes | Status or Notification |

Example:
```json
{
  "event": "update",
  "payload": {
    "id": "200",
    "created_at": "2026-02-11T10:00:00Z",
    "sensitive": false,
    "spoiler_text": "",
    "visibility": "public",
    "content": "<p>Hello</p>",
    "account": {"id": "100", "username": "alice", "acct": "alice@example.social", "display_name": "Alice", "locked": false, "bot": false, "created_at": "2026-02-10T12:00:00Z", "note": "", "url": "https://example.social/@alice", "avatar": "", "header": "", "followers_count": 10, "following_count": 5, "statuses_count": 123, "fields": []},
    "media_attachments": []
  }
}
```

## AdminAccount
Used by: admin endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | Account id |
| `username` | string | yes | Username |
| `domain` | string | no | Remote domain |
| `status` | string | yes | `active`, `suspended`, `silenced` |
| `created_at` | string | yes | ISO-8601 |
| `email` | string | no | Admin-only |
| `ip` | string | no | Last known IP |

Example:
```json
{
  "id": "100",
  "username": "alice",
  "domain": null,
  "status": "active",
  "created_at": "2026-02-10T12:00:00Z",
  "email": "alice@example.social",
  "ip": "203.0.113.10"
}
```

## Report
Used by: admin report endpoints

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | yes | Report id |
| `created_at` | string | yes | ISO-8601 |
| `account` | Account | yes | Reporter |
| `target_account` | Account | yes | Reported account |
| `comment` | string | no | Moderator note |
| `status_ids` | array | no | Reported statuses |
| `action_taken` | boolean | yes | Action taken |

Example:
```json
{
  "id": "500",
  "created_at": "2026-02-11T11:00:00Z",
  "account": {"id": "100", "username": "alice", "acct": "alice@example.social", "display_name": "Alice", "locked": false, "bot": false, "created_at": "2026-02-10T12:00:00Z", "note": "", "url": "https://example.social/@alice", "avatar": "", "header": "", "followers_count": 10, "following_count": 5, "statuses_count": 123, "fields": []},
  "target_account": {"id": "101", "username": "bob", "acct": "bob@example.social", "display_name": "Bob", "locked": false, "bot": false, "created_at": "2026-02-09T12:00:00Z", "note": "", "url": "https://example.social/@bob", "avatar": "", "header": "", "followers_count": 2, "following_count": 3, "statuses_count": 7, "fields": []},
  "comment": "Spam",
  "status_ids": ["201"],
  "action_taken": false
}
```

## TranslationBundle
Used by: UI i18n

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `locale` | string | yes | BCP-47 |
| `strings` | object | yes | Key-value translations |

Example:
```json
{
  "locale": "en",
  "strings": {
    "nav.home": "Home",
    "nav.notifications": "Notifications"
  }
}
```

## ThemeConfig
Used by: UI theming

Fields:
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `name` | string | yes | Theme name |
| `colors` | object | yes | Color tokens |
| `fonts` | object | no | Font tokens |

Example:
```json
{
  "name": "default",
  "colors": {
    "bg": "#ffffff",
    "fg": "#111111",
    "accent": "#2b90d9"
  },
  "fonts": {
    "body": "Source Sans 3",
    "mono": "JetBrains Mono"
  }
}
```
