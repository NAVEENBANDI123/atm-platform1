# Documentation Index

## Phase 0/1 — Legacy analysis & initial migration
| Doc | Description |
|-----|-------------|
| [01-CURRENT-SYSTEM-REPORT.md](01-CURRENT-SYSTEM-REPORT.md) | Legacy AtmSimulation inventory, security & architecture assessment |
| [02-MIGRATION-BLUEPRINT.md](02-MIGRATION-BLUEPRINT.md) | Target stack, clean architecture, legacy→target mapping |
| [03-FILE-CHECKLIST.md](03-FILE-CHECKLIST.md) | Per-file generation checklist |
| [04-ER-DIAGRAM.md](04-ER-DIAGRAM.md) | Initial normalized ER design |
| [05-API-REFERENCE.md](05-API-REFERENCE.md) | Initial REST API reference |

## Enterprise upgrade — small-bank core banking system
These design the evolution into a full core banking platform (approval workflows, RBAC,
onboarding, cards, loans, async email, auditing). Foundational code (6-role model +
Flyway `V3`) ships alongside; remaining phases are specified in doc 16.

| Doc | Description |
|-----|-------------|
| [06-SYSTEM-ARCHITECTURE.md](06-SYSTEM-ARCHITECTURE.md) | Topology, clean-architecture layers, subsystems, deployment |
| [07-DATABASE-DESIGN.md](07-DATABASE-DESIGN.md) | Updated ER, enums, full DDL, indexing, FKs, constraints |
| [08-ROLE-PERMISSION-MATRIX.md](08-ROLE-PERMISSION-MATRIX.md) | 6-role RBAC matrix + enforcement mapping |
| [09-REST-API-DESIGN.md](09-REST-API-DESIGN.md) | All module endpoints (dual portal, approvals, teller, reports) |
| [10-PACKAGE-STRUCTURE.md](10-PACKAGE-STRUCTURE.md) | Spring Boot package/module layout |
| [11-SECURITY-ARCHITECTURE.md](11-SECURITY-ARCHITECTURE.md) | JWT, dual-portal login, locking, CSRF, audit security |
| [12-FRONTEND-STRUCTURE.md](12-FRONTEND-STRUCTURE.md) | React customer + employee portal page structure |
| [13-EMAIL-TEMPLATES.md](13-EMAIL-TEMPLATES.md) | Email templates + async notification pipeline |
| [14-APPROVAL-WORKFLOWS.md](14-APPROVAL-WORKFLOWS.md) | Customer / loan / card maker-checker workflow diagrams |
| [15-AUDIT-LOGGING-DESIGN.md](15-AUDIT-LOGGING-DESIGN.md) | Old/new value capture, masking, tracked actions |
| [16-IMPLEMENTATION-PLAN.md](16-IMPLEMENTATION-PLAN.md) | Phased, production-ready rollout plan |
