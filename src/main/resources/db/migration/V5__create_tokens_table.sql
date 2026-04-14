-- V5: Create tokens table as per ER diagram
-- Create custom type for token status
CREATE TYPE token_status AS ENUM ('active', 'used', 'revoked', 'expired');

-- Create tokens table
CREATE TABLE IF NOT EXISTS tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(1024) UNIQUE NOT NULL,
    status token_status NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    token_type_id UUID NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_token_type_id FOREIGN KEY (token_type_id) REFERENCES token_types(id)
);
