# 06 — System Architecture (Enterprise Upgrade)

This document describes the target architecture for evolving the ATM platform into a
small-bank **core banking system** with approval workflows, RBAC, customer onboarding,
card and loan processing, asynchronous email, and auditing.

It builds on the existing stack (Spring Boot 3.3.x, Java 21, Spring Security 6 + JWT,
Spring Data JPA, PostgreSQL 16, Flyway, React 18 + Vite + MUI) documented in
`02-MIGRATION-BLUEPRINT.md`.

---

## 1. High-level topology

```
                         ┌───────────────────────────────────────────────┐
                         │                Browsers (TLS)                  │
                         │   Customer Portal  │   Employee/Admin Portal   │
                         └───────────┬──────────────────────┬────────────┘
                                     │  /customer/*          │  /staff/*
                                     ▼                       ▼
                         ┌───────────────────────────────────────────────┐
                         │      React 18 SPA (Vite) — single bundle       │
                         │  RTK Query · Axios (refresh interceptor)       │
                         │  Two route trees gated by role + portal type   │
                         └───────────────────────┬───────────────────────┘
                                                 │ HTTPS  Bearer JWT
                                                 ▼
              ┌───────────────────────────────────────────────────────────────┐
              │                 Spring Boot 3 REST API (stateless)             │
              │                                                                 │
              │  Controller  →  Service (interface)  →  ServiceImpl (@Tx)       │
              │       │                                     │                   │
              │       │             ┌───────────────────────┼───────────────┐   │
              │       ▼             ▼                       ▼               ▼   │
              │   DTO/MapStruct  Repository (JPA)     Domain events     Security│
              │                      │                  (App events)     (JWT) │
              └──────────────────────┼───────────────────────┬─────────────────┘
                                     │                        │ @Async
                            ┌────────▼────────┐      ┌────────▼─────────┐
                            │  PostgreSQL 16  │      │  Email worker    │
                            │  (Flyway)       │      │  JavaMailSender  │
                            └─────────────────┘      │  + Thymeleaf     │
                                                     │  → SMTP relay     │
                                                     └──────────────────┘
```

Key principles:

- **One SPA, two portals.** Customers and employees never see the same login screen.
  The SPA exposes `/customer/login` and `/staff/login`; the backend marks each user as
  `userType = CUSTOMER | EMPLOYEE` and rejects cross-portal logins (see
  `11-SECURITY-ARCHITECTURE.md`).
- **Stateless API.** JWT access tokens (short-lived) + opaque refresh tokens (DB-backed,
  revocable). No HTTP session.
- **Clean layering.** Controllers are thin; business rules live in `service.impl` and run
  inside `@Transactional` boundaries; persistence is isolated behind repositories.
- **Event-driven side effects.** Email and audit are triggered by Spring
  `ApplicationEvent`s so the core transaction is never blocked or rolled back by a mail
  failure.

---

## 2. Clean architecture layers

| Layer | Responsibility | Depends on |
|-------|----------------|------------|
| **Web / API** (`controller`, `dto`, `mapper`) | HTTP, validation, serialization, role gate | Application |
| **Application** (`service`, `service.impl`, `workflow`) | Use-cases, transactions, orchestration, state machines | Domain |
| **Domain** (`entity`, enums, domain `event`) | Entities, invariants, value objects | — |
| **Infrastructure** (`repository`, `security`, `email`, `report`, `audit`, `config`) | JPA, JWT, SMTP, PDF/Excel, cross-cutting | Domain interfaces |

Dependencies point inward. Controllers depend on service **interfaces**, never on
`*Impl`. Infrastructure implements ports defined by the application/domain.

---

## 3. Core subsystems

### 3.1 Identity & access
- `User` is the single identity root, with `userType` (CUSTOMER / EMPLOYEE) and a set of
  `Role`s (6 roles, see `08-ROLE-PERMISSION-MATRIX.md`).
- Customer-specific profile data (KYC, address, etc.) lives in a `customer_profiles`
  table; employee-specific data in `employee_profiles`. This keeps `users` lean and
  avoids nullable sprawl.

### 3.2 Onboarding & approval (state machines)
- Customer registration creates a `User` in status `PENDING_APPROVAL` with **no account
  number and no login capability** until approved.
- Generic **maker-checker** workflow engine backs three flows: customer approval, card
  issuance, loan sanctioning. Each request row carries a `status`, `reviewed_by`,
  `approved_by`, and a `reason`. See `14-APPROVAL-WORKFLOWS.md`.

### 3.3 Accounts & ledger
- `Account` supports types SAVINGS / SALARY / CURRENT (+ derived LOAN account on loan
  sanction). Money is `NUMERIC(19,2)`, balance guarded by `@Version` optimistic locking
  and DB `CHECK (balance >= 0)`.
- All money movement is recorded in an append-only `transactions` ledger.

### 3.4 Cards & loans
- Card and loan **applications** are separate from issued **cards** and **loan accounts**.
  Customers see only "Application Under Review" until final Super Admin approval, after
  which card number / expiry or loan account + repayment schedule are generated.

### 3.5 Notifications (async email)
- A single `NotificationService` publishes `EmailRequested` events; an `@Async` listener
  renders a Thymeleaf template and sends via `JavaMailSender`. Failures are logged and
  retried, never propagated to the business transaction.

### 3.6 Auditing
- Two complementary mechanisms: declarative `@Auditable` AOP for service methods, and
  explicit `AuditService.record(...)` for fine-grained before/after value capture. Every
  sensitive action writes user, action, timestamp, IP, old value, new value.

### 3.7 Reporting
- Read-optimized query services produce daily transaction / deposit / withdrawal / loan /
  card / customer reports, exportable to PDF (OpenPDF) and Excel (Apache POI).

---

## 4. Cross-cutting concerns

| Concern | Mechanism |
|---------|-----------|
| Transactions | `@Transactional` at service layer; `REQUIRES_NEW` for audit writes |
| Validation | Jakarta Bean Validation on request DTOs + custom validators (Aadhaar/PAN) |
| Error handling | `@RestControllerAdvice` → `ApiResponse` envelope, sanitized messages |
| Concurrency | Optimistic locking (`@Version`) + ordered locking for transfers |
| Async | `@EnableAsync` + dedicated `ThreadPoolTaskExecutor` (`emailExecutor`) |
| Scheduling | `@EnableScheduling` for scheduled transfers, token cleanup, RD/FD maturity |
| Config | Spring profiles (`default`, `docker`), env-driven secrets |
| Observability | Actuator health/info/metrics; structured logback (JSON-ready) |

---

## 5. Deployment

`docker-compose` extends the existing three services with email + worker concerns:

```
db (postgres:16)  ──►  backend (spring boot)  ──►  frontend (nginx + SPA)
                              │
                              └──►  mailhog (dev SMTP capture) / SMTP relay (prod)
```

- **Dev:** MailHog (or GreenMail) captures outbound email so workflows can be exercised
  without a real provider.
- **Prod:** SMTP relay credentials injected via environment variables / secrets.

See `16-IMPLEMENTATION-PLAN.md` for rollout phasing and `11-SECURITY-ARCHITECTURE.md`
for the trust boundaries.
