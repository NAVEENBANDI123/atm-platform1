-- =====================================================================
-- V4 - Final operational hardening:
--      * email_outbox for the asynchronous mail pipeline
--      * Customer onboarding lifecycle on customer_profiles
--      * rejection reasons / approval timestamps on workflow tables
--      * audit_logs.user_role for role-aware audit reporting
--      * sequences for human-readable customer / employee identifiers
--      * extra demo employees so every role has a working seed account
-- This migration is purely additive and idempotent-friendly so existing
-- deployments keep working.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Customer onboarding lifecycle
-- ---------------------------------------------------------------------
ALTER TABLE customer_profiles
    ADD COLUMN IF NOT EXISTS customer_status     VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL',
    ADD COLUMN IF NOT EXISTS rejection_reason    VARCHAR(500),
    ADD COLUMN IF NOT EXISTS approved_at         TIMESTAMP,
    ADD COLUMN IF NOT EXISTS approved_by         BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS submitted_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS area                VARCHAR(120);

CREATE INDEX IF NOT EXISTS idx_customer_profiles_status ON customer_profiles(customer_status);

-- The columns added in V3 use NOT NULL on aadhaar/pan and DOB.  Existing
-- rows (admin/customer seeded earlier) lack a profile row so this is safe.

-- Backfill: any pre-V4 customer rows considered already approved.
UPDATE customer_profiles SET customer_status = 'APPROVED' WHERE customer_status = 'PENDING_APPROVAL'
    AND created_at < CURRENT_TIMESTAMP - INTERVAL '1 day';

-- ---------------------------------------------------------------------
-- 2. Card workflow extra fields
-- ---------------------------------------------------------------------
ALTER TABLE cards
    ADD COLUMN IF NOT EXISTS daily_limit  NUMERIC(19,2) NOT NULL DEFAULT 50000.00,
    ADD COLUMN IF NOT EXISTS cvv_hash     VARCHAR(100);

ALTER TABLE card_applications
    ADD COLUMN IF NOT EXISTS approved_at  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS rejected_at  TIMESTAMP;

-- ---------------------------------------------------------------------
-- 3. Loan workflow extra fields
-- ---------------------------------------------------------------------
ALTER TABLE loan_applications
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP;

-- ---------------------------------------------------------------------
-- 4. Audit log: capture actor role
-- ---------------------------------------------------------------------
ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS user_role VARCHAR(40);

-- ---------------------------------------------------------------------
-- 5. Email outbox (asynchronous email pipeline)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS email_outbox (
    id           BIGSERIAL PRIMARY KEY,
    to_address   VARCHAR(255)  NOT NULL,
    cc_addresses VARCHAR(1000),
    subject      VARCHAR(255)  NOT NULL,
    body         TEXT          NOT NULL,
    template     VARCHAR(80),
    status       VARCHAR(20)   NOT NULL DEFAULT 'QUEUED',
    attempts     INT           NOT NULL DEFAULT 0,
    last_error   VARCHAR(1000),
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at      TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_email_outbox_status ON email_outbox(status);

-- ---------------------------------------------------------------------
-- 6. Sequences for human-readable identifiers
-- ---------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS customer_id_seq  START WITH 100001;
CREATE SEQUENCE IF NOT EXISTS employee_code_seq START WITH 9001;

-- ---------------------------------------------------------------------
-- 7. Demo employees so every role has a real login during dev/QA.
--    All passwords are the well-known BCrypt hash of "password".
-- ---------------------------------------------------------------------
INSERT INTO users (username, email, mobile, password_hash, full_name,
                   enabled, account_locked, user_type, status)
VALUES
    ('accountant',  'accountant@atm.local',  '9000000020',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Anita Accountant',  TRUE, FALSE, 'EMPLOYEE', 'ACTIVE'),
    ('cashier',     'cashier@atm.local',     '9000000021',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Carlos Cashier',    TRUE, FALSE, 'EMPLOYEE', 'ACTIVE'),
    ('cardofficer', 'cardofficer@atm.local', '9000000022',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Carla Card Officer', TRUE, FALSE, 'EMPLOYEE', 'ACTIVE'),
    ('loanofficer', 'loanofficer@atm.local', '9000000023',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Leo Loan Officer',  TRUE, FALSE, 'EMPLOYEE', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'accountant'  AND r.name = 'ROLE_ACCOUNTANT'
ON CONFLICT DO NOTHING;
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'cashier'     AND r.name = 'ROLE_CASHIER'
ON CONFLICT DO NOTHING;
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'cardofficer' AND r.name = 'ROLE_CARD_OFFICER'
ON CONFLICT DO NOTHING;
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'loanofficer' AND r.name = 'ROLE_LOAN_OFFICER'
ON CONFLICT DO NOTHING;

-- Each demo employee gets an employee_profile row so the dashboards
-- can render employee_code / department.
INSERT INTO employee_profiles (user_id, employee_code, designation, department)
SELECT u.id, 'EMP' || LPAD(NEXTVAL('employee_code_seq')::TEXT, 5, '0'),
       'Senior ' || INITCAP(REPLACE(u.username, 'officer', ' Officer')),
       'Operations'
FROM users u
WHERE u.user_type = 'EMPLOYEE'
  AND NOT EXISTS (SELECT 1 FROM employee_profiles ep WHERE ep.user_id = u.id);

-- ---------------------------------------------------------------------
-- 8. Helper: a default branch reference for legacy users (so reports work).
-- ---------------------------------------------------------------------
UPDATE users SET branch_id = (SELECT id FROM branches WHERE code = 'BR0001')
WHERE branch_id IS NULL;
