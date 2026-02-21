# Key Domain Models

This document outlines the core domain models implemented in mastodon_4j.

## Core Entities

### Account

The Account entity represents a user account on the Mastodon network, either local or federated.

**Table:** `accounts`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| username | VARCHAR(30) | Username |
| domain | VARCHAR(255) | Domain for remote accounts (null for local) |
| acct | VARCHAR(255) | Full account handle (username@domain) |
| display_name | VARCHAR(255) | Display name |
| note | TEXT | Bio/description |
| url | TEXT | Profile URL |
| avatar_url | TEXT | Avatar image URL |
| header_url | TEXT | Header image URL |
| locked | BOOLEAN | Whether account is locked (requires follow approval) |
| bot | BOOLEAN | Whether account is a bot |
| created_at | TIMESTAMPTZ | Account creation time |
| followers_count | INTEGER | Number of followers |
| following_count | INTEGER | Number following |
| statuses_count | INTEGER | Number of statuses |
| inbox_url | TEXT | ActivityPub inbox URL (federation) |
| public_key_pem | TEXT | Public key for HTTP signatures |
| suspended | BOOLEAN | Whether account is suspended |
| silenced | BOOLEAN | Whether account is silenced |
| disabled | BOOLEAN | Whether account is disabled |

### User

The User entity represents authentication credentials and user settings for local accounts.

**Table:** `users`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Foreign key to Account |
| email | VARCHAR(255) | Email address (unique) |
| password_hash | VARCHAR(255) | BCrypt hashed password |
| locale | VARCHAR(10) | User locale preference |
| created_at | TIMESTAMPTZ | Account creation time |
| last_sign_in_at | TIMESTAMPTZ | Last sign in timestamp |
| last_sign_in_ip | VARCHAR(45) | IP address of last sign in |
| confirmed | BOOLEAN | Whether email is confirmed |
| confirmed_at | TIMESTAMPTZ | Email confirmation timestamp |
| approved | BOOLEAN | Whether account is approved |
| approval_required | BOOLEAN | Whether approval was required |
| role | VARCHAR(32) | User role (USER, MODERATOR, ADMIN) |

### Status

The Status entity represents a post (toot) in the Mastodon network.

**Table:** `statuses`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Foreign key to Account |
| content | TEXT | Status content (HTML) |
| created_at | TIMESTAMPTZ | Creation time |
| in_reply_to_id | BIGINT | Parent status ID for replies |
| in_reply_to_account_id | BIGINT | Parent account ID for replies |
| sensitive | BOOLEAN | Whether content is sensitive |
| spoiler_text | TEXT | Content warning text |
| visibility | VARCHAR(16) | Visibility (public, unlisted, private, direct) |
| language | VARCHAR(10) | ISO language code |
| uri | TEXT | ActivityPub URI |
| url | TEXT | Web URL |
| reblog_of_id | BIGINT | Original status ID for boosts |

### Follow

The Follow entity represents a relationship between accounts.

**Table:** `follows`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Follower account |
| target_account_id | BIGINT | Target account being followed |
| created_at | TIMESTAMPTZ | Follow creation time |
| pending | BOOLEAN | Whether follow request is pending approval |

### Favourite

The Favourite entity represents a like/favorite relationship.

**Table:** `favourites`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Account that favourited |
| status_id | BIGINT | Status that was favourited |
| created_at | TIMESTAMPTZ | Favourite time |

### Bookmark

The Bookmark entity represents a bookmarked status.

**Table:** `bookmarks`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Account that bookmarked |
| status_id | BIGINT | Status that was bookmarked |
| created_at | TIMESTAMPTZ | Bookmark time |

### Block

The Block entity represents a blocked account relationship.

**Table:** `blocks`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Account that blocked |
| target_account_id | BIGINT | Account that was blocked |
| created_at | TIMESTAMPTZ | Block time |

### Mute

The Mute entity represents a muted account relationship.

**Table:** `mutes`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Account that muted |
| target_account_id | BIGINT | Account that was muted |
| created_at | TIMESTAMPTZ | Mute time |

### UserDomainBlock

The UserDomainBlock entity represents a user-level domain block.

**Table:** `user_domain_blocks`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Account that blocked the domain |
| domain | VARCHAR(255) | Domain that was blocked |
| created_at | TIMESTAMPTZ | Block time |

### StatusPin

The StatusPin entity represents a pinned status on a profile.

**Table:** `status_pins`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Account that pinned the status |
| status_id | BIGINT | Status that was pinned |
| created_at | TIMESTAMPTZ | Pin time |

### Notification

The Notification entity represents user notifications.

**Table:** `notifications`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Account receiving notification |
| type | VARCHAR(32) | Notification type |
| created_at | TIMESTAMPTZ | Notification time |
| from_account_id | BIGINT | Account that triggered notification |
| status_id | BIGINT | Related status (if applicable) |

### MediaAttachment

The MediaAttachment entity represents uploaded media files.

**Table:** `media_attachments`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| status_id | BIGINT | Associated status |
| account_id | BIGINT | Uploading account |
| type | VARCHAR(16) | Media type (image, video, audio, unknown) |
| url | TEXT | Media URL |
| preview_url | TEXT | Thumbnail URL |
| remote_url | TEXT | Original remote URL |
| description | TEXT | Alt text |
| blurhash | VARCHAR(64) | Blurhash for preview |
| meta | JSONB | Media metadata |
| processing_status | VARCHAR(32) | Processing status |

### Application

The Application entity represents OAuth applications.

**Table:** `applications`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| name | VARCHAR(255) | Application name |
| client_id | VARCHAR(255) | OAuth client ID |
| client_secret | VARCHAR(255) | OAuth client secret |
| redirect_uris | TEXT | Allowed redirect URIs |
| scopes | TEXT | Requested scopes |
| website | TEXT | Application website |

### Tag

The Tag entity represents hashtags.

**Table:** `tags`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| name | VARCHAR(255) | Hashtag name (without #) |
| created_at | TIMESTAMPTZ | Creation time |

### Mention

The Mention entity represents account references in statuses.

**Table:** `mentions`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| status_id | BIGINT | Status containing mention |
| account_id | BIGINT | Mentioned account |
| silent | BOOLEAN | Whether mention is silent |

### Poll

The Poll entity represents polls attached to statuses.

**Table:** `polls`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| status_id | BIGINT | Associated status |
| multiple | BOOLEAN | Allow multiple choices |
| expires_at | TIMESTAMPTZ | Expiration time |
| votes_count | INTEGER | Total votes |
| voters_count | INTEGER | Total voters |

### PollVote

The PollVote entity represents votes in polls.

**Table:** `poll_votes`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| poll_id | BIGINT | Associated poll |
| account_id | BIGINT | Voting account |
| choice | INTEGER | Choice index |
| created_at | TIMESTAMPTZ | Vote time |

### ListEntity

The ListEntity entity represents user-created lists.

**Table:** `lists`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | List owner |
| title | VARCHAR(255) | List title |
| replies_policy | VARCHAR(16) | Reply inclusion policy |

### Filter

The Filter entity represents content filters.

**Table:** `filters`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Filter owner |
| title | VARCHAR(255) | Filter title |
| context | VARCHAR(64) | Filter context (home, public, etc.) |
| expires_at | TIMESTAMPTZ | Expiration time |
| filter_action | VARCHAR(16) | Action (warn, hide) |

### Report

The Report entity represents user reports for moderation.

**Table:** `reports`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Reporting account |
| target_account_id | BIGINT | Reported account |
| status_ids | BIGINT[] | Reported status IDs |
| comment | TEXT | Report comment |
| created_at | TIMESTAMPTZ | Report time |
| action_taken | BOOLEAN | Whether action was taken |
| action_taken_at | TIMESTAMPTZ | Action time |
| assigned_account_id | BIGINT | Assigned moderator |

### AccountAction

The AccountAction entity tracks moderation actions on accounts.

**Table:** `account_actions`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| account_id | BIGINT | Account action was taken on |
| action_type | VARCHAR(32) | Action type (suspend, silence, etc.) |
| reason | TEXT | Reason for action |
| action_taken_by_account_id | BIGINT | Moderator who took action |
| created_at | TIMESTAMPTZ | Action time |

### DomainBlock

The DomainBlock entity represents instance-level domain blocks.

**Table:** `domain_blocks`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| domain | VARCHAR(255) | Blocked domain |
| severity | VARCHAR(32) | Severity (silence, suspend) |
| reject_media | BOOLEAN | Reject media from domain |
| reject_reports | BOOLEAN | Reject reports from domain |
| private_comment | TEXT | Private comment |
| public_comment | TEXT | Public comment |

## OAuth Entities

### OAuthAccessToken

**Table:** `oauth_access_tokens`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| application_id | BIGINT | Associated application |
| user_id | BIGINT | Associated user |
| token | VARCHAR(255) | Access token |
| scopes | TEXT | Granted scopes |
| created_at | TIMESTAMPTZ | Creation time |
| expires_at | TIMESTAMPTZ | Expiration time |
| revoked_at | TIMESTAMPTZ | Revocation time |

### OAuthRefreshToken

**Table:** `oauth_refresh_tokens`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| access_token_id | BIGINT | Associated access token |
| application_id | BIGINT | Associated application |
| user_id | BIGINT | Associated user |
| token | VARCHAR(255) | Refresh token |
| expires_at | TIMESTAMPTZ | Expiration time |

### OAuthAuthorizationCode

**Table:** `oauth_authorization_codes`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| application_id | BIGINT | Associated application |
| user_id | BIGINT | Associated user |
| code | VARCHAR(255) | Authorization code |
| redirect_uri | TEXT | Redirect URI |
| scopes | TEXT | Requested scopes |
| expires_at | TIMESTAMPTZ | Expiration time |

## Federation Entities

### FederationDelivery

**Table:** `federation_deliveries`

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| activity_id | VARCHAR(500) | Activity ID |
| activity_type | VARCHAR(64) | Activity type |
| target_inbox | VARCHAR(500) | Target inbox URL |
| sender_account_id | BIGINT | Sending account |
| payload | TEXT | Activity payload |
| attempts | INTEGER | Delivery attempts |
| status | VARCHAR(32) | Delivery status |
| next_attempt_at | TIMESTAMPTZ | Next retry time |

## Entity Relationships

```
Account
  ├── User (1:1, local accounts only)
  ├── Status (1:N)
  ├── Follow (1:N as follower)
  ├── Follow (1:N as following)
  ├── Favourite (1:N)
  ├── Bookmark (1:N)
  ├── Block (1:N)
  ├── Mute (1:N)
  ├── UserDomainBlock (1:N)
  ├── StatusPin (1:N)
  ├── Notification (1:N)
  ├── MediaAttachment (1:N)
  ├── ListEntity (1:N)
  ├── Filter (1:N)
  └── Report (1:N)

Status
  ├── Account (N:1)
  ├── MediaAttachment (1:N)
  ├── Mention (1:N)
  ├── Tag (M:N via status_tags)
  ├── Poll (1:1)
  ├── Favourite (1:N)
  ├── Bookmark (1:N)
  └── StatusPin (1:N)
```
