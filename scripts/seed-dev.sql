-- Development seed data (run manually)
INSERT INTO accounts (username, domain, acct, display_name, locked, bot, note, url, avatar_url, header_url)
VALUES ('admin', NULL, 'admin@local', 'Admin', FALSE, FALSE, 'Local admin account', 'http://localhost:8080/@admin', NULL, NULL);

INSERT INTO users (account_id, email, password_hash, locale)
VALUES ((SELECT id FROM accounts WHERE username = 'admin' AND domain IS NULL), 'admin@local', 'changeme', 'en');

INSERT INTO statuses (account_id, content, visibility)
VALUES ((SELECT id FROM accounts WHERE username = 'admin' AND domain IS NULL), '<p>Welcome to Mastodon Java</p>', 'PUBLIC');
