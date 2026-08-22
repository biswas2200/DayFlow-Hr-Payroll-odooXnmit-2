# Dayflow HRMS — API Documentation

> REST API contract implementing the modules in the [LLD](03-lld.md) and [HLD](02-hld.md). Base path: `/api/v1`. All endpoints except `/auth/login` and `/auth/refresh` require `Authorization: Bearer <JWT>`.
>
> **Live Swagger UI:** add `springdoc-openapi-starter-webmvc-ui` to `hrmtool/pom.xml` — it auto-generates and serves this contract from the controller annotations at `/swagger-ui.html` (raw spec at `/v3/api-docs`). The OpenAPI document below is the hand-authored source of truth to build the controllers against; once implemented, springdoc's generated spec should match it.

## 1. Conventions

- **Auth:** `Bearer` JWT in `Authorization` header. Access token TTL ~15 min; refresh via `/auth/refresh`.
- **Roles:** `ADMIN` (Admin/HR Officer), `EMPLOYEE`. Enforced via `@PreAuthorize`.
- **Pagination:** list endpoints accept `page`, `size`, `sort` query params and return `PageResponse<T>`.
- **Errors:** uniform `ApiError` shape — `{ "timestamp", "status", "error", "message", "path" }`.
- **Ownership shorthand:** "own" = the authenticated user's own employee record; "any" = Admin-only access to arbitrary employee records.

## 2. Endpoint Reference

| Module | Method & Path | Role | Description |
|---|---|---|---|
| Auth | `POST /auth/login` | Public | Sign in with Login ID/Email + password → JWT pair |
| Auth | `POST /auth/refresh` | Public (valid refresh token) | Exchange refresh token for new access token |
| Auth | `POST /auth/logout` | Authenticated | Invalidate refresh token |
| Auth | `POST /auth/change-password` | Authenticated | Change own password (also clears `mustChangePassword`) |
| Employees | `POST /employees` | ADMIN | Provision new employee (Sign Up form); auto-generates Login ID + temp password |
| Employees | `GET /employees` | Authenticated | Directory card grid, paginated, `?search=` |
| Employees | `GET /employees/{id}` | Authenticated | View-only profile (any employee) |
| Employees | `PUT /employees/{id}` | ADMIN | Edit any employee's full profile |
| Employees | `PATCH /employees/{id}/status` | ADMIN | Activate/deactivate (off-boarding) |
| Employees | `GET /employees/me` | Authenticated | My Profile (editable view) |
| Employees | `PUT /employees/me` | Authenticated | Edit own limited fields |
| Employees | `POST /employees/me/photo` | Authenticated | Upload/replace own avatar |
| Resume | `GET/PUT /employees/{id}/resume` | Owner or ADMIN | About / love-about-job / interests |
| Resume | `GET/POST/DELETE /employees/{id}/skills` | Owner or ADMIN | Skills list |
| Resume | `GET/POST/DELETE /employees/{id}/certifications` | Owner or ADMIN | Certifications list |
| Company | `POST /company` | ADMIN (first-run) | Create company profile + logo |
| Company | `GET/PUT /company/{id}` | ADMIN | View/update company profile |
| Attendance | `POST /attendance/check-in` | Authenticated | Mark check-in for today |
| Attendance | `POST /attendance/check-out` | Authenticated | Mark check-out for today |
| Attendance | `GET /attendance/me` | Authenticated | Own day-wise attendance, `?month=&year=` |
| Attendance | `GET /attendance/me/summary` | Authenticated | Days present, leaves count, total working days |
| Attendance | `GET /attendance` | ADMIN | All employees for `?date=`, `?search=` |
| Leave | `GET /leave-types` | Authenticated | Paid Time Off / Sick Leave / Unpaid Leave |
| Leave | `GET /leave-balances/me` | Authenticated | Own balances per type |
| Leave | `POST /leave-requests` | Authenticated | Apply for leave (+ optional attachment) |
| Leave | `GET /leave-requests/me` | Authenticated | Own history / calendar feed |
| Leave | `GET /leave-requests` | ADMIN | All requests, `?status=`, paginated |
| Leave | `PATCH /leave-requests/{id}/approve` | ADMIN | Approve + optional comment |
| Leave | `PATCH /leave-requests/{id}/reject` | ADMIN | Reject + optional comment |
| Leave | `GET /leave-allocations/{employeeId}` | Owner or ADMIN | View allocation |
| Leave | `PUT /leave-allocations/{employeeId}` | ADMIN | Adjust allocated days per type/year |
| Leave | `GET /public-holidays` | Authenticated | Company holiday list for calendar overlay |
| Payroll | `GET /employees/{id}/salary` | Owner (read-only) or ADMIN (full) | Salary Info tab |
| Payroll | `PUT /employees/{id}/salary` | ADMIN | Configure wage & components |
| Payroll | `POST /payslips/generate` | ADMIN | Generate payslip for employee + month/year |
| Payroll | `GET /payslips/me` | Authenticated | Own payslip history |
| Payroll | `GET /payslips/{employeeId}` | ADMIN | Any employee's payslip history |
| Payroll | `GET /payslips/{id}/download` | Owner or ADMIN | Download payslip PDF |
| Notifications | `GET /notifications/me` | Authenticated | Own notifications, `?unreadOnly=` |
| Notifications | `PATCH /notifications/{id}/read` | Authenticated | Mark one as read |
| Notifications | `GET/PUT /notification-preferences/me` | Authenticated | Per-type email/in-app toggles |
| Notifications | `WS /ws/notifications` | Authenticated (JWT on handshake) | STOMP topic `/topic/notifications/{userId}` |
| Reports | `GET /reports/attendance-summary` | ADMIN | Daily/weekly/monthly aggregates |
| Reports | `GET /reports/leave-utilization` | ADMIN | Balance & utilization report |
| Reports | `GET /reports/dashboard` | ADMIN | Attendance %, leave trends, headcount by department |

## 3. OpenAPI 3.0 Specification

```yaml
openapi: 3.0.3
info:
  title: Dayflow HRMS API
  description: >
    REST API for Dayflow — Human Resource Management System.
    Implements SRS v4.0 §3 (Functional Requirements).
  version: "1.0.0"
  contact:
    name: Dayflow Team
servers:
  - url: /api/v1
    description: Default (behind Nginx reverse proxy)

tags:
  - name: Auth
  - name: Employees
  - name: Resume
  - name: Company
  - name: Attendance
  - name: Leave
  - name: Payroll
  - name: Notifications
  - name: Reports

security:
  - bearerAuth: []

paths:
  /auth/login:
    post:
      tags: [Auth]
      summary: Sign in
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/LoginRequest' }
      responses:
        '200':
          description: Authenticated
          content:
            application/json:
              schema: { $ref: '#/components/schemas/LoginResponse' }
        '401':
          description: Invalid credentials
          content:
            application/json:
              schema: { $ref: '#/components/schemas/ApiError' }

  /auth/refresh:
    post:
      tags: [Auth]
      summary: Refresh access token
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [refreshToken]
              properties:
                refreshToken: { type: string }
      responses:
        '200':
          description: New token pair
          content:
            application/json:
              schema: { $ref: '#/components/schemas/LoginResponse' }

  /auth/change-password:
    post:
      tags: [Auth]
      summary: Change own password (also clears mustChangePassword)
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [currentPassword, newPassword, confirmPassword]
              properties:
                currentPassword: { type: string, format: password }
                newPassword: { type: string, format: password }
                confirmPassword: { type: string, format: password }
      responses:
        '204': { description: Password changed }
        '400': { description: Validation error }

  /employees:
    post:
      tags: [Employees]
      summary: Provision a new employee account (Admin/HR only)
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema: { $ref: '#/components/schemas/EmployeeResponse' }
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/CreateEmployeeRequest' }
    get:
      tags: [Employees]
      summary: Employee directory (searchable card grid)
      parameters:
        - { name: search, in: query, schema: { type: string } }
        - { name: page, in: query, schema: { type: integer, default: 0 } }
        - { name: size, in: query, schema: { type: integer, default: 20 } }
      responses:
        '200':
          description: Page of employee cards
          content:
            application/json:
              schema: { $ref: '#/components/schemas/EmployeeCardPage' }

  /employees/me:
    get:
      tags: [Employees]
      summary: My Profile (editable view)
      responses:
        '200':
          description: Own profile
          content:
            application/json:
              schema: { $ref: '#/components/schemas/EmployeeProfile' }
    put:
      tags: [Employees]
      summary: Edit own limited fields
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/EmployeeSelfEditRequest' }
      responses:
        '200': { description: Updated }

  /employees/{id}:
    parameters:
      - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
    get:
      tags: [Employees]
      summary: View employee profile (read-only)
      responses:
        '200':
          description: Profile
          content:
            application/json:
              schema: { $ref: '#/components/schemas/EmployeeProfile' }
    put:
      tags: [Employees]
      summary: Edit any employee's profile (Admin only)
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/EmployeeAdminEditRequest' }
      responses:
        '200': { description: Updated }

  /employees/{id}/status:
    patch:
      tags: [Employees]
      summary: Activate/deactivate employee (Admin only)
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                status: { type: string, enum: [ACTIVE, INACTIVE] }
      responses:
        '204': { description: Updated }

  /employees/{id}/salary:
    parameters:
      - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
    get:
      tags: [Payroll]
      summary: Salary Info (read-only for owner, full detail for Admin)
      responses:
        '200':
          description: Salary structure
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SalaryStructure' }
    put:
      tags: [Payroll]
      summary: Configure wage & salary components (Admin only)
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/SalaryStructureRequest' }
      responses:
        '200':
          description: Recomputed structure
          content:
            application/json:
              schema: { $ref: '#/components/schemas/SalaryStructure' }
        '400': { description: "Components exceed wage" }

  /attendance/check-in:
    post:
      tags: [Attendance]
      summary: Check in for today
      responses:
        '200':
          description: Attendance row
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Attendance' }
        '409': { description: Already checked in today }

  /attendance/check-out:
    post:
      tags: [Attendance]
      summary: Check out for today
      responses:
        '200':
          description: Attendance row
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Attendance' }
        '409': { description: Not checked in / already checked out }

  /attendance/me:
    get:
      tags: [Attendance]
      summary: Own day-wise attendance for a month
      parameters:
        - { name: month, in: query, schema: { type: integer } }
        - { name: year, in: query, schema: { type: integer } }
      responses:
        '200':
          description: List of attendance rows
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Attendance' }

  /attendance:
    get:
      tags: [Attendance]
      summary: All employees' attendance for a selected date (Admin only)
      parameters:
        - { name: date, in: query, required: true, schema: { type: string, format: date } }
        - { name: search, in: query, schema: { type: string } }
      responses:
        '200':
          description: Page of attendance rows
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/AttendanceRow' }

  /leave-types:
    get:
      tags: [Leave]
      summary: List leave types
      responses:
        '200':
          description: Leave types
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/LeaveType' }

  /leave-balances/me:
    get:
      tags: [Leave]
      summary: Own leave balances
      responses:
        '200':
          description: Balances
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/LeaveBalance' }

  /leave-requests:
    post:
      tags: [Leave]
      summary: Apply for leave
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema: { $ref: '#/components/schemas/ApplyLeaveRequest' }
      responses:
        '201':
          description: Created (PENDING)
          content:
            application/json:
              schema: { $ref: '#/components/schemas/LeaveRequest' }
        '400': { description: "Insufficient balance or missing attachment" }
    get:
      tags: [Leave]
      summary: All leave requests (Admin only)
      parameters:
        - { name: status, in: query, schema: { type: string, enum: [PENDING, APPROVED, REJECTED] } }
        - { name: page, in: query, schema: { type: integer, default: 0 } }
      responses:
        '200':
          description: Page of requests
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/LeaveRequest' }

  /leave-requests/me:
    get:
      tags: [Leave]
      summary: Own leave history / calendar feed
      parameters:
        - { name: year, in: query, schema: { type: integer } }
      responses:
        '200':
          description: Requests + public holidays for the year
          content:
            application/json:
              schema: { $ref: '#/components/schemas/LeaveCalendar' }

  /leave-requests/{id}/approve:
    patch:
      tags: [Leave]
      summary: Approve a leave request (Admin only)
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      requestBody:
        content:
          application/json:
            schema:
              type: object
              properties:
                comment: { type: string }
      responses:
        '204': { description: Approved }

  /leave-requests/{id}/reject:
    patch:
      tags: [Leave]
      summary: Reject a leave request (Admin only)
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      requestBody:
        content:
          application/json:
            schema:
              type: object
              properties:
                comment: { type: string }
      responses:
        '204': { description: Rejected }

  /leave-allocations/{employeeId}:
    parameters:
      - { name: employeeId, in: path, required: true, schema: { type: integer, format: int64 } }
    get:
      tags: [Leave]
      summary: View leave allocation (owner or Admin)
      responses:
        '200':
          description: Allocation list
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/LeaveAllocation' }
    put:
      tags: [Leave]
      summary: Adjust leave allocation (Admin only)
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: array
              items: { $ref: '#/components/schemas/LeaveAllocation' }
      responses:
        '200': { description: Updated }

  /public-holidays:
    get:
      tags: [Leave]
      summary: Company public holiday list
      parameters:
        - { name: year, in: query, schema: { type: integer } }
      responses:
        '200':
          description: Holiday list
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/PublicHoliday' }

  /payslips/generate:
    post:
      tags: [Payroll]
      summary: Generate a payslip (Admin only)
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required: [employeeId, month, year]
              properties:
                employeeId: { type: integer, format: int64 }
                month: { type: integer, minimum: 1, maximum: 12 }
                year: { type: integer }
      responses:
        '201':
          description: Payslip generated
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Payslip' }

  /payslips/me:
    get:
      tags: [Payroll]
      summary: Own payslip history
      responses:
        '200':
          description: List
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Payslip' }

  /payslips/{id}/download:
    get:
      tags: [Payroll]
      summary: Download payslip PDF (owner or Admin)
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '200':
          description: PDF file
          content:
            application/pdf:
              schema: { type: string, format: binary }

  /notifications/me:
    get:
      tags: [Notifications]
      summary: Own notifications
      parameters:
        - { name: unreadOnly, in: query, schema: { type: boolean, default: false } }
      responses:
        '200':
          description: List
          content:
            application/json:
              schema:
                type: array
                items: { $ref: '#/components/schemas/Notification' }

  /notifications/{id}/read:
    patch:
      tags: [Notifications]
      summary: Mark a notification read
      parameters:
        - { name: id, in: path, required: true, schema: { type: integer, format: int64 } }
      responses:
        '204': { description: Marked read }

  /reports/dashboard:
    get:
      tags: [Reports]
      summary: Admin analytics dashboard (Admin only)
      responses:
        '200':
          description: Dashboard aggregates
          content:
            application/json:
              schema: { $ref: '#/components/schemas/DashboardSummary' }

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    ApiError:
      type: object
      properties:
        timestamp: { type: string, format: date-time }
        status: { type: integer }
        error: { type: string }
        message: { type: string }
        path: { type: string }

    LoginRequest:
      type: object
      required: [loginId, password]
      properties:
        loginId: { type: string, description: "Login ID or Email" }
        password: { type: string, format: password }

    LoginResponse:
      type: object
      properties:
        accessToken: { type: string }
        refreshToken: { type: string }
        role: { type: string, enum: [ADMIN, EMPLOYEE] }
        mustChangePassword: { type: boolean }

    CreateEmployeeRequest:
      type: object
      required: [firstName, lastName, email, phone, password, confirmPassword]
      properties:
        companyName: { type: string }
        companyLogoUrl: { type: string }
        firstName: { type: string }
        lastName: { type: string }
        email: { type: string, format: email }
        phone: { type: string }
        password: { type: string, format: password }
        confirmPassword: { type: string, format: password }

    EmployeeResponse:
      type: object
      properties:
        id: { type: integer, format: int64 }
        loginId: { type: string, example: "OIJODO20220001" }
        tempPasswordIssued: { type: boolean }

    EmployeeCardPage:
      type: object
      properties:
        content:
          type: array
          items: { $ref: '#/components/schemas/EmployeeCard' }
        totalElements: { type: integer }
        page: { type: integer }
        size: { type: integer }

    EmployeeCard:
      type: object
      properties:
        id: { type: integer, format: int64 }
        name: { type: string }
        profilePictureUrl: { type: string }
        statusDot: { type: string, enum: [GREEN, AIRPLANE, YELLOW] }

    EmployeeProfile:
      type: object
      properties:
        id: { type: integer, format: int64 }
        loginId: { type: string }
        firstName: { type: string }
        lastName: { type: string }
        jobPosition: { type: string }
        department: { type: string }
        manager: { type: string }
        location: { type: string }
        email: { type: string }
        mobile: { type: string }
        resume: { $ref: '#/components/schemas/Resume' }
        privateInfo: { $ref: '#/components/schemas/PrivateInfo' }
        salaryVisible: { type: boolean, description: "true if caller may view Salary Info tab" }
        salaryEditable: { type: boolean, description: "true only for ADMIN" }

    Resume:
      type: object
      properties:
        about: { type: string }
        whatILoveAboutMyJob: { type: string }
        interestsAndHobbies: { type: string }
        skills: { type: array, items: { type: string } }
        certifications:
          type: array
          items:
            type: object
            properties:
              name: { type: string }
              issuer: { type: string }
              issueDate: { type: string, format: date }

    PrivateInfo:
      type: object
      properties:
        dateOfBirth: { type: string, format: date }
        residingAddress: { type: string }
        nationality: { type: string }
        personalEmail: { type: string }
        gender: { type: string }
        maritalStatus: { type: string }
        dateOfJoining: { type: string, format: date }
        bankDetails:
          type: object
          properties:
            accountNumber: { type: string }
            bankName: { type: string }
            ifscCode: { type: string }
            panNo: { type: string }
            uanNo: { type: string }
            empCode: { type: string }

    EmployeeSelfEditRequest:
      type: object
      properties:
        phone: { type: string }
        residingAddress: { type: string }
        profilePictureUrl: { type: string }

    EmployeeAdminEditRequest:
      allOf:
        - $ref: '#/components/schemas/EmployeeSelfEditRequest'
        - type: object
          properties:
            jobPosition: { type: string }
            department: { type: string }
            managerId: { type: integer, format: int64 }
            status: { type: string, enum: [ACTIVE, INACTIVE] }

    Attendance:
      type: object
      properties:
        id: { type: integer, format: int64 }
        date: { type: string, format: date }
        checkInTime: { type: string, format: time }
        checkOutTime: { type: string, format: time }
        workHours: { type: string, example: "09:00" }
        extraHours: { type: string, example: "01:00" }
        status: { type: string, enum: [PRESENT, ABSENT, HALF_DAY, ON_LEAVE] }

    AttendanceRow:
      allOf:
        - $ref: '#/components/schemas/Attendance'
        - type: object
          properties:
            employeeId: { type: integer, format: int64 }
            employeeName: { type: string }

    LeaveType:
      type: object
      properties:
        id: { type: integer, format: int64 }
        name: { type: string, example: "Paid Time Off" }
        isPaid: { type: boolean }
        requiresAttachment: { type: boolean }

    LeaveBalance:
      type: object
      properties:
        leaveTypeId: { type: integer, format: int64 }
        leaveTypeName: { type: string }
        allocatedDays: { type: number }
        usedDays: { type: number }
        availableDays: { type: number }

    ApplyLeaveRequest:
      type: object
      required: [leaveTypeId, startDate, endDate, numDays]
      properties:
        leaveTypeId: { type: integer, format: int64 }
        startDate: { type: string, format: date }
        endDate: { type: string, format: date }
        numDays: { type: number }
        attachment: { type: string, format: binary }

    LeaveRequest:
      type: object
      properties:
        id: { type: integer, format: int64 }
        employeeId: { type: integer, format: int64 }
        employeeName: { type: string }
        leaveType: { type: string }
        startDate: { type: string, format: date }
        endDate: { type: string, format: date }
        numDays: { type: number }
        status: { type: string, enum: [PENDING, APPROVED, REJECTED] }
        approverComment: { type: string }
        attachmentUrl: { type: string }

    LeaveCalendar:
      type: object
      properties:
        year: { type: integer }
        requests:
          type: array
          items: { $ref: '#/components/schemas/LeaveRequest' }
        publicHolidays:
          type: array
          items: { $ref: '#/components/schemas/PublicHoliday' }

    LeaveAllocation:
      type: object
      properties:
        leaveTypeId: { type: integer, format: int64 }
        year: { type: integer }
        allocatedDays: { type: number }
        usedDays: { type: number }

    PublicHoliday:
      type: object
      properties:
        name: { type: string }
        date: { type: string, format: date }

    SalaryStructure:
      type: object
      properties:
        employeeId: { type: integer, format: int64 }
        monthlyWage: { type: number, example: 50000 }
        yearlyWage: { type: number, example: 600000 }
        workingDaysPerWeek: { type: integer }
        breakHours: { type: number }
        components:
          type: array
          items: { $ref: '#/components/schemas/SalaryComponent' }
        pfEmployeePercent: { type: number, example: 12 }
        pfEmployerPercent: { type: number, example: 12 }
        professionalTax: { type: number, example: 200 }

    SalaryComponent:
      type: object
      properties:
        type: { type: string, enum: [BASIC, HRA, STANDARD_ALLOWANCE, PERFORMANCE_BONUS, LTA, FIXED_ALLOWANCE] }
        computationType: { type: string, enum: [FIXED, PERCENTAGE] }
        value: { type: number, description: "amount if FIXED, percentage if PERCENTAGE" }
        computedAmount: { type: number }

    SalaryStructureRequest:
      type: object
      required: [monthlyWage, components, pfEmployeePercent, pfEmployerPercent, professionalTax]
      properties:
        monthlyWage: { type: number }
        workingDaysPerWeek: { type: integer }
        breakHours: { type: number }
        components:
          type: array
          items:
            type: object
            properties:
              type: { type: string, enum: [BASIC, HRA, STANDARD_ALLOWANCE, PERFORMANCE_BONUS, LTA] }
              computationType: { type: string, enum: [FIXED, PERCENTAGE] }
              value: { type: number }
        pfEmployeePercent: { type: number }
        pfEmployerPercent: { type: number }
        professionalTax: { type: number }

    Payslip:
      type: object
      properties:
        id: { type: integer, format: int64 }
        employeeId: { type: integer, format: int64 }
        month: { type: integer }
        year: { type: integer }
        payableDays: { type: integer }
        grossSalary: { type: number }
        totalDeductions: { type: number }
        netSalary: { type: number }
        pdfUrl: { type: string }
        generatedAt: { type: string, format: date-time }

    Notification:
      type: object
      properties:
        id: { type: integer, format: int64 }
        type: { type: string, example: "LEAVE_APPROVED" }
        message: { type: string }
        read: { type: boolean }
        createdAt: { type: string, format: date-time }

    DashboardSummary:
      type: object
      properties:
        attendancePercentToday: { type: number }
        leaveTrends:
          type: array
          items:
            type: object
            properties:
              month: { type: string }
              count: { type: integer }
        headcountByDepartment:
          type: array
          items:
            type: object
            properties:
              department: { type: string }
              count: { type: integer }
```

## 4. Error Codes

| HTTP Status | Meaning | Example |
|---|---|---|
| 400 | Validation failure | passwords don't match, components exceed wage, insufficient leave balance |
| 401 | Not authenticated / bad credentials | expired/invalid JWT, wrong login |
| 403 | Authenticated but not authorized | Employee calling an Admin-only endpoint |
| 404 | Resource not found | unknown employee/leave-request id |
| 409 | Conflict | duplicate check-in, duplicate leave allocation for year |
| 500 | Unhandled server error | mapped generically, never leaks stack traces to the client |

---
**Related documents:** [LLD](03-lld.md) · [ER Diagram](04-er-diagram.md) · [Sequence Diagrams](08-sequence-diagrams.md)
