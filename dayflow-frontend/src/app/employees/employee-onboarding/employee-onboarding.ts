import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, ValidationErrors, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';

function passwordsMatch(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password && confirmPassword && password !== confirmPassword ? { mismatch: true } : null;
}

/**
 * Admin-only new-employee provisioning — SRS §3.1.1, wireframe "Sign Up Page".
 * There is no public self-registration; this is reached via Employees → New.
 */
@Component({
  selector: 'app-employee-onboarding',
  imports: [ReactiveFormsModule],
  templateUrl: './employee-onboarding.html',
  styleUrl: './employee-onboarding.scss',
})
export class EmployeeOnboarding {
  private readonly fb = inject(FormBuilder);
  private readonly employeeService = inject(EmployeeService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly created = signal<{ loginId: string } | null>(null);
  readonly showPassword = signal(false);
  readonly showConfirm = signal(false);

  readonly form = this.fb.nonNullable.group(
    {
      companyName: [''],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch },
  );

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.employeeService.create(this.form.getRawValue()).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.created.set({ loginId: res.loginId });
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('Could not create the employee. Check the details and try again.');
      },
    });
  }

  backToDirectory(): void {
    this.router.navigate(['/employees']);
  }
}
