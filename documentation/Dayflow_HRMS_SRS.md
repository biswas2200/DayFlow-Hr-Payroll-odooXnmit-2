# Dayflow — Human Resource Management System

*"Every workday, perfectly aligned."*

**Software Requirements Specification (SRS)**
Prepared for: Odoo Hackathon Submission
Version 4.0 | August 22, 2026

---

## 1. Introduction

### 1.1 Purpose

This document defines the functional and non-functional requirements of Dayflow, a Human Resource Management System (HRMS). The system digitizes and streamlines core HR operations, including employee onboarding, profile management, attendance tracking, leave management, payroll visibility, and approval workflows for admins and HR officers.

### 1.2 Scope

Dayflow will provide:

- Secure authentication, provisioned by Admin/HR (Sign Up / Sign In)
- Role-based access (Admin vs Employee)
- Employee profile management
- Attendance tracking (daily/weekly view) linked to payroll
- Leave and time-off management with a calendar view
- Approval workflows for HR/Admin
- Notifications and alerts
- Analytics and reporting dashboards

> **UI direction:** Dayflow's interface follows Odoo's design system (theme, components, navigation patterns) exactly, per the approved wireframes referenced throughout Section 3 and detailed in Section 6. This governs visual design and UX flow only — see Section 5 for the actual implementation stack.

### 1.3 Definitions & Abbreviations

- **Admin / HR Officer** — User with management and approval privileges
- **Employee** — Regular user with limited access
- **Time-Off** — Paid leave, sick leave, unpaid leave, etc.
- **HRMS** — Human Resource Management System
- **RBAC** — Role-Based Access Control
- **SRS** — Software Requirements Specification
- **HRA / LTA / PF** — House Rent Allowance / Leave Travel Allowance / Provident Fund

---

## 2. User Classes and Characteristics

| User Type | Description |
|---|---|
| Admin / HR Officer | Manages employees, approves leave & attendance, views and updates payroll details |
| Employee | Views personal profile, attendance, applies for leave, views salary details |

---

## 3. Functional Requirements

### 3.1 Authentication & Authorization

#### 3.1.1 Sign Up — Admin/HR-provisioned only

There is no public self-registration. Only an Admin/HR Officer can create a new employee account, using a form that captures:

- Company Name (with logo upload)
- Employee Name, Email, Phone
- Password and Confirm Password (both with a show/hide toggle)

On creation, the system auto-generates the employee's Login ID in the format:

- `[Company Initials] + [First 2 letters of first name + first 2 letters of last name] + [Year of Joining] + [4-digit serial number for that year]`
- Example: `OIJODO20220001` — `OI` = company initials, `JODO` = first 2 letters of first name + first 2 letters of last name, `2022` = year of joining, `0001` = serial number of joining that year

A temporary password is auto-generated for the employee's first login; the employee can log in and change it afterward. Passwords must follow the system's security rules.

#### 3.1.2 Sign In

- Users log in with their Login ID/Email and Password.
- Incorrect credentials display a clear error message.
- Successful login redirects to the Employees landing page (see 3.2).

### 3.2 Landing Page & Navigation

> This supersedes the earlier draft's separate "dashboard cards" concept: the wireframes show one shared Employees directory as the landing page for both roles, with behaviour differing by permission rather than by separate dashboards.

#### 3.2.1 Employee Directory (all roles)

- Top navigation: Company Logo, and tabs for Employees / Attendance / Time Off, with the user's avatar on the right.
- The Employees tab shows a searchable card grid (New button + Search bar) of all employees.
- Each card shows the employee's photo and basic info, with a status dot in the top-right corner:
  - Green = present in office
  - Airplane icon = on leave
  - Yellow = absent (no time-off applied, unaccounted)
- Clicking a card opens that employee's profile in view-only (non-editable) mode.
- Clicking the avatar opens a dropdown: **My Profile** (opens the logged-in user's own profile in editable form view) and **Log Out**.

#### 3.2.2 Check-In / Check-Out

- A systray Check In / Check Out control lets employees mark attendance from anywhere in the app.
- On successful check-in, the status dot next to the avatar turns from red to green; after check-in, the control shows elapsed time (e.g., "Since 00:00 PM") until check-out.
- Each check-in/check-out event populates that day's Attendance record (see 3.4).

#### 3.2.3 Admin/HR View

- Admin/HR see the same Employees / Attendance / Time Off tabs, with elevated permissions: edit any employee's profile, view all employees' attendance, and approve/reject leave requests.

### 3.3 Employee Profile Management

#### 3.3.1 Profile Structure

Each profile is organised into tabs:

- **Resume** — free-text sections (About, What I Love About My Job, My Interests and Hobbies), plus independently editable Skills and Certification lists.
- **Private Info** — Date of Birth, Residing Address, Nationality, Personal Email, Gender, Marital Status, Date of Joining, plus Bank Details (Account Number, Bank Name, IFSC Code, PAN No, UAN No, Employee Code).
- **Salary Info** — wage and salary-structure details (see 3.6); full edit access is Admin-only.
- **Security** — account/password management for the logged-in user (see 3.9).

#### 3.3.2 View Profile

- Employees can view their own full profile, including a read-only view of their own Salary Info.
- Admin/HR can view any employee's profile; opening a profile from the Employees grid opens it in view-only mode, with an explicit action to switch to edit.

> **⚠ Open item — needs confirmation:** the wireframes show a Salary Info tab present on the employee's own "My Profile" view, alongside a separate annotation that the tab "should only be visible to Admin." Current assumption: employees see their own Salary Info read-only, while only Admin can edit/configure any employee's salary. Confirm this is the intended behaviour before implementing.

#### 3.3.3 Edit Profile

- Employees can edit limited fields on their own profile (address, phone, profile picture, and other permitted Private Info fields).
- Admin can edit all employee details, including Salary Info.

### 3.4 Attendance Management

#### 3.4.1 Attendance Tracking

- Attendance is captured via Check In/Check Out (3.2.2), recording Check In time, Check Out time, Work Hours, and Extra Hours per day.
- By default, employees see a day-wise view of their own attendance for the current month, including break time.
- Status types: Present, Absent, Half-day, Leave.

#### 3.4.2 Attendance View

- Employees can view only their own attendance, with summary counters: Count of Days Present, Leaves Count, Total Working Days; navigable by date/day.
- Admin/HR can view attendance of all employees for a selected day, with search and date/day navigation.

#### 3.4.3 Attendance → Payroll Link

- Attendance data is the basis for payslip generation: the system uses attendance records to determine each employee's total payable days.
- Unpaid leave or missing attendance automatically reduces the number of payable days used in payroll computation.

### 3.5 Leave & Time-Off Management

#### 3.5.1 Leave Types & Balances

- Leave types: Paid Time Off, Sick Leave, Unpaid Leave.
- Employees see their available balance per type (e.g., Paid Time Off: 24 days available, Sick Leave: 7 days available).

#### 3.5.2 Apply for Leave (Employee)

- A "Time Off Type Request" form captures: Employee, Time Off Type, Validity Period (start/end date), Allocation (number of days), and an optional Attachment (e.g., sick leave certificate).
- Employees can view their time-off history on a full-year calendar, colour-coded by status (Validated / To Approve / Refused), alongside the list of company public holidays for the year.
- Leave request status: Pending, Approved, Rejected.

#### 3.5.3 Leave Approval (Admin/HR)

- Admin/HR view all leave requests in a table (Name, Start Date, End Date, Time Off Type, Status) with inline Approve/Reject actions and the ability to add comments.
- Admin/HR can also manage Time Off Allocation — adjusting how many days of each leave type an employee has.
- Approvals/rejections reflect immediately in the employee's records and calendar.

### 3.6 Payroll/Salary Management

#### 3.6.1 Employee Payroll View

- Payroll data is read-only for employees, shown under their own Salary Info tab.

#### 3.6.2 Admin Payroll Control — Wage & Salary Structure

- Wage Type: Fixed Wage, entered as a Monthly wage (with Yearly shown/derived).
- Salary Components: Basic Salary, House Rent Allowance (HRA), Standard Allowance, Performance Bonus, Leave Travel Allowance (LTA), Fixed Allowance.
- Each component is configured as either a Fixed Amount or a Percentage of Wage; component values auto-recalculate whenever the Wage changes.
- The sum of all components must not exceed the defined Wage — Fixed Allowance absorbs the remainder.
- Example: Wage = ₹50,000, Basic = 50% of Wage → Basic = ₹25,000. HRA = 50% of Basic → HRA = ₹12,500.
- Provident Fund (PF) Contribution: separate Employee and Employer percentages (e.g., 12% each), both calculated on Basic Salary.
- Tax Deductions: configurable Professional Tax (flat amount, e.g., ₹200/month), deducted from gross salary.
- Admin can view and update payroll for all employees and is responsible for payroll accuracy.

### 3.7 Notifications & Alerts

The system sends timely notifications to keep users informed:

- Email alerts when a leave request is approved or rejected.
- In-app notification bell showing pending items (e.g., unread approvals).
- Alerts to Admin/HR for new leave requests awaiting action.
- Reminders for missed check-in/check-out.
- Configurable notification preferences per user.

### 3.8 Analytics & Reports

Dashboards and exportable reports support HR decision-making:

- Attendance summary reports (daily / weekly / monthly).
- Leave balance and leave-utilization reports.
- Payroll reports and downloadable salary slips (PDF), generated from attendance-derived payable days (3.4.3) and the salary structure (3.6.2).
- Admin analytics dashboard: attendance %, leave trends, headcount by department.

### 3.9 Account Security

- The Security tab on a profile lets the logged-in user manage their own password/account settings, including changing the system-generated temporary password on first login.

---

## 4. Non-Functional Requirements

### 4.1 Performance

- Dashboard and key pages should load within 2 seconds under normal load.
- The system should support concurrent access by all registered employees without degradation.

### 4.2 Security

- Passwords stored using a strong one-way hash (e.g., bcrypt); never stored in plain text.
- Session/token-based authentication (JWT) with expiry and refresh handling.
- Role-based access control (RBAC) enforced on both UI and API layers — Salary Info edit rights are Admin-only (3.3.1).
- All traffic served over HTTPS; sensitive fields (salary, documents) access-logged.
- Input validation and sanitisation to prevent injection attacks.

### 4.3 Usability

- Responsive UI usable on desktop and mobile browsers, matching Odoo's design system throughout.
- Consistent navigation with minimal clicks for common tasks (apply leave, check-in).
- Clear, actionable error and success messages.

### 4.4 Availability & Reliability

- Target uptime of 99% during business hours.
- Automated daily backups of the database.
- Graceful error handling with user-friendly fallback messages.

### 4.5 Scalability & Maintainability

- Modular design so new modules (recruitment, performance appraisal) can be added later without reworking the core.
- Codebase follows consistent structure/conventions to ease handover after the hackathon.

### 4.6 Compliance & Data Privacy

- Employee PII (personal, salary, documents) restricted to authorised roles only.
- Audit trail for changes to payroll and profile data (who changed what, when).
- Data retention and deletion policy for employees who exit the organisation.

---

## 5. System Architecture & Technology Stack

### 5.1 High-Level Architecture

Dayflow is a decoupled application: an **Angular** single-page frontend styled to match Odoo's design system (theme, navigation patterns, and component styling exactly as shown in the wireframes, Section 6), backed by a **Spring Boot** REST API. Role-based middleware/security gates every request so Employees and Admin/HR see only the data and actions their role permits.

> **Note:** "Follow Odoo" governs visual design and UX flow only — the wireframes define the look, navigation, and interaction patterns to replicate. The underlying implementation is Angular + Spring Boot, not the Odoo platform itself.

### 5.2 Technology Stack

| Layer | Technology |
|---|---|
| Frontend | Angular — components and theme styled to match Odoo's design system |
| Backend | Spring Boot (Java), Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Authentication | Spring Security with JWT-based auth; role-based access control (RBAC) |
| Notifications | Spring Mail for email alerts; WebSocket/STOMP for real-time in-app notifications |
| Reporting | Server-side PDF generation (e.g., iText/OpenPDF) for payslips and attendance/leave reports |
| Containerisation | Docker for local development and deployment |

### 5.3 Key Data Entities

- **Employee** — personal details, job details, role, linked user account, auto-generated Login ID
- **Attendance** — employee, date, check-in/out time, work hours, extra hours, status
- **LeaveRequest** — employee, leave type, date range, allocation, status, remarks, attachment, approver
- **SalaryStructure** — employee, wage, components (type, computation type, value), PF %, professional tax
- **Notification** — recipient, type, message, read/unread state

---

## 6. Wireframes / UI Design

Screens are defined in Excalidraw and are the source of truth for both flow and Odoo-matched visual styling:
[View wireframes on Excalidraw](https://link.excalidraw.com/l/65VNwvy7c4X/58RLEJ4oOwh)

### Sign In
- Login ID/Email + Password fields
- "Sign In" button; link to Sign Up

### Sign Up (Admin/HR only)
- Company Name + logo upload, Name, Email, Phone
- Password / Confirm Password with show-hide toggle
- Auto-generated Login ID & temporary password (3.1.1)

### Employees (landing directory)
- Card grid with New + Search
- Status dot per card: green / yellow / airplane
- Avatar dropdown: My Profile, Log Out
- Check In / Check Out systray

### Employee Profile
- Tabs: Resume, Private Info, Salary Info (Admin edit), Security
- View-only when opened from directory; editable via My Profile

### Attendance — Employee
- Day-wise view, current month default
- Counters: Days Present, Leaves Count, Total Working Days
- Date/Day navigation

### Attendance — Admin/HR
- All employees for selected date
- Check In, Check Out, Work Hours, Extra Hours
- Search + date/day navigation

### Time Off — Employee
- Balance cards (Paid / Sick)
- Full-year calendar: Validated / To Approve / Refused + public holidays
- New request modal: Type, Validity Period, Allocation, Attachment

### Time Off — Admin/HR
- Requests table: Name, Start/End Date, Type, Status
- Inline Approve/Reject actions
- Time-off Allocation management

> The calendar's public-holiday list is company-configurable; the wireframe sample uses an India-based set (e.g., Republic Day, Independence Day, Diwali) as an illustration, not a hard-coded requirement.

---

## 7. Future Enhancements

- Native mobile app (iOS/Android) for check-in/out and leave requests on the go.
- Biometric or geo-fenced attendance for on-site check-in verification.
- AI-assisted leave-pattern insights and staffing/coverage forecasts.
- Multi-language support for regional teams.
- Integration with external payroll/tax-filing systems.
- Performance review and appraisal module.
- Recruitment and onboarding workflow module.
- HR chatbot for common employee queries (leave balance, policy lookup).
