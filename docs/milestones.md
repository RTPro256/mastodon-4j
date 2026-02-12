# Milestone Checklist

Decisions locked on 2026-02-11:
- Full web UI parity with upstream Mastodon.
- Postgres-only architecture (no Redis, no Elasticsearch).
- API compatibility target: Mastodon v4.5.6 (released 2026-02-03). Re-verify latest tag before closing Milestone 0.

Effort estimates are in person-weeks for 1 senior engineer. Parallel work can reduce calendar time if multiple engineers are available.

## AI Workflow (Ollama)
Models and purpose:
- `qwen-mastodon`: primary planner/orchestrator for migration tasks.
- `llama-codegen`: code generation for entities, services, controllers.
- `llama-testing`: test generation and coverage expansion.
- `llama-architect`: architecture and design reviews.
- `qwen-security`: security reviews and threat modeling.
- `llama-docs`: documentation updates.

Executable commands:
- Build models: `./.claude/commands/Build-OllamaModels.ps1`
- Windows batch alternative: `./.claude/commands/build-models.bat`
- Validate environment: `./.claude/commands/Test-Setup.ps1`
- Generate a feature slice (entity -> API -> tests): `./.claude/commands/Generate-Feature.ps1 -FeatureName <feature>`
- Run review agent: `claude code run ./.claude/agents/review-agent.json --model llama-architect`
- Run security agent: `claude code run ./.claude/agents/security-agent.json --model qwen-security`

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
- [~] `mastodon-core` (3w): Implement Account, User, Status, Follow, MediaAttachment, Notification, Application, Tag, Mention, Poll, List, Filter, Report entities and repositories.
- [~] `mastodon-core` (2w): Flyway migrations aligned with upstream schema and indexes; seed minimal dev data.
- [~] `mastodon-web` (1w): DTOs, mappers, validation rules, pagination helpers.
- [ ] `mastodon-media` (1w): Storage abstraction and media metadata model (no processing yet).

Milestone 1 status notes (2026-02-11):
- Completed entities + repositories/services: Account, User, Status, Follow, MediaAttachment, Tag, Mention, Poll (+ PollOption, PollVote).
- Flyway: V1/V2 migrations added; dev seed data not added yet.
- DTOs/mappers: Account/Status/Media/Mention/Tag/Poll covered; validation rules still minimal.
- Missing entities: Notification, Application, List, Filter, Report.

Remaining sub-tasks (Milestone 1):
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
- [ ] `mastodon-core` (2w): Password hashing, sessions, OAuth client and token models, scopes, grants.
- [ ] `mastodon-web` (2w): OAuth endpoints (`/oauth/token`, `/oauth/revoke`, `/api/v1/apps`) and user auth flows.
- [ ] `mastodon-web` (1w): Security filters and rate limiting primitives (per-user/IP) using Postgres-only.

## Milestone 3 — Core API + Timelines (8-12 weeks)
Deliverables:
- Functional API for accounts, statuses, timelines, notifications, and lists.
- Postgres-only fanout and streaming.

Checklist:
- [ ] `mastodon-core` (4w): Status lifecycle (create/delete/boost/favorite/bookmark), visibility rules, timeline fanout logic.
- [ ] `mastodon-web` (4w): Accounts, statuses, timelines, notifications, lists, filters, and search endpoints (API v1/v2).
- [ ] `mastodon-streaming` (2w): Stream user/public/hashtag/list timelines via SSE using Postgres LISTEN/NOTIFY.
- [ ] `mastodon-jobs` (2w): Postgres-backed job queue with `SKIP LOCKED`, retries, and scheduling.

## Milestone 4 — Media Pipeline (4-6 weeks)
Deliverables:
- Media upload, processing, and lifecycle management.

Checklist:
- [ ] `mastodon-media` (3w): Upload ingestion, AV scanning hook, thumbnails, video transcoding (FFmpeg), cleanup jobs.
- [ ] `mastodon-web` (1w): Media endpoints and async processing status reporting.
- [ ] `mastodon-jobs` (1w): Retry/backoff policies and processing orchestration.

## Milestone 5 — Federation + ActivityPub (8-12 weeks)
Deliverables:
- ActivityPub compliance and federation interoperability.

Checklist:
- [ ] `mastodon-activitypub` (4w): JSON-LD models, serialization, HTTP signature generation/verification.
- [ ] `mastodon-federation` (4w): WebFinger, actor discovery, inbox/outbox, delivery retries, shared inbox.
- [ ] `mastodon-core` (2w): Remote account/status persistence and federation audit logging.
- [ ] `mastodon-jobs` (2w): Delivery queues and retry scheduling.

## Milestone 6 — Web UI Parity (8-12 weeks)
Deliverables:
- Feature-complete user and admin UI parity.

Checklist:
- [x] `mastodon-ui` (decision): Port upstream React UI into a standalone module and adapt API calls to the Java backend.
- [ ] `mastodon-ui` (2w): Baseline port (build system, routing, i18n, theming, asset pipeline).
- [ ] `mastodon-ui` (4w): Core UI features (login, timelines, compose, profiles, notifications, settings) with design parity.
- [ ] `mastodon-ui` (3w): Admin UI parity (reports, accounts, domain blocks, instance settings).
- [ ] `mastodon-ui` (1w): Replace server-side assumptions (Rails endpoints, CSRF flow) with Java backend equivalents.
- [ ] `mastodon-web` (1w): Static asset hosting, CSP headers, and OAuth integrations for UI.

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
- [ ] Login: OAuth flow, error states, remember session, MFA prompt placeholders.
- [ ] Signup: invite/registration rules, validation, terms acceptance.
- [ ] Home timeline: infinite scroll, filters, CW/media toggles, boost/favorite actions.
- [ ] Public timeline: local/federated tabs, filters, CW/media toggles.
- [ ] Hashtag timeline: tag follow/unfollow, tab state, filters.
- [ ] Lists timeline: list selection, list management shortcuts.
- [ ] Compose: CW, media upload, poll creation, visibility controls, draft handling.
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
- [ ] `mastodon-core` (3w): Moderation models (reports, actions, strikes), RBAC policies.
- [ ] `mastodon-web` (2w): Admin API endpoints and auditing.
- [ ] `mastodon-core` (3w): Postgres full-text search, trigram indexes, autocomplete, ranking.

## Milestone 8 — Hardening + Release (6-10 weeks)
Deliverables:
- Conformance, interop, and production readiness.

Checklist:
- [ ] `mastodon-web` (3w): API conformance tests against upstream behavior; regression suite.
- [ ] `mastodon-federation` (2w): Interop testing with real instances and signature edge cases.
- [ ] `mastodon-streaming` (1w): Load tests and connection churn handling.
- [ ] `mastodon-media` (1w): Storage integrity checks and cleanup routines.
- [ ] `all modules` (2w): Security review, dependency scanning, production readiness checklist.

## Exit Criteria
- Full API compatibility with upstream v4.5.6.
- UI parity for core user flows and admin console.
- Federation interoperability with at least 3 external Mastodon instances.
- Postgres-only operation with documented scaling limits.
