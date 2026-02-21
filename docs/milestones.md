# Milestone Checklist

Decisions locked on 2026-02-11:
- Full web UI parity with upstream Mastodon.
- Postgres-only architecture (no Redis, no Elasticsearch).
- API compatibility target: Mastodon v4.5.6 (released 2026-02-03). Re-verify latest tag before closing Milestone 0.

Effort estimates are in person-weeks for 1 senior engineer. Parallel work can reduce calendar time if multiple engineers are available.

## AI Workflow (Ollama)

All agent Modelfiles live in `.claude/agents/*.Modelfile`. Build them with:
```
.claude\ollama\create_modelfiles.bat .claude\agents
```
Or using the CLI script:
```
.\.claude\commands\mastodon.ps1 models build
```

| Agent Model | Base Model | Purpose |
|---|---|---|
| `orchestrator` | `qwen3:32b` | Primary planner/orchestrator for migration tasks |
| `code-gen` | `qwen3-coder:30b` | Code generation: entities, services, controllers, federation |
| `test-gen` | `deepseek-coder:33b-instruct-q5_K_M` | Test generation and coverage expansion |
| `architect` | `deepseek-r1:32b` | Architecture and design reviews |
| `security` | `deepseek-r1:32b` | Security reviews and threat modeling |
| `docs` | `llama3.3:70b` | Documentation updates |

Executable commands:
- Build all agent models: `./.claude/ollama/create_modelfiles.bat .claude/agents`
- Build all base models: `./.claude/ollama/create_modelfiles.bat .claude/ollama`
- Validate environment: `./.claude/commands/mastodon.ps1 test-setup`
- Generate a feature slice (entity -> API -> tests): `./.claude/commands/mastodon.ps1 feature <n>`
- Run review agent: `claude code run ./.claude/agents/review-agent.json --model architect`
- Run security agent: `claude code run ./.claude/agents/security-agent.json --model security`

## Milestone 0 — Scope Lock + Build Baseline (2-3 weeks)
Deliverables:
- Updated project goals and build pipeline with UI and jobs modules.
- Baseline API versioning, error format, and observability scaffolding.

Checklist:
- [x] `docs` (0.5w): Update `docs/project.md` and `README.md` for UI parity, Postgres-only, and API target version.
- [x] `pom.xml` (0.5w): Add `mastodon-ui` and `mastodon-jobs` modules; wire UI asset build into `mastodon-web` packaging.
- [x] `mastodon-web` (1w): Global error format, validation, API versioning, metrics/logging baselines.
- [x] `mastodon-core` (0.5w): Shared value objects and interfaces (IDs, timestamps, visibility).
- [x] `mastodon-ui` (1w): React/TypeScript build, linting, and asset pipeline; serve static assets from `mastodon-web`.
- [x] `mastodon-streaming` (0.5w): Skeleton SSE service with auth handshake and health endpoints.

## Milestone 1 — Schema + Core Domain (6-8 weeks)
Deliverables:
- Core domain models and migrations aligned to upstream schema.

Checklist:
- [x] `mastodon-core` (3w): Implement Account, User, Status, Follow, MediaAttachment, Notification, Application, Tag, Mention, Poll, List, Filter, Report entities and repositories.
- [x] `mastodon-core` (2w): Flyway migrations aligned with upstream schema and indexes; seed minimal dev data.
- [x] `mastodon-web` (1w): DTOs, mappers, validation rules, pagination helpers.
- [x] `mastodon-media` (1w): Storage abstraction and media metadata model (no processing yet).

Milestone 1 status notes (2026-02-12):
- Entities + repositories/services now include Notification, Application, List, Filter, Report and join tables.
- Flyway: V1-V3 migrations present; dev seed data added under dev profile (`db/dev`).
- DTOs/mappers: Account/Status/Media/Mention/Tag/Poll plus Notification/Application/List/Filter/Report; create/update DTO validation added.

Completed sub-tasks (Milestone 1):
- [x] `mastodon-core`: Add entities + repositories for Notification and NotificationType (include actor, status link, created_at).
- [x] `mastodon-core`: Add entities + repositories for Application (OAuth app metadata, redirect URIs).
- [x] `mastodon-core`: Add entities + repositories for List and ListAccount join table.
- [x] `mastodon-core`: Add entities + repositories for Filter and FilterKeyword (visibility scope).
- [x] `mastodon-core`: Add entities + repositories for Report and ReportStatus join table.
- [x] `mastodon-web`: Add DTOs for Notification, Application, List, Filter, Report + mapper coverage.
- [x] `mastodon-web`: Add validation annotations for create/update DTOs (basic length/required constraints).
- [x] `mastodon-web`: Add pagination `Link` headers to remaining list endpoints (timelines, notifications) as they are implemented.
- [x] `mastodon-core`: Add Flyway V3 migration for missing tables above (with indexes).
- [x] `mastodon-web`: Add minimal dev seed data (one local account + status) or a seed script.
- [x] `mastodon-media`: Create storage abstraction interface + local filesystem implementation stub.

## Milestone 2 — Auth + OAuth2 (4-6 weeks)
Deliverables:
- OAuth2 provider compatibility and first-party auth flows.

Checklist:
- [x] `mastodon-core` (2w): Password hashing, sessions, OAuth client and token models, scopes, grants.
- [x] `mastodon-web` (2w): OAuth endpoints (`/oauth/token`, `/oauth/revoke`, `/api/v1/apps`) and user auth flows.
- [x] `mastodon-web` (1w): Security filters and rate limiting primitives (per-user/IP) using Postgres-only.

## Milestone 3 — Core API + Timelines (8-12 weeks)
Deliverables:
- Functional API for accounts, statuses, timelines, notifications, and lists.
- Postgres-only fanout and streaming.

Checklist:
- [x] `mastodon-core` (4w): Status lifecycle (create/delete/boost/favorite/bookmark), visibility rules, timeline fanout logic.
- [x] `mastodon-web` (4w): Accounts, statuses, timelines, notifications, lists, filters, and search endpoints (API v1/v2).
- [x] `mastodon-streaming` (2w): Stream user/public/hashtag/list timelines via SSE using Postgres LISTEN/NOTIFY.
- [x] `mastodon-jobs` (2w): Postgres-backed job queue with `SKIP LOCKED`, retries, and scheduling.

Milestone 3 status notes (2026-02-12):
- Core: added status lifecycle service, visibility checks, poll voting support, and cursored follower/following queries.
- Web: added follow/block/mute + relationships, filters (v2), search (v2) and accounts search (v1), notification clear/dismiss, poll voting, context, and visibility filtering.
- Streaming: added WebFlux SSE service with Postgres LISTEN/NOTIFY hub and OAuth token auth for user streams.
- Jobs: added `mastodon-jobs` module with job entity/repository/service and `V6__jobs_table.sql` migration.

## Milestone 4 — Media Pipeline (4-6 weeks)
Deliverables:
- Media upload, processing, and lifecycle management.

Checklist:
- [x] `mastodon-media` (3w): Upload ingestion, AV scanning hook, thumbnails, video transcoding (FFmpeg), cleanup jobs.
- [x] `mastodon-web` (1w): Media endpoints and async processing status reporting.
- [x] `mastodon-jobs` (1w): Retry/backoff policies and processing orchestration.

Milestone 4 status notes (2026-02-12):
- Media ingestion service wired to local storage with AV scan hook, metadata, and async processing queue.
- Media processing pipeline generates thumbnails for images and preview frames for video via FFmpeg (when available).
- Cleanup scheduler enqueues jobs; worker deletes orphaned attachments and stored files.

## Milestone 5 — Federation + ActivityPub (8-12 weeks)
Deliverables:
- ActivityPub compliance and federation interoperability.

Checklist:
- [x] `mastodon-activitypub` (4w): JSON-LD models, serialization, HTTP signature generation/verification.
- [x] `mastodon-federation` (4w): WebFinger, actor discovery, inbox/outbox, delivery retries, shared inbox.
- [x] `mastodon-core` (2w): Remote account/status persistence and federation audit logging.
- [x] `mastodon-jobs` (2w): Delivery queues and retry scheduling.

Milestone 5 status notes (2026-02-20):
- ActivityPub: models for Actor, Note, Activity types (Create, Follow, Accept, Reject, Undo, Like, Announce, Delete, Update) implemented.
- Federation: WebFinger controller, Actor controller, Inbox/Outbox controllers, activity handlers for all major activity types.
- Delivery: FederationDeliveryService with HTTP signature signing, retry logic with exponential backoff.
- Core: Added findByUri() to StatusRepository for remote status lookups.
- Database: V11 migration adds federation_deliveries and follow_requests tables plus indexes for federation lookups.

Completed sub-tasks (Milestone 5):
- [x] `mastodon-federation/web`: ActorController for serving actor profiles at /users/:username.
- [x] `mastodon-federation/web`: InboxController for shared inbox and user-specific inboxes with signature verification.
- [x] `mastodon-federation/web`: OutboxController for serving user outboxes.
- [x] `mastodon-federation/web`: WebFingerController for actor discovery via /.well-known/webfinger.
- [x] `mastodon-federation/service`: ActivityDispatcher for routing activities to handlers.
- [x] `mastodon-federation/service`: FederationDeliveryService for outgoing activity delivery with retry.
- [x] `mastodon-federation/handler`: FollowActivityHandler for incoming follow requests.
- [x] `mastodon-federation/handler`: AcceptActivityHandler for follow acceptances.
- [x] `mastodon-federation/handler`: RejectActivityHandler for follow rejections.
- [x] `mastodon-federation/handler`: UndoActivityHandler for undoing follows, likes, boosts.
- [x] `mastodon-federation/handler`: CreateActivityHandler for incoming remote statuses.
- [x] `mastodon-federation/handler`: AnnounceActivityHandler for boosts from remote users.
- [x] `mastodon-federation/handler`: LikeActivityHandler for favorites from remote users.
- [x] `mastodon-federation/handler`: DeleteActivityHandler for deletion of remote content.
- [x] `mastodon-federation/handler`: UpdateActivityHandler for profile/status updates.
- [x] `mastodon-core`: Added findByUri() method to StatusRepository and StatusService.
- [x] `mastodon-web/db`: V11 migration for federation tables and indexes.

## Milestone 6 — Web UI Parity (8-12 weeks)
Deliverables:
- Feature-complete user and admin UI parity.

Checklist:
- [x] `mastodon-ui` (decision): Port upstream React UI into a standalone module and adapt API calls to the Java backend.
- [x] `mastodon-ui` (2w): Baseline port (build system, routing, i18n, theming, asset pipeline).
- [x] `mastodon-ui` (4w): Core UI features (login, timelines, compose, profiles, notifications, settings) with design parity.
- [ ] `mastodon-ui` (3w): Admin UI parity (reports, accounts, domain blocks, instance settings).
- [x] `mastodon-ui` (1w): Replace server-side assumptions (Rails endpoints, CSRF flow) with Java backend equivalents.
- [x] `mastodon-web` (1w): Static asset hosting, CSP headers, and OAuth integrations for UI.

Milestone 6 status notes (2026-02-20):
- Build system: Vite + TypeScript configured with path aliases, CSS modules, and proxy to backend API.
- Types: Complete TypeScript types for all Mastodon entities (Account, Status, Notification, MediaAttachment, etc.).
- API client: Typed fetch wrapper with auth token handling, endpoints for accounts, statuses, timelines, notifications, media, streaming, OAuth.
- State management: React Context for auth (AuthContext) and timeline state (TimelineContext) with streaming integration.
- Components: Status display with actions, Compose box with media/visibility/CW, Avatar, Loading placeholder.
- Screens: Home timeline with infinite scroll, OAuth callback handler, public timeline, hashtag timeline.
- Authentication: Full OAuth 2.0 authorization code flow with token storage and refresh.
- i18n: i18next configured with English translations, language detection.
- Theming: CSS variables for light/dark mode support.

Epics and stories (dependencies called out explicitly):
- Epic: UI baseline port. Depends on Milestone 0. Stories: Vite/webpack build wired, routing working, i18n loaded, theming tokens mapped, asset pipeline integrated.
- Epic: Auth and session UX. Depends on Milestone 2. Stories: login flow, signup flow, password reset, MFA UX hooks, token refresh handling.
- Epic: API client layer. Depends on Milestone 3. Stories: typed API client, pagination helpers, error mapping, rate-limit handling, upload helpers.
- Epic: Timelines and feeds. Depends on Milestone 3. Stories: home/public/tag/list feeds, infinite scroll, filters, content warnings.
- Epic: Compose and media. Depends on Milestone 4. Stories: compose box, media upload and processing state, content warnings, polls.
- Epic: Profiles and relationships. Depends on Milestone 3. Stories: profile view, follow/unfollow, mute/block, lists management.
- Epic: Notifications. Depends on Milestone 3. Stories: notifications list, filters, clear/dismiss actions.
- Epic: Search and discovery. Depends on Milestone 7. Stories: search UI, suggestions, account/status/tag results.
- Epic: Settings. Depends on Milestone 2. Stories: profile edit, preferences, privacy, notifications, app management.
- Epic: Admin console. Depends on Milestone 7. Stories: reports triage, account moderation, domain blocks, instance settings.
- Epic: Accessibility and performance. Depends on UI baseline port. Stories: keyboard navigation, ARIA audits, bundle sizing, code-splitting.

Per-screen checklist (UI parity):
- [x] Login: OAuth flow, error states, remember session, MFA prompt placeholders.
- [ ] Signup: invite/registration rules, validation, terms acceptance.
- [x] Home timeline: infinite scroll, filters, CW/media toggles, boost/favorite actions.
- [x] Public timeline: local/federated tabs, filters, CW/media toggles.
- [x] Hashtag timeline: tag follow/unfollow, tab state, filters.
- [ ] Lists timeline: list selection, list management shortcuts.
- [x] Compose: CW, media upload, poll creation, visibility controls, draft handling.
- [ ] Status detail: thread view, replies, boosts/favorites, link previews.
- [ ] Profile: header/avatar, follow stats, follow/mute/block actions, featured tags.
- [ ] Edit profile: display name, bio, fields, avatar/header upload.
- [ ] Notifications: filters, clear/dismiss, type-specific renderers.
- [ ] Search: accounts, statuses, hashtags, recent searches.
- [ ] Bookmarks: pagination, removal actions.
- [ ] Favorites: pagination, removal actions.
- [ ] Settings: preferences, notifications, privacy, interface, language.
- [ ] Applications: OAuth apps list, revoke tokens.
- [ ] Admin reports: report queue, actions, notes.
- [ ] Admin accounts: review, freeze/suspend/limit, timeline view.
- [ ] Admin domain blocks: add/edit/remove, severity levels.
- [ ] Admin instance settings: site details, contact, policies.

Epic to API/Data Contract map (align UI with backend milestones):
- Epic: UI baseline port. Depends on Milestone 0. Endpoints: `GET /api/v1/instance`. Data contracts: `InstanceInfo`, `TranslationBundle`, `ThemeConfig`.
- Epic: Auth and session UX. Depends on Milestone 2. Endpoints: `POST /api/v1/apps`, `POST /oauth/token`, `POST /oauth/revoke`, `GET /api/v1/accounts/verify_credentials`. Data contracts: `OAuthApp`, `TokenResponse`, `ErrorResponse`, `Account`.
- Epic: API client layer. Depends on Milestone 3. Endpoints: all v1/v2 endpoints with pagination (`limit`, `max_id`, `since_id`). Data contracts: `PagedResponse<T>`, `ErrorResponse`, `RateLimitInfo`.
- Epic: Timelines and feeds. Depends on Milestone 3. Endpoints: `GET /api/v1/timelines/home`, `GET /api/v1/timelines/public`, `GET /api/v1/timelines/tag/:hashtag`, `GET /api/v1/timelines/list/:list_id`. Data contracts: `Status`, `Account`, `MediaAttachment`, `Poll`, `Tag`.
- Epic: Compose and media. Depends on Milestone 4. Endpoints: `POST /api/v1/statuses`, `POST /api/v1/media`, `GET /api/v1/media/:id`, `PUT /api/v1/media/:id`, `POST /api/v1/polls/:id/votes`. Data contracts: `StatusCreateRequest`, `Status`, `MediaAttachment`, `Poll`, `PollVoteRequest`.
- Epic: Profiles and relationships. Depends on Milestone 3. Endpoints: `GET /api/v1/accounts/:id`, `GET /api/v1/accounts/:id/statuses`, `GET /api/v1/accounts/:id/followers`, `GET /api/v1/accounts/:id/following`, `POST /api/v1/accounts/:id/follow`, `POST /api/v1/accounts/:id/unfollow`, `POST /api/v1/accounts/:id/block`, `POST /api/v1/accounts/:id/unblock`, `POST /api/v1/accounts/:id/mute`, `POST /api/v1/accounts/:id/unmute`. Data contracts: `Account`, `Relationship`, `Status`.
- Epic: Notifications. Depends on Milestone 3. Endpoints: `GET /api/v1/notifications`, `GET /api/v1/notifications/:id`, `POST /api/v1/notifications/clear`, `POST /api/v1/notifications/:id/dismiss`. Data contracts: `Notification`, `Account`, `Status`.
- Epic: Search and discovery. Depends on Milestone 7. Endpoints: `GET /api/v2/search`, `GET /api/v1/accounts/search`. Data contracts: `SearchResults`, `Account`, `Status`, `Tag`.
- Epic: Settings. Depends on Milestone 2. Endpoints: `GET /api/v1/accounts/verify_credentials`, `PATCH /api/v1/accounts/update_credentials`. Data contracts: `Account`, `AccountUpdateRequest`.
- Epic: Admin console. Depends on Milestone 7. Endpoints: `GET /api/v1/admin/accounts`, `GET /api/v1/admin/reports`. Data contracts: `AdminAccount`, `Report`.
- Epic: Streaming integration. Depends on Milestone 3. Endpoints: `GET /api/v1/streaming/user`, `GET /api/v1/streaming/public`, `GET /api/v1/streaming/public/local`, `GET /api/v1/streaming/hashtag`, `GET /api/v1/streaming/list`. Data contracts: `StreamEvent`, `Status`, `Notification`.

## Milestone 7 — Admin, Moderation, Search (6-10 weeks)
Deliverables:
- Admin tools, moderation workflows, and Postgres-only search.

Checklist:
- [x] `mastodon-core` (3w): Moderation models (reports, actions, strikes), RBAC policies.
- [x] `mastodon-web` (2w): Admin API endpoints and auditing.
- [x] `mastodon-core` (3w): Postgres full-text search, trigram indexes, autocomplete, ranking.

Milestone 7 status notes (2026-02-20):
- Core: Added AccountAction, DomainBlock, ReportNote entities with repositories.
- Core: Extended Report entity with assigned_account, action_taken_at, action_taken_by, forwarded fields.
- Core: Extended Account entity with suspended, silenced, disabled moderation fields.
- Core: Added User.Role enum (USER, MODERATOR, ADMIN) for RBAC.
- Core: Added ModerationService for account actions (suspend/silence/disable) and domain blocks.
- Core: Extended ReportService with admin operations (assign, resolve, reopen).
- Core: Added SearchService with Postgres full-text search using tsvector and trigram indexes.
- Web: Added @AdminOnly annotation and AdminRoleInterceptor for RBAC.
- Web: Added AdminAccountController with account moderation actions.
- Web: Added AdminReportController with report management endpoints.
- Web: Added AdminDomainBlockController for instance-level moderation.
- Web: Added AdminInstanceController for instance settings.
- Web: Updated SearchController to use SearchService.
- Database: V12 migration for admin tables (account_actions, domain_blocks, report_notes).
- Database: V13 migration for user role field.
- Database: V14 migration for full-text search indexes (tsvector, pg_trgm).

Completed sub-tasks (Milestone 7):
- [x] `mastodon-core/entity`: AccountAction entity for tracking moderation actions.
- [x] `mastodon-core/entity`: DomainBlock entity for instance-level blocks.
- [x] `mastodon-core/entity`: ReportNote entity for moderator notes on reports.
- [x] `mastodon-core/repository`: AccountActionRepository, DomainBlockRepository, ReportNoteRepository.
- [x] `mastodon-core/service`: ModerationService for account actions and domain blocks.
- [x] `mastodon-core/service`: Extended ReportService for admin operations.
- [x] `mastodon-core/service`: SearchService with Postgres full-text search.
- [x] `mastodon-core/repository`: Extended AccountRepository with admin queries.
- [x] `mastodon-core/repository`: Extended ReportRepository with admin queries.
- [x] `mastodon-core/repository`: Extended StatusRepository with full-text search.
- [x] `mastodon-web/auth`: @AdminOnly annotation for RBAC.
- [x] `mastodon-web/auth`: AdminRoleInterceptor for role-based access control.
- [x] `mastodon-web/auth`: Updated AuthenticatedPrincipal with role field.
- [x] `mastodon-web/config`: WebMvcConfig to register admin interceptor.
- [x] `mastodon-web/api/admin`: AdminAccountController for account moderation.
- [x] `mastodon-web/api/admin`: AdminReportController for report management.
- [x] `mastodon-web/api/admin`: AdminDomainBlockController for domain blocks.
- [x] `mastodon-web/api/admin`: AdminInstanceController for instance settings.
- [x] `mastodon-web/api`: Updated SearchController with SearchService.
- [x] `mastodon-web/db`: V12 migration for admin tables.
- [x] `mastodon-web/db`: V13 migration for user role.
- [x] `mastodon-web/db`: V14 migration for search indexes (tsvector, pg_trgm).

## Milestone 8 — Hardening + Release (6-10 weeks)
Deliverables:
- Conformance, interop, and production readiness.

Checklist:
- [x] `mastodon-web` (3w): API conformance tests against upstream behavior; regression suite.
- [x] `mastodon-federation` (2w): Interop testing with real instances and signature edge cases.
- [x] `mastodon-streaming` (1w): Load tests and connection churn handling.
- [x] `mastodon-media` (1w): Storage integrity checks and cleanup routines.
- [x] `all modules` (2w): Security review, dependency scanning, production readiness checklist.

Milestone 8 status notes (2026-02-20):
- API Conformance: Comprehensive test suite for Account, Status, Timeline, Notification, Media, OAuth, Search, and Admin endpoints.
- Federation Interop: HTTP signature tests with test vectors, ActivityPub serialization tests, WebFinger discovery tests, inbox/outbox delivery tests.
- Streaming Load: Connection load tests, event throughput tests, reconnection behavior tests.
- Media Integrity: Storage integrity tests, cleanup tests for orphaned media, processing tests for thumbnails and video.
- Security: Authentication/authorization security tests, input validation tests, production security headers configuration.
- Production: Deployment guide, configuration reference, security documentation, application-prod.yml template.
- Dependencies: OWASP Dependency Check plugin configured with suppression rules.

Completed sub-tasks (Milestone 8):
- [x] `mastodon-web/test/conformance`: BaseApiConformanceTest with helper methods for auth, pagination, assertions.
- [x] `mastodon-web/test/conformance`: AccountApiConformanceTest for account endpoints.
- [x] `mastodon-web/test/conformance`: StatusApiConformanceTest for status CRUD, context, actions.
- [x] `mastodon-web/test/conformance`: TimelineApiConformanceTest for timeline endpoints.
- [x] `mastodon-web/test/conformance`: NotificationApiConformanceTest for notification endpoints.
- [x] `mastodon-web/test/conformance`: MediaApiConformanceTest for media upload and management.
- [x] `mastodon-web/test/conformance`: OAuthApiConformanceTest for OAuth flows.
- [x] `mastodon-web/test/conformance`: SearchApiConformanceTest for search functionality.
- [x] `mastodon-web/test/conformance`: AdminApiConformanceTest for admin endpoints.
- [x] `mastodon-activitypub/test/signature`: HttpSignatureInteropTest with test vectors.
- [x] `mastodon-activitypub/test/model`: ActivityPubSerializationTest for JSON-LD format.
- [x] `mastodon-federation/test/interop`: WebFingerInteropTest for discovery.
- [x] `mastodon-federation/test/interop`: InboxDeliveryTest for receiving activities.
- [x] `mastodon-streaming/test`: StreamingConnectionLoadTest for concurrent connections.
- [x] `mastodon-streaming/test`: StreamingEventThroughputTest for event delivery.
- [x] `mastodon-streaming/test`: StreamingReconnectionTest for reconnection behavior.
- [x] `mastodon-media/test/storage`: MediaStorageIntegrityTest for file storage.
- [x] `mastodon-media/test/processing`: MediaCleanupTest for orphaned media.
- [x] `mastodon-media/test/processing`: MediaProcessingTest for thumbnails/video.
- [x] `mastodon-web/test/security`: AuthenticationSecurityTest for auth bypass attempts.
- [x] `mastodon-web/test/security`: InputValidationTest for input sanitization.
- [x] `mastodon-web/config`: ProductionSecurityConfig with CSP, X-Frame-Options, etc.
- [x] `docs`: deployment.md - Deployment guide.
- [x] `docs`: configuration.md - Configuration reference.
- [x] `docs`: security.md - Security considerations.
- [x] `mastodon-web/resources`: application-prod.yml - Production configuration template.
- [x] `pom.xml`: OWASP Dependency Check plugin for vulnerability scanning.
- [x] `dependency-check-suppressions.xml`: False positive suppression rules.

## Exit Criteria
- Full API compatibility with upstream v4.5.6.
- UI parity for core user flows and admin console.
- Federation interoperability with at least 3 external Mastodon instances.
- Postgres-only operation with documented scaling limits.

---

## Extended Features (Post-Release)

These milestones extend mastodon_4j with advanced features while maintaining full compatibility with upstream Mastodon. See [Extended Features Plan](./extended-features-plan.md) for detailed specifications.

### Milestone 9 — Quick Setup & Resource Management (8-10 weeks)
Deliverables:
- Express setup system for rapid deployment
- Script-based configuration for reproducible setups
- Resource allocation (CPU, GPU, network, memory)

Checklist:
- [ ] `mastodon-setup` (2w): Setup CLI, configuration parser, templates
- [ ] `mastodon-resources` (2w): CPU allocation, GPU acceleration, network management
- [ ] `mastodon-web` (2w): Setup API endpoints and admin UI
- [ ] `mastodon-core` (2w): Resource monitoring and reporting
- [ ] Documentation (2w): Setup guides, configuration reference

### Milestone 10 — Distributed Architecture (8-10 weeks)
Deliverables:
- Multi-node cluster support for horizontal scaling
- Workload distribution and load balancing
- Fault tolerance and failover

Checklist:
- [ ] `mastodon-cluster` (4w): Node discovery, state sync, event bus
- [ ] `mastodon-core` (2w): Cluster-aware services
- [ ] `mastodon-jobs` (2w): Distributed job queue
- [ ] `mastodon-streaming` (2w): Cluster-wide streaming
- [ ] Documentation (2w): Cluster setup, scaling guide

### Milestone 11 — Content Access Control (6-8 weeks)
Deliverables:
- Private content with permission-based access
- Server-level access policies
- Enhanced visibility controls

Checklist:
- [ ] `mastodon-core` (3w): ContentAccess, ContentPermission entities
- [ ] `mastodon-web` (2w): Access control API endpoints
- [ ] `mastodon-activitypub` (2w): Federation-aware access control
- [ ] `mastodon-ui` (2w): Access control UI components
- [ ] Documentation (1w): Access control configuration

### Milestone 12 — Federation Index (6-8 weeks)
Deliverables:
- Server index database with ratings
- Index sharing between trusted instances
- Restriction tracking with reasons

Checklist:
- [ ] `mastodon-federation-index` (3w): Entities, repositories, services
- [ ] `mastodon-core` (2w): Server index integration
- [ ] `mastodon-web` (2w): Index API endpoints
- [ ] `mastodon-ui` (2w): Server directory UI
- [ ] Documentation (1w): Index configuration and sharing

### Milestone 13 — BitTorrent Integration (12-16 weeks)
Deliverables:
- qBittorrent refactored to Java/Spring Boot
- Media distribution via BitTorrent
- Federation content sync

Checklist:
- [ ] `mastodon-torrent` (6w): Core torrent client, DHT, trackers
- [ ] `mastodon-torrent` (3w): Peer management, storage
- [ ] `mastodon-media` (2w): Media-torrent bridge
- [ ] `mastodon-federation` (2w): Torrent federation sync
- [ ] `mastodon-web` (2w): Torrent management API
- [ ] Documentation (2w): Torrent configuration, seeding policies

### Milestone 14 — Content Authority System (8-10 weeks)
Deliverables:
- Server as Certificate Authority for content authentication
- Cryptographic signatures for statuses, media, and federated content
- Content verification metadata with server location
- External verification of content authenticity

Checklist:
- [ ] `mastodon-content-authority` (2w): CA infrastructure, key management, certificate generation
- [ ] `mastodon-content-authority` (2w): Content signing service with signature algorithms
- [ ] `mastodon-content-authority` (2w): Verification and revocation system (CRL, OCSP)
- [ ] `mastodon-federation` (2w): Authority discovery, cross-signing, trust store
- [ ] `mastodon-web` (1w): Authority API endpoints and admin UI
- [ ] Documentation (1w): CA configuration, trust policies, verification guide

---

## Extended Exit Criteria

In addition to base exit criteria:

1. **Setup**: New instance operational in < 5 minutes with express setup
2. **Scalability**: Linear performance scaling up to 10 cluster nodes
3. **Federation Index**: 95% accuracy in server availability tracking
4. **Torrent Integration**: 50% bandwidth reduction for popular media
5. **Compatibility**: 100% pass rate on Mastodon API conformance tests
6. **Non-Breaking**: All extended features behind feature flags
