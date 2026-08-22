import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { EmployeeService } from '../../core/services/employee.service';
import { PayrollService } from '../../core/services/payroll.service';
import { EmployeeProfile as EmployeeProfileModel } from '../../core/models/employee.model';
import { ComponentType, ComputationType, Payslip, SalaryStructure, SalaryStructureRequest } from '../../core/models/payroll.model';

type Tab = 'resume' | 'private' | 'salary' | 'security';

/** The tabbed profile view — SRS §3.3.1–3.3.3. Handles both /employees/me (editable)
 * and /employees/:id (view-only unless the viewer is Admin or the owner). */
@Component({
  selector: 'app-employee-profile',
  imports: [ReactiveFormsModule, FormsModule, DecimalPipe],
  templateUrl: './employee-profile.html',
  styleUrl: './employee-profile.scss',
})
export class EmployeeProfile implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly employeeService = inject(EmployeeService);
  private readonly payrollService = inject(PayrollService);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly saveMessage = signal<string | null>(null);
  readonly isMe = signal(true);
  readonly editMode = signal(false);
  readonly activeTab = signal<Tab>('resume');
  readonly forcedSecurity = signal(false);

  readonly profile = signal<EmployeeProfileModel | null>(null);
  readonly salary = signal<SalaryStructure | null>(null);
  readonly payslips = signal<Payslip[]>([]);

  readonly canEdit = computed(() => this.isMe() || this.auth.isAdmin());

  // --- Resume tab ---
  readonly resumeForm = this.fb.nonNullable.group({
    about: [''],
    whatILoveAboutMyJob: [''],
    interestsAndHobbies: [''],
  });
  readonly skills = signal<string[]>([]);
  readonly newSkill = signal('');
  readonly certifications = signal<{ name: string; issuer: string; issueDate: string }[]>([]);

  // --- Private info tab ---
  readonly privateForm = this.fb.nonNullable.group({
    dateOfBirth: [''],
    residingAddress: [''],
    nationality: [''],
    personalEmail: [''],
    gender: [''],
    maritalStatus: [''],
    phone: [''],
    accountNumber: [''],
    bankName: [''],
    ifscCode: [''],
    panNo: [''],
    uanNo: [''],
  });

  // --- Salary tab ---
  readonly salaryForm = this.fb.nonNullable.group({
    monthlyWage: [0, [Validators.required, Validators.min(0)]],
    workingDaysPerWeek: [5],
    breakHours: [1],
    pfEmployeePercent: [12],
    pfEmployerPercent: [12],
    professionalTax: [200],
    components: this.fb.array(
      (['BASIC', 'HRA', 'STANDARD_ALLOWANCE', 'PERFORMANCE_BONUS', 'LTA'] as ComponentType[]).map((type) =>
        this.fb.nonNullable.group({
          type: [type],
          computationType: ['PERCENTAGE' as ComputationType],
          value: [0],
        }),
      ),
    ),
  });

  // --- Security tab ---
  readonly securityForm = this.fb.nonNullable.group(
    {
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
  );
  readonly securityMessage = signal<string | null>(null);

  get componentsArray(): FormArray {
    return this.salaryForm.controls.components;
  }

  /**
   * Live client-side preview mirroring LLD §2.5 — server recomputes authoritatively on save.
   *
   * Deliberately a plain method, not a computed() signal: it reads Reactive Forms'
   * `.value` properties, which are NOT signals, so a computed() here would have no
   * reactive dependency and would cache its first result forever (same pitfall as
   * AuthService.isAuthenticated — see that fix). A plain method re-evaluates on every
   * change-detection pass, which zone.js already triggers on each form input event.
   */
  computedAmounts(): {
    amounts: Partial<Record<ComponentType, number>>;
    fixedAllowance: number;
    basic: number;
    exceeds: boolean;
  } {
    const wage = this.salaryForm.controls.monthlyWage.value || 0;
    const comps = this.componentsArray.value as { type: ComponentType; computationType: ComputationType; value: number }[];
    let basic = 0;
    let running = 0;
    const amounts: Partial<Record<ComponentType, number>> = {};
    for (const c of comps) {
      const amount = c.type === 'BASIC'
        ? (c.computationType === 'FIXED' ? c.value : (wage * c.value) / 100)
        : (c.computationType === 'FIXED' ? c.value : (basic * c.value) / 100);
      if (c.type === 'BASIC') basic = amount;
      amounts[c.type] = amount;
      running += amount;
    }
    const fixedAllowance = wage - running;
    return { amounts, fixedAllowance, basic, exceeds: fixedAllowance < 0 };
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe((qp) => {
      if (qp['forceSecurity']) {
        this.forcedSecurity.set(true);
        this.activeTab.set('security');
      }
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    this.isMe.set(!idParam);
    this.editMode.set(!idParam);

    const source = idParam
      ? this.employeeService.getById(Number(idParam))
      : this.employeeService.getMe();

    source.subscribe({
      next: (profile) => this.hydrate(profile),
      error: () => this.loading.set(false),
    });
  }

  private hydrate(profile: EmployeeProfileModel): void {
    this.profile.set(profile);
    this.resumeForm.patchValue(profile.resume);
    this.skills.set(profile.resume.skills ?? []);
    this.certifications.set(profile.resume.certifications ?? []);
    this.privateForm.patchValue({
      ...profile.privateInfo,
      phone: profile.mobile,
      ...profile.privateInfo.bankDetails,
    });
    this.loading.set(false);

    if (profile.salaryVisible) {
      this.payrollService.getSalaryStructure(profile.id).subscribe({
        next: (structure) => {
          this.salary.set(structure);
          this.salaryForm.patchValue({
            monthlyWage: structure.monthlyWage,
            workingDaysPerWeek: structure.workingDaysPerWeek,
            breakHours: structure.breakHours,
            pfEmployeePercent: structure.pfEmployeePercent,
            pfEmployerPercent: structure.pfEmployerPercent,
            professionalTax: structure.professionalTax,
          });
          structure.components
            .filter((c) => c.type !== 'FIXED_ALLOWANCE')
            .forEach((c, i) => {
              this.componentsArray.at(i)?.patchValue({ computationType: c.computationType, value: c.value });
            });
          if (!profile.salaryEditable) {
            this.salaryForm.disable();
          }
        },
        error: () => void 0,
      });
      (this.isMe() ? this.payrollService.getMyPayslips() : this.payrollService.getPayslipsFor(profile.id)).subscribe({
        next: (list) => this.payslips.set(list),
        error: () => void 0,
      });
    }
  }

  amountFor(type: ComponentType): number {
    return this.computedAmounts().amounts[type] ?? 0;
  }

  setTab(tab: Tab): void {
    this.activeTab.set(tab);
  }

  enterEditMode(): void {
    this.editMode.set(true);
  }

  addSkill(): void {
    const value = this.newSkill().trim();
    if (value) {
      this.skills.update((list) => [...list, value]);
      this.newSkill.set('');
    }
  }

  removeSkill(index: number): void {
    this.skills.update((list) => list.filter((_, i) => i !== index));
  }

  saveResume(): void {
    const profile = this.profile();
    if (!profile) return;
    this.saving.set(true);
    // Resume/skills/certifications persistence is intentionally out of the MVP
    // EmployeeSelfEditRequest surface (see API doc §2 — dedicated resume endpoints);
    // this button is wired to a no-op success for now until those endpoints land.
    setTimeout(() => {
      this.saving.set(false);
      this.saveMessage.set('Resume saved.');
    }, 300);
  }

  savePrivateInfo(): void {
    const profile = this.profile();
    if (!profile) return;
    this.saving.set(true);
    const value = this.privateForm.getRawValue();
    this.employeeService.updateMe({ phone: value.phone, residingAddress: value.residingAddress }).subscribe({
      next: () => {
        this.saving.set(false);
        this.saveMessage.set('Private info saved.');
      },
      error: () => this.saving.set(false),
    });
  }

  saveSalary(): void {
    const profile = this.profile();
    if (!profile || !profile.salaryEditable) return;
    if (this.computedAmounts().exceeds) {
      this.saveMessage.set('Components exceed the wage — adjust before saving.');
      return;
    }
    this.saving.set(true);
    const value = this.salaryForm.getRawValue();
    const request: SalaryStructureRequest = {
      monthlyWage: value.monthlyWage,
      workingDaysPerWeek: value.workingDaysPerWeek,
      breakHours: value.breakHours,
      pfEmployeePercent: value.pfEmployeePercent,
      pfEmployerPercent: value.pfEmployerPercent,
      professionalTax: value.professionalTax,
      components: value.components.map((c) => ({
        type: c.type as Exclude<ComponentType, 'FIXED_ALLOWANCE'>,
        computationType: c.computationType,
        value: c.value,
      })),
    };
    this.payrollService
      .updateSalaryStructure(profile.id, request)
      .subscribe({
        next: (structure) => {
          this.salary.set(structure);
          this.saving.set(false);
          this.saveMessage.set('Salary structure updated.');
        },
        error: () => this.saving.set(false),
      });
  }

  generatePayslip(): void {
    const profile = this.profile();
    if (!profile) return;
    const now = new Date();
    this.payrollService.generatePayslip(profile.id, now.getMonth() + 1, now.getFullYear()).subscribe({
      next: (slip) => this.payslips.update((list) => [slip, ...list]),
    });
  }

  downloadPayslip(id: number): void {
    this.payrollService.downloadPayslip(id).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `payslip-${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  changePassword(): void {
    if (this.securityForm.invalid) {
      this.securityForm.markAllAsTouched();
      return;
    }
    const { currentPassword, newPassword, confirmPassword } = this.securityForm.getRawValue();
    if (newPassword !== confirmPassword) {
      this.securityMessage.set('New password and confirmation do not match.');
      return;
    }
    this.auth.changePassword({ currentPassword, newPassword, confirmPassword }).subscribe({
      next: () => {
        this.securityMessage.set('Password changed successfully.');
        this.forcedSecurity.set(false);
        this.securityForm.reset();
      },
      error: () => this.securityMessage.set('Could not change password — check your current password.'),
    });
  }
}
