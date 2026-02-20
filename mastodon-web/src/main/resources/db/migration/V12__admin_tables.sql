-- Admin and moderation tables

-- Account actions table for tracking moderation actions on accounts
CREATE TABLE account_actions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    action_type VARCHAR(32) NOT NULL,
    reason TEXT,
    report_id BIGINT REFERENCES reports(id) ON DELETE SET NULL,
    action_taken_by_account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    target_account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_account_actions_account ON account_actions(account_id);
CREATE INDEX idx_account_actions_target_account ON account_actions(target_account_id);
CREATE INDEX idx_account_actions_type ON account_actions(action_type);
CREATE INDEX idx_account_actions_created_at ON account_actions(created_at);

-- Domain blocks table for instance-level moderation
CREATE TABLE domain_blocks (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(255) NOT NULL UNIQUE,
    severity VARCHAR(32) NOT NULL DEFAULT 'silence',
    reject_media BOOLEAN NOT NULL DEFAULT FALSE,
    reject_reports BOOLEAN NOT NULL DEFAULT FALSE,
    private_comment TEXT,
    public_comment TEXT,
    obfuscate BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_domain_blocks_domain ON domain_blocks(domain);
CREATE INDEX idx_domain_blocks_severity ON domain_blocks(severity);

-- Report notes table for moderator notes on reports
CREATE TABLE report_notes (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_report_notes_report ON report_notes(report_id);
CREATE INDEX idx_report_notes_account ON report_notes(account_id);
CREATE INDEX idx_report_notes_created_at ON report_notes(created_at);

-- Add additional fields to reports table for admin operations
ALTER TABLE reports ADD COLUMN IF NOT EXISTS assigned_account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL;
ALTER TABLE reports ADD COLUMN IF NOT EXISTS action_taken_at TIMESTAMPTZ;
ALTER TABLE reports ADD COLUMN IF NOT EXISTS action_taken_by_account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL;
ALTER TABLE reports ADD COLUMN IF NOT EXISTS forwarded BOOLEAN DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_reports_assigned_account ON reports(assigned_account_id);
CREATE INDEX IF NOT EXISTS idx_reports_action_taken ON reports(action_taken);

-- Add moderation-related fields to accounts table
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS suspended BOOLEAN DEFAULT FALSE;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS suspended_at TIMESTAMPTZ;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS silenced BOOLEAN DEFAULT FALSE;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS silenced_at TIMESTAMPTZ;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS disabled BOOLEAN DEFAULT FALSE;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS disabled_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_accounts_suspended ON accounts(suspended);
CREATE INDEX IF NOT EXISTS idx_accounts_silenced ON accounts(silenced);
CREATE INDEX IF NOT EXISTS idx_accounts_disabled ON accounts(disabled);
