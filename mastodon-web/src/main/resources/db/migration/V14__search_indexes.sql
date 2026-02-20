-- Full-text search indexes using PostgreSQL tsvector and pg_trgm

-- Enable pg_trgm extension for trigram similarity matching
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Add tsvector columns for full-text search
ALTER TABLE statuses ADD COLUMN IF NOT EXISTS content_tsvector TSVECTOR;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS search_tsvector TSVECTOR;

-- Create GIN indexes for full-text search
CREATE INDEX IF NOT EXISTS idx_statuses_content_tsvector ON statuses USING GIN(content_tsvector);
CREATE INDEX IF NOT EXISTS idx_accounts_search_tsvector ON accounts USING GIN(search_tsvector);

-- Create trigram indexes for similarity matching
CREATE INDEX IF NOT EXISTS idx_accounts_username_trgm ON accounts USING GIN(username gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_accounts_display_name_trgm ON accounts USING GIN(display_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_accounts_acct_trgm ON accounts USING GIN(acct gin_trgm_ops);

-- Create trigram index for status content
CREATE INDEX IF NOT EXISTS idx_statuses_content_trgm ON statuses USING GIN(content gin_trgm_ops);

-- Function to update status tsvector
CREATE OR REPLACE FUNCTION update_status_tsvector() RETURNS TRIGGER AS $$
BEGIN
    NEW.content_tsvector := to_tsvector('english', COALESCE(NEW.content, '') || ' ' || COALESCE(NEW.spoiler_text, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to update account tsvector
CREATE OR REPLACE FUNCTION update_account_tsvector() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_tsvector := to_tsvector('english', 
        COALESCE(NEW.username, '') || ' ' || 
        COALESCE(NEW.display_name, '') || ' ' || 
        COALESCE(NEW.note, '')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to automatically update tsvector columns
DROP TRIGGER IF EXISTS statuses_tsvector_update ON statuses;
CREATE TRIGGER statuses_tsvector_update
    BEFORE INSERT OR UPDATE ON statuses
    FOR EACH ROW EXECUTE FUNCTION update_status_tsvector();

DROP TRIGGER IF EXISTS accounts_tsvector_update ON accounts;
CREATE TRIGGER accounts_tsvector_update
    BEFORE INSERT OR UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_account_tsvector();

-- Populate existing data
UPDATE statuses SET content_tsvector = to_tsvector('english', COALESCE(content, '') || ' ' || COALESCE(spoiler_text, ''));
UPDATE accounts SET search_tsvector = to_tsvector('english', COALESCE(username, '') || ' ' || COALESCE(display_name, '') || ' ' || COALESCE(note, ''));

-- Add index for tag search
CREATE INDEX IF NOT EXISTS idx_tags_name_trgm ON tags USING GIN(name gin_trgm_ops);

-- Add index for hashtag search by name
CREATE INDEX IF NOT EXISTS idx_tags_name_lower ON tags(LOWER(name));
