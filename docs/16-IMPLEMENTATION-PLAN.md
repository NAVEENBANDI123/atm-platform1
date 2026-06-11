# 16 — Production-Ready Implementation Plan

This is the phased roadmap to evolve the current app (auth + single account + transfers)
into the small-bank core banking system designed in docs 06–15. Phases are ordered by
dependency and value; each ends in a **shippable, tested** increment.

> Build constraint note: this design package and the foundational **Flyway V3 migration**
> + **role model** are delivered now. Subsequent phases are concrete code increments to be
> implemented and compiled in an environment with Maven/dependency access (the current
> sandbox has no Maven cache and restricted network, so the Java for later phases is
> specified here rather than half-built and unverifiable).

---

## Phase 0 — Foundation (DELIVERED in this PR)
- `RoleName` enum expanded to 6 roles (+ legacy `ROLE_ADMIN` retained).
- Flyway `V3__rbac_onboarding_and_products.sql`: additive schema for all new tables +
  ALTERs to `users`, `accounts`, `audit_logs`; seed new roles, a super admin, sample branch.
- Full design docs (06–15) + this plan.
- **Acceptance:** `flyway migrate` applies cleanly on a V1/V2 database; existing app still
  validates (additive columns have defaults).

## Phase 1 — Identity, dual portal & RBAC
- Add `UserType`, `UserStatus` to `User` + `UserPrincipal`; map `customer_profiles` /
  `employee_profiles`.
- `PortalAuthenticationService`; split `/auth/customer/login` and `/auth/staff/login`;
  block `PENDING_APPROVAL`.
- `SecurityConfig` URL matrix for 6 roles; `@PreAuthorize` on new endpoints.
- `login_history` writes + `LOGIN_ALERT` email hook (stub until Phase 3).
- **Tests:** wrong-portal login → 403; pending customer cannot log in; role gates.

## Phase 2 — Customer onboarding & approval
- `OnboardingService.register(CustomerRegisterRequest)` → PENDING user + profile, Aadhaar/
  PAN validators, password-match.
- `CustomerApprovalService` (approve/reject) → `CustomerIdGenerator`, account creation,
  activation; pending queue endpoints.
- **Tests:** register → pending; approve generates ids + account; reject sets reason;
  idempotency guard.

## Phase 3 — Async email + notification center
- Add `spring-boot-starter-mail` + `thymeleaf`; `MailConfig`, `AsyncConfig`.
- `NotificationService` + `EmailRequestedEvent` + `@TransactionalEventListener(AFTER_COMMIT)`
  `@Async` listener; 15 templates (doc 13); in-app `notifications` rows.
- Wire registration/approval/login emails.
- **Tests:** event published after commit only; listener renders + records notification
  (MailHog/GreenMail in IT).

## Phase 4 — Audit enrichment
- `audit_logs.old_value/new_value`; `AuditService.recordChange(...)` + `AuditMasker`.
- `@Auditable` on sensitive methods; `/audit/logs` + `/admin/activity`.
- **Tests:** approval writes old/new status; secrets masked; append-only.

## Phase 5 — Teller (deposit/withdraw) & extended accounts
- `AccountType` on accounts; multi-account per customer.
- `TellerService` deposit/withdraw (SUPER_ADMIN/CASHIER) with ledger + email; daily limit
  field on accounts.
- **Tests:** deposit role gate; balance + ledger correctness; optimistic lock.

## Phase 6 — Fund transfer hardening & beneficiaries
- `BeneficiaryService`; transfer validations: balance, daily limit, beneficiary verified;
  ordered locking; `FUND_TRANSFER` email.
- **Tests:** limit exceeded → 409; unverified beneficiary blocked; concurrent transfers.

## Phase 7 — Card workflow
- `CardApplication`/`Card`; apply → review (CARD_OFFICER) → decide (SUPER_ADMIN);
  `CardNumberGenerator` (Luhn) + expiry; masked customer view; emails.
- **Tests:** maker≠checker; pending hides number; approval issues card.

## Phase 8 — Loan workflow
- `LoanApplication`/`LoanAccount`/`loan_schedule`; apply → review (LOAN_OFFICER) → decide
  (SUPER_ADMIN); `EmiCalculator` schedule; disbursal to account; emails.
- **Tests:** EMI math; disbursal ledger + balance; schedule rows; SoD.

## Phase 9 — Deposits (FD/RD), nominees, service requests, complaints
- `DepositService` (FD/RD requests + maturity calc), `NomineeService`,
  `ServiceRequestService` (cheque/address/mobile), `ComplaintService` (ticket lifecycle).
- **Tests:** maturity amount; request/complaint state machines.

## Phase 10 — Reports & exports
- Read-optimized query services; `PdfReportWriter` (OpenPDF) + `ExcelReportWriter` (POI);
  daily txns / deposits / withdrawals / loans / cards / customers; role-scoped.
- **Tests:** report row counts vs ledger; export content-type/headers.

## Phase 11 — Scheduling & hardening
- `@EnableScheduling`: scheduled transfers, FD/RD maturity, refresh-token cleanup.
- Rate limiting on `/auth` + money endpoints; security headers at nginx; idempotency keys.
- **Tests:** scheduled job runs; rate limit triggers.

## Phase 12 — Frontend dual portal
- Customer portal (register wizard, masked dashboard, services hub, transfers,
  beneficiaries, cards, loans, deposits, requests, complaints, notifications).
- Staff portal (role-aware menu, approval queues, teller, employee/branch admin, reports,
  audit). Extend `ProtectedRoute` for `allowedRoles` + `portal`.
- **Tests:** route guards; balance reveal; pending product states.

---

## Cross-cutting definition of done (every phase)
- Flyway migration (if schema changes) + entity mapping aligned (`ddl-auto=validate` passes).
- Bean Validation on all request DTOs; `@PreAuthorize` on protected methods.
- Unit tests (service) + slice/integration tests (controller + security).
- Audit + email hooks for sensitive actions.
- Swagger updated; `docs/05-API-REFERENCE.md` + `03-FILE-CHECKLIST.md` updated.
- `mvn verify` green; frontend `npm run build` + lint green.

## Suggested PR sequence
1. **This PR** — design package + Phase 0 foundation (migration + roles).
2. Phase 1+2 (identity, dual portal, onboarding/approval) — the highest-value workflow.
3. Phase 3+4 (email + audit) — makes workflows observable.
4. Phases 5–8 (teller, transfers, cards, loans) — core banking.
5. Phases 9–11 (deposits/requests/complaints, reports, scheduling).
6. Phase 12 (frontend) — incrementally per backend module.

This ordering keeps `main` releasable at every step and lets us validate each workflow
end-to-end before layering the next.
