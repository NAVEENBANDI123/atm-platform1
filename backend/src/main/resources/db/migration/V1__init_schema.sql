-- =====================================================================
-- ATM Platform - Initial schema (PostgreSQL)
-- Replaces the legacy single `user` table (plain-text passwords, int money)
-- with a normalized, audited, RBAC-enabled banking schema.
-- =====================================================================

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE users (
    id               BIGSERIAL PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    email            VARCHAR(120) NOT NULL UNIQUE,
    mobile           VARCHAR(20)  NOT NULL UNIQUE,
    password_hash    VARCHAR(100) NOT NULL,
    full_name        VARCHAR(120) NOT NULL,
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    account_locked   BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_attempts  INT          NOT NULL DEFAULT 0,
    lock_time        TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(50),
    updated_by       VARCHAR(50)
);

CREATE TABLE user_roles (
    user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id  BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE accounts (
    id              BIGSERIAL PRIMARY KEY,
    account_number  VARCHAR(20)   NOT NULL UNIQUE,
    user_id         BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    balance         NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    currency        VARCHAR(3)    NOT NULL DEFAULT 'USD',
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    version         BIGINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50)
);

CREATE TABLE transactions (
    id                       BIGSERIAL PRIMARY KEY,
    reference                VARCHAR(40)   NOT NULL UNIQUE,
    account_id               BIGINT        NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    counterparty_account_id  BIGINT        REFERENCES accounts(id) ON DELETE SET NULL,
    type                     VARCHAR(20)   NOT NULL,
    status                   VARCHAR(20)   NOT NULL DEFAULT 'COMPLETED',
    amount                   NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    balance_after            NUMERIC(19,2) NOT NULL,
    description              VARCHAR(255),
    created_at               TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token        VARCHAR(255) NOT NULL UNIQUE,
    expiry_date  TIMESTAMP    NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50),
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(50),
    entity_id    VARCHAR(50),
    details      VARCHAR(1000),
    ip_address   VARCHAR(45),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for frequent lookups
CREATE INDEX idx_accounts_user_id        ON accounts(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_refresh_tokens_user_id  ON refresh_tokens(user_id);
CREATE INDEX idx_audit_logs_username     ON audit_logs(username);
CREATE INDEX idx_audit_logs_created_at   ON audit_logs(created_at);
