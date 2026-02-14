ALTER TABLE accounts ADD COLUMN actor_uri VARCHAR(500);
CREATE UNIQUE INDEX idx_accounts_actor_uri ON accounts(actor_uri) WHERE actor_uri IS NOT NULL;