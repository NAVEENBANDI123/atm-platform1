# 14 — Approval Workflow Diagrams

Three maker-checker workflows share a common state model. Each is backed by an application
row with `status`, `reviewed_by` (maker/officer), `approved_by` (checker/Super Admin), and
a `reason`/`note`.

Generic states: `PENDING → UNDER_REVIEW → RECOMMENDED → APPROVED | REJECTED`.

---

## 1. Customer onboarding & approval

```
 Customer                 System                    SUPER_ADMIN / ACCOUNTANT
    │                        │                                │
    │ register (personal,    │                                │
    │ address, account type, │                                │
    │ security)              │                                │
    ├───────────────────────►│                                │
    │                        │ create User(status=PENDING_APPROVAL)
    │                        │ create CustomerProfile (no customerId yet)
    │                        │ email CUSTOMER_REGISTERED ──► Customer
    │                        │ email ADMIN_NEW_REGISTRATION ──► Admin/Accountant
    │   "Application Pending" │                                │
    │◄───────────────────────┤                                │
    │   (cannot log in)       │                                │
    │                        │        GET /approvals/customers/pending
    │                        │◄───────────────────────────────┤
    │                        │                                │ review detail
    │                        │         ┌──────────── APPROVE ──┤
    │                        │         │  generate customerId  │
    │                        │         │  generate accountNo   │
    │                        │         │  create Account(ACTIVE)
    │                        │         │  User.status = ACTIVE │
    │                        │         │  email ACCOUNT_APPROVED (customerId, accountNo)
    │   can now log in        │◄────────┘                       │
    │                        │         ┌──────────── REJECT ───┤ {reason}
    │                        │         │  User.status=REJECTED │
    │                        │         │  email ACCOUNT_REJECTED(reason)
    │◄───────────────────────┤◄────────┘                       │
```

Rules: only `SUPER_ADMIN`/`ACCOUNTANT` decide. Approval is idempotent-guarded (a second
approve on an already-`ACTIVE` user → `422`). Account number + customer ID generated only
on approval.

---

## 2. Loan workflow (Loan Officer → Super Admin)

```
 Customer            System              LOAN_OFFICER             SUPER_ADMIN
    │ apply (type,amount, │                  │                        │
    │ income, employment, │                  │                        │
    │ purpose)            │                  │                        │
    ├────────────────────►│ LoanApplication(PENDING)                  │
    │                     │ email LOAN_REVIEW_REQUIRED ──► Loan Officer│
    │ "Under Review"      │                  │                        │
    │◄────────────────────┤                  │                        │
    │                     │  review {recommend, note}                 │
    │                     │◄─────────────────┤                        │
    │                     │ status=RECOMMENDED, reviewed_by=officer    │
    │                     │                  │   decide {decision,rate,tenure}
    │                     │                  │◄───────────────────────┤
    │                     │      ┌── APPROVE: create LoanAccount,      │
    │                     │      │   compute EMI + schedule,           │
    │                     │      │   credit principal to account,      │
    │                     │      │   ledger LOAN_DISBURSAL,            │
    │                     │      │   email LOAN_APPROVED               │
    │ schedule visible     │◄─────┘                                    │
    │                     │      └── REJECT {reason}: status=REJECTED, │
    │                     │          email LOAN_REJECTED               │
    │◄────────────────────┤                                           │
```

Separation of duties: `reviewed_by` (officer) must differ from `approved_by` (Super Admin)
— enforced by `ApprovalGuard`. EMI uses reducing-balance:
`EMI = P·r·(1+r)^n / ((1+r)^n − 1)`, `r = annualRate/12/100`.

---

## 3. Card workflow (Card Officer → Super Admin)

```
 Customer            System              CARD_OFFICER             SUPER_ADMIN
    │ apply (cardType,    │                  │                        │
    │ account)            │                  │                        │
    ├────────────────────►│ CardApplication(PENDING)                  │
    │                     │ email CARD_REVIEW_REQUIRED ──► Card Officer│
    │ "Application Under  │                  │                        │
    │  Review"            │                  │                        │
    │◄────────────────────┤                  │                        │
    │                     │  review {recommend, note}                 │
    │                     │◄─────────────────┤                        │
    │                     │ status=RECOMMENDED, reviewed_by=officer    │
    │                     │                  │   decide {decision}     │
    │                     │                  │◄───────────────────────┤
    │                     │      ┌── APPROVE: generate cardNumber(Luhn),│
    │                     │      │   expiry (+3y), create Card(ACTIVE), │
    │                     │      │   email CARD_APPROVED (masked no.)   │
    │ card visible         │◄─────┘                                    │
    │                     │      └── REJECT {reason}: status=REJECTED,  │
    │                     │          email CARD_REJECTED                │
    │◄────────────────────┤                                           │
```

Customer visibility: while `PENDING`/`UNDER_REVIEW`/`RECOMMENDED`, the customer sees only
"Application Under Review". Card number/expiry are revealed only after `APPROVED`.

---

## 4. State transition table (shared)

| From | Event | To | Side effects |
|------|-------|----|--------------|
| — | submit | `PENDING` | create application; notify maker/admin |
| `PENDING` | officer opens | `UNDER_REVIEW` | (optional) |
| `PENDING`/`UNDER_REVIEW` | review(recommend) | `RECOMMENDED` | set `reviewed_by`, `note` |
| `RECOMMENDED` (or `PENDING` for customer flow) | approve | `APPROVED` | generate ids/accounts/cards; disburse; email approved |
| any non-terminal | reject(reason) | `REJECTED` | set `reject_reason`; email rejected |
| `APPROVED`/`REJECTED` | any | — | `422` terminal (immutable) |

All transitions write an `audit_log` entry with old/new status and the acting user.
