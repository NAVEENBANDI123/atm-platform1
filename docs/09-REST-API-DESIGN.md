# 09 — REST API Design

Base URL `/api/v1`. All responses use the existing `ApiResponse<T>` envelope
(`success`, `message`, `data`, `errors`, `timestamp`). Auth via
`Authorization: Bearer <accessToken>`. Paging via `?page=&size=&sort=`.

This extends `05-API-REFERENCE.md` (auth/accounts/transactions/admin already exist).

---

## 1. Authentication — dual portal (`/auth`, public)

| Method | Path | Body | Description |
|--------|------|------|-------------|
| POST | `/auth/customer/register` | `CustomerRegisterRequest` | Onboard customer → `PENDING_APPROVAL`, emails customer + admin. **No tokens returned.** |
| POST | `/auth/customer/login` | `LoginRequest` | Customer login; rejected unless `userType=CUSTOMER` and `status=ACTIVE`. |
| POST | `/auth/staff/login` | `LoginRequest` | Employee login; rejected unless `userType=EMPLOYEE`. |
| POST | `/auth/refresh` | `RefreshTokenRequest` | Rotate access token. |
| POST | `/auth/logout` | `RefreshTokenRequest` | Revoke refresh token. |
| POST | `/auth/forgot-password` | `ForgotPasswordRequest` | Issue reset (emailed). |
| POST | `/auth/reset-password` | `ResetPasswordRequest` | Set new password. |

`CustomerRegisterRequest` (validated):
```json
{
  "prefix": "MR", "firstName": "...", "middleName": "...", "lastName": "...",
  "gender": "MALE", "dateOfBirth": "1990-05-21",
  "mobile": "9000000003", "email": "a@b.com",
  "aadhaar": "123412341234", "pan": "ABCDE1234F",
  "address": { "houseNumber": "...", "street": "...", "city": "...",
               "state": "...", "country": "...", "postalCode": "..." },
  "accountType": "SAVINGS",
  "password": "...", "confirmPassword": "..."
}
```

---

## 2. Customer self-service (`/customer/**`, `ROLE_CUSTOMER`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/customer/dashboard` | Name, account number, type, **masked** balance (balance only via explicit reveal call) |
| GET | `/customer/account/balance` | Returns real balance (the "Show Balance" action) |
| GET | `/customer/profile` | Profile + KYC status |
| PUT | `/customer/profile` | Update editable profile fields |
| POST | `/customer/change-password` | Change password (emails alert) |
| GET | `/customer/accounts` | All own accounts |
| GET | `/customer/accounts/{id}/statement?from=&to=&format=pdf|excel` | Download statement |
| GET | `/customer/transactions?page=&size=` | Paged history |
| GET | `/customer/transactions/mini-statement` | Last 5 |
| GET | `/customer/transactions/search?type=&from=&to=&min=&max=` | Filtered search |
| POST | `/customer/transfer` | Fund transfer (balance, daily-limit, beneficiary checks; emails) |
| GET/POST/DELETE | `/customer/beneficiaries` | Beneficiary management |
| POST | `/customer/cards/apply` | Apply debit/credit card → `PENDING` |
| GET | `/customer/cards` | Own cards; pending shows "Application Under Review" only |
| POST | `/customer/loans/apply` | Apply loan → `PENDING` |
| GET | `/customer/loans` | Own loan applications + accounts |
| GET | `/customer/loans/{id}/schedule` | EMI repayment schedule |
| POST | `/customer/deposits/fd` · `/customer/deposits/rd` | Fixed / recurring deposit request |
| GET/POST | `/customer/nominees` | Nominee management |
| POST | `/customer/requests/cheque-book` · `/address-change` · `/mobile-update` | Service requests |
| GET/POST | `/customer/complaints` | Raise / list complaints |
| GET | `/customer/notifications` · PATCH `/{id}/read` | Notification center |
| POST | `/customer/scheduled-transfers` · GET · DELETE `/{id}` | Scheduled transfers |

---

## 3. Customer approval (`/approvals/customers`, `SUPER_ADMIN`+`ACCOUNTANT`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/approvals/customers/pending` | Queue of `PENDING_APPROVAL` customers |
| GET | `/approvals/customers/{id}` | Full application detail |
| POST | `/approvals/customers/{id}/approve` | Generate customer ID + account number, activate, email |
| POST | `/approvals/customers/{id}/reject` | `{ "reason": "..." }`, email rejection |

---

## 4. Loan workflow (`/approvals/loans`)

| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/approvals/loans/pending` | LOAN_OFFICER, SUPER_ADMIN | Review queue |
| POST | `/approvals/loans/{id}/review` | LOAN_OFFICER, SUPER_ADMIN | `{ recommend: APPROVE|REJECT, note }` → `RECOMMENDED` |
| POST | `/approvals/loans/{id}/decide` | SUPER_ADMIN | `{ decision, rate, tenureMonths, reason }`; on approve → loan account + disbursal + schedule + email |

## 5. Card workflow (`/approvals/cards`)

| Method | Path | Role | Description |
|--------|------|------|-------------|
| GET | `/approvals/cards/pending` | CARD_OFFICER, SUPER_ADMIN | Review queue |
| POST | `/approvals/cards/{id}/review` | CARD_OFFICER, SUPER_ADMIN | `{ recommend, note }` → `RECOMMENDED` |
| POST | `/approvals/cards/{id}/decide` | SUPER_ADMIN | On approve → generate card number/expiry, activate, email |

---

## 6. Teller (`/teller`, `SUPER_ADMIN`+`CASHIER`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/teller/deposit` | `{ accountNumber, amount, note }` deposit into customer account |
| POST | `/teller/withdraw` | Assisted withdrawal |
| GET | `/teller/accounts/{accountNumber}` | Lookup account for teller ops |

---

## 7. Employee & branch admin (`/admin`, `SUPER_ADMIN`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/admin/employees` | Create employee (assign role) |
| PUT | `/admin/employees/{id}` | Update employee |
| PATCH | `/admin/employees/{id}/disable` | Disable employee |
| PATCH | `/admin/employees/{id}/roles` | Reassign roles |
| GET | `/admin/employees` | List employees |
| GET/POST/PUT | `/admin/branches` | Branch management |
| GET | `/admin/activity?employeeId=&from=&to=` | Employee activity monitoring (from audit/login history) |

---

## 8. Reports (`/reports`, `SUPER_ADMIN`+`ACCOUNTANT`; officers scoped)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/reports/transactions/daily?date=&format=` | Daily transactions |
| GET | `/reports/deposits?from=&to=&format=` | Deposits report |
| GET | `/reports/withdrawals?from=&to=&format=` | Withdrawals report |
| GET | `/reports/loans?status=&format=` | Loan report (LOAN_OFFICER scoped) |
| GET | `/reports/cards?status=&format=` | Card report (CARD_OFFICER scoped) |
| GET | `/reports/customers?status=&format=` | Customer report |

`format` ∈ `pdf | excel`; default JSON. Binary responses set `Content-Disposition`.

---

## 9. Audit (`/audit`, `SUPER_ADMIN`+`ACCOUNTANT` read)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/audit/logs?user=&action=&from=&to=&page=&size=` | Filtered audit trail |
| GET | `/audit/login-history?user=` | Login/logout feed |

---

## 10. Status codes & conventions

`200` OK · `201` Created · `202` Accepted (async-confirmed) · `400` validation · `401`
unauthenticated · `403` forbidden / wrong portal · `404` not found · `409` conflict
(duplicate Aadhaar/PAN, insufficient funds, limit exceeded) · `423` locked · `422`
workflow state violation (e.g. approving an already-decided application).

Idempotency: money endpoints accept optional `Idempotency-Key` header; replays return the
original result instead of double-processing.
