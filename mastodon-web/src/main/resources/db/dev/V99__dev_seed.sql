-- Development seed data (dev profile only)
INSERT INTO accounts (username, domain, acct, display_name, locked, bot, note, url, avatar_url, header_url)
SELECT 'admin', NULL, 'admin@local', 'Admin', FALSE, FALSE, 'Local admin account',
       'http://localhost:8080/@admin', NULL, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM accounts WHERE acct = 'admin@local'
);

INSERT INTO users (account_id, email, password_hash, locale)
SELECT id, 'admin@local', 'changeme', 'en'
FROM accounts
WHERE username = 'admin' AND domain IS NULL
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@local');

INSERT INTO statuses (account_id, content, visibility)
SELECT id, '<p>Welcome to Mastodon Java</p>', 'PUBLIC'
FROM accounts
WHERE username = 'admin' AND domain IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM statuses
      WHERE content = '<p>Welcome to Mastodon Java</p>'
        AND account_id = (SELECT id FROM accounts WHERE username = 'admin' AND domain IS NULL)
  );
