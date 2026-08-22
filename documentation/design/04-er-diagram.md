# Dayflow HRMS — Entity-Relationship Diagram

> Formalizes the entities introduced in [LLD §3](03-lld.md#3-database-schema-ddl-level-detail) and SRS §5.3 (Key Data Entities).

## 1. ER Diagram

```mermaid
erDiagram
    COMPANY ||--o{ EMPLOYEE : employs
    COMPANY ||--o{ LEAVE_TYPE : defines
    COMPANY ||--o{ PUBLIC_HOLIDAY : observes

    EMPLOYEE ||--|| APP_USER : "has login"
    EMPLOYEE ||--|| RESUME : has
    EMPLOYEE ||--o{ SKILL : lists
    EMPLOYEE ||--o{ CERTIFICATION : lists
    EMPLOYEE ||--|| BANK_DETAIL : has
    EMPLOYEE ||--|| SALARY_STRUCTURE : "compensated by"
    EMPLOYEE ||--o{ ATTENDANCE : records
    EMPLOYEE ||--o{ LEAVE_REQUEST : submits
    EMPLOYEE ||--o{ LEAVE_ALLOCATION : "is allocated"
    EMPLOYEE ||--o{ PAYSLIP : receives
    EMPLOYEE }o--o| EMPLOYEE : "reports to (manager)"
    EMPLOYEE ||--o{ LEAVE_REQUEST : "approves (as approver)"

    SALARY_STRUCTURE ||--o{ SALARY_COMPONENT : "composed of"

    LEAVE_TYPE ||--o{ LEAVE_ALLOCATION : "balance for"
    LEAVE_TYPE ||--o{ LEAVE_REQUEST : "type of"

    APP_USER ||--o{ NOTIFICATION : receives
    APP_USER ||--o{ NOTIFICATION_PREFERENCE : configures

    EMPLOYEE ||--o{ AUDIT_LOG : "subject of (polymorphic)"

    COMPANY {
        bigint id PK
        string name
        string logo_url
        string initials
        int working_days_per_week
        decimal break_hours
    }
    APP_USER {
        bigint id PK
        bigint employee_id FK
        string login_id UK
        string email UK
        string password_hash
        string role "ADMIN | EMPLOYEE"
        boolean must_change_password
    }
    EMPLOYEE {
        bigint id PK
        bigint company_id FK
        bigint manager_id FK
        string first_name
        string last_name
        string work_email
        string personal_email
        string phone
        string job_position
        string department
        string location
        string profile_picture_url
        date dob
        date date_of_joining
        int year_of_joining
        int serial_no
        string residing_address
        string nationality
        string gender
        string marital_status
        string status "ACTIVE | INACTIVE"
    }
    RESUME {
        bigint employee_id PK_FK
        text about
        text love_about_job
        text interests
    }
    SKILL {
        bigint id PK
        bigint employee_id FK
        string name
    }
    CERTIFICATION {
        bigint id PK
        bigint employee_id FK
        string name
        string issuer
        date issue_date
    }
    BANK_DETAIL {
        bigint employee_id PK_FK
        string account_number
        string bank_name
        string ifsc_code
        string pan_no
        string uan_no
        string emp_code
    }
    ATTENDANCE {
        bigint id PK
        bigint employee_id FK
        date date
        time check_in_time
        time check_out_time
        interval work_hours
        interval extra_hours
        interval break_duration
        string status "PRESENT | ABSENT | HALF_DAY | ON_LEAVE"
    }
    LEAVE_TYPE {
        bigint id PK
        bigint company_id FK
        string name
        boolean requires_attachment
        boolean is_paid
    }
    LEAVE_ALLOCATION {
        bigint id PK
        bigint employee_id FK
        bigint leave_type_id FK
        int year
        decimal allocated_days
        decimal used_days
    }
    LEAVE_REQUEST {
        bigint id PK
        bigint employee_id FK
        bigint leave_type_id FK
        bigint approver_id FK
        date start_date
        date end_date
        decimal num_days
        string attachment_url
        string status "PENDING | APPROVED | REJECTED"
        string approver_comment
        timestamp decided_at
    }
    PUBLIC_HOLIDAY {
        bigint id PK
        bigint company_id FK
        string name
        date date
    }
    SALARY_STRUCTURE {
        bigint employee_id PK_FK
        decimal monthly_wage
        decimal yearly_wage
        decimal pf_employee_pct
        decimal pf_employer_pct
        decimal professional_tax
        int working_days_per_week
        decimal break_hours
        bigint updated_by FK
    }
    SALARY_COMPONENT {
        bigint id PK
        bigint salary_structure_id FK
        string type "BASIC|HRA|STANDARD_ALLOWANCE|PERFORMANCE_BONUS|LTA|FIXED_ALLOWANCE"
        string computation_type "FIXED | PERCENTAGE"
        decimal value
        decimal computed_amount
    }
    PAYSLIP {
        bigint id PK
        bigint employee_id FK
        int month
        int year
        int payable_days
        decimal gross_salary
        decimal total_deductions
        decimal net_salary
        string pdf_url
        bigint generated_by FK
    }
    NOTIFICATION {
        bigint id PK
        bigint recipient_user_id FK
        string type
        string message
        string reference_type
        bigint reference_id
        boolean read
        timestamp created_at
    }
    NOTIFICATION_PREFERENCE {
        bigint id PK
        bigint user_id FK
        string type
        boolean email_enabled
        boolean in_app_enabled
    }
    AUDIT_LOG {
        bigint id PK
        string entity_name
        bigint entity_id
        string action
        bigint changed_by FK
        timestamp changed_at
        text old_value_json
        text new_value_json
    }
```

## 2. Cardinality Notes

| Relationship | Cardinality | Business rule |
|---|---|---|
| Company → Employee | 1 : N | An employee belongs to exactly one company (multi-tenant ready, though MVP ships single-tenant) |
| Employee → App_User | 1 : 1 | Every employee has exactly one login credential, created at onboarding (SRS §3.1.1) |
| Employee → Resume, Employee → Bank_Detail | 1 : 1 | Created empty at onboarding, populated later |
| Employee → Skill / Certification | 1 : N | Independently editable lists (SRS §3.3.1) |
| Employee → Salary_Structure | 1 : 1 | One active structure per employee; history of changes lives in `Audit_Log`, not extra rows |
| Salary_Structure → Salary_Component | 1 : 6 (fixed set) | Exactly one row per `ComponentType`; `FIXED_ALLOWANCE` is always system-computed |
| Employee → Attendance | 1 : N | One row per calendar date (`UNIQUE(employee_id, date)`) |
| Employee → Leave_Request | 1 : N | An employee may have many requests over time |
| Employee → Leave_Request (approver) | 1 : N | Self-referencing via `approver_id`; approver must have `role = ADMIN` |
| Leave_Type → Leave_Allocation | 1 : N | One allocation row per `(employee, leave_type, year)` |
| Employee → Manager (self-FK) | N : 1 | Optional; supports the "Manager" field shown in the Private Info wireframe |
| Employee → Payslip | 1 : N | One payslip per `(employee, month, year)` |
| App_User → Notification | 1 : N | Notifications target the login, not the employee record, so Admin-only alerts work even before an employee record exists in edge cases |

## 3. Design Decisions Worth Calling Out

- **`FIXED_ALLOWANCE` is derived, not user-editable** — enforced at the service layer (see [LLD §2.5](03-lld.md#25-payroll--salary)), not a DB constraint, because the "remainder" computation needs the other five components' final values first.
- **`Attendance.status` vs. `LeaveRequest.status`** are kept in separate tables rather than folding leave into attendance, because a day can be `ON_LEAVE` without ever having a Check-In/Check-Out event — this matches the wireframe's card status dot logic (airplane icon is driven by Leave, not Attendance).
- **`BankDetail` and salary fields are the only columns flagged for field-level access control** (SRS §4.6) — the LLD's `EmployeeProfileDto` serializer strips these for any caller who is neither the owning employee nor an Admin.
- **No hard delete** on `Employee` — off-boarding sets `status = INACTIVE`; retention/deletion policy (SRS §4.6) is a scheduled job operating on inactive records past the retention window, not a cascading `DELETE`.

---
**Related documents:** [Low-Level Design](03-lld.md) · [DFD](06-dfd.md) · [API Documentation](09-api-documentation.md)
