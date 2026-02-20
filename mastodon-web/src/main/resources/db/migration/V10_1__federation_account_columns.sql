-- Add federation-related columns to accounts table

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS inbox_url TEXT;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS shared_inbox_url TEXT;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS public_key_pem TEXT;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS local_account BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS last_fetched_at TIMESTAMPTZ;
