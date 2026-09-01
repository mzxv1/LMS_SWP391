-- ============================================================
-- Migration: Email-confirmation self-registration
-- Thư mục: database/migration-email-verification.sql
--
-- Additive only - safe to run against an existing lms_db with data.
-- Do NOT run schema.sql to apply this; schema.sql drops every table.
--
-- Registration becomes a two-step flow: submitting the form no longer
-- inserts into "users" directly. Instead the (already password-hashed)
-- form data is held in this table under a single-use token, and the
-- user row is only created once the emailed confirmation link is
-- clicked - see UserService.registerPending()/confirmRegistration().
-- ============================================================

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id            SERIAL PRIMARY KEY,
    token_hash    VARCHAR(64)  NOT NULL UNIQUE,  -- SHA-256 hex, never the raw token
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    expires_at    TIMESTAMP    NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evt_email ON email_verification_tokens(email);
