# Dayflow HRMS — Sequence Diagrams

> Runtime interaction detail for the key flows in [Process Flows](07-process-flows.md), showing component-level calls per the [LLD](03-lld.md) class model.

## 1. Sign In (JWT Issuance)

```mermaid
sequenceDiagram
    actor U as User
    participant SPA as Angular SPA
    participant AC as AuthController
    participant AS as AuthService
    participant UDS as UserDetailsServiceImpl
    participant DB as PostgreSQL
    participant JWT as JwtTokenProvider

    U->>SPA: enter Login ID/Email + Password
    SPA->>AC: POST /api/v1/auth/login
    AC->>AS: authenticate(loginId, password)
    AS->>UDS: loadUserByLoginId(loginId)
    UDS->>DB: SELECT app_user WHERE login_id=?
    DB-->>UDS: User row
    UDS-->>AS: UserDetails
    AS->>AS: BCrypt.matches(password, hash)
    alt credentials invalid
        AS-->>AC: AuthenticationException
        AC-->>SPA: 401 { error: "Invalid credentials" }
        SPA-->>U: show error message
    else credentials valid
        AS->>JWT: generateAccessToken(user) / generateRefreshToken(user)
        JWT-->>AS: accessToken, refreshToken
        AS-->>AC: LoginResponse(tokens, mustChangePassword)
        AC-->>SPA: 200 OK
        SPA->>SPA: store tokens, attach Authorization header
        alt mustChangePassword == true
            SPA-->>U: redirect to Security tab (forced)
        else
            SPA-->>U: redirect to Employee Directory
        end
    end
```

## 2. Admin Creates a New Employee (Provisioning)

```mermaid
sequenceDiagram
    actor A as Admin/HR
    participant SPA as Angular SPA
    participant EC as EmployeeController
    participant ES as EmployeeService
    participant LG as LoginIdGenerator
    participant TG as TempPasswordGenerator
    participant DB as PostgreSQL
    participant NS as NotificationService
    participant Mail as SMTP

    A->>SPA: fill Company/Name/Email/Phone/Password form
    SPA->>EC: POST /api/v1/employees
    EC->>ES: create(CreateEmployeeRequest)
    ES->>LG: generate(companyInitials, firstName, lastName, joinYear)
    LG-->>ES: loginId (e.g. OIJODO20220001)
    ES->>TG: generate()
    TG-->>ES: tempPassword
    ES->>DB: INSERT employee, resume, bank_detail (empty),\nINSERT app_user (must_change_password=true, BCrypt(tempPassword))
    DB-->>ES: saved entities
    ES->>NS: notify(email delivery: loginId + tempPassword)
    NS->>Mail: send onboarding email
    ES-->>EC: EmployeeResponse
    EC-->>SPA: 201 Created
    SPA-->>A: show success, new card in directory
```

## 3. Check-In / Check-Out

```mermaid
sequenceDiagram
    actor E as Employee
    participant SPA as Angular SPA (systray)
    participant AC as AttendanceController
    participant AS as AttendanceService
    participant DB as PostgreSQL

    E->>SPA: click "Check In"
    SPA->>AC: POST /api/v1/attendance/check-in
    AC->>AS: checkIn(employeeId)
    AS->>DB: SELECT attendance WHERE employee_id=? AND date=today
    alt no row for today
        AS->>DB: INSERT attendance(check_in_time=now, status=PRESENT)
    else already checked in
        AS-->>AC: 409 Conflict "already checked in"
    end
    DB-->>AS: attendance row
    AS-->>AC: AttendanceDto
    AC-->>SPA: 200 OK
    SPA-->>E: status dot turns green, "Since HH:MM" starts

    Note over E,SPA: ...later in the day...

    E->>SPA: click "Check Out"
    SPA->>AC: POST /api/v1/attendance/check-out
    AC->>AS: checkOut(employeeId)
    AS->>DB: UPDATE attendance SET check_out_time=now,\nwork_hours=..., extra_hours=... WHERE id=?
    DB-->>AS: updated row
    AS-->>AC: AttendanceDto
    AC-->>SPA: 200 OK
    SPA-->>E: systray resets, today's row visible in Attendance tab
```

## 4. Apply for Leave

```mermaid
sequenceDiagram
    actor E as Employee
    participant SPA as Angular SPA
    participant LC as LeaveController
    participant LS as LeaveService
    participant DB as PostgreSQL
    participant NS as NotificationService

    E->>SPA: open "Time Off Type Request" modal, fill & submit
    SPA->>LC: POST /api/v1/leave-requests
    LC->>LS: apply(employeeId, ApplyLeaveRequest)
    LS->>DB: SELECT leave_allocation WHERE employee_id=? AND leave_type_id=? AND year=?
    DB-->>LS: allocation (allocatedDays, usedDays)
    alt paid type & insufficient balance
        LS-->>LC: 400 Bad Request "insufficient balance"
        LC-->>SPA: error
        SPA-->>E: show validation message
    else attachment required & missing
        LS-->>LC: 400 Bad Request "attachment required"
    else valid
        LS->>DB: INSERT leave_request(status=PENDING)
        DB-->>LS: saved request
        LS->>NS: publish LeaveAppliedEvent(requestId)
        NS-->>NS: notify all Admin/HR (in-app + queue)
        LS-->>LC: LeaveRequestDto
        LC-->>SPA: 201 Created
        SPA-->>E: request shows PENDING in calendar
    end
```

## 5. Approve / Reject Leave Request

```mermaid
sequenceDiagram
    actor A as Admin/HR
    participant SPA as Angular SPA
    participant LC as LeaveController
    participant LS as LeaveService
    participant DB as PostgreSQL
    participant NS as NotificationService
    participant Mail as SMTP
    participant WS as WebSocket

    A->>SPA: click Approve (or Reject) with optional comment
    SPA->>LC: PATCH /api/v1/leave-requests/{id}/approve
    LC->>LS: approve(requestId, approverId, comment)
    LS->>DB: SELECT leave_request WHERE id=?
    DB-->>LS: request (PENDING)
    LS->>DB: UPDATE leave_request SET status=APPROVED, approver_id=?,\napprover_comment=?, decided_at=now
    alt paid leave type
        LS->>DB: UPDATE leave_allocation SET used_days += num_days
    end
    LS->>NS: publish LeaveDecidedEvent(requestId, APPROVED)
    NS->>DB: INSERT notification(recipient=requester)
    NS->>WS: push to /topic/notifications/{requesterUserId}
    NS->>Mail: send "leave approved" email
    LS-->>LC: void / 204
    LC-->>SPA: 204 No Content
    SPA-->>A: row removed from pending queue

    Note over WS: Requester's browser, if connected,\nreceives the push live
```

## 6. Configure Salary Structure (Admin)

```mermaid
sequenceDiagram
    actor A as Admin/HR
    participant SPA as Angular SPA
    participant PC as PayrollController
    participant SS as SalaryService
    participant DB as PostgreSQL
    participant AA as AuditAspect

    A->>SPA: set Monthly Wage / edit a component
    SPA->>PC: PUT /api/v1/employees/{id}/salary
    PC->>SS: upsertStructure(employeeId, SalaryStructureRequest)
    SS->>SS: recomputeComponents(wage, componentConfigs)
    Note right of SS: Basic → HRA/StdAllowance/PerfBonus/LTA →\nFixedAllowance = wage − Σ(others)
    alt Σ(components) > wage
        SS-->>PC: 400 Bad Request "components exceed wage"
        PC-->>SPA: error
    else valid
        SS->>DB: UPSERT salary_structure, salary_component rows
        SS->>AA: @Auditable → capture old/new snapshot
        AA->>DB: INSERT audit_log
        DB-->>SS: saved
        SS-->>PC: SalaryStructureDto
        PC-->>SPA: 200 OK
        SPA-->>A: UI shows recalculated amounts
    end
```

## 7. Payslip Generation & Download

```mermaid
sequenceDiagram
    actor A as Admin/HR
    participant SPA as Angular SPA
    participant PC as PayrollController
    participant PS as PayrollService
    participant AttS as AttendanceService
    participant SalS as SalaryService
    participant PDF as PayslipPdfService
    participant Store as File Storage
    participant DB as PostgreSQL

    A->>SPA: click "Generate Payslip" (employee, month, year)
    SPA->>PC: POST /api/v1/payslips/generate
    PC->>PS: generatePayslip(employeeId, month, year)
    PS->>AttS: getPayableDays(employeeId, month, year)
    AttS-->>PS: payableDays
    PS->>SalS: getStructure(employeeId)
    SalS-->>PS: SalaryStructureDto
    PS->>PS: compute gross, deductions, net
    PS->>DB: INSERT payslip
    PS->>PDF: render(payslip, structure, employee)
    PDF-->>PS: pdfBytes
    PS->>Store: save pdfBytes → pdfUrl
    PS->>DB: UPDATE payslip SET pdf_url=?
    PS-->>PC: PayslipDto
    PC-->>SPA: 201 Created
    SPA-->>A: payslip listed, downloadable

    Note over A,SPA: Employee later downloads their own copy
    actor E as Employee
    E->>SPA: click "Download Payslip"
    SPA->>PC: GET /api/v1/payslips/{id}/download
    PC->>Store: fetch PDF by pdfUrl
    Store-->>PC: pdfBytes
    PC-->>SPA: 200 OK (application/pdf)
    SPA-->>E: file download
```

## 8. In-App Notification Delivery (WebSocket)

```mermaid
sequenceDiagram
    participant Source as "Any module\n(Leave/Attendance/Payroll)"
    participant NS as NotificationService
    participant DB as PostgreSQL
    participant WS as NotificationWebSocketHandler
    participant SPA as Angular SPA (subscribed client)
    actor U as User

    Source->>NS: publish DomainEvent (e.g. LeaveDecidedEvent)
    NS->>DB: INSERT notification(recipient, type, message, read=false)
    NS->>WS: pushToUser(recipientUserId, NotificationDto)
    alt client connected to /topic/notifications/{userId}
        WS-->>SPA: STOMP message
        SPA-->>U: bell badge increments, toast shown
    else client offline
        Note over WS,SPA: delivered on next login via\nGET /api/v1/notifications/me (unread)
    end
```

---
**Related documents:** [Process Flows](07-process-flows.md) · [LLD](03-lld.md) · [API Documentation](09-api-documentation.md)
