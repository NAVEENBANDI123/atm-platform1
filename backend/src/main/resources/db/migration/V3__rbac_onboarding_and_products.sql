-- =====================================================================
-- ATM Platform - Enterprise upgrade (PostgreSQL)
-- Phase 0 foundation: 6-role RBAC, dual-portal identity, customer
-- onboarding/approval, cards, loans, deposits, beneficiaries, nominees,
-- service requests, complaints, notifications, scheduled transfers,
-- branches, login history, and audit old/new value capture.
--
-- This migration is ADDITIVE and backward compatible: existing rows get
-- sensible defaults so the current application keeps working while the new
-- modules are layered in (see docs/07-DATABASE-DESIGN.md).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. New roles (6-role model). ROLE_ADMIN / ROLE_CUSTOMER already seeded in V2.
-- ---------------------------------------------------------------------
INSERT INTO roles (name) VALUES
    ('ROLE_SUPER_ADMIN'),
    ('ROLE_ACCOUNTANT'),
    ('ROLE_CASHIER'),
    ('ROLE_LOAN_OFFICER'),
    ('ROLE_CARD_OFFICER')
ON CONFLICT (name) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. Branches
-- ---------------------------------------------------------------------
CREATE TABLE branches (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(12)  NOT NULL UNIQUE,
    name        VARCHAR(120) NOT NULL,
    address     VARCHAR(255),
    city        VARCHAR(80),
    state       VARCHAR(80),
    ifsc        VARCHAR(11)  UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO branches (code, name, address, city, state, ifsc)
VALUES ('BR0001', 'Main Branch', '1 Banking Street', 'Metropolis', 'State', 'ATMB0000001');

-- ---------------------------------------------------------------------
-- 3. Extend users: identity type, lifecycle status, home branch
-- ---------------------------------------------------------------------
ALTER TABLE users ADD COLUMN user_type VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';
ALTER TABLE users ADD COLUMN status    VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN branch_id BIGINT REFERENCES branches(id);

CREATE INDEX idx_users_status    ON users(status);
CREATE INDEX idx_users_user_type ON users(user_type);

-- Existing admin becomes an EMPLOYEE; existing customer stays CUSTOMER (default).
UPDATE users SET user_type = 'EMPLOYEE' WHERE username = 'admin';

-- ---------------------------------------------------------------------
-- 4. Seed a SUPER_ADMIN (password: "password" - change in any real env)
-- ---------------------------------------------------------------------
INSERT INTO users (username, email, mobile, password_hash, full_name,
                   enabled, account_locked, user_type, status)
VALUES ('superadmin', 'superadmin@atm.local', '9000000010',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'Super Administrator', TRUE, FALSE, 'EMPLOYEE', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'superadmin' AND r.name = 'ROLE_SUPER_ADMIN';

-- Also grant the legacy admin the SUPER_ADMIN role (alias during transition).
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------------
-- 5. Profiles (1:1 with users)
-- ---------------------------------------------------------------------
CREATE TABLE customer_profiles (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    customer_id   VARCHAR(20) UNIQUE,
    prefix        VARCHAR(5),
    first_name    VARCHAR(60) NOT NULL,
    middle_name   VARCHAR(60),
    last_name     VARCHAR(60) NOT NULL,
    gender        VARCHAR(10),
    date_of_birth DATE        NOT NULL,
    aadhaar       VARCHAR(12) NOT NULL UNIQUE CHECK (aadhaar ~ '^[0-9]{12}$'),
    pan           VARCHAR(10) NOT NULL UNIQUE CHECK (pan ~ '^[A-Z]{5}[0-9]{4}[A-Z]$'),
    house_number  VARCHAR(40),
    street        VARCHAR(120),
    city          VARCHAR(80),
    state         VARCHAR(80),
    country       VARCHAR(80),
    postal_code   VARCHAR(12),
    kyc_status    VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_account_type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS',
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_customer_profiles_customer_id ON customer_profiles(customer_id);
CREATE INDEX idx_customer_profiles_kyc         ON customer_profiles(kyc_status);

CREATE TABLE employee_profiles (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    employee_code       VARCHAR(20) UNIQUE,
    designation         VARCHAR(60),
    department          VARCHAR(60),
    created_by_admin_id BIGINT      REFERENCES users(id),
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 6. Extend accounts: account type, transfer limit, opened_at
-- ---------------------------------------------------------------------
ALTER TABLE accounts ADD COLUMN account_type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS';
ALTER TABLE accounts ADD COLUMN daily_transfer_limit NUMERIC(19,2) NOT NULL DEFAULT 100000.00;
ALTER TABLE accounts ADD COLUMN opened_at TIMESTAMP;
CREATE INDEX idx_accounts_type ON accounts(account_type);

-- ---------------------------------------------------------------------
-- 7. Card workflow
-- ---------------------------------------------------------------------
CREATE TABLE card_applications (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id    BIGINT      NOT NULL REFERENCES accounts(id),
    card_type     VARCHAR(10) NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by   BIGINT      REFERENCES users(id),
    review_note   VARCHAR(500),
    approved_by   BIGINT      REFERENCES users(id),
    reject_reason VARCHAR(500),
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_card_applications_status ON card_applications(status);
CREATE INDEX idx_card_applications_user   ON card_applications(user_id);

CREATE TABLE cards (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT      NOT NULL UNIQUE REFERENCES card_applications(id),
    account_id      BIGINT      NOT NULL REFERENCES accounts(id),
    card_number     VARCHAR(19) UNIQUE,
    masked_number   VARCHAR(19),
    card_type       VARCHAR(10) NOT NULL,
    expiry_date     DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 8. Loan workflow
-- ---------------------------------------------------------------------
CREATE TABLE loan_applications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    loan_type       VARCHAR(20)   NOT NULL,
    amount          NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    tenure_months   INT           NOT NULL CHECK (tenure_months > 0),
    monthly_income  NUMERIC(19,2) NOT NULL,
    employment_type VARCHAR(60),
    employer_name   VARCHAR(120),
    purpose         VARCHAR(500),
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    reviewed_by     BIGINT        REFERENCES users(id),
    review_note     VARCHAR(500),
    approved_by     BIGINT        REFERENCES users(id),
    reject_reason   VARCHAR(500),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);
CREATE INDEX idx_loan_applications_user   ON loan_applications(user_id);

CREATE TABLE loan_accounts (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT        NOT NULL UNIQUE REFERENCES loan_applications(id),
    account_id      BIGINT        NOT NULL REFERENCES accounts(id),
    loan_account_no VARCHAR(20)   NOT NULL UNIQUE,
    principal       NUMERIC(19,2) NOT NULL,
    interest_rate   NUMERIC(5,2)  NOT NULL,
    tenure_months   INT           NOT NULL,
    emi_amount      NUMERIC(19,2) NOT NULL,
    outstanding     NUMERIC(19,2) NOT NULL,
    disbursed_at    TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_schedule (
    id              BIGSERIAL PRIMARY KEY,
    loan_account_id BIGINT        NOT NULL REFERENCES loan_accounts(id) ON DELETE CASCADE,
    installment_no  INT           NOT NULL,
    due_date        DATE          NOT NULL,
    emi_amount      NUMERIC(19,2) NOT NULL,
    principal_part  NUMERIC(19,2) NOT NULL,
    interest_part   NUMERIC(19,2) NOT NULL,
    balance         NUMERIC(19,2) NOT NULL,
    paid            BOOLEAN       NOT NULL DEFAULT FALSE,
    paid_at         TIMESTAMP,
    UNIQUE (loan_account_id, installment_no)
);

-- ---------------------------------------------------------------------
-- 9. Beneficiaries, nominees, deposits (FD/RD)
-- ---------------------------------------------------------------------
CREATE TABLE beneficiaries (
    id               BIGSERIAL PRIMARY KEY,
    owner_user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    nickname         VARCHAR(60),
    account_number   VARCHAR(20)  NOT NULL,
    beneficiary_name VARCHAR(120) NOT NULL,
    bank_name        VARCHAR(120),
    ifsc             VARCHAR(11),
    verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (owner_user_id, account_number)
);

CREATE TABLE nominees (
    id            BIGSERIAL PRIMARY KEY,
    account_id    BIGINT       NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    name          VARCHAR(120) NOT NULL,
    relationship  VARCHAR(40),
    date_of_birth DATE,
    share_percent NUMERIC(5,2) DEFAULT 100.00,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE deposits (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id      BIGINT        REFERENCES accounts(id),
    deposit_type    VARCHAR(20)   NOT NULL,
    principal       NUMERIC(19,2) NOT NULL CHECK (principal > 0),
    interest_rate   NUMERIC(5,2)  NOT NULL,
    tenure_months   INT           NOT NULL,
    monthly_amount  NUMERIC(19,2),
    maturity_date   DATE,
    maturity_amount NUMERIC(19,2),
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 10. Service requests, complaints, notifications, scheduled transfers
-- ---------------------------------------------------------------------
CREATE TABLE service_requests (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    request_type VARCHAR(30) NOT NULL,
    payload      JSONB,
    status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    handled_by   BIGINT      REFERENCES users(id),
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject      VARCHAR(160) NOT NULL,
    description  VARCHAR(2000),
    status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    assigned_to  BIGINT       REFERENCES users(id),
    resolution   VARCHAR(2000),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(160) NOT NULL,
    body        VARCHAR(1000),
    read        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read);

CREATE TABLE scheduled_transfers (
    id                BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT        NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    beneficiary_id    BIGINT        NOT NULL REFERENCES beneficiaries(id),
    amount            NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    frequency         VARCHAR(20)   NOT NULL,
    next_run_date     DATE          NOT NULL,
    active            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE login_history (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    username    VARCHAR(50),
    event       VARCHAR(20)  NOT NULL,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_login_history_user ON login_history(user_id);

-- ---------------------------------------------------------------------
-- 11. Audit: capture old/new value
-- ---------------------------------------------------------------------
ALTER TABLE audit_logs ADD COLUMN old_value VARCHAR(2000);
ALTER TABLE audit_logs ADD COLUMN new_value VARCHAR(2000);
