-- V4: Refactor users table to match ER diagram
-- Add new columns
ALTER TABLE users ADD COLUMN role_id UUID;
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;
ALTER TABLE users ADD COLUMN password_updated_at TIMESTAMP;

-- Migrate existing role strings to role_id FK
UPDATE users u SET role_id = (SELECT r.id FROM roles r WHERE r.name = u.role);

-- Make role_id NOT NULL after migration
ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES roles(id);

-- Drop old columns and constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_role;
ALTER TABLE users DROP COLUMN role;
ALTER TABLE users DROP COLUMN user_id;

-- Rename uuid to id
ALTER TABLE users RENAME COLUMN uuid TO id;
