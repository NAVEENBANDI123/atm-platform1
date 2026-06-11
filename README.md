# ATM Platform — Enterprise Migration

[![CI](https://github.com/NAVEENBANDI123/atm-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/NAVEENBANDI123/atm-platform/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Production-grade rebuild of the legacy **AtmSimulation** (Java Servlets + JSP + JDBC + MySQL)
into a modern, secure banking platform:

- **Backend:** Java 21, Spring Boot 3.3, Spring Security 6 (JWT + refresh tokens), Spring Data JPA/Hibernate, PostgreSQL, Flyway, MapStruct, Lombok, springdoc OpenAPI.
- **Frontend:** React 18, Vite, Redux Toolkit, React Router, Axios, React Hook Form, Material UI.
- **DevOps:** Docker, Docker Compose, GitHub Actions CI, environment-based configuration.

> The original app, its analysis, the migration blueprint, ER design, API reference and a
> per-file checklist live in [`docs/`](docs).
>
> **Enterprise upgrade in progress** — the platform is being extended into a small-bank
> core banking system (6-role RBAC, customer onboarding & approval workflows, card/loan
> processing, asynchronous email, and enriched auditing). The full design package
> ([`docs/06`](docs/06-SYSTEM-ARCHITECTURE.md)–[`16`](docs/16-IMPLEMENTATION-PLAN.md)) and
> the foundational Flyway `V3` migration + 6-role model ship as Phase 0. See the
> [docs index](docs/README.md) and the [implementation plan](docs/16-IMPLEMENTATION-PLAN.md).

## Project structure
```
atm-platform/
├── backend/                # Spring Boot 3 REST API (com.atm.*)
├── frontend/               # React 18 + Vite SPA
├── docs/                   # analysis, blueprint, ER diagram, API reference, checklist
├── .github/workflows/      # CI pipeline (backend tests, frontend build, docker build)
├── docker-compose.yml      # db + api + web
├── Makefile                # common dev commands (make help)
├── .editorconfig           # cross-editor style
├── LICENSE                 # MIT
└── README.md
```

## Common commands (Makefile)
```bash
make help            # list all targets
make up              # docker compose up --build (full stack)
make down            # stop containers
make backend-test    # mvn verify
make frontend-build  # npm install + lint + build
```

## What was fixed vs. the legacy app
| Legacy problem | Resolution |
|----------------|------------|
| Plain-text passwords | BCrypt hashing |
| Hardcoded DB credentials in source | Externalized env config |
| Password echoed into hidden HTML fields | Stateless JWT, credentials never returned |
| No auth on deposit/withdraw/balance | JWT + role-based access control |
| `int` money + `amount`/`balance` column bug | `BigDecimal` / `NUMERIC(19,2)`, single schema |
| Non-atomic withdraw (double-spend) | `@Transactional` + pessimistic + optimistic locking |
| No transactions/history | Append-only ledger + paged history + mini statement |
| No validation / error handling | Bean Validation + global exception handler |
| No audit trail | `audit_logs` + JPA auditing |

## Repository layout
```
atm-platform/
├── backend/      # Spring Boot REST API (com.atm.*)
├── frontend/     # React + Vite SPA
├── docs/         # analysis, blueprint, ER diagram, API reference, file checklist
├── docker-compose.yml
└── .env.example
```

## Run with Docker (recommended)
```bash
cd atm-platform
cp .env.example .env        # edit secrets
docker compose up --build
```
- Frontend: http://localhost:8081
- API:      http://localhost:8080/api/v1
- Swagger:  http://localhost:8080/swagger-ui.html

## Run locally (without Docker)
**Database** — start PostgreSQL and create a DB `atm` (user/pass `atm`/`atm`), or point env vars at your own.

**Backend**
```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5432/atm DB_USERNAME=atm DB_PASSWORD=atm \
JWT_SECRET="<64+ byte secret>" mvn spring-boot:run
```
Flyway applies `V1__init_schema.sql` and `V2__seed_data.sql` automatically.

**Frontend**
```bash
cd frontend
npm install
npm run dev     # http://localhost:5173 (proxies /api to :8080)
```

## Seeded accounts (change in production!)
| Username | Password | Role | Account |
|----------|----------|------|---------|
| `admin`  | `password` | ADMIN | 1000000000 |
| `jdoe`   | `password` | CUSTOMER | 1000000001 (balance 5000.00) |

## Key API endpoints
`POST /api/v1/auth/register` · `POST /api/v1/auth/login` · `POST /api/v1/auth/refresh`
`GET /api/v1/accounts/me` · `POST /api/v1/accounts/deposit|withdraw|transfer`
`GET /api/v1/transactions` · `GET /api/v1/transactions/mini-statement`
`GET /api/v1/users/profile` · `GET /api/v1/admin/users`

See [`docs/05-API-REFERENCE.md`](docs/05-API-REFERENCE.md) for the full list.

## Tests
```bash
cd backend && mvn test
```

## Build notes
This project was generated in a sandbox without access to the public Maven/npm registries,
so the dependency download + full `mvn package` / `npm install` could not be executed there.
The code targets the exact dependency versions pinned in `backend/pom.xml` and
`frontend/package.json`; run the commands above in an environment with registry access to build.
