import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ACCESS_TOKEN_KEY, API_BASE_URL, REFRESH_TOKEN_KEY, ROLE_KEY } from '../config/app-config';
import { LoginResponse } from '../models/auth.model';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const loginResponse: LoginResponse = {
    accessToken: 'access-1',
    refreshToken: 'refresh-1',
    role: 'EMPLOYEE',
    mustChangePassword: false,
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('starts unauthenticated with no stored session', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.accessToken).toBeNull();
    expect(service.role()).toBeNull();
  });

  it('picks up a session already sitting in localStorage at construction time', () => {
    localStorage.setItem(ACCESS_TOKEN_KEY, 'existing-token');
    localStorage.setItem(REFRESH_TOKEN_KEY, 'existing-refresh');
    localStorage.setItem(ROLE_KEY, 'ADMIN');

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const freshService = TestBed.inject(AuthService);

    expect(freshService.isAuthenticated()).toBe(true);
    expect(freshService.isAdmin()).toBe(true);
  });

  describe('login()', () => {
    it('POSTs credentials and, on success, flips isAuthenticated reactively', () => {
      // This is the exact bug that shipped once: isAuthenticated was a computed()
      // wrapping a plain localStorage getter, so it never noticed a login. Reading
      // the signal *before* login here pins down that regression if it comes back.
      expect(service.isAuthenticated()).toBe(false);

      service.login({ loginId: 'OIJODO20220002', password: 'Employee@123' }).subscribe();

      const req = httpMock.expectOne(`${API_BASE_URL}/auth/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ loginId: 'OIJODO20220002', password: 'Employee@123' });
      req.flush(loginResponse);

      expect(service.isAuthenticated()).toBe(true);
      expect(service.accessToken).toBe('access-1');
      expect(service.role()).toBe('EMPLOYEE');
      expect(service.isAdmin()).toBe(false);
      expect(service.mustChangePassword()).toBe(false);
    });

    it('persists tokens to localStorage so a reload keeps the session', () => {
      service.login({ loginId: 'x', password: 'y' }).subscribe();
      httpMock.expectOne(`${API_BASE_URL}/auth/login`).flush(loginResponse);

      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBe('access-1');
      expect(localStorage.getItem(REFRESH_TOKEN_KEY)).toBe('refresh-1');
      expect(localStorage.getItem(ROLE_KEY)).toBe('EMPLOYEE');
    });

    it('surfaces mustChangePassword from the response', () => {
      service.login({ loginId: 'x', password: 'y' }).subscribe();
      httpMock
        .expectOne(`${API_BASE_URL}/auth/login`)
        .flush({ ...loginResponse, mustChangePassword: true });

      expect(service.mustChangePassword()).toBe(true);
    });

    it('leaves the session untouched on a failed login', () => {
      service.login({ loginId: 'x', password: 'wrong' }).subscribe({ error: () => void 0 });
      httpMock
        .expectOne(`${API_BASE_URL}/auth/login`)
        .flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

      expect(service.isAuthenticated()).toBe(false);
    });
  });

  describe('refresh()', () => {
    it('POSTs the current refresh token and re-applies the session', () => {
      service.login({ loginId: 'x', password: 'y' }).subscribe();
      httpMock.expectOne(`${API_BASE_URL}/auth/login`).flush(loginResponse);

      service.refresh().subscribe();
      const req = httpMock.expectOne(`${API_BASE_URL}/auth/refresh`);
      expect(req.request.body).toEqual({ refreshToken: 'refresh-1' });
      req.flush({ ...loginResponse, accessToken: 'access-2', refreshToken: 'refresh-2' });

      expect(service.accessToken).toBe('access-2');
      expect(service.refreshToken).toBe('refresh-2');
    });
  });

  describe('changePassword()', () => {
    it('clears mustChangePassword on success', () => {
      service.login({ loginId: 'x', password: 'y' }).subscribe();
      httpMock
        .expectOne(`${API_BASE_URL}/auth/login`)
        .flush({ ...loginResponse, mustChangePassword: true });
      expect(service.mustChangePassword()).toBe(true);

      service
        .changePassword({ currentPassword: 'old', newPassword: 'newpass1', confirmPassword: 'newpass1' })
        .subscribe();
      const req = httpMock.expectOne(`${API_BASE_URL}/auth/change-password`);
      expect(req.request.method).toBe('POST');
      req.flush(null);

      expect(service.mustChangePassword()).toBe(false);
    });
  });

  describe('logout()', () => {
    it('clears the session immediately, before the server call resolves', () => {
      service.login({ loginId: 'x', password: 'y' }).subscribe();
      httpMock.expectOne(`${API_BASE_URL}/auth/login`).flush(loginResponse);
      expect(service.isAuthenticated()).toBe(true);

      service.logout();

      // Session is gone client-side right away — no need to wait on the network call.
      expect(service.isAuthenticated()).toBe(false);
      expect(service.accessToken).toBeNull();
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBeNull();

      const req = httpMock.expectOne(`${API_BASE_URL}/auth/logout`);
      expect(req.request.body).toEqual({ refreshToken: 'refresh-1' });
      req.flush(null);
    });

    it('does not call the server if there was no session to begin with', () => {
      service.logout();
      httpMock.expectNone(`${API_BASE_URL}/auth/logout`);
    });
  });
});
