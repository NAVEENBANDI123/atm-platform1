# Phase 2 — Migration Blueprint

## Target architecture

```
                ┌─────────────────────────┐        ┌──────────────────────────┐
   Browser ───► │  React 18 + Vite (SPA)   │ ─HTTPS►│  Spring Boot 3 REST API   │
                │  Redux Toolkit / Axios   │  JWT   │  Spring Security 6 / JPA  │
                └─────────────────────────┘        └─────────────┬────────────┘
                                                                  │ JPA/Hibernate
                                                          ┌───────▼────────┐
                                                          │  PostgreSQL 16 │  (Flyway-managed)
                                                          └────────────────┘
```

### Backend stack
Java 21, Spring Boot 3.3.x, Spring Security 6 (stateless JWT + refresh tokens), Spring Data JPA / Hibernate, Maven, PostgreSQL, Flyway, springdoc OpenAPI (Swagger UI), Lombok, MapStruct, global exception handling, structured logging (logback JSON-ready), JPA auditing + dedicated audit-log table.

### Frontend stack
React 18, Vite, React Router v6, Axios (interceptors for token refresh), Redux Toolkit, React Hook Form, Material UI, protected routes by role.

### DevOps
Multi-stage Dockerfiles (backend + frontend/nginx), Docker Compose (db + api + web), environment-based configuration via env vars and Spring profiles.

## Clean architecture / package layout
```
com.atm
├── config        # OpenAPI, JPA auditing, CORS, app properties
├── controller    # REST controllers (thin)
├── dto.request / dto.response
├── entity        # JPA entities + enums
├── repository    # Spring Data JPA repos
├── service       # interfaces
├── service.impl  # business logic, @Transactional
├── security      # JWT provider, filter, config, userdetails
├── exception     # custom exceptions + @RestControllerAdvice
├── mapper        # MapStruct entity<->DTO
├── validation    # custom annotations/validators
├── audit         # auditing aspect/service
├── util          # helpers
└── common        # constants, ApiResponse envelope, enums
```

## Legacy → target mapping
| Legacy | Target |
|--------|--------|
| `bank.html` login form | `POST /api/v1/auth/login` + React `LoginPage` |
| `create_account.jsp` + `/CreateAccount` | `POST /api/v1/auth/register` + React `RegisterPage` |
| `/Login` (`Second.m1`) | `AuthService.login` → JWT access + refresh |
| `/BalanceEnquiry` | `GET /api/v1/accounts/me` |
| `/Deposit` (`Second.deposit`) | `POST /api/v1/accounts/deposit` (`@Transactional`, atomic) |
| `/Withdraw` (`Second.withdraw`) | `POST /api/v1/accounts/withdraw` (atomic, balance check, lock) |
| `/ForgotPassword` | `POST /api/v1/auth/forgot-password` + `reset-password` |
| *(missing)* | `POST /api/v1/accounts/transfer`, `GET /api/v1/transactions`, mini-statement, admin user mgmt |
| `Second.User` inner class | `User` + `Account` entities, `*Response` DTOs |
| Plain-text password | BCrypt hashing |
| Hardcoded DB creds | Env-based config / Docker secrets |
| `int` money | `BigDecimal` / `NUMERIC(19,2)` |
| read-modify-write balance | DB transaction + optimistic locking (`@Version`) + atomic SQL |

## Bug/vuln remediation summary
- S1/S3 → BCrypt + never return password; tokens instead of credential round-trips.
- S2 → externalized config.
- S4/S5/S6/S7 → stateless JWT auth + RBAC + method security; login is POST only.
- S8 → single source-of-truth schema with `balance NUMERIC`.
- S9 → JPA manages connections; no manual JDBC.
- S10 → Bean Validation on all request DTOs (positive amounts, sizes, patterns).
- S11 → `audit_logs` + JPA auditing.
- S12 → CORS config, security headers, stateless (CSRF disabled for token API — documented strategy).
- S13 → global exception handler returns sanitized `ApiResponse`.
- S14 → `@Transactional` + pessimistic/optimistic locking on account balance updates.

## Standard API response envelope
```json
{ "success": true, "message": "OK", "data": { }, "timestamp": "...", "errors": null }
```
