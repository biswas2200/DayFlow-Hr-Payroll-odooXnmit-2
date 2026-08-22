# Dayflow HRMS — Design Documentation Index

This folder contains the technical design artifacts derived from:
- [`../Dayflow_HRMS_SRS.md`](../Dayflow_HRMS_SRS.md) — SRS v4.0 (authoritative requirements)
- `../../Original Source/pdf/` — earlier SRS draft (superseded by v4.0; kept for history)
- `../../Original Source/imgs/` — Excalidraw wireframe screenshots (UI source of truth, SRS §6)

All diagrams are authored as [Mermaid](https://mermaid.js.org/) code blocks, which render natively in GitHub, GitLab, VS Code (with the Mermaid extension), and most modern Markdown viewers.

## Documents

| # | Document | Contents |
|---|---|---|
| 1 | [System Architecture](01-system-architecture.md) | Architecture style, C4 context/container/component diagrams, deployment diagram, cross-cutting concerns, tech stack |
| 2 | [High-Level Design (HLD)](02-hld.md) | Module map, layered view, per-module responsibilities, role → capability matrix |
| 3 | [Low-Level Design (LLD)](03-lld.md) | Backend package structure, class diagrams per module, DB schema (DDL-level), core algorithms (Login ID, payable days, salary computation), Angular structure |
| 4 | [ER Diagram](04-er-diagram.md) | Full entity-relationship model, cardinalities, key design decisions |
| 5 | [Use Case Diagram](05-use-case-diagram.md) | Actors, use case diagram, primary-flow specifications, SRS traceability |
| 6 | [Data Flow Diagrams (DFD)](06-dfd.md) | Level 0 context, Level 1 major processes, Level 2 drill-down (Leave, Payroll) |
| 7 | [Process Flows](07-process-flows.md) | Flowcharts: onboarding, first login, check-in/out, leave apply/approve, payable-days calc, payslip generation, salary auto-recalculation |
| 8 | [Sequence Diagrams](08-sequence-diagrams.md) | Runtime call sequences for the 8 key flows across all modules |
| 9 | [API Documentation](09-api-documentation.md) | Endpoint reference table + full OpenAPI 3.0 spec (Swagger-ready) |
| 10 | [Class Diagram](10-class-diagram.md) | Unified UML class diagram of the final, implemented backend domain model |

## Reading order

- **New to the project?** Start at System Architecture → HLD → Use Case Diagram to get the shape of the system.
- **Implementing the backend?** LLD → ER Diagram → API Documentation → Sequence Diagrams.
- **Implementing the frontend?** Use Case Diagram → Process Flows → API Documentation.
- **Building payroll logic specifically?** LLD §2.5 (Payroll & Salary) → Process Flows §5–7 → Sequence Diagrams §6–7.

## Scope notes carried over from the SRS

- Admin and HR Officer are modeled as a single `ADMIN` role — SRS §2 lists them as one user class with one privilege set.
- The Salary Info tab is visible **read-only** to an employee on their own profile; only Admin can edit any employee's salary — this resolves the SRS §3.3.2 open item using the wireframe annotation as tie-breaker.
- Leave types shipped: Paid Time Off, Sick Leave, Unpaid Leave — the data model (`leave_type` table) is extensible for more without a schema change.
- Notifications, analytics, and reporting are treated as first-class modules here (matching SRS v4.0 §3.7–3.8), even though the earlier draft PDF filed them under "Future Enhancements" — v4.0 supersedes that draft.
