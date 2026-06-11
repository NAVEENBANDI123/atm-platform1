# 10 — Spring Boot Package Structure

Extends the existing `com.atm` layout (`02-MIGRATION-BLUEPRINT.md`). New code is grouped
by **feature module** under shared layer packages, keeping the clean-architecture
boundaries (controller → service → repository, domain in the middle).

```
com.atm
├── AtmApplication.java
│
├── config/                      # cross-cutting configuration
│   ├── AppProperties.java            (extend: mail, limits, workflow)
│   ├── JpaAuditingConfig.java
│   ├── OpenApiConfig.java
│   ├── AsyncConfig.java         # NEW @EnableAsync + emailExecutor
│   ├── SchedulingConfig.java    # NEW @EnableScheduling
│   └── MailConfig.java          # NEW JavaMailSender + Thymeleaf templ. engine
│
├── common/                      # ApiResponse, PageResponse, Constants (extend)
│
├── security/                    # JWT, filter, dual-portal user details (extend)
│   ├── SecurityConfig.java           (add new URL rules)
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetailsService.java
│   ├── UserPrincipal.java            (add userType, status)
│   └── PortalAuthenticationService.java  # NEW customer vs staff login guard
│
├── audit/                       # AuditService, AuditAspect, @Auditable (extend old/new value)
│
├── email/                       # NEW notification subsystem
│   ├── NotificationService.java          # interface (publishes events)
│   ├── EmailTemplate.java                # enum of templates
│   ├── event/EmailRequestedEvent.java
│   ├── listener/EmailEventListener.java  # @Async @EventListener -> JavaMailSender
│   └── impl/NotificationServiceImpl.java
│
├── workflow/                    # NEW generic maker-checker engine
│   ├── WorkflowStatus.java
│   ├── ReviewDecision.java
│   └── ApprovalGuard.java                # maker != checker enforcement
│
├── entity/                      # JPA entities + enums (existing + new)
│   ├── User.java  Role.java  RoleName.java  Account.java  Transaction.java ...
│   ├── enums/  UserType, UserStatus, AccountType, Gender, Prefix, KycStatus,
│   │           CardType, CardStatus, LoanType, DepositType, ComplaintStatus, RequestType
│   ├── Branch.java
│   ├── CustomerProfile.java  EmployeeProfile.java
│   ├── CardApplication.java  Card.java
│   ├── LoanApplication.java  LoanAccount.java  LoanScheduleEntry.java
│   ├── Beneficiary.java  Nominee.java  Deposit.java
│   ├── ServiceRequest.java  Complaint.java  Notification.java
│   ├── ScheduledTransfer.java  LoginHistory.java
│   └── AuditLog.java  (add oldValue/newValue)
│
├── dto/
│   ├── request/   CustomerRegisterRequest, AddressDto, EmployeeCreateRequest,
│   │              ApproveRequest, RejectRequest, ReviewRequest, LoanDecisionRequest,
│   │              CardApplyRequest, LoanApplyRequest, BeneficiaryRequest,
│   │              TransferRequest(extend), DepositRequest(teller), ...
│   └── response/  CustomerDashboardResponse, CustomerDetailResponse,
│                  CardResponse, LoanResponse, LoanScheduleResponse,
│                  BeneficiaryResponse, NotificationResponse, ReportRow*, ...
│
├── mapper/                      # MapStruct mappers per aggregate
│
├── repository/                  # one Spring Data repo per aggregate root
│   ├── (existing) UserRepository, AccountRepository, TransactionRepository ...
│   ├── BranchRepository, CustomerProfileRepository, EmployeeProfileRepository
│   ├── CardApplicationRepository, CardRepository
│   ├── LoanApplicationRepository, LoanAccountRepository, LoanScheduleRepository
│   ├── BeneficiaryRepository, NomineeRepository, DepositRepository
│   ├── ServiceRequestRepository, ComplaintRepository, NotificationRepository
│   ├── ScheduledTransferRepository, LoginHistoryRepository
│
├── service/                     # interfaces, grouped by module
│   ├── auth/        AuthService, OnboardingService
│   ├── customer/    CustomerAccountService, ProfileService, BeneficiaryService,
│   │                StatementService, NotificationCenterService
│   ├── approval/    CustomerApprovalService, LoanWorkflowService, CardWorkflowService
│   ├── product/     CardService, LoanService, DepositService
│   ├── teller/      TellerService
│   ├── admin/       EmployeeService, BranchService, ActivityService
│   ├── report/      ReportService, ReportExportService
│   └── txn/         TransactionService, TransferService, ScheduledTransferService
│
├── service/impl/                # @Transactional implementations mirroring above
│
├── controller/                  # thin REST controllers, one per module
│   ├── AuthController (customer+staff), CustomerController, ApprovalController,
│   ├── LoanWorkflowController, CardWorkflowController, TellerController,
│   ├── EmployeeAdminController, BranchController, ReportController, AuditController
│
├── exception/                   # custom exceptions + GlobalExceptionHandler (extend)
│   └── WorkflowStateException.java, DailyLimitExceededException.java ...
│
├── report/                      # NEW export engine
│   ├── pdf/PdfReportWriter.java         # OpenPDF
│   └── excel/ExcelReportWriter.java     # Apache POI
│
├── scheduler/                   # NEW scheduled jobs
│   ├── ScheduledTransferJob.java
│   ├── DepositMaturityJob.java
│   └── RefreshTokenCleanupJob.java
│
└── util/                        # generators
    ├── AccountNumberGenerator.java (existing)
    ├── CustomerIdGenerator.java     # NEW
    ├── CardNumberGenerator.java     # NEW (Luhn-valid, masked)
    ├── LoanAccountNumberGenerator.java
    └── EmiCalculator.java           # NEW reducing-balance EMI + schedule
```

## Conventions (unchanged from existing code)

- Controllers depend only on **service interfaces**; impls live in `service/impl`.
- Request/response DTOs are Java `record`s with Bean Validation annotations (as today,
  e.g. `LoginRequest`).
- MapStruct mappers (`componentModel = "spring"`) translate entity ↔ DTO.
- Business logic + `@Transactional` strictly in `service/impl`.
- Lombok `@Builder`/`@Getter`/`@Setter` on entities, matching existing style.
- New dependencies to add in `pom.xml`: `spring-boot-starter-mail`,
  `spring-boot-starter-thymeleaf`, `org.apache.poi:poi-ooxml`, `com.github.librepdf:openpdf`.
