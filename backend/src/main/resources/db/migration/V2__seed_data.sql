-- =====================================================================
-- Seed data
-- NOTE: password_hash below is the BCrypt hash of the password "password"
--       (the well-known Spring Security reference hash). Change these
--       credentials immediately in any real environment.
-- =====================================================================

INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_CUSTOMER');

-- Admin user (password: password)
INSERT INTO users (username, email, mobile, password_hash, full_name, enabled, account_locked)
VALUES ('admin', 'admin@atm.local', '9000000001',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'System Administrator', TRUE, FALSE);

-- Demo customer (password: password)
INSERT INTO users (username, email, mobile, password_hash, full_name, enabled, account_locked)
VALUES ('jdoe', 'jdoe@atm.local', '9000000002',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'John Doe', TRUE, FALSE);

-- Role assignments
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin'    AND r.name = 'ROLE_ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'jdoe'     AND r.name = 'ROLE_CUSTOMER';

-- Accounts
INSERT INTO accounts (account_number, user_id, balance, currency, status)
SELECT '1000000001', u.id, 5000.00, 'USD', 'ACTIVE' FROM users u WHERE u.username = 'jdoe';
INSERT INTO accounts (account_number, user_id, balance, currency, status)
SELECT '1000000000', u.id, 0.00, 'USD', 'ACTIVE' FROM users u WHERE u.username = 'admin';
