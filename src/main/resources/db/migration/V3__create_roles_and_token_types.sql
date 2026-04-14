-- V3: Create roles and token_types tables
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS token_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Seed initial data
INSERT INTO roles (name) VALUES ('OWNER'), ('ADMIN'), ('EMPLOYEE');
INSERT INTO token_types (name) VALUES ('ACCESS'), ('REFRESH');
