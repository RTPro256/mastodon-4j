CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(30) NOT NULL,
    domain VARCHAR(255),
    acct VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    bot BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    note TEXT,
    url TEXT,
    avatar_url TEXT,
    header_url TEXT,
    followers_count INTEGER NOT NULL DEFAULT 0,
    following_count INTEGER NOT NULL DEFAULT 0,
    statuses_count INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_accounts_acct ON accounts (acct);
CREATE UNIQUE INDEX idx_accounts_username_domain ON accounts (username, domain);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE REFERENCES accounts(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    locale VARCHAR(10),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_sign_in_at TIMESTAMPTZ
);

CREATE TABLE statuses (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    in_reply_to_id BIGINT,
    in_reply_to_account_id BIGINT,
    sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    spoiler_text TEXT,
    visibility VARCHAR(16) NOT NULL,
    language VARCHAR(10),
    uri TEXT,
    url TEXT,
    content TEXT NOT NULL,
    reblog_of_id BIGINT REFERENCES statuses(id) ON DELETE SET NULL
);

CREATE INDEX idx_statuses_account_id ON statuses (account_id);
CREATE INDEX idx_statuses_created_at ON statuses (created_at);
CREATE INDEX idx_statuses_in_reply_to_id ON statuses (in_reply_to_id);

CREATE TABLE follows (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    target_account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, target_account_id)
);

CREATE INDEX idx_follows_account_id ON follows (account_id);
CREATE INDEX idx_follows_target_account_id ON follows (target_account_id);
