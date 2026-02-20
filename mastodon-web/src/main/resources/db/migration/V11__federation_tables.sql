-- Federation-related tables and indexes

-- Add index for status URI lookups (for federation)
CREATE INDEX IF NOT EXISTS idx_statuses_uri ON statuses(uri) WHERE uri IS NOT NULL;

-- Add index for account inbox URL lookups
CREATE INDEX IF NOT EXISTS idx_accounts_inbox_url ON accounts(inbox_url) WHERE inbox_url IS NOT NULL;

-- Add index for account public key lookups
CREATE INDEX IF NOT EXISTS idx_accounts_public_key ON accounts(public_key_pem) WHERE public_key_pem IS NOT NULL;

-- Add index for remote accounts (non-local)
CREATE INDEX IF NOT EXISTS idx_accounts_local ON accounts(local_account);

-- Add index for account last fetch time (for cache invalidation)
CREATE INDEX IF NOT EXISTS idx_accounts_last_fetched ON accounts(last_fetched_at);

-- Create federation delivery queue table (for tracking outgoing deliveries)
CREATE TABLE IF NOT EXISTS federation_deliveries (
    id BIGSERIAL PRIMARY KEY,
    activity_id VARCHAR(500) NOT NULL,
    activity_type VARCHAR(64) NOT NULL,
    target_inbox VARCHAR(500) NOT NULL,
    sender_account_id BIGINT NOT NULL REFERENCES accounts(id),
    payload TEXT NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    last_error TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for federation_deliveries
CREATE INDEX IF NOT EXISTS idx_federation_deliveries_status ON federation_deliveries(status);
CREATE INDEX IF NOT EXISTS idx_federation_deliveries_next_attempt ON federation_deliveries(next_attempt_at) WHERE status = 'pending';
CREATE INDEX IF NOT EXISTS idx_federation_deliveries_sender ON federation_deliveries(sender_account_id);

-- Create follow request table for tracking pending follow requests to remote instances
CREATE TABLE IF NOT EXISTS follow_requests (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    target_account_id BIGINT NOT NULL REFERENCES accounts(id),
    activity_id VARCHAR(500),
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(account_id, target_account_id)
);

CREATE INDEX IF NOT EXISTS idx_follow_requests_account ON follow_requests(account_id);
CREATE INDEX IF NOT EXISTS idx_follow_requests_target ON follow_requests(target_account_id);
CREATE INDEX IF NOT EXISTS idx_follow_requests_status ON follow_requests(status);
