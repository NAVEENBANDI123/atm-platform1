# 07 — Updated Database Design

Extends the V1 schema (`users`, `roles`, `user_roles`, `accounts`, `transactions`,
`refresh_tokens`, `audit_logs`) with the entities required for onboarding, RBAC,
cards, loans, beneficiaries, deposits (FD/RD), KYC, complaints, branches, and
notifications. PostgreSQL 16, Flyway-managed, 3NF, `NUMERIC(19,2)` money.

---

## 1. ER diagram (target)

```
                                  ┌───────────┐
                                  │  branches │
                                  └─────┬─────┘
                                        │ 1
            ┌───────────────────────────┼───────────────────────────┐
            │ N                          │ N                          │
      ┌─────▼──────┐   M:N         ┌─────▼───────────┐         ┌──────▼───────────┐
      │   roles    │◄────user_roles┤      users      ├────────►│ employee_profiles│ (1:1, EMPLOYEE)
      └────────────┘               │  id, username   │ 1     1 └──────────────────┘
                                   │  email, mobile  │
                                   │  password_hash  │ 1     1 ┌──────────────────┐
                                   │  user_type      ├────────►│ customer_profiles│ (1:1, CUSTOMER)
                                   │  status         │         │  prefix,names,dob│
                                   │  enabled,locked │         │  gender,aadhaar  │
                                   └───┬───┬───┬─────┘         │  pan, customer_id│
                                       │   │   │               │  address fields  │
                 ┌─────────────────────┘   │   └───────────────┤  kyc_status      │
                 │ 1:N                      │ 1:N               └────────┬─────────┘
          ┌──────▼───────┐          ┌───────▼────────┐                  │ 1:N
          │ refresh_tokens│         │   audit_logs   │           ┌──────▼───────┐
          └──────────────┘          └────────────────┘           │  accounts    │
                                                                 │ account_no   │
   ┌─────────────────────────────────────────────────────────── │ type,balance │
   │            │              │             │            │      │ status,ver   │
   │ 1:N        │ 1:N          │ 1:N         │ 1:N        │ 1:N  └──────┬───────┘
┌──▼────────┐ ┌─▼──────────┐ ┌─▼─────────┐ ┌▼──────────┐ ┌▼────────┐  │ 1:N
│beneficiar.│ │card_apps   │ │loan_apps  │ │deposits   │ │nominees │  │
└───────────┘ │  │ 1:1      │ │  │ 1:1     │ │(FD/RD)   │ └─────────┘  │
              ┌▼─────────┐  ┌▼──────────┐  └───────────┘         ┌─────▼────────┐
              │  cards   │  │loan_accts │                        │ transactions │
              └──────────┘  │   │ 1:N     │                        └──────────────┘
                            ┌▼────────────┐
                            │loan_schedule│
                            └─────────────┘

   complaints (N:1 users)      notifications (N:1 users)      login_history (N:1 users)
```

Relationship summary:

| Parent | Child | Card. | Notes |
|--------|-------|-------|-------|
| branches | users | 1:N | every staff/customer belongs to a home branch |
| users | customer_profiles | 1:1 | only for `user_type = CUSTOMER` |
| users | employee_profiles | 1:1 | only for `user_type = EMPLOYEE` |
| users | accounts | 1:N | a customer may hold multiple account types |
| users | beneficiaries | 1:N | payees added by a customer |
| users | card_applications | 1:N | each yields at most one issued card |
| card_applications | cards | 1:1 | card created only on final approval |
| users | loan_applications | 1:N | each yields at most one loan account |
| loan_applications | loan_accounts | 1:1 | created on sanction |
| loan_accounts | loan_schedule | 1:N | EMI rows |
| accounts | transactions | 1:N | append-only ledger |
| accounts | nominees | 1:N | nominee per account |
| users | deposits | 1:N | FD / RD requests + accounts |
| users | complaints | 1:N | ticket system |
| users | notifications | 1:N | in-app notification center |
| users | login_history | 1:N | login/logout audit feed |

---

## 2. Enumerations

| Enum | Values |
|------|--------|
| `RoleName` | `ROLE_SUPER_ADMIN`, `ROLE_ACCOUNTANT`, `ROLE_CASHIER`, `ROLE_LOAN_OFFICER`, `ROLE_CARD_OFFICER`, `ROLE_CUSTOMER` (legacy `ROLE_ADMIN` retained → alias of SUPER_ADMIN) |
| `UserType` | `CUSTOMER`, `EMPLOYEE` |
| `UserStatus` | `PENDING_APPROVAL`, `ACTIVE`, `REJECTED`, `DISABLED` |
| `AccountType` | `SAVINGS`, `SALARY`, `CURRENT`, `LOAN`, `FIXED_DEPOSIT`, `RECURRING_DEPOSIT` |
| `AccountStatus` | `ACTIVE`, `INACTIVE`, `FROZEN`, `CLOSED` |
| `Gender` | `MALE`, `FEMALE`, `OTHER` |
| `Prefix` | `MR`, `MRS`, `MS`, `DR` |
| `KycStatus` | `PENDING`, `VERIFIED`, `REJECTED` |
| `WorkflowStatus` | `PENDING`, `UNDER_REVIEW`, `RECOMMENDED`, `APPROVED`, `REJECTED` |
| `CardType` | `DEBIT`, `CREDIT` |
| `CardStatus` | `PENDING`, `ACTIVE`, `BLOCKED`, `EXPIRED`, `REJECTED` |
| `LoanType` | `PERSONAL`, `VEHICLE`, `EDUCATION`, `HOME` |
| `DepositType` | `FIXED`, `RECURRING` |
| `TransactionType` | `DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`, `LOAN_DISBURSAL`, `EMI`, `INTEREST`, `FEE` |
| `TransactionStatus` | `PENDING`, `COMPLETED`, `FAILED`, `REVERSED` |
| `ComplaintStatus` | `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED` |
| `RequestType` | `CHEQUE_BOOK`, `ADDRESS_CHANGE`, `MOBILE_UPDATE` |

---

## 3. New / changed tables (DDL highlights)

> Full executable DDL ships as Flyway `V3__rbac_onboarding_and_products.sql` (see
> `backend/src/main/resources/db/migration`). The snippets below document intent,
> constraints, and indexes.

### 3.1 `users` (extended — additive columns)
```sql
ALTER TABLE users ADD COLUMN user_type   VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER';
ALTER TABLE users ADD COLUMN status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN branch_id    BIGINT      REFERENCES branches(id);
-- username/email/mobile remain UNIQUE; PENDING_APPROVAL users cannot authenticate.
CREATE INDEX idx_users_status    ON users(status);
CREATE INDEX idx_users_user_type ON users(user_type);
```

### 3.2 `branches`
```sql
CREATE TABLE branches (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(12)  NOT NULL UNIQUE,        -- IFSC-like
    name        VARCHAR(120) NOT NULL,
    address     VARCHAR(255),
    city        VARCHAR(80),
    state       VARCHAR(80),
    ifsc        VARCHAR(11)  UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.3 `customer_profiles`
```sql
CREATE TABLE customer_profiles (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    customer_id   VARCHAR(20) UNIQUE,                -- generated on approval
    prefix        VARCHAR(5),                        -- MR/MRS/MS/DR
    first_name    VARCHAR(60) NOT NULL,
    middle_name   VARCHAR(60),
    last_name     VARCHAR(60) NOT NULL,
    gender        VARCHAR(10),
    date_of_birth DATE        NOT NULL,
    aadhaar       VARCHAR(12) NOT NULL UNIQUE CHECK (aadhaar ~ '^[0-9]{12}$'),
    pan           VARCHAR(10) NOT NULL UNIQUE CHECK (pan ~ '^[A-Z]{5}[0-9]{4}[A-Z]$'),
    -- address
    house_number  VARCHAR(40),
    street        VARCHAR(120),
    city          VARCHAR(80),
    state         VARCHAR(80),
    country       VARCHAR(80),
    postal_code   VARCHAR(12),
    kyc_status    VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_account_type VARCHAR(20) NOT NULL,     -- SAVINGS/SALARY/CURRENT
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_customer_profiles_customer_id ON customer_profiles(customer_id);
CREATE INDEX idx_customer_profiles_kyc         ON customer_profiles(kyc_status);
```

### 3.4 `employee_profiles`
```sql
CREATE TABLE employee_profiles (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    employee_code VARCHAR(20) UNIQUE,
    designation  VARCHAR(60),
    department   VARCHAR(60),
    created_by_admin_id BIGINT REFERENCES users(id),
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.5 `accounts` (extended — additive columns)
```sql
ALTER TABLE accounts ADD COLUMN account_type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS';
ALTER TABLE accounts ADD COLUMN daily_transfer_limit NUMERIC(19,2) NOT NULL DEFAULT 100000.00;
ALTER TABLE accounts ADD COLUMN opened_at TIMESTAMP;
CREATE INDEX idx_accounts_type ON accounts(account_type);
```

### 3.6 Approval-workflow tables (maker-checker)

```sql
-- Card applications
CREATE TABLE card_applications (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id    BIGINT      NOT NULL REFERENCES accounts(id),
    card_type     VARCHAR(10) NOT NULL,                 -- DEBIT/CREDIT
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by   BIGINT      REFERENCES users(id),      -- CARD_OFFICER
    review_note   VARCHAR(500),
    approved_by   BIGINT      REFERENCES users(id),      -- SUPER_ADMIN
    reject_reason VARCHAR(500),
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cards (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT      NOT NULL UNIQUE REFERENCES card_applications(id),
    account_id      BIGINT      NOT NULL REFERENCES accounts(id),
    card_number     VARCHAR(19) UNIQUE,                  -- generated on approval
    masked_number   VARCHAR(19),                         -- **** **** **** 1234
    card_type       VARCHAR(10) NOT NULL,
    expiry_date     DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Loan applications
CREATE TABLE loan_applications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    loan_type       VARCHAR(20)  NOT NULL,               -- PERSONAL/VEHICLE/...
    amount          NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    tenure_months   INT          NOT NULL CHECK (tenure_months > 0),
    monthly_income  NUMERIC(19,2) NOT NULL,
    employment_type VARCHAR(60),
    employer_name   VARCHAR(120),
    purpose         VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reviewed_by     BIGINT       REFERENCES users(id),   -- LOAN_OFFICER
    review_note     VARCHAR(500),
    approved_by     BIGINT       REFERENCES users(id),   -- SUPER_ADMIN
    reject_reason   VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_accounts (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT       NOT NULL UNIQUE REFERENCES loan_applications(id),
    account_id      BIGINT       NOT NULL REFERENCES accounts(id), -- disbursal target
    loan_account_no VARCHAR(20)  NOT NULL UNIQUE,
    principal       NUMERIC(19,2) NOT NULL,
    interest_rate   NUMERIC(5,2)  NOT NULL,              -- annual %
    tenure_months   INT           NOT NULL,
    emi_amount      NUMERIC(19,2) NOT NULL,
    outstanding     NUMERIC(19,2) NOT NULL,
    disbursed_at    TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_schedule (
    id              BIGSERIAL PRIMARY KEY,
    loan_account_id BIGINT       NOT NULL REFERENCES loan_accounts(id) ON DELETE CASCADE,
    installment_no  INT          NOT NULL,
    due_date        DATE         NOT NULL,
    emi_amount      NUMERIC(19,2) NOT NULL,
    principal_part  NUMERIC(19,2) NOT NULL,
    interest_part   NUMERIC(19,2) NOT NULL,
    balance         NUMERIC(19,2) NOT NULL,
    paid            BOOLEAN      NOT NULL DEFAULT FALSE,
    paid_at         TIMESTAMP,
    UNIQUE (loan_account_id, installment_no)
);
```

### 3.7 Beneficiaries, nominees, deposits
```sql
CREATE TABLE beneficiaries (
    id              BIGSERIAL PRIMARY KEY,
    owner_user_id   BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    nickname        VARCHAR(60),
    account_number  VARCHAR(20) NOT NULL,
    beneficiary_name VARCHAR(120) NOT NULL,
    bank_name       VARCHAR(120),
    ifsc            VARCHAR(11),
    verified        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (owner_user_id, account_number)
);

CREATE TABLE nominees (
    id           BIGSERIAL PRIMARY KEY,
    account_id   BIGINT      NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    name         VARCHAR(120) NOT NULL,
    relationship VARCHAR(40),
    date_of_birth DATE,
    share_percent NUMERIC(5,2) DEFAULT 100.00,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE deposits (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id    BIGINT       REFERENCES accounts(id),  -- funding account
    deposit_type  VARCHAR(20)  NOT NULL,                 -- FIXED/RECURRING
    principal     NUMERIC(19,2) NOT NULL CHECK (principal > 0),
    interest_rate NUMERIC(5,2)  NOT NULL,
    tenure_months INT           NOT NULL,
    monthly_amount NUMERIC(19,2),                        -- RD only
    maturity_date DATE,
    maturity_amount NUMERIC(19,2),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3.8 Service requests, complaints, notifications, scheduled transfers
```sql
CREATE TABLE service_requests (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    request_type VARCHAR(30) NOT NULL,                  -- CHEQUE_BOOK/ADDRESS_CHANGE/MOBILE_UPDATE
    payload      JSONB,                                 -- type-specific fields
    status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    handled_by   BIGINT      REFERENCES users(id),
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject      VARCHAR(160) NOT NULL,
    description  VARCHAR(2000),
    status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    assigned_to  BIGINT      REFERENCES users(id),
    resolution   VARCHAR(2000),
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
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
    id              BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT     NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    beneficiary_id  BIGINT       NOT NULL REFERENCES beneficiaries(id),
    amount          NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    frequency       VARCHAR(20)  NOT NULL,              -- ONCE/DAILY/WEEKLY/MONTHLY
    next_run_date   DATE         NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE login_history (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    username    VARCHAR(50),
    event       VARCHAR(20)  NOT NULL,                  -- LOGIN_SUCCESS/LOGIN_FAILED/LOGOUT
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_login_history_user ON login_history(user_id);
```

### 3.9 `audit_logs` (extended for old/new value)
```sql
ALTER TABLE audit_logs ADD COLUMN old_value VARCHAR(2000);
ALTER TABLE audit_logs ADD COLUMN new_value VARCHAR(2000);
```

---

## 4. Indexing strategy

| Goal | Index |
|------|-------|
| Login lookup | existing UNIQUE on `users.username`, `users.email`, `users.mobile` |
| Approval queues | `idx_users_status`, `card_applications(status)`, `loan_applications(status)` |
| Customer lookup | `idx_customer_profiles_customer_id`, UNIQUE `aadhaar`, `pan` |
| Account access | existing `idx_accounts_user_id`, new `idx_accounts_type` |
| Ledger queries | existing `idx_transactions_account_id`, `idx_transactions_created_at` |
| Notification center | `idx_notifications_user_unread` (composite, partial-friendly) |
| Reporting | `idx_transactions_created_at`, add `(type, created_at)` for typed reports |
| Audit | existing `idx_audit_logs_username`, `idx_audit_logs_created_at` |

For reporting at scale, consider partial indexes (e.g. `WHERE status='PENDING'`) and a
read replica; out of scope for V1 rollout but noted.

---

## 5. Integrity & concurrency rules

- **Money:** `NUMERIC(19,2)` everywhere; `CHECK (amount > 0)`, `CHECK (balance >= 0)`.
- **FKs:** `ON DELETE CASCADE` for owned children (profiles, schedule, nominees),
  `ON DELETE SET NULL` for soft references (audit/login history).
- **Optimistic locking:** `accounts.version` guards balance updates; transfers acquire
  account locks in deterministic id order to avoid deadlock.
- **Uniqueness:** `customer_id`, `account_number`, `card_number`, `loan_account_no`,
  `reference`, `aadhaar`, `pan`, branch `code`/`ifsc`.
- **Workflow integrity:** application rows are immutable once `APPROVED`/`REJECTED`
  (enforced in service layer); issued `cards`/`loan_accounts` are created only on final
  Super Admin approval.

---

## 6. Migration ordering

| Version | Contents |
|---------|----------|
| `V1` | base schema (existing) |
| `V2` | base seed (existing) |
| `V3` | **this design**: extend users/accounts/audit, add branches/profiles/cards/loans/deposits/beneficiaries/nominees/requests/complaints/notifications/scheduled_transfers/login_history; seed new roles + super admin + sample branch |
| `V4+` | per-feature refinements (reports views, partial indexes) |

`V3` is **additive and backward compatible**: existing rows get sensible defaults
(`user_type='CUSTOMER'`, `status='ACTIVE'`, `account_type='SAVINGS'`), so the current app
keeps working while new modules are layered in.
