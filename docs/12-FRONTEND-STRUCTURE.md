# 12 — React Page Structure (Dual Portal)

Extends the existing React 18 + Vite + MUI + Redux Toolkit SPA. One bundle, two portals
with fully separate login screens and route trees. Customers and employees never share a
login page.

---

## 1. Routing tree

```
/                                   → redirect by auth+portal
│
├─ /customer                        CUSTOMER PORTAL (public + protected)
│   ├─ /customer/login              CustomerLoginPage
│   ├─ /customer/register           CustomerRegisterPage (multi-step wizard)
│   ├─ /customer/forgot-password    ForgotPasswordPage
│   ├─ /customer/reset-password     ResetPasswordPage
│   ├─ /customer/pending            ApplicationPendingPage (shown post-register)
│   └─ [ProtectedRoute role=CUSTOMER] <CustomerLayout>
│       ├─ /customer/dashboard      CustomerDashboardPage (masked balance + Show Balance)
│       ├─ /customer/services       ServicesHomePage (services hub)
│       ├─ /customer/profile        ProfilePage / ChangePasswordPage
│       ├─ /customer/accounts/:id/statement  StatementPage
│       ├─ /customer/transfer       FundTransferPage
│       ├─ /customer/beneficiaries  BeneficiariesPage
│       ├─ /customer/transactions   TransactionsPage (+ search, mini-statement)
│       ├─ /customer/cards          CardsPage / ApplyCardPage
│       ├─ /customer/loans          LoansPage / ApplyLoanPage / LoanSchedulePage
│       ├─ /customer/deposits       FixedDepositPage / RecurringDepositPage
│       ├─ /customer/nominees       NomineesPage
│       ├─ /customer/requests       ServiceRequestsPage (cheque/address/mobile)
│       ├─ /customer/complaints     ComplaintsPage
│       └─ /customer/notifications  NotificationCenterPage
│
└─ /staff                          EMPLOYEE PORTAL
    ├─ /staff/login                 StaffLoginPage
    └─ [ProtectedRoute role=EMPLOYEE] <StaffLayout>  (menu filtered by role)
        ├─ /staff/dashboard         StaffDashboardPage (role-aware KPIs)
        ├─ /staff/approvals/customers   CustomerApprovalQueuePage  [SUPER_ADMIN, ACCOUNTANT]
        ├─ /staff/approvals/loans        LoanReviewPage / LoanDecisionPage [LOAN_OFFICER, SUPER_ADMIN]
        ├─ /staff/approvals/cards        CardReviewPage / CardDecisionPage [CARD_OFFICER, SUPER_ADMIN]
        ├─ /staff/teller                 TellerPage (deposit/withdraw)     [SUPER_ADMIN, CASHIER]
        ├─ /staff/customers              CustomerSearchPage / CustomerDetailPage
        ├─ /staff/employees              EmployeeAdminPage                 [SUPER_ADMIN]
        ├─ /staff/branches               BranchAdminPage                   [SUPER_ADMIN]
        ├─ /staff/reports                ReportsPage (PDF/Excel export)    [SUPER_ADMIN, ACCOUNTANT, officers scoped]
        ├─ /staff/activity               ActivityMonitorPage               [SUPER_ADMIN]
        └─ /staff/audit                  AuditLogPage                      [SUPER_ADMIN, ACCOUNTANT]
```

---

## 2. Source layout (additions to existing `src/`)

```
src/
├── app/store.js                     # add new RTK slices/apis
├── api/axiosClient.js               # existing refresh interceptor (reused)
│
├── features/
│   ├── auth/
│   │   ├── authSlice.js             # add userType, status, role helpers
│   │   ├── customerAuthApi.js       # register/login/forgot/reset (customer)
│   │   └── staffAuthApi.js          # staff login
│   ├── customer/
│   │   ├── dashboardApi.js  profileApi.js  transferApi.js
│   │   ├── beneficiaryApi.js  cardApi.js  loanApi.js  depositApi.js
│   │   ├── nomineeApi.js  requestApi.js  complaintApi.js  notificationApi.js
│   │   └── statementApi.js
│   ├── approvals/   customerApprovalApi.js  loanWorkflowApi.js  cardWorkflowApi.js
│   ├── teller/      tellerApi.js
│   ├── admin/       employeeApi.js  branchApi.js  activityApi.js
│   ├── reports/     reportApi.js
│   └── audit/       auditApi.js
│
├── components/
│   ├── layout/  CustomerLayout.jsx  StaffLayout.jsx  RoleMenu.jsx
│   ├── common/  BalanceReveal.jsx  StatusChip.jsx  ConfirmDialog.jsx
│   │            DataTable.jsx  ExportButtons.jsx  WizardStepper.jsx
│   └── forms/   AddressFields.jsx  PasswordFields.jsx
│
├── routes/
│   ├── ProtectedRoute.jsx           # extend: accepts allowedRoles + portal
│   └── PortalRedirect.jsx           # "/" → correct portal/dashboard
│
└── pages/
    ├── customer/ ... (per routing tree)
    └── staff/    ... (per routing tree)
```

---

## 3. Key UX behaviors

- **Separate logins**: `CustomerLoginPage` and `StaffLoginPage` are distinct components on
  distinct routes; there is no shared login. Wrong-portal logins surface the 403 message.
- **Registration wizard**: `CustomerRegisterPage` is a 4-step stepper — Personal → Address
  → Account type → Security — with `react-hook-form` validation (Aadhaar 12 digits, PAN
  pattern, password match). On submit → `ApplicationPendingPage`: "Application submitted,
  awaiting approval." No tokens issued.
- **Masked balance**: `CustomerDashboardPage` shows `Balance: ******` with a
  `<BalanceReveal>` button that calls `GET /customer/account/balance` only on click;
  auto-re-masks after a timeout.
- **Pending product visibility**: `CardsPage`/`LoansPage` render "Application Under Review"
  for pending items and reveal card number/expiry or loan schedule only when approved.
- **Role-aware staff menu**: `RoleMenu` filters nav items by the roles in the token, so a
  Cashier sees only Teller + customer lookup, a Loan Officer sees only loan review, etc.
- **Protected routing**: `ProtectedRoute` now takes `allowedRoles` and `portal`; redirects
  to the matching login when unauthenticated and to the user's dashboard on role mismatch.

---

## 4. State & API conventions

- Reuse the existing Axios client + token-refresh interceptor.
- One RTK Query API slice per feature; tags for cache invalidation (e.g. approving a
  customer invalidates the `PendingCustomers` queue).
- Binary exports (PDF/Excel) fetched as `blob` and downloaded client-side.
- Auth slice persists `accessToken`, `refreshToken`, `userType`, `roles`, and derived
  `isCustomer`/`isStaff`/role selectors (extends current `selectIsAdmin`).
