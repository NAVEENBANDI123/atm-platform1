# Phase 3 — ER Diagram & Normalized Design

```
┌──────────────┐        ┌────────────────┐        ┌────────────────────┐
│    roles     │        │     users      │        │      accounts      │
├──────────────┤        ├────────────────┤        ├────────────────────┤
│ id (PK)      │◄──┐    │ id (PK)        │◄──┐     │ id (PK)            │
│ name (UQ)    │   │    │ username (UQ)  │   │     │ account_number(UQ) │
└──────────────┘   │    │ email (UQ)     │   └────►│ user_id (FK)       │
                   │    │ mobile (UQ)    │         │ balance NUMERIC    │
┌──────────────┐   │    │ password_hash  │         │ status             │
│  user_roles  │   │    │ full_name      │         │ currency           │
├──────────────┤   │    │ failed_attempts│         │ version (optimistic)│
│ user_id (FK) │───┘    │ account_locked │         └─────────┬──────────┘
│ role_id (FK) │───┐    │ enabled        │                   │
└──────────────┘   └───►│ created_at ... │                   │
                        └───────┬────────┘                   │
                                │                             │
                   ┌────────────▼─────────┐      ┌────────────▼───────────┐
                   │    refresh_tokens    │      │     transactions       │
                   ├──────────────────────┤      ├────────────────────────┤
                   │ id (PK)              │      │ id (PK)                │
                   │ user_id (FK)         │      │ reference (UQ)         │
                   │ token (UQ)           │      │ account_id (FK)        │
                   │ expiry_date          │      │ counterparty_account_id│
                   │ revoked              │      │ type / status          │
                   └──────────────────────┘      │ amount / balance_after │
                                                  │ description / created  │
                                                  └────────────────────────┘
┌────────────────────────────────────────────┐
│                 audit_logs                   │
├──────────────────────────────────────────────┤
│ id (PK) · username · action · entity_type    │
│ entity_id · details · ip_address · created_at │
└──────────────────────────────────────────────┘
```

## Normalization
- **3NF**: users (identity), accounts (financial), transactions (ledger) are separated; roles via M:N `user_roles`.
- Money: `NUMERIC(19,2)` (never floating point / int cents loss).
- Integrity: FKs with `ON DELETE` rules, unique constraints (username/email/mobile/account_number/reference), `CHECK (balance >= 0)`, `CHECK (amount > 0)`.
- Concurrency: `accounts.version` for optimistic locking; transfers use ordered locking to avoid deadlocks.
- Indexes: on FKs and frequent lookups (`account_number`, `transactions.account_id`, `transactions.created_at`).
- Auditability: append-only `transactions` ledger + `audit_logs` for security events.
