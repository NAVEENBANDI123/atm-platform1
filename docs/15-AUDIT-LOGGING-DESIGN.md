# 15 — Audit Logging Design

Extends the existing `audit_logs` table and `AuditService`/`AuditAspect` to capture
**old value / new value** alongside user, action, timestamp, and IP — for every sensitive
action.

---

## 1. What we capture

`audit_logs` columns (V1 + V3 additions):

| Column | Source | Notes |
|--------|--------|-------|
| `username` | `SecurityContext` (or `anonymous`) | acting user |
| `action` | enum string | e.g. `CUSTOMER_APPROVED` |
| `entity_type` | target type | `USER`, `ACCOUNT`, `LOAN`, `CARD`, ... |
| `entity_id` | target id | string |
| `details` | free text | human-readable summary |
| `ip_address` | `X-Forwarded-For` / remote addr | existing logic |
| `old_value` | **NEW** | serialized prior state (masked) |
| `new_value` | **NEW** | serialized new state (masked) |
| `created_at` | now | append-only |

Login/logout events additionally feed the `login_history` table (with user-agent).

---

## 2. Tracked actions

| Domain | Actions |
|--------|---------|
| Auth | `LOGIN_SUCCESS`, `LOGIN_FAILED`, `LOGOUT`, `ACCOUNT_LOCKED`, `FORGOT_PASSWORD`, `RESET_PASSWORD`, `PASSWORD_CHANGED` |
| Onboarding | `CUSTOMER_REGISTERED`, `CUSTOMER_APPROVED`, `CUSTOMER_REJECTED`, `KYC_VERIFIED` |
| Employees | `EMPLOYEE_CREATED`, `EMPLOYEE_UPDATED`, `EMPLOYEE_DISABLED`, `ROLES_ASSIGNED` |
| Money | `DEPOSIT`, `WITHDRAWAL`, `FUND_TRANSFER`, `LOAN_DISBURSAL`, `EMI_PAID` |
| Loans | `LOAN_APPLIED`, `LOAN_REVIEWED`, `LOAN_APPROVED`, `LOAN_REJECTED` |
| Cards | `CARD_APPLIED`, `CARD_REVIEWED`, `CARD_APPROVED`, `CARD_REJECTED` |
| Account | `ACCOUNT_STATUS_CHANGED`, `BENEFICIARY_ADDED`, `NOMINEE_ADDED`, `PROFILE_UPDATED` |
| Branch | `BRANCH_CREATED`, `BRANCH_UPDATED` |

---

## 3. Two capture mechanisms

### 3.1 Declarative (`@Auditable` AOP) — for coarse actions
Existing `AuditAspect` intercepts methods annotated `@Auditable`. Extend the annotation:

```java
@Auditable(action = "CUSTOMER_APPROVED", entityType = "USER")
public void approveCustomer(Long id, ApproveRequest req) { ... }
```
The aspect records user/action/ip after a successful return.

### 3.2 Explicit before/after (for old/new value) — for state changes
For changes where the prior and new state matter, call the extended service directly:

```java
auditService.recordChange(
    "ACCOUNT_STATUS_CHANGED", "ACCOUNT", account.getId().toString(),
    /* old */ Map.of("status", previousStatus),
    /* new */ Map.of("status", account.getStatus()),
    "Status changed by teller");
```

`recordChange` serializes the maps to compact JSON (Jackson), applies masking, and
persists. It runs in `REQUIRES_NEW` (existing pattern) so it never rolls back the business
transaction.

---

## 4. Masking rules (never store secrets)

| Field | Stored as |
|-------|-----------|
| password / hash | omitted entirely |
| card number | masked `**** **** **** 1234` |
| Aadhaar | `XXXXXXXX1234` (last 4) |
| PAN | `XXXXX1234X` |
| balance/amount | stored (financial audit needs it) |

Masking is centralized in an `AuditMasker` utility used by both capture mechanisms.

---

## 5. Integrity & access

- **Append-only:** no update/delete API for `audit_logs`; DB user has no `DELETE` grant in
  prod (hardening).
- **Read access:** `SUPER_ADMIN`, `ACCOUNTANT` via `GET /audit/logs` (filter by user,
  action, date range, entity) — paged.
- **Activity monitoring:** `GET /admin/activity` joins `audit_logs` + `login_history` per
  employee for the Employee Activity Monitoring feature.
- **Retention:** configurable (default keep 24 months); a scheduled archival job can move
  old rows to cold storage (noted, not in V1).

---

## 6. Example audit row (customer approval)

```json
{
  "username": "accountant1",
  "action": "CUSTOMER_APPROVED",
  "entityType": "USER",
  "entityId": "42",
  "oldValue": "{\"status\":\"PENDING_APPROVAL\",\"customerId\":null}",
  "newValue": "{\"status\":\"ACTIVE\",\"customerId\":\"CUST00042\",\"accountNumber\":\"1000000042\"}",
  "ipAddress": "10.0.0.5",
  "details": "Approved by accountant1",
  "createdAt": "2026-06-11T10:15:30Z"
}
```
