# 08 — Role & Permission Matrix

Six roles, two portals. Customers authenticate at the **customer portal**; all employee
roles authenticate at the **employee portal**. A user's `userType` is checked at login so
the two never mix (see `11-SECURITY-ARCHITECTURE.md`).

> Legacy note: the existing `ROLE_ADMIN` is retained and treated as an **alias of
> `ROLE_SUPER_ADMIN`** during transition, so current admin logins keep working.

---

## 1. Roles

| Role | Portal | Summary |
|------|--------|---------|
| `ROLE_SUPER_ADMIN` | Employee | Full control. Creates employees; final approver for customers, loans, cards; can deposit. |
| `ROLE_ACCOUNTANT` | Employee | Approves customers, views customer details, reviews transactions, generates reports. |
| `ROLE_CASHIER` | Employee | Deposits, withdrawals (assisted), view customer accounts. |
| `ROLE_LOAN_OFFICER` | Employee | Reviews loan applications and recommends approve/reject. No final sanction. |
| `ROLE_CARD_OFFICER` | Employee | Reviews card requests and recommends approve/reject. No final issuance. |
| `ROLE_CUSTOMER` | Customer | Self-service: profile, account, transfers, cards/loan applications, history. |

---

## 2. Capability matrix

Legend: ✅ allowed · ➖ not applicable · ❌ explicitly denied · 🟡 recommend-only (maker)

| Capability | SUPER_ADMIN | ACCOUNTANT | CASHIER | LOAN_OFFICER | CARD_OFFICER | CUSTOMER |
|---|:--:|:--:|:--:|:--:|:--:|:--:|
| Employee: create / update / disable | ✅ | ❌ | ❌ | ❌ | ❌ | ➖ |
| Employee: assign roles | ✅ | ❌ | ❌ | ❌ | ❌ | ➖ |
| Customer: approve / reject registration | ✅ | ✅ | ❌ | ❌ | ❌ | ➖ |
| Customer: view details | ✅ | ✅ | ✅ | ✅(loan ctx) | ✅(card ctx) | own |
| Deposit funds | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Withdraw (assisted) | ✅ | ❌ | ✅ | ❌ | ❌ | ➖ |
| Withdraw (own account) | ➖ | ➖ | ➖ | ➖ | ➖ | ✅ |
| Fund transfer | ➖ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Loan: review / recommend | ✅ | ❌ | ❌ | 🟡 | ❌ | ➖ |
| Loan: final approve / reject | ✅ | ❌ | ❌ | ❌ | ❌ | ➖ |
| Loan: apply | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Card: review / recommend | ✅ | ❌ | ❌ | ❌ | 🟡 | ➖ |
| Card: final approve / reject | ✅ | ❌ | ❌ | ❌ | ❌ | ➖ |
| Card: apply / view own | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Beneficiary management | ➖ | ❌ | ❌ | ❌ | ❌ | ✅ |
| View own profile / account / balance | ➖ | ➖ | ➖ | ➖ | ➖ | ✅ |
| Transaction history (own) | ➖ | ➖ | ➖ | ➖ | ➖ | ✅ |
| Reports (generate / export) | ✅ | ✅ | ❌ | loan only | card only | ❌ |
| Audit log: view | ✅ | ✅(read) | ❌ | ❌ | ❌ | ❌ |
| Branch management | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Complaints: raise | ➖ | ➖ | ➖ | ➖ | ➖ | ✅ |
| Complaints: handle / resolve | ✅ | ✅ | ✅ | ❌ | ❌ | ➖ |
| Service requests: raise (cheque/address/mobile) | ➖ | ➖ | ➖ | ➖ | ➖ | ✅ |
| Service requests: handle | ✅ | ✅ | ✅ | ❌ | ❌ | ➖ |

---

## 3. Enforcement mapping (Spring Security)

Method-level `@PreAuthorize` plus URL rules in `SecurityConfig`. Authorities are the role
names (`ROLE_` prefix), so `hasRole('SUPER_ADMIN')` ⇔ authority `ROLE_SUPER_ADMIN`.

| Endpoint group | Rule |
|----------------|------|
| `/api/v1/auth/customer/**` | public |
| `/api/v1/auth/staff/**` | public |
| `/api/v1/admin/employees/**` | `hasRole('SUPER_ADMIN')` |
| `/api/v1/admin/branches/**` | `hasRole('SUPER_ADMIN')` |
| `/api/v1/approvals/customers/**` | `hasAnyRole('SUPER_ADMIN','ACCOUNTANT')` |
| `/api/v1/approvals/loans/review/**` | `hasAnyRole('LOAN_OFFICER','SUPER_ADMIN')` |
| `/api/v1/approvals/loans/decide/**` | `hasRole('SUPER_ADMIN')` |
| `/api/v1/approvals/cards/review/**` | `hasAnyRole('CARD_OFFICER','SUPER_ADMIN')` |
| `/api/v1/approvals/cards/decide/**` | `hasRole('SUPER_ADMIN')` |
| `/api/v1/teller/deposit` | `hasAnyRole('SUPER_ADMIN','CASHIER')` |
| `/api/v1/teller/withdraw` | `hasAnyRole('SUPER_ADMIN','CASHIER')` |
| `/api/v1/reports/**` | `hasAnyRole('SUPER_ADMIN','ACCOUNTANT')` (+ scoped officer reports) |
| `/api/v1/customer/**` | `hasRole('CUSTOMER')` |
| `/api/v1/audit/**` | `hasAnyRole('SUPER_ADMIN','ACCOUNTANT')` |

Example annotation:

```java
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ACCOUNTANT')")
public ApiResponse<Void> approveCustomer(@PathVariable Long id, @RequestBody ApproveRequest req) { ... }

@PreAuthorize("hasRole('SUPER_ADMIN')")
public ApiResponse<LoanAccountResponse> sanctionLoan(@PathVariable Long appId) { ... }
```

---

## 4. Separation-of-duties invariants

1. **Maker ≠ checker.** The officer who recommends a loan/card (`reviewed_by`) cannot be
   the Super Admin who finalizes it (`approved_by`) — enforced in the workflow service.
2. **No customer self-approval.** A `PENDING_APPROVAL` customer cannot authenticate, so
   cannot reach any approval endpoint.
3. **No employee self-registration.** There is no staff registration endpoint; employees
   exist only via `POST /api/v1/admin/employees` by a Super Admin.
4. **Deposit restricted.** Only Super Admin and Cashier hit the teller deposit endpoint;
   customers have no deposit capability.
5. **Cross-portal denial.** A CUSTOMER token rejected on `/staff` endpoints and vice versa.
