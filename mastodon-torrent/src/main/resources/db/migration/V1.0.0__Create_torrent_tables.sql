-- Torrent metadata for shared content
CREATE TABLE shared_torrents (
    id BIGSERIAL PRIMARY KEY,
    
    -- Content reference
    media_attachment_id BIGINT UNIQUE REFERENCES media_attachments(id),
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    
    -- Torrent identification
    info_hash VARCHAR(40) UNIQUE NOT NULL,
    
    -- Torrent metadata
    torrent_name VARCHAR(255),
    total_size BIGINT NOT NULL,
    piece_size INTEGER NOT NULL,
    piece_count INTEGER NOT NULL,
    
    -- Generated files
    torrent_file_path VARCHAR(512),
    magnet_link TEXT,
    
    -- Seeding status
    seeding_enabled BOOLEAN DEFAULT true,
    seeding_status VARCHAR(20) DEFAULT 'INITIALIZING',
    
    -- Statistics
    total_uploaded BIGINT DEFAULT 0,
    total_downloaded BIGINT DEFAULT 0,
    seed_count INTEGER DEFAULT 0,
    peer_count INTEGER DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_announce_at TIMESTAMP
);

CREATE INDEX idx_shared_torrents_info_hash ON shared_torrents(info_hash);
CREATE INDEX idx_shared_torrents_account ON shared_torrents(account_id);
CREATE INDEX idx_shared_torrents_seeding ON shared_torrents(seeding_status);

-- Tracker list for shared torrents
CREATE TABLE shared_torrent_trackers (
    shared_torrent_id BIGINT NOT NULL REFERENCES shared_torrents(id) ON DELETE CASCADE,
    tracker VARCHAR(512) NOT NULL
);

CREATE INDEX idx_shared_torrent_trackers_torrent ON shared_torrent_trackers(shared_torrent_id);

-- User torrent preferences
CREATE TABLE account_torrent_settings (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT UNIQUE NOT NULL REFERENCES accounts(id),
    
    -- Sharing preferences
    share_via_torrent BOOLEAN DEFAULT false,
    auto_seed_uploads BOOLEAN DEFAULT true,
    
    -- Seeding limits
    max_seeding_ratio DECIMAL(4,2) DEFAULT 2.0,
    max_seeding_hours INTEGER DEFAULT 168,
    max_upload_rate VARCHAR(20) DEFAULT '1MB',
    
    -- Privacy
    anonymous_seeding BOOLEAN DEFAULT false,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Tracker announce history
CREATE TABLE tracker_announces (
    id BIGSERIAL PRIMARY KEY,
    
    shared_torrent_id BIGINT REFERENCES shared_torrents(id) ON DELETE CASCADE,
    tracker_url VARCHAR(512) NOT NULL,
    
    -- Announce result
    status VARCHAR(20) NOT NULL,
    seeders INTEGER,
    leechers INTEGER,
    peers_found INTEGER,
    
    -- Timing
    announced_at TIMESTAMP DEFAULT NOW(),
    response_time_ms INTEGER,
    
    -- Error info
    error_message TEXT
);

CREATE INDEX idx_tracker_announces_torrent ON tracker_announces(shared_torrent_id);
CREATE INDEX idx_tracker_announces_time ON tracker_announces(announced_at);
