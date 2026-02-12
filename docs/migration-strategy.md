# Migration Strategy

## Phase 1: Core Infrastructure (Weeks 1-4)
- [ ] Set up project structure and build system
- [ ] Configure Spring Boot application
- [ ] Set up PostgreSQL schema migration from Ruby
- [ ] Implement basic domain models (Account, Status, User)
- [ ] Define Postgres-only caching and pub/sub strategy (LISTEN/NOTIFY, materialized views)
- [ ] Create basic REST API structure

## Phase 2: Authentication & Authorization (Weeks 5-6)
- [ ] Implement OAuth 2.0 server
- [ ] User authentication system
- [ ] API token management
- [ ] Application registration
- [ ] Scope-based authorization

## Phase 3: Core Features (Weeks 7-10)
- [ ] Status creation and retrieval
- [ ] Timeline generation
- [ ] Following/Followers system
- [ ] Favorites and boosts
- [ ] Notifications system
- [ ] Media attachment handling

## Phase 4: ActivityPub Federation (Weeks 11-14)
- [ ] ActivityPub protocol implementation
- [ ] HTTP Signatures for authentication
- [ ] WebFinger implementation
- [ ] Remote account resolution
- [ ] Federation inbox/outbox
- [ ] Activity processing queue

## Phase 5: Advanced Features (Weeks 15-18)
- [ ] Search functionality (PostgreSQL full-text + pg_trgm)
- [ ] Direct messages
- [ ] Lists and filters
- [ ] Polls
- [ ] Reports and moderation
- [ ] Admin interface

## Phase 6: Real-time & Optimization (Weeks 19-20)
- [ ] Streaming API (WebSockets/SSE)
- [ ] Performance optimization
- [ ] Caching strategies
- [ ] Background job processing
- [ ] Rate limiting

## Phase 7: Testing & Deployment (Weeks 21-24)
- [ ] Comprehensive unit tests
- [ ] Integration tests
- [ ] Load testing
- [ ] Security audit
- [ ] Documentation
- [ ] Deployment guide
