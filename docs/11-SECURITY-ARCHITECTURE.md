# 11 — Security Architecture

Builds on the existing stateless JWT setup (`SecurityConfig`, `JwtTokenProvider`,
`JwtAuthenticationFilter`, `CustomUserDetailsService`) and adds dual-portal login,
6-role RBAC, account locking hardening, and login tracking.

---

## 1. Trust boundaries

```
[Browser/SPA]  --TLS-->  [nginx]  --TLS-->  [Spring Boot API]  -->  [PostgreSQL]
                                                   │
                                                   └── @Async --> [SMTP relay]
```

- Frontend is untrusted; all authorization is enforced server-side.
- The SPA's portal split (`/customer/*` vs `/staff/*`) is **UX only**; the backend
  independently enforces `userType` on every login and token.

---

## 2. Authentication

### 2.1 Dual-portal login
- `POST /auth/customer/login` → `PortalAuthenticationService.login(req, CUSTOMER)`
- `POST /auth/staff/login`    → `PortalAuthenticationService.login(req, EMPLOYEE)`

The service authenticates credentials, then asserts:
```
user.userType == expectedPortal           else 403 "Use the correct portal"
user.status   == ACTIVE                    else 403 (PENDING_APPROVAL/REJECTED/DISABLED)
!user.accountLocked                        else 423
user.enabled                               else 403
```
A `PENDING_APPROVAL` customer therefore can never obtain a token.

### 2.2 JWT
- **Access token**: short-lived (15 min, existing default), signed HS256, carries
  `sub=username`, `roles`, and `ut` (userType) claims.
- **Refresh token**: opaque UUID persisted in `refresh_tokens`, revocable, 7-day TTL,
  rotated on use. On password reset / logout all refresh tokens for the user are revoked.
- Tokens are returned in the JSON body (no cookies) → CSRF surface minimized.

### 2.3 Password security
- BCrypt (existing `BCryptPasswordEncoder`). Passwords never returned in any DTO.
- Registration enforces `password == confirmPassword` + strength policy
  (min length, upper/lower/digit/symbol) via a custom validator.
- `change-password` and `reset-password` revoke existing sessions and email an alert.

---

## 3. Authorization (RBAC)

- `@EnableMethodSecurity` (already on) + `@PreAuthorize` on service/controller methods.
- URL matrix in `SecurityConfig` (see `08-ROLE-PERMISSION-MATRIX.md` §3).
- Authorities = role names with `ROLE_` prefix, so `hasRole('SUPER_ADMIN')` works.
- **Separation of duties** enforced in the workflow layer (`ApprovalGuard`): the
  recommending officer id must differ from the approving Super Admin id.
- Ownership checks: customer endpoints resolve the account/loan/card from the
  authenticated principal; a customer can never reference another customer's resource id.

---

## 4. Account locking & failed-login tracking

Existing behavior (kept and reused):
- `failed_attempts` increments on bad password; at `max-failed-attempts` (5) the account
  locks and `lock_time` is set; auto-unlock after `lock-duration-minutes` (15).

Additions:
- Every login attempt writes a `login_history` row (`LOGIN_SUCCESS` / `LOGIN_FAILED` /
  `LOGOUT`) with IP + user-agent.
- On `LOGIN_SUCCESS`, a "Login Alert" email is sent asynchronously (per requirements).
- Repeated failures across accounts from one IP can be throttled (bucket filter — noted
  as a hardening item).

---

## 5. Session management

- Stateless (`SessionCreationPolicy.STATELESS`) — no `JSESSIONID`.
- "Sessions" are the set of active refresh tokens per user; logout / reset revoke them.
- A future `GET /customer/sessions` + `DELETE /customer/sessions/{id}` can expose active
  refresh tokens for self-service revocation (noted, not in V1).

---

## 6. API security & headers

- CORS restricted to configured origins (existing `AppProperties.cors`).
- Security headers (existing): `frameOptions deny`, CSP `default-src 'self'`. Add
  `Referrer-Policy`, `X-Content-Type-Options: nosniff`, HSTS at the nginx tier.
- Request validation on all DTOs; size limits on free-text fields; `JSONB` payloads
  schema-checked in the service layer.
- Rate limiting (bucket4j or gateway) on `/auth/**` and money endpoints — hardening item.

---

## 7. CSRF strategy

The API is a **stateless, token-in-header** service consumed by an SPA. CSRF protection
is intentionally disabled for the JWT API because:

1. Authentication uses the `Authorization: Bearer` header, **not** cookies. CSRF exploits
   ambient cookie credentials; with no auth cookie there is nothing for a forged
   cross-site request to ride on.
2. CORS is locked to known origins.

If a future requirement moves refresh tokens into `HttpOnly` cookies, CSRF protection
**must** be re-enabled for the cookie-bearing endpoints (double-submit token or
`SameSite=Strict` + CSRF token). This decision is documented so it is revisited if the
token transport changes.

---

## 8. Audit security

- Audit writes run in `REQUIRES_NEW` transactions so they survive business rollbacks and
  never cause one (existing `AuditService` behavior).
- Audit logs are **append-only**; no update/delete endpoints are exposed.
- Sensitive values (passwords, full card numbers, full Aadhaar) are **never** stored in
  audit `old_value`/`new_value`; they are masked at the capture site.
- Audit read access is limited to `SUPER_ADMIN` and `ACCOUNTANT`.

---

## 9. Data protection

- Card numbers stored only as needed; customers see masked PAN (`**** **** **** 1234`).
- Aadhaar/PAN unique + format-validated; exposed masked in customer-facing responses.
- Money is `NUMERIC(19,2)`; no floating point.
- Secrets (JWT key, DB creds, SMTP creds) injected via environment variables / Docker
  secrets — never committed (existing `.env.example` pattern).
