# 13 — Email Templates & Notification Design

All emails are sent **asynchronously** (`@Async` event listener → `JavaMailSender`) so the
triggering transaction never blocks or fails on SMTP issues. Templates are Thymeleaf HTML
under `src/main/resources/templates/email/`, rendered with a shared layout
(`layout.html`: bank header/footer, brand color, support contact).

---

## 1. Template catalog

| `EmailTemplate` enum | Trigger | Recipient | Subject |
|----------------------|---------|-----------|---------|
| `CUSTOMER_REGISTERED` | Customer registers | Customer | Application Submitted Successfully |
| `ADMIN_NEW_REGISTRATION` | Customer registers | Admin/Accountant | New Customer Registration |
| `ACCOUNT_APPROVED` | Customer approved | Customer | Account Approved |
| `ACCOUNT_REJECTED` | Customer rejected | Customer | Account Application Rejected |
| `LOGIN_ALERT` | Login success | Customer | New Login to Your Account |
| `PASSWORD_CHANGED` | Password changed/reset | Customer | Your Password Was Changed |
| `FUND_TRANSFER` | Transfer completes | Customer (sender) | Fund Transfer Successful |
| `DEPOSIT` | Deposit posted | Customer | Amount Credited to Your Account |
| `WITHDRAWAL` | Withdrawal posted | Customer | Amount Debited from Your Account |
| `LOAN_REVIEW_REQUIRED` | Loan applied | Loan Officer | Loan Review Required |
| `LOAN_APPROVED` | Loan sanctioned | Customer | Loan Approved |
| `LOAN_REJECTED` | Loan rejected | Customer | Loan Application Rejected |
| `CARD_REVIEW_REQUIRED` | Card applied | Card Officer | Card Review Required |
| `CARD_APPROVED` | Card issued | Customer | Card Approved |
| `CARD_REJECTED` | Card rejected | Customer | Card Application Rejected |

Each template receives a typed context map (e.g. `{ name, customerId, accountNumber }`).

---

## 2. Required template bodies (verbatim per spec)

### CUSTOMER_REGISTERED — "Application Submitted Successfully"
```
Dear {{prefix}} {{lastName}},

Your account application has been submitted and is awaiting approval from the bank.

We will notify you by email once a decision has been made.

Regards,
{{bankName}}
```

### ADMIN_NEW_REGISTRATION — "New Customer Registration"
```
Hello,

A new customer registration is waiting for approval.

Applicant: {{firstName}} {{lastName}}
Email: {{email}}    Mobile: {{mobile}}
Submitted: {{submittedAt}}

Please review it in the staff portal → Approvals → Customers.
```

### ACCOUNT_APPROVED — "Account Approved"
```
Dear {{prefix}} {{lastName}},

Your account has been approved. Welcome to {{bankName}}!

Customer ID:     {{customerId}}
Account Number:  {{accountNumber}}
Account Type:    {{accountType}}

You can now log in to the customer portal.
```

### ACCOUNT_REJECTED — "Account Application Rejected"
```
Dear {{prefix}} {{lastName}},

We regret to inform you that your account application has been rejected.

Reason: {{reason}}

If you believe this is a mistake, please contact support.
```

### LOAN_APPROVED — "Loan Approved"
```
Dear {{prefix}} {{lastName}},

Your {{loanType}} loan has been approved.

Loan Account: {{loanAccountNo}}
Amount:       {{amount}}
Tenure:       {{tenureMonths}} months
EMI:          {{emiAmount}}
Disbursed to: {{accountNumber}}

Your repayment schedule is available in the customer portal.
```

### LOAN_REJECTED — "Loan Application Rejected"
```
Dear {{prefix}} {{lastName}},

Your {{loanType}} loan application has been rejected.

Reason: {{reason}}
```

### CARD_APPROVED — "Card Approved"
```
Dear {{prefix}} {{lastName}},

Your {{cardType}} card has been approved and issued.

Card Number: {{maskedCardNumber}}
Expiry:      {{expiry}}

The full card details are available in the customer portal.
```

### CARD_REJECTED — "Card Application Rejected"
```
Dear {{prefix}} {{lastName}},

Your {{cardType}} card application has been rejected.

Reason: {{reason}}
```

### Transaction alerts (DEPOSIT / WITHDRAWAL / FUND_TRANSFER)
```
Dear {{prefix}} {{lastName}},

A {{txnType}} of {{amount}} was {{creditOrDebit}} {{accountNumber}} on {{timestamp}}.
Available balance: {{balanceAfter}}
Reference: {{reference}}

If you did not authorize this, contact us immediately.
```

### LOGIN_ALERT / PASSWORD_CHANGED / *_REVIEW_REQUIRED
- **LOGIN_ALERT**: time, IP, device; "If this wasn't you, reset your password."
- **PASSWORD_CHANGED**: confirmation + "If this wasn't you, contact support."
- **LOAN_REVIEW_REQUIRED / CARD_REVIEW_REQUIRED**: applicant + application id + link to the
  staff review queue.

---

## 3. Async pipeline

```
Service (@Transactional)
   └─ publishes EmailRequestedEvent(template, to, contextMap)
         (published AFTER_COMMIT so emails only fire on success)
            └─ @Async @TransactionalEventListener(phase = AFTER_COMMIT)
                  EmailEventListener
                     ├─ render Thymeleaf template
                     ├─ JavaMailSender.send(MimeMessage)
                     ├─ on success: write Notification (in-app center)
                     └─ on failure: log + retry (max 3, backoff); never rethrow
```

Key points:
- `@TransactionalEventListener(AFTER_COMMIT)` guarantees we never email about a rolled-back
  action (e.g. a failed transfer).
- A dedicated `emailExecutor` (`ThreadPoolTaskExecutor`) isolates mail load from request
  threads.
- Every email also creates an in-app `Notification` row for the Notification Center.

---

## 4. Configuration

`application.yml` additions:
```yaml
spring:
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}        # MailHog in dev
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail.smtp.auth: ${MAIL_SMTP_AUTH:false}
      mail.smtp.starttls.enable: ${MAIL_STARTTLS:false}
app:
  mail:
    from: ${MAIL_FROM:no-reply@atm.local}
    admin-recipients: ${MAIL_ADMIN:admin@atm.local}
    bank-name: ${BANK_NAME:ATM Bank}
  async:
    email-pool-size: 4
```

Dev compose adds a `mailhog` service (SMTP `1025`, UI `8025`) so every workflow email is
visible without a real provider.
