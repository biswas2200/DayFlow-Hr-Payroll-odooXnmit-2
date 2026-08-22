import { convertToParamMap } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeProfile as EmployeeProfileModel } from '../../core/models/employee.model';
import { PayrollService } from '../../core/services/payroll.service';
import { SalaryStructure } from '../../core/models/payroll.model';
import { EmployeeProfile } from './employee-profile';

function makeProfile(overrides: Partial<EmployeeProfileModel> = {}): EmployeeProfileModel {
  return {
    id: 2,
    loginId: 'OIJODO20220002',
    firstName: 'Jon',
    lastName: 'Doe',
    email: 'jon.doe@dayflow.io',
    mobile: '9000000002',
    status: 'ACTIVE',
    resume: { skills: [], certifications: [] },
    privateInfo: { bankDetails: {} },
    salaryVisible: true,
    salaryEditable: false,
    ...overrides,
  };
}

function makeSalaryStructure(monthlyWage: number): SalaryStructure {
  return {
    employeeId: 2,
    monthlyWage,
    yearlyWage: monthlyWage * 12,
    workingDaysPerWeek: 5,
    breakHours: 1,
    components: [
      { type: 'BASIC', computationType: 'PERCENTAGE', value: 50, computedAmount: monthlyWage * 0.5 },
      { type: 'HRA', computationType: 'PERCENTAGE', value: 50, computedAmount: monthlyWage * 0.25 },
      { type: 'STANDARD_ALLOWANCE', computationType: 'PERCENTAGE', value: 16.67, computedAmount: 0 },
      { type: 'PERFORMANCE_BONUS', computationType: 'PERCENTAGE', value: 8.33, computedAmount: 0 },
      { type: 'LTA', computationType: 'PERCENTAGE', value: 8.33, computedAmount: 0 },
      { type: 'FIXED_ALLOWANCE', computationType: 'FIXED', value: 0, computedAmount: 0 },
    ],
    pfEmployeePercent: 12,
    pfEmployerPercent: 12,
    professionalTax: 200,
  };
}

describe('EmployeeProfile', () => {
  let fixture: ComponentFixture<EmployeeProfile>;
  let component: EmployeeProfile;
  let employeeServiceStub: {
    getMe: ReturnType<typeof vi.fn>;
    getById: ReturnType<typeof vi.fn>;
    updateMe: ReturnType<typeof vi.fn>;
  };
  let payrollServiceStub: {
    getSalaryStructure: ReturnType<typeof vi.fn>;
    updateSalaryStructure: ReturnType<typeof vi.fn>;
    getMyPayslips: ReturnType<typeof vi.fn>;
    getPayslipsFor: ReturnType<typeof vi.fn>;
  };
  let authStub: { isAdmin: () => boolean; changePassword: ReturnType<typeof vi.fn> };

  function setup(opts: { idParam?: string; queryParams?: Record<string, string>; isAdmin?: boolean } = {}) {
    employeeServiceStub = {
      getMe: vi.fn().mockReturnValue(of(makeProfile())),
      getById: vi.fn().mockReturnValue(of(makeProfile())),
      updateMe: vi.fn().mockReturnValue(of(undefined)),
    };
    payrollServiceStub = {
      getSalaryStructure: vi.fn().mockReturnValue(of(makeSalaryStructure(50000))),
      updateSalaryStructure: vi.fn().mockReturnValue(of(makeSalaryStructure(50000))),
      getMyPayslips: vi.fn().mockReturnValue(of([])),
      getPayslipsFor: vi.fn().mockReturnValue(of([])),
    };
    authStub = { isAdmin: () => opts.isAdmin ?? false, changePassword: vi.fn() };

    TestBed.configureTestingModule({
      imports: [EmployeeProfile],
      providers: [
        { provide: EmployeeService, useValue: employeeServiceStub },
        { provide: PayrollService, useValue: payrollServiceStub },
        { provide: AuthService, useValue: authStub },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of(opts.queryParams ?? {}),
            snapshot: { paramMap: convertToParamMap(opts.idParam ? { id: opts.idParam } : {}) },
          },
        },
      ],
    });
    fixture = TestBed.createComponent(EmployeeProfile);
    component = fixture.componentInstance;
  }

  describe('salary component computation (computedAmounts)', () => {
    // Pinned to the exact worked example from the wireframe / LLD §2.5:
    // Wage 50,000 → Basic 50% = 25,000; HRA 50%-of-Basic = 12,500;
    // Standard Allowance 16.67%-of-Basic ≈ 4,167.50; Performance Bonus and LTA
    // 8.33%-of-Basic ≈ 2,082.50 each; Fixed Allowance absorbs the remainder.
    beforeEach(() => setup());

    it('computes every component as a percentage of Basic (except Basic itself, which is % of Wage)', () => {
      component.salaryForm.controls.monthlyWage.setValue(50000);
      component.componentsArray.at(0).patchValue({ computationType: 'PERCENTAGE', value: 50 }); // BASIC
      component.componentsArray.at(1).patchValue({ computationType: 'PERCENTAGE', value: 50 }); // HRA
      component.componentsArray.at(2).patchValue({ computationType: 'PERCENTAGE', value: 16.67 }); // STANDARD_ALLOWANCE
      component.componentsArray.at(3).patchValue({ computationType: 'PERCENTAGE', value: 8.33 }); // PERFORMANCE_BONUS
      component.componentsArray.at(4).patchValue({ computationType: 'PERCENTAGE', value: 8.33 }); // LTA

      const result = component.computedAmounts();

      expect(result.basic).toBe(25000);
      expect(result.amounts['BASIC']).toBe(25000);
      expect(result.amounts['HRA']).toBe(12500);
      expect(result.amounts['STANDARD_ALLOWANCE']).toBeCloseTo(4167.5, 2);
      expect(result.amounts['PERFORMANCE_BONUS']).toBeCloseTo(2082.5, 2);
      expect(result.amounts['LTA']).toBeCloseTo(2082.5, 2);
      expect(result.fixedAllowance).toBeCloseTo(4167.5, 2);
      expect(result.exceeds).toBe(false);

      // Invariant from LLD §2.5: Σ(components) must equal the wage exactly.
      const sum = Object.values(result.amounts).reduce((a, b) => a + (b ?? 0), 0) + result.fixedAllowance;
      expect(sum).toBeCloseTo(50000, 6);
    });

    it('re-evaluates on every call — it is a plain method, not a cached computed()', () => {
      // This is the regression test for the exact bug that shipped: computedAmounts()
      // used to be a computed() signal reading Reactive Forms' .value, which has no
      // signal dependency, so it cached its first result forever. Calling it twice
      // with different wages in between must produce different answers.
      component.salaryForm.controls.monthlyWage.setValue(50000);
      component.componentsArray.at(0).patchValue({ computationType: 'PERCENTAGE', value: 50 });
      const first = component.computedAmounts().basic;

      component.salaryForm.controls.monthlyWage.setValue(60000);
      const second = component.computedAmounts().basic;

      expect(first).toBe(25000);
      expect(second).toBe(30000);
      expect(second).not.toBe(first);
    });

    it('flags exceeds=true when the components alone outrun the wage', () => {
      component.salaryForm.controls.monthlyWage.setValue(1000);
      component.componentsArray.at(0).patchValue({ computationType: 'FIXED', value: 700 }); // BASIC
      component.componentsArray.at(1).patchValue({ computationType: 'FIXED', value: 500 }); // HRA
      component.componentsArray.at(2).patchValue({ computationType: 'FIXED', value: 0 });
      component.componentsArray.at(3).patchValue({ computationType: 'FIXED', value: 0 });
      component.componentsArray.at(4).patchValue({ computationType: 'FIXED', value: 0 });

      const result = component.computedAmounts();

      expect(result.fixedAllowance).toBeLessThan(0);
      expect(result.exceeds).toBe(true);
    });

    it('amountFor() reads the same numbers computedAmounts() would produce', () => {
      component.salaryForm.controls.monthlyWage.setValue(50000);
      component.componentsArray.at(0).patchValue({ computationType: 'PERCENTAGE', value: 50 });
      expect(component.amountFor('BASIC')).toBe(component.computedAmounts().amounts['BASIC']);
      expect(component.amountFor('FIXED_ALLOWANCE')).toBe(0); // not in `amounts`, falls back to 0
    });
  });

  describe('ngOnInit — /employees/me (own, editable)', () => {
    beforeEach(() => setup());

    it('loads via getMe(), marks isMe and editMode true, and is not loading afterwards', () => {
      component.ngOnInit();
      expect(employeeServiceStub.getMe).toHaveBeenCalled();
      expect(component.isMe()).toBe(true);
      expect(component.editMode()).toBe(true);
      expect(component.loading()).toBe(false);
      expect(component.profile()?.id).toBe(2);
    });

    it('fetches and patches the salary structure when salaryVisible is true', () => {
      component.ngOnInit();
      expect(payrollServiceStub.getSalaryStructure).toHaveBeenCalledWith(2);
      expect(component.salaryForm.controls.monthlyWage.value).toBe(50000);
      expect(component.salary()?.monthlyWage).toBe(50000);
    });

    it('disables the salary form when the profile says salaryEditable is false', () => {
      component.ngOnInit();
      expect(component.salaryForm.disabled).toBe(true);
    });
  });

  describe('ngOnInit — /employees/:id (viewing someone else)', () => {
    it('loads via getById() and starts in view-only mode', () => {
      setup({ idParam: '2' });
      component.ngOnInit();
      expect(employeeServiceStub.getById).toHaveBeenCalledWith(2);
      expect(component.isMe()).toBe(false);
      expect(component.editMode()).toBe(false);
    });

    it('canEdit is true for an Admin viewing someone else', () => {
      setup({ idParam: '2', isAdmin: true });
      component.ngOnInit();
      expect(component.canEdit()).toBe(true);
    });

    it('canEdit is false for a non-Admin viewing someone else', () => {
      setup({ idParam: '2', isAdmin: false });
      component.ngOnInit();
      expect(component.canEdit()).toBe(false);
    });

    it('does not fetch salary at all when salaryVisible is false', () => {
      setup({ idParam: '2' });
      employeeServiceStub.getById.mockReturnValue(of(makeProfile({ salaryVisible: false })));
      component.ngOnInit();
      expect(payrollServiceStub.getSalaryStructure).not.toHaveBeenCalled();
    });
  });

  describe('forced Security tab', () => {
    it('opens on the Security tab and sets forcedSecurity when ?forceSecurity=1', () => {
      setup({ queryParams: { forceSecurity: '1' } });
      component.ngOnInit();
      expect(component.forcedSecurity()).toBe(true);
      expect(component.activeTab()).toBe('security');
    });
  });

  describe('saveSalary()', () => {
    beforeEach(() => {
      setup({ isAdmin: true });
      employeeServiceStub.getMe.mockReturnValue(of(makeProfile({ salaryEditable: true })));
      component.ngOnInit();
    });

    it('refuses to save and shows a message when components exceed the wage', () => {
      component.salaryForm.controls.monthlyWage.setValue(100);
      component.componentsArray.at(0).patchValue({ computationType: 'FIXED', value: 5000 });

      component.saveSalary();

      expect(payrollServiceStub.updateSalaryStructure).not.toHaveBeenCalled();
      expect(component.saveMessage()).toContain('exceed the wage');
    });

    it('sends the recomputed structure and reports success', () => {
      component.salaryForm.controls.monthlyWage.setValue(60000);
      component.componentsArray.at(0).patchValue({ computationType: 'PERCENTAGE', value: 50 });

      component.saveSalary();

      expect(payrollServiceStub.updateSalaryStructure).toHaveBeenCalledWith(
        2,
        expect.objectContaining({ monthlyWage: 60000 }),
      );
      expect(component.saveMessage()).toBe('Salary structure updated.');
      expect(component.saving()).toBe(false);
    });
  });

  describe('changePassword()', () => {
    beforeEach(() => setup());

    it('does nothing and marks fields touched when the form is invalid', () => {
      component.changePassword();
      expect(authStub.changePassword).not.toHaveBeenCalled();
      expect(component.securityForm.touched).toBe(true);
    });

    it('rejects a mismatched confirmation without calling the API', () => {
      component.securityForm.setValue({
        currentPassword: 'old',
        newPassword: 'NewPass1!',
        confirmPassword: 'Different1!',
      });
      component.changePassword();
      expect(authStub.changePassword).not.toHaveBeenCalled();
      expect(component.securityMessage()).toContain('do not match');
    });

    it('calls the API and clears forcedSecurity on success', () => {
      authStub.changePassword.mockReturnValue(of(undefined));
      component.forcedSecurity.set(true);
      component.securityForm.setValue({
        currentPassword: 'old',
        newPassword: 'NewPass1!',
        confirmPassword: 'NewPass1!',
      });

      component.changePassword();

      expect(component.securityMessage()).toBe('Password changed successfully.');
      expect(component.forcedSecurity()).toBe(false);
    });

    it('shows an error message when the API rejects the change', () => {
      authStub.changePassword.mockReturnValue(throwError(() => new Error('wrong current password')));
      component.securityForm.setValue({
        currentPassword: 'bad',
        newPassword: 'NewPass1!',
        confirmPassword: 'NewPass1!',
      });

      component.changePassword();

      expect(component.securityMessage()).toContain('Could not change password');
    });
  });

  describe('skills list', () => {
    beforeEach(() => setup());

    it('addSkill() trims and appends, then clears the input', () => {
      component.newSkill.set('  Java  ');
      component.addSkill();
      expect(component.skills()).toEqual(['Java']);
      expect(component.newSkill()).toBe('');
    });

    it('addSkill() ignores blank input', () => {
      component.newSkill.set('   ');
      component.addSkill();
      expect(component.skills()).toEqual([]);
    });

    it('removeSkill() removes by index', () => {
      component.skills.set(['Java', 'Angular', 'SQL']);
      component.removeSkill(1);
      expect(component.skills()).toEqual(['Java', 'SQL']);
    });
  });

  it('renders the real template without throwing, end to end', () => {
    setup();
    expect(() => fixture.detectChanges()).not.toThrow();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Jon');
  });
});
