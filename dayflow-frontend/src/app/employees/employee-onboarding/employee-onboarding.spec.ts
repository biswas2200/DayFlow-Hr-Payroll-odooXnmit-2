import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeOnboarding } from './employee-onboarding';

describe('EmployeeOnboarding', () => {
  let fixture: ComponentFixture<EmployeeOnboarding>;
  let component: EmployeeOnboarding;
  let employeeServiceStub: { create: ReturnType<typeof vi.fn> };
  let navigateSpy: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    employeeServiceStub = { create: vi.fn() };
    navigateSpy = vi.fn();

    TestBed.configureTestingModule({
      imports: [EmployeeOnboarding],
      providers: [
        { provide: EmployeeService, useValue: employeeServiceStub },
        { provide: Router, useValue: { navigate: navigateSpy } },
      ],
    });
    fixture = TestBed.createComponent(EmployeeOnboarding);
    component = fixture.componentInstance;
  });

  function fillValidForm(overrides: Partial<{ password: string; confirmPassword: string }> = {}) {
    component.form.setValue({
      companyName: 'Odoo India',
      firstName: 'Jon',
      lastName: 'Doe',
      email: 'jon.doe@dayflow.io',
      phone: '9123456789',
      password: overrides.password ?? 'Welcome@2026',
      confirmPassword: overrides.confirmPassword ?? 'Welcome@2026',
    });
  }

  it('flags mismatched passwords at the form-group level', () => {
    fillValidForm({ password: 'Welcome@2026', confirmPassword: 'Different@2026' });
    expect(component.form.errors?.['mismatch']).toBe(true);
    expect(component.form.valid).toBe(false);
  });

  it('is valid once both passwords match and required fields are filled', () => {
    fillValidForm();
    expect(component.form.valid).toBe(true);
  });

  it('does not call the API when the form is invalid', () => {
    component.submit();
    expect(employeeServiceStub.create).not.toHaveBeenCalled();
  });

  it('shows the generated Login ID on success', () => {
    fillValidForm();
    employeeServiceStub.create.mockReturnValue(of({ id: 4, loginId: 'OIJODO20260004', tempPasswordIssued: true }));

    component.submit();

    expect(component.created()).toEqual({ loginId: 'OIJODO20260004' });
    expect(component.submitting()).toBe(false);
  });

  it('shows an error message if the API call fails', () => {
    fillValidForm();
    employeeServiceStub.create.mockReturnValue(throwError(() => new Error('boom')));

    component.submit();

    expect(component.created()).toBeNull();
    expect(component.errorMessage()).toContain('Could not create the employee');
  });

  it('navigates back to the directory', () => {
    component.backToDirectory();
    expect(navigateSpy).toHaveBeenCalledWith(['/employees']);
  });
});
