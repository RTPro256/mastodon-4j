CREATE TABLE favourites (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    status_id BIGINT NOT NULL REFERENCES statuses(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, status_id)
);

CREATE INDEX idx_favourites_account_id ON favourites (account_id);
CREATE INDEX idx_favourites_status_id ON favourites (status_id);

CREATE TABLE bookmarks (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    status_id BIGINT NOT NULL REFERENCES statuses(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, status_id)
);

CREATE INDEX idx_bookmarks_account_id ON bookmarks (account_id);
CREATE INDEX idx_bookmarks_status_id ON bookmarks (status_id);

CREATE TABLE blocks (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    target_account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, target_account_id)
);

CREATE INDEX idx_blocks_account_id ON blocks (account_id);
CREATE INDEX idx_blocks_target_account_id ON blocks (target_account_id);

CREATE TABLE mutes (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    target_account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (account_id, target_account_id)
);

CREATE INDEX idx_mutes_account_id ON mutes (account_id);
CREATE INDEX idx_mutes_target_account_id ON mutes (target_account_id);
