-- ============================================================
-- Migration: Google Sign-In + Self-service Password Reset
-- Thư mục: database/migration-auth-features.sql
--
-- Additive only - safe to run against an existing lms_db with data.
-- Do NOT run schema.sql to apply this; schema.sql drops every table.
--
-- No change to the "users" table: Google accounts are linked by
-- verified email, so no google_id column is required.
-- ============================================================

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         SERIAL PRIMARY KEY,
    user_id    INTEGER     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,   -- SHA-256 hex, never the raw token
    expires_at TIMESTAMP   NOT NULL,
    used_at    TIMESTAMP,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_prt_user ON password_reset_tokens(user_id);
