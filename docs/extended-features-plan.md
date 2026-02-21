# Extended Features Plan

This document outlines the plan for extending mastodon_4j with advanced administration, scalability, and content distribution features while maintaining full compatibility with the upstream Mastodon repository.

## Design Principles

1. **Non-Breaking Extensions**: All new features must be optional and not interfere with standard Mastodon API compatibility
2. **Default-Secure**: Default settings prioritize security, accountability, and transparency
3. **Modular Architecture**: New features are isolated in separate modules to prevent coupling
4. **Backward Compatible**: Existing Mastodon clients and federated servers work without modification

---

## Part 1: Enhanced Server Administration

### 1.1 Quick Setup System

#### Overview
Provide administrators with streamlined setup options:
- **Express Setup**: Single-command deployment with sensible defaults
- **Script-Based Setup**: YAML/JSON configuration file for reproducible deployments
- **Interactive Setup**: CLI wizard for guided configuration

#### Implementation Plan

**Module: `mastodon-setup`**

```
mastodon-setup/
├── src/main/java/org/joinmastodon/setup/
│   ├── SetupApplication.java          # Standalone setup CLI
│   ├── config/
│   │   ├── SetupConfiguration.java    # Setup configuration model
│   │   ├── ServerProfile.java         # Pre-defined server profiles
│   │   └── ResourceAllocation.java    # Resource allocation config
│   ├── wizard/
│   │   ├── InteractiveWizard.java     # CLI interactive setup
│   │   └── SetupSteps.java            # Step-by-step configuration
│   ├── script/
│   │   ├── ScriptParser.java          # Parse YAML/JSON setup scripts
│   │   └── ScriptExecutor.java        # Apply scripted configuration
│   └── templates/
│       ├── minimal.yaml               # Minimal server config
│       ├── standard.yaml              # Standard community server
│       └── enterprise.yaml            # High-availability config
└── pom.xml
```

**Setup Script Schema** (`setup.yaml`):
```yaml
# Server Setup Configuration
server:
  domain: example.social
  name: "Example Social"
  description: "A friendly Mastodon instance"
  admin_email: admin@example.social

# Resource Allocation (optional - defaults to auto-detect)
resources:
  cpu_cores: 4           # Number of CPU cores to utilize
  memory_gb: 8           # Maximum memory allocation
  gpu_enabled: false     # Enable GPU acceleration for media
  network_adapters:
    - name: eth0
      bandwidth_mbps: 1000

# Content Policy
content:
  default_visibility: public
  private_content_enabled: true
  federation_mode: open  # open, limited, closed

# Federation Index
federation_index:
  enabled: true
  share_ratings: true
  share_restrictions: true
```

#### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v2/admin/setup/status` | GET | Get setup completion status |
| `/api/v2/admin/setup/validate` | POST | Validate setup script |
| `/api/v2/admin/setup/apply` | POST | Apply setup configuration |
| `/api/v2/admin/setup/export` | GET | Export current config as script |

---

### 1.2 Resource Allocation System

#### Overview
Allow administrators to commit specific computing resources to the server:
- CPU core allocation and affinity
- GPU acceleration for media transcoding
- Multiple network adapter support
- Memory management and limits

#### Implementation Plan

**Module: `mastodon-resources`**

```
mastodon-resources/
├── src/main/java/org/joinmastodon/resources/
│   ├── config/
│   │   ├── ResourceConfiguration.java   # Resource configuration
│   │   └── ResourceProperties.java      # Application properties
│   ├── cpu/
│   │   ├── CpuAllocator.java            # CPU core allocation
│   │   ├── ThreadAffinity.java          # Thread affinity management
│   │   └── CpuMonitor.java              # CPU usage monitoring
│   ├── gpu/
│   │   ├── GpuManager.java              # GPU detection and management
│   │   ├── CudaProcessor.java           # CUDA-based processing
│   │   └── OpenClProcessor.java         # OpenCL fallback
│   ├── network/
│   │   ├── NetworkManager.java          # Network adapter management
│   │   ├── BandwidthAllocator.java      # Bandwidth allocation
│   │   └── LoadBalancer.java            # Network load balancing
│   └── memory/
│       ├── MemoryManager.java           # Memory management
│       └── HeapMonitor.java             # Heap usage monitoring
└── pom.xml
```

**Configuration Properties**:
```yaml
mastodon:
  resources:
    cpu:
      enabled: true
      cores: 4
      affinity: [0, 1, 2, 3]
    gpu:
      enabled: false
      devices: [0]
      media_transcoding: true
    network:
      adapters:
        - name: eth0
          priority: 1
          bandwidth_limit: 1GB
    memory:
      max_heap: 4GB
      media_cache: 2GB
```

---

### 1.3 Distributed Server Architecture

#### Overview
Enable multiple computers to act as a single Mastodon server:
- Node discovery and registration
- Workload distribution and load balancing
- Shared state synchronization
- Fault tolerance and failover

#### Implementation Plan

**Module: `mastodon-cluster`**

```
mastodon-cluster/
├── src/main/java/org/joinmastodon/cluster/
│   ├── config/
│   │   ├── ClusterConfiguration.java    # Cluster configuration
│   │   └── NodeProperties.java          # Node-specific properties
│   ├── discovery/
│   │   ├── NodeDiscovery.java           # Discover cluster nodes
│   │   ├── NodeRegistry.java            # Node registration
│   │   └── HealthChecker.java           # Node health monitoring
│   ├── distribution/
│   │   ├── WorkloadDistributor.java     # Distribute work across nodes
│   │   ├── TaskQueue.java               # Distributed task queue
│   │   └── LoadBalancer.java            # Request load balancing
│   ├── sync/
│   │   ├── StateSynchronizer.java       # Synchronize shared state
│   │   ├── CacheReplicator.java         # Replicate cache across nodes
│   │   └── EventBus.java                # Cluster-wide event bus
│   ├── failover/
│   │   ├── FailoverManager.java         # Handle node failures
│   │   └── RecoveryService.java         # Recover from failures
│   └── protocol/
│       ├── ClusterProtocol.java         # Inter-node communication
│       └── ClusterMessage.java          # Message types
└── pom.xml
```

**Cluster Configuration**:
```yaml
mastodon:
  cluster:
    enabled: true
    node_id: node-1
    role: worker  # coordinator, worker, or hybrid
    coordinator:
      host: coordinator.example.social
      port: 7946
    discovery:
      type: consul  # consul, etcd, or static
      endpoints:
        - consul.example.social:8500
    replication:
      factor: 2
      sync_interval: 5s
```

**Node Roles**:
- **Coordinator**: Manages cluster state, distributes work, handles failover
- **Worker**: Processes assigned tasks (API, streaming, media, jobs)
- **Hybrid**: Both coordinator and worker capabilities

---

### 1.4 Content Visibility and Access Control

#### Overview
Enhanced content visibility options:
- Public content (standard Mastodon behavior)
- Private content with permission-based access
- Server-level access control for external servers
- User-level permission management

#### Implementation Plan

**Extended Domain Models**:

```java
// ContentAccess entity
@Entity
@Table(name = "content_access")
public class ContentAccess {
    @Id
    private Long id;
    
    @ManyToOne
    private Status status;
    
    @Enumerated(EnumType.STRING)
    private AccessLevel accessLevel;  // PUBLIC, INSTANCE_ONLY, FOLLOWERS_ONLY, PRIVATE
    
    @OneToMany(mappedBy = "contentAccess")
    private Set<ContentPermission> permissions;
}

// ContentPermission entity
@Entity
@Table(name = "content_permissions")
public class ContentPermission {
    @Id
    private Long id;
    
    @ManyToOne
    private ContentAccess contentAccess;
    
    @ManyToOne
    private Account grantee;
    
    @Enumerated(EnumType.STRING)
    private Permission permission;  // VIEW, COMMENT, SHARE, ADMIN
}

// ServerAccessPolicy entity
@Entity
@Table(name = "server_access_policies")
public class ServerAccessPolicy {
    @Id
    private Long id;
    
    private String targetDomain;
    
    @Enumerated(EnumType.STRING)
    private AccessPolicy policy;  // ALLOWED, RESTRICTED, BLOCKED
    
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String description;
}
```

**API Endpoints**:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v2/statuses/:id/access` | GET | Get content access settings |
| `/api/v2/statuses/:id/access` | PUT | Update access settings |
| `/api/v2/statuses/:id/permissions` | POST | Grant permission to user |
| `/api/v2/statuses/:id/permissions/:account_id` | DELETE | Revoke permission |
| `/api/v2/admin/server-policies` | GET | List server access policies |
| `/api/v2/admin/server-policies` | POST | Create server policy |

---

## Part 2: Federation Index Database

### 2.1 Server Index Schema

#### Overview
A distributed database mapping Mastodon servers with:
- Internal information (our server's view of external servers)
- External information (other servers' views of specific servers)
- Restriction tracking (blocked/restricted servers with reasons)
- User ratings (5-star system for servers and content)

#### Database Schema

```sql
-- Server Index Entries
CREATE TABLE server_index (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(255) UNIQUE NOT NULL,
    
    -- Basic Information
    display_name VARCHAR(255),
    description TEXT,
    version VARCHAR(100),
    
    -- Availability Tracking
    last_seen_at TIMESTAMP,
    availability_score DECIMAL(3,2),  -- 0.00 to 1.00
    is_online BOOLEAN DEFAULT true,
    
    -- Content Classification
    content_rating VARCHAR(20),  -- G, PG, PG-13, R, NC-17, NSFW
    content_description TEXT,
    
    -- Internal Rating (our server's view)
    internal_rating DECIMAL(2,1),  -- 0.0 to 5.0
    internal_notes TEXT,
    
    -- Aggregated External Rating
    external_rating DECIMAL(2,1),  -- 0.0 to 5.0
    external_rating_count INTEGER DEFAULT 0,
    
    -- Access Policy
    access_policy VARCHAR(20) DEFAULT 'OPEN',  -- OPEN, RESTRICTED, BLOCKED
    restriction_reason TEXT,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_server_index_domain ON server_index(domain);
CREATE INDEX idx_server_index_rating ON server_index(internal_rating);
CREATE INDEX idx_server_index_policy ON server_index(access_policy);

-- User Ratings for Servers
CREATE TABLE server_ratings (
    id BIGSERIAL PRIMARY KEY,
    server_index_id BIGINT REFERENCES server_index(id),
    user_id BIGINT REFERENCES users(id),
    
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    content_rating VARCHAR(20),  -- User's content rating assessment
    review TEXT,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(server_index_id, user_id)
);

-- External Server Information (from other servers)
CREATE TABLE external_server_info (
    id BIGSERIAL PRIMARY KEY,
    server_index_id BIGINT REFERENCES server_index(id),
    source_domain VARCHAR(255) NOT NULL,  -- Who provided this info
    
    rating DECIMAL(2,1),
    content_rating VARCHAR(20),
    notes TEXT,
    
    received_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(server_index_id, source_domain)
);

-- Server Restrictions
CREATE TABLE server_restrictions (
    id BIGSERIAL PRIMARY KEY,
    target_domain VARCHAR(255) NOT NULL,
    
    restriction_type VARCHAR(20) NOT NULL,  -- BLOCK, SILENCE, MEDIA_BLOCK
    reason TEXT NOT NULL,
    description TEXT,
    
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP,  -- NULL for permanent
    
    UNIQUE(target_domain, restriction_type)
);

-- Index Sharing Configuration
CREATE TABLE index_sharing_config (
    id BIGSERIAL PRIMARY KEY,
    
    share_internal_info BOOLEAN DEFAULT true,
    share_ratings BOOLEAN DEFAULT true,
    share_restrictions BOOLEAN DEFAULT false,  -- Privacy-sensitive
    
    sharing_partners TEXT[],  -- List of trusted domains
    update_interval INTEGER DEFAULT 3600,  -- seconds
    
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### 2.2 Federation Index Service

**Module: `mastodon-federation-index`**

```
mastodon-federation-index/
├── src/main/java/org/joinmastodon/federation/index/
│   ├── config/
│   │   └── IndexConfiguration.java
│   ├── entity/
│   │   ├── ServerIndex.java
│   │   ├── ServerRating.java
│   │   ├── ExternalServerInfo.java
│   │   └── ServerRestriction.java
│   ├── repository/
│   │   ├── ServerIndexRepository.java
│   │   ├── ServerRatingRepository.java
│   │   └── ServerRestrictionRepository.java
│   ├── service/
│   │   ├── ServerIndexService.java       # Manage server index
│   │   ├── RatingService.java            # Handle ratings
│   │   ├── RestrictionService.java       # Manage restrictions
│   │   └── IndexSharingService.java      # Share index with partners
│   ├── sync/
│   │   ├── IndexSynchronizer.java        # Sync with partner servers
│   │   └── IndexProtocol.java            # Protocol for sharing
│   └── api/
│       ├── ServerIndexController.java    # Public API
│       └── AdminIndexController.java     # Admin API
└── pom.xml
```

### 2.3 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v2/federation/servers` | GET | List indexed servers |
| `/api/v2/federation/servers/:domain` | GET | Get server details |
| `/api/v2/federation/servers/:domain/rate` | POST | Rate a server |
| `/api/v2/federation/servers/:domain/info` | GET | Get external info about server |
| `/api/v2/admin/federation/restrictions` | GET | List restrictions |
| `/api/v2/admin/federation/restrictions` | POST | Create restriction |
| `/api/v2/admin/federation/sharing` | GET/PUT | Index sharing config |

---

## Part 3: qBittorrent Integration

### 3.1 Overview

Integrate qBittorrent functionality as a content distribution layer:
- Refactor qBittorrent from C++/Qt to OpenJDK 25/Spring Boot
- Provide BitTorrent-based content distribution for media files
- Enable peer-to-peer content sharing between Mastodon instances
- Reduce server bandwidth through distributed content delivery

### 3.2 Architecture

**Module: `mastodon-torrent`**

```
mastodon-torrent/
├── src/main/java/org/joinmastodon/torrent/
│   ├── config/
│   │   ├── TorrentConfiguration.java
│   │   └── TorrentProperties.java
│   ├── core/
│   │   ├── TorrentClient.java            # Main BitTorrent client
│   │   ├── TorrentSession.java           # Session management
│   │   └── TorrentSettings.java          # Client settings
│   ├── torrent/
│   │   ├── Torrent.java                  # Torrent entity
│   │   ├── TorrentInfo.java              # Torrent metadata
│   │   ├── TorrentHandle.java            # Active torrent handle
│   │   └── TorrentStatus.java            # Download status
│   ├── peer/
│   │   ├── PeerInfo.java                 # Peer information
│   │   ├── PeerManager.java              # Manage connections
│   │   └── PeerId.java                   # Peer identification
│   ├── tracker/
│   │   ├── Tracker.java                  # Tracker client
│   │   ├── TrackerEntry.java             # Tracker entry
│   │   └── TrackerStatus.java            # Tracker status
│   ├── storage/
│   │   ├── TorrentStorage.java           # File storage
│   │   ├── PieceStorage.java             # Piece management
│   │   └── ResumeData.java               # Resume data
│   ├── dht/
│   │   ├── DhtNode.java                  # DHT node
│   │   └── DhtRouter.java                # DHT routing
│   ├── integration/
│   │   ├── MediaTorrentBridge.java       # Bridge to mastodon-media
│   │   ├── ContentSeeder.java            # Seed Mastodon content
│   │   └── FederationSync.java           # Sync with other instances
│   └── api/
│       ├── TorrentController.java        # REST API
│       └── AdminTorrentController.java   # Admin API
└── pom.xml
```

### 3.3 Integration Points

#### Media Integration
```java
@Service
public class MediaTorrentBridge {
    
    // Create torrent for large media files
    public TorrentInfo createTorrentForMedia(MediaAttachment media) {
        // Generate torrent file for media
        // Add to seeding queue
    }
    
    // Download media via torrent if available
    public Optional<InputStream> downloadViaTorrent(String infoHash) {
        // Check if torrent is available
        // Download from peers
    }
    
    // Seed popular media content
    public void seedPopularContent(List<MediaAttachment> media) {
        // Prioritize seeding of popular content
    }
}
```

#### Federation Integration
```java
@Service
public class FederationSync {
    
    // Share torrent info with federated servers
    public void shareTorrentInfo(TorrentInfo info, List<String> domains) {
        // Send torrent metadata to partner servers
    }
    
    // Receive torrent info from federated servers
    public void receiveTorrentInfo(TorrentInfo info, String sourceDomain) {
        // Validate and add to index
    }
}
```

### 3.4 Configuration

```yaml
mastodon:
  torrent:
    enabled: true
    
    client:
      download_rate_limit: 10MB
      upload_rate_limit: 5MB
      max_connections: 200
      max_uploads: 10
      
    storage:
      download_path: ./data/torrents
      temp_path: ./data/torrents/temp
      resume_data: true
      
    dht:
      enabled: true
      port: 6881
      
    tracker:
      enabled: true
      trackers:
        - udp://tracker.example.com:1337
        - wss://tracker.example.com:443
        
    seeding:
      enabled: true
      ratio_limit: 2.0
      seeding_time_limit: 168h  # 1 week
      
    federation:
      share_torrents: true
      trusted_trackers:
        - tracker.federated.social
```

### 3.5 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v2/torrents` | GET | List active torrents |
| `/api/v2/torrents` | POST | Add new torrent |
| `/api/v2/torrents/:infoHash` | GET | Get torrent status |
| `/api/v2/torrents/:infoHash` | DELETE | Remove torrent |
| `/api/v2/torrents/:infoHash/pause` | POST | Pause torrent |
| `/api/v2/torrents/:infoHash/resume` | POST | Resume torrent |
| `/api/v2/admin/torrents/settings` | GET/PUT | Client settings |

---

## Part 4: Content Authority and Verification System

### 4.1 Overview

Enable a Mastodon server to act as a Certificate Authority (CA) for content authentication:

- **Server as CA**: Administrators can configure the server to sign distributed content
- **Content Signing**: Cryptographic signatures for statuses, media, and federated content
- **Verification Metadata**: Content includes server location for authentication
- **External Verification**: Third parties can verify content authenticity without direct server access

### 4.2 Architecture

**Module: `mastodon-content-authority`**

```
mastodon-content-authority/
├── src/main/java/org/joinmastodon/contentauthority/
│   ├── config/
│   │   ├── ContentAuthorityConfiguration.java   # CA configuration
│   │   └── ContentAuthorityProperties.java      # Properties
│   ├── ca/
│   │   ├── CertificateAuthority.java            # CA operations
│   │   ├── KeyManager.java                      # Key pair management
│   │   ├── CertificateGenerator.java            # Generate X.509 certificates
│   │   └── CertificateStore.java                # Certificate storage
│   ├── signing/
│   │   ├── ContentSigner.java                   # Sign content
│   │   ├── SignatureAlgorithm.java              # Supported algorithms
│   │   └── SignatureFormat.java                 # Signature formats
│   ├── verification/
│   │   ├── ContentVerifier.java                 # Verify content signatures
│   │   ├── CertificateChainValidator.java       # Validate certificate chains
│   │   └── RevocationChecker.java               # Check certificate revocation
│   ├── metadata/
│   │   ├── ContentMetadata.java                 # Content metadata model
│   │   ├── AuthorityMetadata.java               # Authority info in content
│   │   └── MetadataBuilder.java                 # Build metadata structures
│   ├── distribution/
│   │   ├── SignedContentPublisher.java          # Publish signed content
│   │   └── SignatureAnnouncement.java           # Announce signatures
│   ├── federation/
│   │   ├── AuthorityDiscovery.java              # Discover CAs in federation
│   │   ├── CrossSigning.java                    # Cross-sign with other CAs
│   │   └── TrustStore.java                      # Trusted authorities
│   └── api/
│       ├── AuthorityController.java             # Public CA API
│       └── AdminAuthorityController.java        # Admin CA management
└── pom.xml
```

### 4.3 Database Schema

```sql
-- Certificate Authority Configuration
CREATE TABLE content_authority_config (
    id BIGSERIAL PRIMARY KEY,
    
    -- CA Identity
    common_name VARCHAR(255) NOT NULL,           -- e.g., "example.social CA"
    organization VARCHAR(255),
    organizational_unit VARCHAR(255),
    country VARCHAR(2),
    
    -- Key Information
    key_algorithm VARCHAR(50) DEFAULT 'RSA',     -- RSA, ECDSA, EdDSA
    key_size INTEGER DEFAULT 4096,               -- Key size in bits
    signature_algorithm VARCHAR(100) DEFAULT 'SHA512withRSA',
    
    -- Certificate Settings
    validity_days INTEGER DEFAULT 365,
    certificate_pem TEXT,                        -- CA certificate
    private_key_encrypted TEXT,                  -- Encrypted private key
    
    -- Trust Settings
    trust_level VARCHAR(20) DEFAULT 'INSTANCE',  -- INSTANCE, FEDERATION, PUBLIC
    auto_sign_content BOOLEAN DEFAULT false,
    sign_media BOOLEAN DEFAULT true,
    sign_statuses BOOLEAN DEFAULT true,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Issued Certificates
CREATE TABLE issued_certificates (
    id BIGSERIAL PRIMARY KEY,
    
    -- Certificate Identity
    serial_number VARCHAR(64) UNIQUE NOT NULL,
    subject_dn VARCHAR(512) NOT NULL,
    
    -- Certificate Data
    certificate_pem TEXT NOT NULL,
    public_key_pem TEXT NOT NULL,
    
    -- Validity
    not_before TIMESTAMP NOT NULL,
    not_after TIMESTAMP NOT NULL,
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE',         -- ACTIVE, REVOKED, EXPIRED
    revocation_reason VARCHAR(50),
    revoked_at TIMESTAMP,
    
    -- Association
    account_id BIGINT REFERENCES accounts(id),
    domain VARCHAR(255),                         -- For remote certificates
    
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_issued_certs_serial ON issued_certificates(serial_number);
CREATE INDEX idx_issued_certs_account ON issued_certificates(account_id);
CREATE INDEX idx_issued_certs_domain ON issued_certificates(domain);

-- Content Signatures
CREATE TABLE content_signatures (
    id BIGSERIAL PRIMARY KEY,
    
    -- Content Reference
    content_type VARCHAR(50) NOT NULL,           -- STATUS, MEDIA, ACCOUNT, etc.
    content_id BIGINT NOT NULL,
    content_hash VARCHAR(128) NOT NULL,          -- SHA-256 hash of content
    
    -- Signature Data
    signature_algorithm VARCHAR(100) NOT NULL,
    signature_value TEXT NOT NULL,               -- Base64 signature
    certificate_id BIGINT REFERENCES issued_certificates(id),
    
    -- Metadata
    signed_at TIMESTAMP DEFAULT NOW(),
    signing_url VARCHAR(512),                    -- URL to verify signature
    
    UNIQUE(content_type, content_id, certificate_id)
);

CREATE INDEX idx_content_sigs_content ON content_signatures(content_type, content_id);
CREATE INDEX idx_content_sigs_hash ON content_signatures(content_hash);

-- Trusted Authorities
CREATE TABLE trusted_authorities (
    id BIGSERIAL PRIMARY KEY,
    
    -- Authority Identity
    domain VARCHAR(255) UNIQUE NOT NULL,
    common_name VARCHAR(255) NOT NULL,
    
    -- Certificate
    certificate_pem TEXT NOT NULL,
    public_key_pem TEXT NOT NULL,
    
    -- Trust Settings
    trust_level VARCHAR(20) DEFAULT 'VERIFIED',  -- VERIFIED, KNOWN, UNKNOWN
    auto_verify BOOLEAN DEFAULT true,
    
    -- Verification
    verified_at TIMESTAMP,
    verified_by BIGINT REFERENCES users(id),
    
    -- Cross-Signing
    cross_signed_by BIGINT REFERENCES content_authority_config(id),
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Certificate Revocation List
CREATE TABLE certificate_revocation_list (
    id BIGSERIAL PRIMARY KEY,
    
    serial_number VARCHAR(64) NOT NULL,
    revocation_date TIMESTAMP DEFAULT NOW(),
    revocation_reason VARCHAR(50),
    
    UNIQUE(serial_number)
);

CREATE INDEX idx_crl_serial ON certificate_revocation_list(serial_number);
```

### 4.4 Content Metadata Format

When content is signed, the metadata includes the authority location for verification:

```json
{
  "id": "123456789012345678",
  "type": "Status",
  "content": "<p>Hello world</p>",
  "created_at": "2026-02-20T12:00:00Z",
  "signature": {
    "algorithm": "SHA512withRSA",
    "value": "base64-encoded-signature...",
    "certificate_id": "serial-12345",
    "signed_at": "2026-02-20T12:00:01Z",
    "authority": {
      "type": "MastodonCA",
      "domain": "example.social",
      "verification_url": "https://example.social/api/v2/authority/verify",
      "certificate_url": "https://example.social/api/v2/authority/certificate",
      "public_key_url": "https://example.social/api/v2/authority/public-key"
    }
  }
}
```

### 4.5 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v2/authority` | GET | Get CA information |
| `/api/v2/authority/certificate` | GET | Get CA certificate |
| `/api/v2/authority/public-key` | GET | Get CA public key |
| `/api/v2/authority/verify` | POST | Verify content signature |
| `/api/v2/authority/verify/:contentHash` | GET | Check if content is signed |
| `/api/v2/authority/crl` | GET | Get certificate revocation list |
| `/api/v2/admin/authority` | GET/PUT | CA configuration |
| `/api/v2/admin/authority/certificates` | GET | List issued certificates |
| `/api/v2/admin/authority/certificates/:serial` | DELETE | Revoke certificate |
| `/api/v2/admin/authority/trust` | GET/POST | Manage trusted authorities |
| `/api/v2/admin/authority/sign` | POST | Manually sign content |

### 4.6 Verification Flow

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Content        │     │  Authority      │     │  Verification   │
│  Consumer       │     │  Server         │     │  Result         │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         │  1. Download Content  │                       │
         │  (with signature)     │                       │
         │<──────────────────────│                       │
         │                       │                       │
         │  2. Extract Authority │                       │
         │     Domain            │                       │
         │                       │                       │
         │  3. Request CA Cert   │                       │
         │──────────────────────>│                       │
         │                       │                       │
         │  4. Return Certificate│                       │
         │<──────────────────────│                       │
         │                       │                       │
         │  5. Verify Signature  │                       │
         │     (local)           │                       │
         │                       │                       │
         │  6. Check Revocation  │                       │
         │──────────────────────>│                       │
         │                       │                       │
         │  7. Return CRL Status │                       │
         │<──────────────────────│                       │
         │                       │                       │
         │  8. Verification Result                       │
         │───────────────────────────────────────────────>│
         │                       │                       │
```

### 4.7 Configuration

```yaml
mastodon:
  content-authority:
    enabled: true
    
    ca:
      common_name: "example.social Content Authority"
      organization: "Example Social"
      country: "US"
      key_algorithm: RSA
      key_size: 4096
      validity_days: 365
      
    signing:
      auto_sign_statuses: true
      auto_sign_media: true
      auto_sign_accounts: true
      signature_algorithm: SHA512withRSA
      
    verification:
      cache_duration: 3600
      require_revocation_check: true
      trust_unknown_authorities: false
      
    federation:
      announce_authority: true
      accept_external_authorities: true
      cross_signing_enabled: false
```

### 4.8 Integration with ActivityPub

Content signatures are embedded in ActivityPub objects:

```json
{
  "@context": [
    "https://www.w3.org/ns/activitystreams",
    "https://example.social/ns/content-authority"
  ],
  "id": "https://example.social/users/alice/statuses/123",
  "type": "Note",
  "content": "Hello world",
  "contentSignature": {
    "algorithm": "SHA512withRSA",
    "signatureValue": "base64...",
    "signingAuthority": {
      "id": "https://example.social/authority",
      "type": "MastodonContentAuthority",
      "publicKey": "https://example.social/authority/public-key"
    }
  }
}
```

### 4.9 Security Considerations

1. **Private Key Protection**: CA private keys are encrypted at rest using AES-256-GCM
2. **Key Rotation**: Support for key rotation with grace period for old signatures
3. **Revocation**: OCSP and CRL support for certificate revocation
4. **Trust Chain**: Support for cross-signing with other Mastodon CAs
5. **Audit Trail**: All signing operations logged for accountability

---

## Implementation Timeline

### Phase 1: Enhanced Administration (8-10 weeks)
- Week 1-2: Quick Setup System (`mastodon-setup`)
- Week 3-4: Resource Allocation (`mastodon-resources`)
- Week 5-7: Distributed Architecture (`mastodon-cluster`)
- Week 8-10: Content Access Control (extend `mastodon-core`)

### Phase 2: Federation Index (6-8 weeks)
- Week 1-2: Database schema and entities
- Week 3-4: Rating and restriction services
- Week 5-6: Index sharing protocol
- Week 7-8: API endpoints and UI integration

### Phase 3: qBittorrent Integration (12-16 weeks)
- Week 1-3: Core torrent client implementation
- Week 4-6: Peer and tracker management
- Week 7-9: DHT and PEX support
- Week 10-12: Media integration bridge
- Week 13-14: Federation sync
- Week 15-16: Testing and optimization

### Phase 4: Content Authority System (8-10 weeks)
- Week 1-2: CA infrastructure and key management
- Week 3-4: Content signing service
- Week 5-6: Verification and revocation system
- Week 7-8: Federation integration and trust store
- Week 9-10: API endpoints and UI integration

---

## Compatibility Guarantees

### Mastodon API Compatibility
All new features are implemented as **extensions** to the standard Mastodon API:
- Standard endpoints remain unchanged
- New endpoints use `/api/v2/` prefix
- Feature detection via `/api/v1/instance` extensions field

### Federation Compatibility
- Standard ActivityPub messages unchanged
- Extension data in optional JSON-LD context
- Graceful degradation for non-supporting servers

### Database Compatibility
- New tables do not modify existing schema
- Flyway migrations are additive only
- Can run alongside standard Mastodon database

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Breaking Mastodon compatibility | All features behind feature flags; extensive API conformance tests |
| Performance degradation | Resource allocation system allows limiting impact; benchmark suite |
| Security vulnerabilities | Security review for each module; audit logging |
| Federation protocol conflicts | Protocol versioning; graceful fallback |
| qBittorrent license issues | GPL v3 compatible with AGPL v3; separate optional module |

---

## Success Criteria

1. **Setup**: New instance operational in < 5 minutes with express setup
2. **Scalability**: Linear performance scaling up to 10 cluster nodes
3. **Federation Index**: 95% accuracy in server availability tracking
4. **Torrent Integration**: 50% bandwidth reduction for popular media
5. **Compatibility**: 100% pass rate on Mastodon API conformance tests