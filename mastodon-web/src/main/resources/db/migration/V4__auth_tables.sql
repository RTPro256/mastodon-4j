ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS scopes TEXT;

CREATE TABLE oauth_access_tokens (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    scopes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_oauth_access_tokens_application_id ON oauth_access_tokens (application_id);
CREATE INDEX idx_oauth_access_tokens_user_id ON oauth_access_tokens (user_id);
CREATE INDEX idx_oauth_access_tokens_expires_at ON oauth_access_tokens (expires_at);

CREATE TABLE oauth_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    access_token_id BIGINT NOT NULL REFERENCES oauth_access_tokens(id) ON DELETE CASCADE,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    scopes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_oauth_refresh_tokens_application_id ON oauth_refresh_tokens (application_id);
CREATE INDEX idx_oauth_refresh_tokens_user_id ON oauth_refresh_tokens (user_id);
CREATE INDEX idx_oauth_refresh_tokens_expires_at ON oauth_refresh_tokens (expires_at);

CREATE TABLE oauth_authorization_codes (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code VARCHAR(255) NOT NULL UNIQUE,
    redirect_uri TEXT,
    scopes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_oauth_authorization_codes_application_id ON oauth_authorization_codes (application_id);
CREATE INDEX idx_oauth_authorization_codes_user_id ON oauth_authorization_codes (user_id);
CREATE INDEX idx_oauth_authorization_codes_expires_at ON oauth_authorization_codes (expires_at);

CREATE TABLE sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    data_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
);

CREATE INDEX idx_sessions_user ON sessions (user_id);
CREATE INDEX idx_sessions_expires ON sessions (expires_at);

CREATE TABLE rate_limits (
    rate_key VARCHAR(128) PRIMARY KEY,
    window_start TIMESTAMPTZ NOT NULL,
    count BIGINT NOT NULL
);

CREATE INDEX idx_rate_limits_window_start ON rate_limits (window_start);
