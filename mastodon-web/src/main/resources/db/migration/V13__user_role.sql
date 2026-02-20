-- Add role field to users table for RBAC

ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(32) NOT NULL DEFAULT 'USER';

CREATE INDEX idx_users_role ON users(role);

-- Add comment to document valid roles
COMMENT ON COLUMN users.role IS 'Valid values: USER, MODERATOR, ADMIN';
