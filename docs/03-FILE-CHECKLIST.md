# Project File Checklist

Legend: `[ ]` Not Generated · `[x]` Generated

## Docs
- [x] docs/01-CURRENT-SYSTEM-REPORT.md
- [x] docs/02-MIGRATION-BLUEPRINT.md
- [x] docs/03-FILE-CHECKLIST.md
- [x] docs/04-ER-DIAGRAM.md
- [x] docs/05-API-REFERENCE.md

## Database (Flyway)
- [x] backend/src/main/resources/db/migration/V1__init_schema.sql
- [x] backend/src/main/resources/db/migration/V2__seed_data.sql

## Backend — build & config
- [x] backend/pom.xml
- [x] backend/src/main/resources/application.yml
- [x] backend/src/main/resources/application-docker.yml
- [x] backend/src/main/resources/logback-spring.xml
- [x] backend/src/main/java/com/atm/AtmApplication.java
- [x] config/OpenApiConfig.java
- [x] config/JpaAuditingConfig.java
- [x] config/AppProperties.java

## Backend — common / util
- [x] common/ApiResponse.java
- [x] common/PageResponse.java
- [x] common/Constants.java
- [x] util/AccountNumberGenerator.java

## Backend — entity
- [x] entity/BaseEntity.java
- [x] entity/User.java
- [x] entity/Role.java
- [x] entity/RoleName.java
- [x] entity/Account.java
- [x] entity/AccountStatus.java
- [x] entity/Transaction.java
- [x] entity/TransactionType.java
- [x] entity/TransactionStatus.java
- [x] entity/RefreshToken.java
- [x] entity/AuditLog.java

## Backend — dto.request
- [x] dto/request/LoginRequest.java
- [x] dto/request/RegisterRequest.java
- [x] dto/request/RefreshTokenRequest.java
- [x] dto/request/DepositRequest.java
- [x] dto/request/WithdrawRequest.java
- [x] dto/request/TransferRequest.java
- [x] dto/request/ForgotPasswordRequest.java
- [x] dto/request/ResetPasswordRequest.java
- [x] dto/request/UpdateAccountStatusRequest.java

## Backend — dto.response
- [x] dto/response/AuthResponse.java
- [x] dto/response/UserResponse.java
- [x] dto/response/AccountResponse.java
- [x] dto/response/TransactionResponse.java

## Backend — repository
- [x] repository/UserRepository.java
- [x] repository/RoleRepository.java
- [x] repository/AccountRepository.java
- [x] repository/TransactionRepository.java
- [x] repository/RefreshTokenRepository.java
- [x] repository/AuditLogRepository.java

## Backend — mapper
- [x] mapper/UserMapper.java
- [x] mapper/AccountMapper.java
- [x] mapper/TransactionMapper.java

## Backend — exception
- [x] exception/ApiException.java
- [x] exception/ResourceNotFoundException.java
- [x] exception/BadRequestException.java
- [x] exception/InsufficientBalanceException.java
- [x] exception/DuplicateResourceException.java
- [x] exception/GlobalExceptionHandler.java
- [x] exception/ErrorResponse.java

## Backend — security
- [x] security/UserPrincipal.java
- [x] security/CustomUserDetailsService.java
- [x] security/JwtTokenProvider.java
- [x] security/JwtAuthenticationFilter.java
- [x] security/JwtAuthenticationEntryPoint.java
- [x] security/SecurityConfig.java

## Backend — audit
- [x] audit/AuditService.java
- [x] audit/Auditable.java
- [x] audit/AuditAspect.java

## Backend — service / impl
- [x] service/AuthService.java
- [x] service/AccountService.java
- [x] service/TransactionService.java
- [x] service/UserService.java
- [x] service/impl/AuthServiceImpl.java
- [x] service/impl/AccountServiceImpl.java
- [x] service/impl/TransactionServiceImpl.java
- [x] service/impl/UserServiceImpl.java

## Backend — controller
- [x] controller/AuthController.java
- [x] controller/AccountController.java
- [x] controller/TransactionController.java
- [x] controller/UserController.java
- [x] controller/AdminController.java

## Backend — tests
- [x] test/.../AccountServiceImplTest.java
- [x] test/.../AuthControllerIntegrationTest.java

## Frontend
- [x] frontend/package.json
- [x] frontend/vite.config.js
- [x] frontend/index.html
- [x] frontend/.env.example
- [x] frontend/src/main.jsx
- [x] frontend/src/App.jsx
- [x] frontend/src/theme.js
- [x] frontend/src/api/axiosClient.js
- [x] frontend/src/app/store.js
- [x] frontend/src/features/auth/authSlice.js
- [x] frontend/src/features/auth/authApi.js
- [x] frontend/src/features/account/accountApi.js
- [x] frontend/src/routes/ProtectedRoute.jsx
- [x] frontend/src/components/Layout.jsx
- [x] frontend/src/pages/LoginPage.jsx
- [x] frontend/src/pages/RegisterPage.jsx
- [x] frontend/src/pages/ForgotPasswordPage.jsx
- [x] frontend/src/pages/DashboardPage.jsx
- [x] frontend/src/pages/TransferPage.jsx
- [x] frontend/src/pages/TransactionsPage.jsx
- [x] frontend/src/pages/AdminUsersPage.jsx

## DevOps
- [x] backend/Dockerfile
- [x] frontend/Dockerfile
- [x] frontend/nginx.conf
- [x] docker-compose.yml
- [x] .env.example
- [x] README.md


---

## Enterprise upgrade — small-bank core banking system

### Design package (Phase 0)
- [x] docs/README.md (docs index)
- [x] docs/06-SYSTEM-ARCHITECTURE.md
- [x] docs/07-DATABASE-DESIGN.md
- [x] docs/08-ROLE-PERMISSION-MATRIX.md
- [x] docs/09-REST-API-DESIGN.md
- [x] docs/10-PACKAGE-STRUCTURE.md
- [x] docs/11-SECURITY-ARCHITECTURE.md
- [x] docs/12-FRONTEND-STRUCTURE.md
- [x] docs/13-EMAIL-TEMPLATES.md
- [x] docs/14-APPROVAL-WORKFLOWS.md
- [x] docs/15-AUDIT-LOGGING-DESIGN.md
- [x] docs/16-IMPLEMENTATION-PLAN.md

### Foundational code (Phase 0)
- [x] entity/RoleName.java — expanded to 6 roles (+ legacy aliases)
- [x] db/migration/V3__rbac_onboarding_and_products.sql — additive RBAC/onboarding/products schema

### Pending phases (see 16-IMPLEMENTATION-PLAN.md)
- [ ] Phase 1 — identity, dual-portal login, RBAC enforcement
- [ ] Phase 2 — customer onboarding & approval
- [ ] Phase 3 — async email + notification center
- [ ] Phase 4 — audit enrichment (old/new value)
- [ ] Phase 5 — teller (deposit/withdraw) & account types
- [ ] Phase 6 — fund transfer hardening & beneficiaries
- [ ] Phase 7 — card workflow
- [ ] Phase 8 — loan workflow
- [ ] Phase 9 — deposits (FD/RD), nominees, requests, complaints
- [ ] Phase 10 — reports & exports (PDF/Excel)
- [ ] Phase 11 — scheduling & hardening
- [ ] Phase 12 — frontend dual portal
