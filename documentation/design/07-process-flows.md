# Dayflow HRMS — Process Flows

> Step-by-step business logic flowcharts for the processes identified in the [DFD](06-dfd.md) and [HLD](02-hld.md).

## 1. Employee Onboarding (Admin-Provisioned Account)

```mermaid
flowchart TD
    Start(["Admin opens Sign Up / New Employee form"]) --> Fill["Enter Company Name (+logo if first-time),\nEmployee Name, Email, Phone, Password, Confirm Password"]
    Fill --> Validate{"Passwords match &\nmeet security rules?"}
    Validate -- No --> Err1["Show validation error"] --> Fill
    Validate -- Yes --> GenId["Generate Login ID:\n[Initials][First2+First2][Year][Serial4]"]
    GenId --> GenTemp["Generate temporary password"]
    GenTemp --> Persist["Create Employee + App_User\n(must_change_password = true)"]
    Persist --> Notify["Email Login ID + temp password to employee"]
    Notify --> End(["Employee can now Sign In"])
```

## 2. First Login & Forced Password Change

```mermaid
flowchart TD
    Start(["Employee submits Login ID/Email + temp password"]) --> Check{"Credentials valid?"}
    Check -- No --> Err["Show 'incorrect credentials' error"] --> Start
    Check -- Yes --> Flag{"must_change_password = true?"}
    Flag -- Yes --> Force["Redirect to Security tab,\nblock other navigation"]
    Force --> NewPw["Employee sets new password\n(meets security rules)"]
    NewPw --> Clear["Clear must_change_password flag"]
    Clear --> Land["Land on Employees directory"]
    Flag -- No --> Land
    Land --> End(["Session active (JWT issued)"])
```

## 3. Daily Check-In / Check-Out & Status Dot

```mermaid
flowchart TD
    Start(["User opens app, any page"]) --> Dot{"Today's Attendance row\nexists for employee?"}
    Dot -- No --> Red["Status dot: red\n(Check In button shown)"]
    Dot -- Yes, checked in only --> Green["Status dot: green\nsystray shows 'Since HH:MM'"]
    Dot -- Yes, checked out --> Done["Attendance finalized for today"]

    Red --> ClickIn["Employee clicks Check In"]
    ClickIn --> CreateRow["Create Attendance row:\ncheck_in_time = now, status = PRESENT"]
    CreateRow --> Green

    Green --> ClickOut["Employee clicks Check Out"]
    ClickOut --> UpdateRow["Set check_out_time = now;\nwork_hours = checkout - checkin - break;\nextra_hours = max(0, work_hours - standard_hours)"]
    UpdateRow --> Done

    Done --> DirectoryDot{"Directory card status dot"}
    DirectoryDot -- "Attendance PRESENT today" --> DotGreen["Green dot"]
    DirectoryDot -- "Approved leave covers today" --> DotAirplane["Airplane icon"]
    DirectoryDot -- "Neither" --> DotYellow["Yellow dot (unaccounted absence)"]
```

## 4. Leave Application & Approval

```mermaid
flowchart TD
    Start(["Employee opens Time Off > New"]) --> Form["Fill Time Off Type Request:\nType, Validity Period, Allocation (days), Attachment?"]
    Form --> Req{"Type requires attachment\n(e.g. Sick Leave)?"}
    Req -- Yes, missing --> ErrAtt["Prompt to attach document"] --> Form
    Req -- No / attached --> BalCheck{"Paid type &\ndays ≤ remaining balance?"}
    BalCheck -- No --> ErrBal["Show insufficient-balance error"] --> Form
    BalCheck -- Yes / Unpaid type --> Submit["Create LeaveRequest\nstatus = PENDING"]
    Submit --> AlertAdmin["Notify all Admin/HR\n(bell + queue entry)"]
    AlertAdmin --> Queue["Request appears in\nAdmin approval table"]

    Queue --> Decision{"Admin decision"}
    Decision -- Approve --> Approve["status = APPROVED\nused_days += num_days (if paid)"]
    Decision -- Reject --> Reject["status = REJECTED\nno balance change"]
    Approve --> Cal["Employee calendar cell → Validated (green)"]
    Reject --> Cal2["Employee calendar cell → Refused"]
    Approve --> EmailApp["Email + in-app notification: approved"]
    Reject --> EmailRej["Email + in-app notification: rejected"]
    Approve --> Payroll["Feed payable-days calc\n(paid leave counts as worked;\nunpaid leave reduces payable days)"]
```

## 5. Payable-Days Computation (Attendance → Payroll link)

```mermaid
flowchart TD
    Start(["Payroll run triggered for\nemployee, month, year"]) --> Working["totalWorkingDays =\nworkingDaysInMonth - publicHolidaysInMonth"]
    Working --> Present["presentDays = count(Attendance\nstatus IN (PRESENT, HALF_DAY*0.5))"]
    Present --> PaidLeave["paidLeaveDays = count(LeaveRequest\nAPPROVED, type != UNPAID, in month)"]
    PaidLeave --> Payable["payableDays = min(totalWorkingDays,\npresentDays + paidLeaveDays)"]
    Payable --> Unpaid["unpaidDays = totalWorkingDays - payableDays"]
    Unpaid --> Output(["payableDays passed to\nPayslip Generation"])
```

## 6. Payslip Generation

```mermaid
flowchart TD
    Start(["Admin triggers 'Generate Payslip'\nfor employee + month/year\n(or scheduled monthly run)"]) --> Fetch["Load SalaryStructure + SalaryComponents"]
    Fetch --> Days["Compute payableDays\n(see Process 5 above)"]
    Days --> Gross["grossSalary = monthlyWage × (payableDays / totalWorkingDays)"]
    Gross --> Ded["deductions = PF-employee-amount + professionalTax"]
    Ded --> Net["netSalary = grossSalary − deductions"]
    Net --> Save["Persist Payslip row"]
    Save --> Render["Render payslip PDF (iText/OpenPDF)"]
    Render --> Store["Store PDF, set pdf_url"]
    Store --> Expose["Expose for download:\nEmployee (own) / Admin (any)"]
    Expose --> End(["Done"])
```

## 7. Salary Component Auto-Recalculation (live UI behaviour)

```mermaid
flowchart TD
    Start(["Admin changes Monthly Wage\nor a component's value/type"]) --> Recalc["For each component in fixed order\n[Basic, HRA, Standard Allowance,\nPerformance Bonus, LTA]:"]
    Recalc --> CompCalc{"Computation type?"}
    CompCalc -- Fixed --> UseValue["amount = configured value"]
    CompCalc -- "% of Wage/Basic" --> Pct["amount = base × value%"]
    UseValue --> Sum["runningTotal += amount"]
    Pct --> Sum
    Sum --> More{"More components\nto process?"}
    More -- Yes --> Recalc
    More -- No --> FixedAll["fixedAllowance = wage − runningTotal"]
    FixedAll --> Guard{"fixedAllowance ≥ 0?"}
    Guard -- No --> ErrCfg["Reject: components exceed wage;\nshow validation error"]
    Guard -- Yes --> Save["Persist all SalaryComponent rows\n+ computed FixedAllowance"]
    Save --> UI["UI reflects updated amounts instantly"]
```

---
**Related documents:** [Sequence Diagrams](08-sequence-diagrams.md) · [DFD](06-dfd.md) · [Use Case Diagram](05-use-case-diagram.md)
