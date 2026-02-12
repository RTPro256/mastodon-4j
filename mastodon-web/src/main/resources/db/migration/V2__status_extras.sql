CREATE TABLE media_attachments (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    type VARCHAR(16) NOT NULL,
    url TEXT,
    preview_url TEXT,
    remote_url TEXT,
    meta_json TEXT,
    description TEXT,
    blurhash VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_media_attachments_account_id ON media_attachments (account_id);

CREATE TABLE status_media_attachments (
    status_id BIGINT NOT NULL REFERENCES statuses(id) ON DELETE CASCADE,
    media_attachment_id BIGINT NOT NULL REFERENCES media_attachments(id) ON DELETE CASCADE,
    position INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (status_id, media_attachment_id)
);

CREATE INDEX idx_status_media_status_id ON status_media_attachments (status_id);
CREATE INDEX idx_status_media_attachment_id ON status_media_attachments (media_attachment_id);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    url TEXT
);

CREATE TABLE statuses_tags (
    status_id BIGINT NOT NULL REFERENCES statuses(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (status_id, tag_id)
);

CREATE INDEX idx_statuses_tags_status_id ON statuses_tags (status_id);
CREATE INDEX idx_statuses_tags_tag_id ON statuses_tags (tag_id);

CREATE TABLE mentions (
    id BIGSERIAL PRIMARY KEY,
    status_id BIGINT NOT NULL REFERENCES statuses(id) ON DELETE CASCADE,
    account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
    username VARCHAR(30),
    acct VARCHAR(255),
    url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mentions_status_id ON mentions (status_id);
CREATE INDEX idx_mentions_account_id ON mentions (account_id);

CREATE TABLE polls (
    id BIGSERIAL PRIMARY KEY,
    status_id BIGINT NOT NULL UNIQUE REFERENCES statuses(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ,
    multiple BOOLEAN NOT NULL DEFAULT FALSE,
    votes_count INTEGER NOT NULL DEFAULT 0,
    voters_count INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE poll_options (
    id BIGSERIAL PRIMARY KEY,
    poll_id BIGINT NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    votes_count INTEGER NOT NULL DEFAULT 0,
    position INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_poll_options_poll_id ON poll_options (poll_id);

CREATE TABLE poll_votes (
    id BIGSERIAL PRIMARY KEY,
    poll_id BIGINT NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    poll_option_id BIGINT NOT NULL REFERENCES poll_options(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_poll_votes_poll_id ON poll_votes (poll_id);
CREATE INDEX idx_poll_votes_account_id ON poll_votes (account_id);
