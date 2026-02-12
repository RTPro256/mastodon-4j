CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    actor_account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    status_id BIGINT REFERENCES statuses(id) ON DELETE SET NULL,
    type VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_account_id ON notifications (account_id);
CREATE INDEX idx_notifications_actor_account_id ON notifications (actor_account_id);
CREATE INDEX idx_notifications_status_id ON notifications (status_id);

CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    website TEXT,
    client_id VARCHAR(255) NOT NULL UNIQUE,
    client_secret VARCHAR(255) NOT NULL,
    redirect_uri TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE lists (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL
);

CREATE INDEX idx_lists_account_id ON lists (account_id);

CREATE TABLE list_accounts (
    id BIGSERIAL PRIMARY KEY,
    list_id BIGINT NOT NULL REFERENCES lists(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    position INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_list_accounts_list_id ON list_accounts (list_id);
CREATE INDEX idx_list_accounts_account_id ON list_accounts (account_id);

CREATE TABLE filters (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    context TEXT,
    expires_at TIMESTAMPTZ,
    filter_action VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_filters_account_id ON filters (account_id);

CREATE TABLE filter_keywords (
    id BIGSERIAL PRIMARY KEY,
    filter_id BIGINT NOT NULL REFERENCES filters(id) ON DELETE CASCADE,
    keyword TEXT NOT NULL,
    whole_word BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_filter_keywords_filter_id ON filter_keywords (filter_id);

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    target_account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    comment TEXT,
    action_taken BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_account_id ON reports (account_id);
CREATE INDEX idx_reports_target_account_id ON reports (target_account_id);

CREATE TABLE report_statuses (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    status_id BIGINT NOT NULL REFERENCES statuses(id) ON DELETE CASCADE,
    UNIQUE (report_id, status_id)
);

CREATE INDEX idx_report_statuses_report_id ON report_statuses (report_id);
CREATE INDEX idx_report_statuses_status_id ON report_statuses (status_id);
