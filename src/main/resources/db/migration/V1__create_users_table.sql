-- V1: Create users table
CREATE TABLE IF NOT EXISTS users (
    uuid         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      VARCHAR(255) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    is_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    role         VARCHAR(50)  NOT NULL DEFAULT 'OWNER',
    organization_id VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

ALTER TABLE users ADD CONSTRAINT chk_role
    CHECK (role IN ('OWNER', 'ADMIN', 'EMPLOYEE'));
