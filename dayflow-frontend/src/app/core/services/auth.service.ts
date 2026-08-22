import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ACCESS_TOKEN_KEY, API_BASE_URL, REFRESH_TOKEN_KEY, ROLE_KEY } from '../config/app-config';
import { ChangePasswordRequest, LoginRequest, LoginResponse } from '../models/auth.model';
import { Role } from '../models/common.model';

/**
 * Auth & session state (SRS §3.1, §3.9). Holds tokens in localStorage and exposes
 * the current role/mustChangePassword as signals so guards and the top nav can react.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly _role = signal<Role | null>(
    (localStorage.getItem(ROLE_KEY) as Role | null) ?? null,
  );
  private readonly _mustChangePassword = signal(false);
  // Tokens are also mirrored into signals — a plain localStorage-reading getter
  // has no reactive dependency, so a computed() built on it would cache its
  // first value forever and never see a login/logout again.
  private readonly _accessToken = signal<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY));
  private readonly _refreshToken = signal<string | null>(localStorage.getItem(REFRESH_TOKEN_KEY));

  readonly role = this._role.asReadonly();
  readonly mustChangePassword = this._mustChangePassword.asReadonly();
  readonly isAuthenticated = computed(() => !!this._accessToken());
  readonly isAdmin = computed(() => this._role() === 'ADMIN');

  get accessToken(): string | null {
    return this._accessToken();
  }

  get refreshToken(): string | null {
    return this._refreshToken();
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE_URL}/auth/login`, request)
      .pipe(tap((res) => this.applySession(res)));
  }

  refresh(): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE_URL}/auth/refresh`, { refreshToken: this.refreshToken })
      .pipe(tap((res) => this.applySession(res)));
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/auth/change-password`, request).pipe(
      tap(() => this._mustChangePassword.set(false)),
    );
  }

  logout(): void {
    // Drop local session immediately; best-effort notify the server (with the
    // refresh token still in the body, since the header is gone once cleared)
    // so it can be invalidated server-side too.
    const refreshToken = this.refreshToken;
    this.clearSession();
    if (refreshToken) {
      this.http
        .post(`${API_BASE_URL}/auth/logout`, { refreshToken })
        .subscribe({ error: () => void 0 });
    }
  }

  private applySession(res: LoginResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, res.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
    localStorage.setItem(ROLE_KEY, res.role);
    this._accessToken.set(res.accessToken);
    this._refreshToken.set(res.refreshToken);
    this._role.set(res.role);
    this._mustChangePassword.set(res.mustChangePassword);
  }

  private clearSession(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    this._accessToken.set(null);
    this._refreshToken.set(null);
    this._role.set(null);
    this._mustChangePassword.set(false);
  }
}
