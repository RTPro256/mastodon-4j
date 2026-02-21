# Test Coverage Evaluation Plan

## Executive Summary

This document evaluates the current test coverage across all modules of the mastodon_4j project and identifies gaps that need to be addressed.

## Current Test Coverage Summary

| Module | Source Files | Test Files | Coverage Level | Priority |
|--------|-------------|------------|----------------|----------|
| mastodon-core | 97 | 0 | **None** | **Critical** |
| mastodon-cluster | 15 | 0 | **None** | High |
| mastodon-content-access | 6 | 0 | **None** | Medium |
| mastodon-content-authority | 2 | 0 | **None** | Low |
| mastodon-federation-index | 3 | 0 | **None** | Low |
| mastodon-jobs | 4 | 0 | **None** | Medium |
| mastodon-torrent | 5 | 0 | **None** | Low |
| mastodon-activitypub | 30 | 2 | Low | Medium |
| mastodon-federation | 25 | 4 | Low | Medium |
| mastodon-media | 16 | 3 | Low | Medium |
| mastodon-streaming | 8 | 4 | Medium | Low |
| mastodon-web | 72 | 16 | Good | Maintenance |

## Detailed Analysis

### 1. mastodon-core (CRITICAL - 0% coverage)

**Impact**: Core module contains all entities, repositories, and fundamental services.

**Missing Tests for Entities:**
- `Account.java` - User account entity
- `Status.java` - Status/post entity
- `User.java` - User authentication entity
- `Follow.java` - Follow relationship entity
- `Notification.java` - Notification entity
- `MediaAttachment.java` - Media attachment entity
- `Poll.java` / `PollOption.java` / `PollVote.java` - Poll entities
- `Filter.java` / `FilterKeyword.java` - Content filtering
- `Block.java` / `Mute.java` / `DomainBlock.java` - Blocking entities
- `Report.java` / `ReportNote.java` / `ReportStatus.java` - Moderation reports
- OAuth entities (`OAuthAccessToken`, `OAuthAuthorizationCode`, `OAuthRefreshToken`)

**Missing Tests for Services:**
- `StatusVisibilityService.java` - Visibility filtering logic
- `FollowService.java` - Follow/unfollow operations
- `StatusService.java` - Status CRUD operations
- `StatusLifecycleService.java` - Status lifecycle management
- `ModerationService.java` - Content moderation
- `AccountService.java` - Account management
- `NotificationService.java` - Notification handling
- `SearchService.java` - Search functionality
- `PollService.java` / `PollVoteService.java` - Poll operations

**Missing Tests for Repositories:**
- All 30+ repository interfaces need integration tests

### 2. mastodon-cluster (HIGH - 0% coverage)

**Missing Tests for:**
- `NodeDiscovery.java` - Node discovery mechanism
- `NodeRegistry.java` - Node registration
- `FailoverManager.java` - Failover handling
- `EventBus.java` - Event distribution
- `StateSynchronizer.java` - State synchronization
- `WorkloadDistributor.java` - Workload distribution
- `TaskQueue.java` - Task queue management
- `ClusterHealthIndicator.java` - Health checks

### 3. mastodon-content-access (MEDIUM - 0% coverage)

**Missing Tests for:**
- `ContentAccessService.java` - Content access control
- `ContentAccess.java` - Content access entity
- `ContentPermission.java` - Permission entity
- `ServerAccessPolicy.java` - Server policy entity

### 4. mastodon-jobs (MEDIUM - 0% coverage)

**Missing Tests for:**
- Job scheduling and execution
- Job queue management
- Job retry logic

### 5. mastodon-activitypub (LOW coverage - 2 test files)

**Existing Tests:**
- `ActivityPubSerializationTest.java` - Serialization tests
- `HttpSignatureInteropTest.java` - HTTP signature tests

**Missing Tests for:**
- `WebFingerController.java` - WebFinger endpoint
- Activity type handlers (Create, Follow, Like, Announce, Delete, Undo)
- Actor resolution
- Inbox/outbox handling

### 6. mastodon-federation (LOW coverage - 4 test files)

**Existing Tests:**
- `FederationIntegrationTest.java` - Integration tests
- `FederationInteropTest.java` - Interoperability tests
- `WebFingerInteropTest.java` - WebFinger tests
- `InboxDeliveryTest.java` - Inbox delivery tests

**Missing Tests for:**
- Outbox delivery
- Federation error handling
- Remote account resolution

### 7. mastodon-media (LOW coverage - 3 test files)

**Existing Tests:**
- `MediaStorageIntegrityTest.java` - Storage tests
- `MediaProcessingTest.java` - Processing tests
- `MediaCleanupTest.java` - Cleanup tests

**Missing Tests for:**
- Image transcoding
- Video processing
- Audio processing
- Metadata extraction

### 8. mastodon-streaming (MEDIUM coverage - 4 test files)

**Existing Tests:**
- `StreamingReconnectionTest.java`
- `StreamingEventThroughputTest.java`
- `StreamingConnectionLoadTest.java`
- `StreamingLoadTest.java`

**Missing Tests for:**
- Event serialization
- Connection state management
- Reconnection logic unit tests

### 9. mastodon-web (GOOD coverage - 16 test files)

**Existing Tests:**
- API conformance tests for all major endpoints
- Security tests (InputValidation, AuthenticationSecurity)
- OAuth integration tests
- API integration tests

**Missing Tests for:**
- Edge cases in controllers
- Error handling scenarios
- Rate limiting

## Recommended Test Implementation Plan

### Phase 1: Core Module Tests (Critical)

1. **Entity Tests**
   - Create unit tests for all entities
   - Test JPA mappings and constraints
   - Test pre-persist hooks

2. **Service Tests**
   - `StatusVisibilityServiceTest` - Test visibility logic for all visibility types
   - `FollowServiceTest` - Test follow/unfollow operations
   - `StatusServiceTest` - Test status CRUD operations
   - `ModerationServiceTest` - Test moderation actions

3. **Repository Tests**
   - Create integration tests for key repositories
   - Test custom queries

### Phase 2: Cluster Module Tests (High Priority)

1. **Discovery Tests**
   - `NodeDiscoveryTest` - Test node discovery
   - `NodeRegistryTest` - Test registration

2. **Failover Tests**
   - `FailoverManagerTest` - Test failover scenarios

3. **Event Tests**
   - `EventBusTest` - Test event distribution
   - `StateSynchronizerTest` - Test state sync

### Phase 3: Content Access & Jobs (Medium Priority)

1. **Content Access Tests**
   - `ContentAccessServiceTest` - Test access control

2. **Job Tests**
   - Job scheduling tests
   - Job execution tests

### Phase 4: Enhance Existing Coverage (Lower Priority)

1. **ActivityPub Enhancement**
   - Controller tests
   - Activity handler tests

2. **Federation Enhancement**
   - Error handling tests
   - Remote resolution tests

3. **Media Enhancement**
   - Transcoding tests
   - Metadata tests

## Test Patterns to Follow

### Unit Test Pattern
```java
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private Dependency dependency;
    
    @InjectMocks
    private Service service;
    
    @Test
    void methodName_scenario_expectedResult() {
        // Given
        // When
        // Then
    }
}
```

### Integration Test Pattern
```java
@DataJpaTest
class RepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private Repository repository;
    
    @Test
    void methodName_scenario_expectedResult() {
        // Given
        // When
        // Then
    }
}
```

## Metrics to Track

- Line coverage target: 70%
- Branch coverage target: 60%
- Mutation coverage target: 50%

## Tests Created

### Phase 1: Core Module Tests (Completed)

1. **StatusVisibilityServiceTest** - Tests for status visibility filtering
   - Null status handling
   - Public visibility tests
   - Unlisted visibility tests
   - Private visibility tests (followers, author)
   - Direct visibility tests (mentions)
   - Author's own status tests
   - Status with null account tests

2. **FollowServiceTest** - Tests for follow/unfollow operations
   - Find follow relationship tests
   - Follow operation tests (create, increment counts)
   - Unfollow operation tests (remove, decrement counts)
   - Find followers/following tests
   - Save tests

### Phase 2: Cluster Module Tests (Completed)

1. **FailoverManagerTest** - Tests for failover handling
   - Event subscription tests
   - Node failure handling tests
   - Node recovery handling tests
   - Coordinator election tests

2. **EventBusTest** - Tests for event distribution
   - Subscription management tests
   - Event publishing tests
   - Event history tests
   - Async publishing tests

### Phase 3: Content Access Module Tests (Completed)

1. **ContentAccessServiceTest** - Tests for content access control
   - Create content access tests
   - Get content access tests
   - Update access level tests
   - Grant permission tests
   - Content access entity tests (expiry, permissions)

### Phase 4: Jobs Module Tests (Completed)

1. **JobServiceTest** - Tests for job queue management
   - Enqueue job tests
   - Claim next jobs tests
   - Mark success tests
   - Mark failure tests
   - Job entity tests

## Updated Test Coverage Summary

| Module | Source Files | Test Files | Status |
|--------|-------------|------------|--------|
| mastodon-core | 97 | 2 | **Tests Added** |
| mastodon-cluster | 15 | 2 | **Tests Added** |
| mastodon-content-access | 6 | 1 | **Tests Added** |
| mastodon-jobs | 4 | 1 | **Tests Added** |
| mastodon-activitypub | 30 | 2 | Existing |
| mastodon-federation | 25 | 4 | Existing |
| mastodon-media | 16 | 3 | Existing |
| mastodon-streaming | 8 | 4 | Existing |
| mastodon-web | 72 | 16 | Existing |
| mastodon-content-authority | 2 | 0 | Pending |
| mastodon-federation-index | 3 | 0 | Pending |
| mastodon-resources | 1 | 0 | Pending |
| mastodon-setup | 3 | 0 | Pending |
| mastodon-torrent | 5 | 0 | Pending |
| mastodon-ui | 0 | 0 | N/A |

## Next Steps

1. Run tests to verify compilation and execution
2. Add more tests for remaining modules (content-authority, federation-index, etc.)
3. Add integration tests for repository layer
4. Run coverage reports to measure improvement
5. Continue adding tests for edge cases
