-- Add missing columns for user features

-- Add pending column to follows table for follow requests
ALTER TABLE follows ADD COLUMN IF NOT EXISTS pending BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_follows_pending ON follows(pending) WHERE pending = TRUE;

-- Add user tracking columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_sign_in_ip VARCHAR(45);
ALTER TABLE users ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN IF NOT EXISTS confirmed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS approved BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS approval_required BOOLEAN NOT NULL DEFAULT FALSE;

-- Create user_domain_blocks table for user-level domain blocking
CREATE TABLE IF NOT EXISTS user_domain_blocks (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    domain VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, domain)
);

CREATE INDEX IF NOT EXISTS idx_user_domain_blocks_account_id ON user_domain_blocks(account_id);
CREATE INDEX IF NOT EXISTS idx_user_domain_blocks_domain ON user_domain_blocks(domain);

-- Create status_pins table for pinned statuses
CREATE TABLE IF NOT EXISTS status_pins (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    status_id BIGINT NOT NULL REFERENCES statuses(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, status_id)
);

CREATE INDEX IF NOT EXISTS idx_status_pins_account_id ON status_pins(account_id);
CREATE INDEX IF NOT EXISTS idx_status_pins_status_id ON status_pins(status_id);

-- Add comments to document the new columns
COMMENT ON COLUMN follows.pending IS 'Whether this follow request is pending approval (for locked accounts)';
COMMENT ON COLUMN users.last_sign_in_ip IS 'IP address of last sign in';
COMMENT ON COLUMN users.confirmed_at IS 'Timestamp when email was confirmed';
COMMENT ON COLUMN users.confirmed IS 'Whether email has been confirmed';
COMMENT ON COLUMN users.approved IS 'Whether account has been approved (for approval-based signups)';
COMMENT ON COLUMN users.approval_required IS 'Whether this account requires approval before activation';
