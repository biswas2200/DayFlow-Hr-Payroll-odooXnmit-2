import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { API_BASE_URL } from '../config/app-config';
import { LoginResponse } from '../models/auth.model';
import { AuthService } from '../services/auth.service';
import { jwtInterceptor } from './jwt.interceptor';

class FakeAuthService {
  accessToken: string | null = 'token-1';
  refreshToken: string | null = 'refresh-1';
  refreshResult: LoginResponse | 'error' = { accessToken: 'token-2', refreshToken: 'refresh-2', role: 'EMPLOYEE', mustChangePassword: false };
  logoutCalled = false;

  refresh() {
    if (this.refreshResult === 'error') {
      return throwError(() => new Error('refresh failed'));
    }
    this.accessToken = this.refreshResult.accessToken;
    return of(this.refreshResult);
  }

  logout() {
    this.logoutCalled = true;
    this.accessToken = null;
    this.refreshToken = null;
  }
}

describe('jwtInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authStub: FakeAuthService;
  let router: Router;

  beforeEach(() => {
    authStub = new FakeAuthService();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authStub },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => httpMock.verify());

  it('attaches the bearer token to a normal API request', () => {
    http.get('/api/v1/employees').subscribe();
    const req = httpMock.expectOne('/api/v1/employees');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-1');
    req.flush([]);
  });

  it('does not attach a token to /auth/login', () => {
    http.post(`${API_BASE_URL}/auth/login`, {}).subscribe();
    const req = httpMock.expectOne(`${API_BASE_URL}/auth/login`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('does not attach a token to /auth/refresh', () => {
    http.post(`${API_BASE_URL}/auth/refresh`, {}).subscribe();
    const req = httpMock.expectOne(`${API_BASE_URL}/auth/refresh`);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('on a 401, transparently refreshes and retries once with the new token', () => {
    let result: unknown;
    http.get('/api/v1/employees').subscribe((res) => (result = res));

    const first = httpMock.expectOne('/api/v1/employees');
    expect(first.request.headers.get('Authorization')).toBe('Bearer token-1');
    first.flush(null, { status: 401, statusText: 'Unauthorized' });

    // Interceptor should have called refresh() and retried automatically —
    // exactly one more request goes out, with the new token attached.
    const retried = httpMock.expectOne('/api/v1/employees');
    expect(retried.request.headers.get('Authorization')).toBe('Bearer token-2');
    retried.flush(['ok']);

    expect(result).toEqual(['ok']);
    expect(authStub.logoutCalled).toBe(false);
  });

  it('logs out and redirects to sign-in if the refresh itself fails', () => {
    authStub.refreshResult = 'error';
    const navigateSpy = vi.spyOn(router, 'navigate');
    let sawError = false;

    http.get('/api/v1/employees').subscribe({ error: () => (sawError = true) });

    const first = httpMock.expectOne('/api/v1/employees');
    first.flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(sawError).toBe(true);
    expect(authStub.logoutCalled).toBe(true);
    expect(navigateSpy).toHaveBeenCalledWith(['/auth/sign-in']);
    httpMock.expectNone('/api/v1/employees');
  });

  it('does not attempt a refresh for a 401 on an auth-free path', () => {
    http.post(`${API_BASE_URL}/auth/login`, {}).subscribe({ error: () => void 0 });
    const req = httpMock.expectOne(`${API_BASE_URL}/auth/login`);
    req.flush(null, { status: 401, statusText: 'Unauthorized' });
    httpMock.expectNone(`${API_BASE_URL}/auth/refresh`);
  });

  it('leaves non-401 errors alone', () => {
    let sawStatus: number | undefined;
    http.get('/api/v1/employees').subscribe({
      error: (err) => (sawStatus = err.status),
    });
    const req = httpMock.expectOne('/api/v1/employees');
    req.flush(null, { status: 500, statusText: 'Server Error' });

    expect(sawStatus).toBe(500);
    httpMock.expectNone(`${API_BASE_URL}/auth/refresh`);
  });
});
