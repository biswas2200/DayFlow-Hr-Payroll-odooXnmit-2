import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { SignIn } from './sign-in';

describe('SignIn', () => {
  let fixture: ComponentFixture<SignIn>;
  let component: SignIn;
  let authStub: { login: ReturnType<typeof vi.fn> };
  let navigateSpy: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    authStub = { login: vi.fn() };
    navigateSpy = vi.fn();

    TestBed.configureTestingModule({
      imports: [SignIn],
      providers: [
        { provide: AuthService, useValue: authStub },
        { provide: Router, useValue: { navigate: navigateSpy } },
      ],
    });
    fixture = TestBed.createComponent(SignIn);
    component = fixture.componentInstance;
  });

  it('does not call the API and marks the form touched when fields are empty', () => {
    component.submit();
    expect(authStub.login).not.toHaveBeenCalled();
    expect(component.form.touched).toBe(true);
  });

  it('logs in and redirects to the directory on success', () => {
    authStub.login.mockReturnValue(
      of({ accessToken: 'a', refreshToken: 'r', role: 'EMPLOYEE', mustChangePassword: false }),
    );
    component.form.setValue({ loginId: 'OIJODO20220002', password: 'Employee@123' });

    component.submit();

    expect(authStub.login).toHaveBeenCalledWith({ loginId: 'OIJODO20220002', password: 'Employee@123' });
    expect(navigateSpy).toHaveBeenCalledWith(['/employees']);
    expect(component.submitting()).toBe(false);
    expect(component.errorMessage()).toBeNull();
  });

  it('redirects to the forced Security tab when mustChangePassword is true', () => {
    authStub.login.mockReturnValue(
      of({ accessToken: 'a', refreshToken: 'r', role: 'EMPLOYEE', mustChangePassword: true }),
    );
    component.form.setValue({ loginId: 'OINEWU20260003', password: 'Temp@1234' });

    component.submit();

    expect(navigateSpy).toHaveBeenCalledWith(['/employees/me'], { queryParams: { forceSecurity: 1 } });
  });

  it('shows a specific message for a 401 and a generic one otherwise', () => {
    component.form.setValue({ loginId: 'x', password: 'wrong' });
    authStub.login.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 401 })));
    component.submit();
    expect(component.errorMessage()).toBe('Incorrect Login ID/Email or password.');
    expect(component.submitting()).toBe(false);

    authStub.login.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    component.submit();
    expect(component.errorMessage()).toBe('Something went wrong. Please try again.');
  });
});
