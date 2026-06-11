# Phase 0/1 — Current System Report

## 1. Project Inventory

### Folder structure (legacy)
```
AtmSimulation/
├── .classpath / .project / .settings/    # Eclipse Dynamic Web Project metadata
├── src/
│   ├── BalanceEnquiry.java                # Servlet (default package)
│   ├── CreateAccount.java                 # Servlet (default package)
│   ├── Deposit.java                       # Servlet (default package)
│   ├── ForgotPassword.java                # Servlet (default package)
│   ├── Login.java                         # Servlet (default package)
│   ├── Withdraw.java                      # Servlet (default package)
│   └── bandi/Second.java                  # JDBC data-access "god class"
└── WebContent/
    ├── bank.html                          # Login page
    ├── create_account.jsp                 # Registration page
    ├── forgotpass.jsp                     # Password reset page
    ├── First.jsp                          # Post-login dashboard
    ├── web.xml                            # (stale/incorrect) servlet mappings
    └── META-INF/MANIFEST.MF
```

### Component inventory
| Type | Files |
|------|-------|
| Servlets | `Login`, `CreateAccount`, `Deposit`, `Withdraw`, `BalanceEnquiry`, `ForgotPassword` |
| Data access (DAO-ish) | `bandi.Second` (static methods + inner `User` class) |
| JSP pages | `First.jsp`, `create_account.jsp`, `forgotpass.jsp` |
| HTML | `bank.html` |
| Config | `web.xml`, Eclipse `.project`/`.classpath`/`.settings` |
| SQL files | **None** (schema is implicit / created manually) |
| JS files | **None** (no client-side scripts) |
| CSS | Inline `<style>` only |
| Dependencies | Servlet API (container-provided), `com.mysql.jdbc.Driver` (MySQL Connector/J), no build tool (no Maven/Gradle) |

## 2. Business Flow Analysis

Modules actually implemented:
- **Authentication** — `bank.html` → `/Login` → `Second.m1()` validates `name`+`password`, forwards to `First.jsp`.
- **Registration** — `create_account.jsp` → `/CreateAccount` → `Second.createAccount()`.
- **Balance Inquiry** — `First.jsp` → `/BalanceEnquiry` → re-reads user via `Second.m1()`.
- **Deposit** — `First.jsp` → `/Deposit` → `Second.deposit()`.
- **Withdrawal** — `First.jsp` → `/Withdraw` → `Second.withdraw()` (checks balance).
- **Password Reset** — `forgotpass.jsp` → `/ForgotPassword` → `Second.forgotPassword()` (matches name+mobile).

Modules **NOT** implemented (required by target scope): Fund Transfer, Transaction History, Mini Statement, Card Management, User Management, Admin features, proper Session/Token management.

### Business flow map (legacy)
```
[bank.html] --login--> /Login --m1()--> [First.jsp dashboard]
   |                                          |--deposit--> /Deposit --> deposit()
   |--create--> create_account.jsp            |--withdraw-> /Withdraw -> withdraw()
   |                --> /CreateAccount          |--balance--> /BalanceEnquiry -> m1()
   |--forgot--> forgotpass.jsp
                    --> /ForgotPassword
```

## 3. Security Assessment

| # | Finding | Severity | Evidence |
|---|---------|----------|----------|
| S1 | **Plain-text passwords** stored & compared | **CRITICAL** | `insert into user (... password ...)`, `where ... password=?` |
| S2 | **Hardcoded DB credentials** (`root` / `Naveen@143`) in source | **CRITICAL** | every method in `Second.java` |
| S3 | **Password sent back to browser and echoed into hidden form fields** | **CRITICAL** | `First.jsp` `<input type="hidden" name="upass" value="...getPassword()">` |
| S4 | **No authentication on money operations** — `/Deposit`,`/Withdraw`,`/BalanceEnquiry` trust request params, no session | **CRITICAL** | servlets read `uname`/`upass` from request only |
| S5 | **Login via GET** with credentials in query string (logged in access logs/history) | **HIGH** | `Login.doGet` reads `uname`/`upass` |
| S6 | **No authorization / roles** | **HIGH** | no role concept anywhere |
| S7 | **No session management** (state passed in hidden fields each request) | **HIGH** | stateless-by-accident, no `HttpSession` |
| S8 | **Schema bug → guaranteed runtime errors**: read uses column `amount`, writes use column `balance` | **HIGH** | `m1()` reads `rs.getInt(5)`/`amount`; `deposit/withdraw` use `balance` |
| S9 | **Resource leaks** — `Connection`/`Statement`/`ResultSet` never closed | **HIGH** | no try-with-resources |
| S10 | **No input validation** (`Integer.parseInt` will throw on bad input; negative amounts allowed) | **MEDIUM** | `Deposit`/`Withdraw`/`CreateAccount` |
| S11 | **No audit logging** | **MEDIUM** | none |
| S12 | **No CSRF/secure headers/CORS strategy** | **MEDIUM** | none |
| S13 | **Stack traces / DB errors leaked** to stdout | **LOW** | `e.printStackTrace()` / `System.out.println(e)` |
| S14 | **Non-atomic withdraw** (read-then-update race / double-spend) | **HIGH** | select balance then update, no transaction/locking |

> Note: SQL Injection itself is largely mitigated because `PreparedStatement` is used everywhere — but every other fundamental control is missing.

## 4. Architecture Assessment

- **God class**: `Second` mixes connection management, persistence, and (partial) business rules with `static` methods → untestable, not thread-safe-friendly, no DI.
- **Default package** for all servlets; `web.xml` references wrong classes (`com.example.Login`, `bandi.Deposit`) that don't exist → `web.xml` is dead/incorrect; app relies on `@WebServlet` annotations instead.
- **Tight coupling** to MySQL driver and connection string in every method (duplicated 6×).
- **No layering** (controller/service/repository), no DTOs, no separation of concerns.
- **No transaction management** → money operations are not ACID.
- **Concurrency**: read-modify-write on balance is a classic lost-update / double-withdraw bug.
- **No build system, no tests, no logging framework, no config externalization.**

## 5. Database Assessment

Implicit single-table schema (`user`) inferred from code:
```
user(account_number PK auto?, name, password, mobile, amount/balance)
```
Problems:
- Column naming inconsistent (`amount` vs `balance`) → app is currently broken.
- No constraints (unique name/mobile, non-negative balance), no indexes beyond PK.
- Money stored as `int` → cannot represent cents/decimals; overflow risk.
- 1NF only; no separation of users / accounts / transactions; no audit trail.
- No roles, no account status, no card data, no refresh-token store.

### Recommended target (normalized, see Phase 3)
`users`, `roles` (RBAC), `accounts`, `transactions` (ledger), `refresh_tokens`, `audit_logs` with FKs, unique constraints, indexes, `NUMERIC(19,2)` money, optimistic locking + DB-level checks.
