# Dayflow HRMS — Data Flow Diagrams (DFD)

> Gane-Sarson style notation, drawn with Mermaid flowcharts: rectangles = external entities, rounded rectangles = processes, open-ended bars = data stores.

## 1. Level 0 — Context Diagram

```mermaid
flowchart LR
    Employee[/"Employee"/]
    AdminHR[/"Admin / HR Officer"/]
    Mail[/"SMTP Mail Server"/]

    System("0.0\nDayflow HRMS")

    Employee -- "credentials, check-in/out,\nleave requests, profile edits" --> System
    System -- "profile data, attendance,\nleave status, payslip" --> Employee

    AdminHR -- "employee onboarding data,\nleave decisions, salary config" --> System
    System -- "employee directory, reports,\napproval queue, dashboards" --> AdminHR

    System -- "leave-decision / temp-password\nemail content" --> Mail
```

## 2. Level 1 — Major Processes

```mermaid
flowchart TB
    Employee[/"Employee"/]
    AdminHR[/"Admin / HR Officer"/]
    Mail[/"SMTP Mail Server"/]

    P1("1.0\nAuthenticate &\nProvision Account")
    P2("2.0\nManage Employee\nProfile")
    P3("3.0\nTrack\nAttendance")
    P4("4.0\nManage Leave /\nTime-Off")
    P5("5.0\nManage Payroll\n& Salary")
    P6("6.0\nManage\nNotifications")
    P7("7.0\nGenerate Reports\n& Dashboards")

    D1[("D1 Users")]
    D2[("D2 Employees")]
    D3[("D3 Attendance")]
    D4[("D4 Leave Requests\n& Allocations")]
    D5[("D5 Salary Structures\n& Payslips")]
    D6[("D6 Notifications")]

    AdminHR -- "onboarding form" --> P1
    P1 -- "login ID + temp password" --> D1
    P1 <-- "credentials" --> D1
    Employee -- "sign-in" --> P1
    P1 -- "JWT" --> Employee
    P1 -- "JWT" --> AdminHR

    Employee -- "profile edits" --> P2
    AdminHR -- "employee edits" --> P2
    P2 <--> D2
    P2 -- "directory / profile view" --> Employee
    P2 -- "directory / profile view" --> AdminHR

    Employee -- "check-in/out" --> P3
    P3 <--> D3
    P3 -- "my attendance + counters" --> Employee
    AdminHR -- "date filter, search" --> P3
    P3 -- "all-employee attendance" --> AdminHR
    P3 -- "payable days" --> P5

    Employee -- "leave application" --> P4
    AdminHR -- "approve/reject, allocation edit" --> P4
    P4 <--> D4
    P4 -- "balances + calendar" --> Employee
    P4 -- "approval queue" --> AdminHR
    P4 -- "unpaid-leave days" --> P5
    P4 -- "leave event" --> P6

    Employee -- "view own salary (read)" --> P5
    AdminHR -- "wage & component config" --> P5
    P5 <--> D5
    P5 -- "salary info / payslip" --> Employee
    P5 -- "salary control panel" --> AdminHR
    P5 -- "generation event" --> P6

    P3 -- "missed checkout event" --> P6
    P6 <--> D6
    P6 -- "in-app bell" --> Employee
    P6 -- "in-app bell + alerts" --> AdminHR
    P6 -- "email" --> Mail

    D2 --> P7
    D3 --> P7
    D4 --> P7
    D5 --> P7
    P7 -- "attendance %, leave trends,\nheadcount, payroll reports" --> AdminHR
```

## 3. Level 2 — Process 4.0 Manage Leave / Time-Off (decomposed)

```mermaid
flowchart TB
    Employee[/"Employee"/]
    AdminHR[/"Admin / HR Officer"/]

    P41("4.1\nView Balances\n& Calendar")
    P42("4.2\nSubmit Leave\nRequest")
    P43("4.3\nReview & Decide\n(Approve/Reject)")
    P44("4.4\nManage\nAllocation")

    D4a[("D4a Leave Allocations")]
    D4b[("D4b Leave Requests")]
    D4c[("D4c Public Holidays")]

    P6[("→ 6.0 Manage Notifications")]
    P5[("→ 5.0 Manage Payroll")]

    Employee -- "view request" --> P41
    D4a --> P41
    D4b --> P41
    D4c --> P41
    P41 -- "balance cards + colour-coded calendar" --> Employee

    Employee -- "type, dates, allocation, attachment" --> P42
    P42 -- "validate against balance" --> D4a
    P42 -- "new request (PENDING)" --> D4b
    P42 -- "leave-applied event" --> P6

    AdminHR -- "review queue" --> P43
    D4b --> P43
    P43 -- "APPROVED: decrement balance" --> D4a
    P43 -- "status + comment" --> D4b
    P43 -- "leave-decided event" --> P6
    P43 -- "approved paid/unpaid days" --> P5

    AdminHR -- "adjust days" --> P44
    P44 --> D4a
```

## 4. Level 2 — Process 5.0 Manage Payroll & Salary (decomposed)

```mermaid
flowchart TB
    AdminHR[/"Admin / HR Officer"/]
    Employee[/"Employee"/]

    P51("5.1\nConfigure Wage &\nSalary Components")
    P52("5.2\nCompute Payable\nDays")
    P53("5.3\nGenerate\nPayslip")
    P54("5.4\nServe Salary Info\n/ Payslip")

    D5a[("D5a Salary Structures\n& Components")]
    D5b[("D5b Payslips")]
    D3[("D3 Attendance")]
    D4[("D4 Leave Requests")]

    AdminHR -- "monthly wage,\ncomponent % / fixed" --> P51
    P51 -- "validate Σcomponents ≤ wage,\nauto-compute Fixed Allowance" --> D5a

    D3 --> P52
    D4 --> P52
    P52 -- "payable days for month" --> P53

    D5a --> P53
    P53 -- "gross, deductions, net, PDF" --> D5b

    Employee -- "view/download" --> P54
    AdminHR -- "view/download any" --> P54
    D5a --> P54
    D5b --> P54
    P54 -- "read-only salary info / payslip PDF" --> Employee
    P54 -- "full salary control view" --> AdminHR
```

## 5. Data Store Reference

| Store | Backing table(s) (see [ER Diagram](04-er-diagram.md)) |
|---|---|
| D1 Users | `app_user` |
| D2 Employees | `employee`, `resume`, `skill`, `certification`, `bank_detail` |
| D3 Attendance | `attendance` |
| D4 Leave Requests & Allocations | `leave_request`, `leave_allocation`, `leave_type`, `public_holiday` |
| D5 Salary Structures & Payslips | `salary_structure`, `salary_component`, `payslip` |
| D6 Notifications | `notification`, `notification_preference` |

---
**Related documents:** [Process Flows](07-process-flows.md) · [ER Diagram](04-er-diagram.md) · [HLD](02-hld.md)
