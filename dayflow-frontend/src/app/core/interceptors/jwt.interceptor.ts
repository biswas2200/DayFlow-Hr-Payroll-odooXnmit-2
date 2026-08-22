import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { API_BASE_URL } from '../config/app-config';
import { AuthService } from '../services/auth.service';

const AUTH_FREE_PATHS = [`${API_BASE_URL}/auth/login`, `${API_BASE_URL}/auth/refresh`];

/**
 * Attaches the JWT access token to every request and transparently retries
 * once via /auth/refresh on a 401, per LLD §2.1 (JwtAuthFilter counterpart).
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const isAuthFree = AUTH_FREE_PATHS.some((p) => req.url.startsWith(p));
  const token = auth.accessToken;
  const authedReq = !isAuthFree && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authedReq).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status === 401 && !isAuthFree && auth.refreshToken) {
        return auth.refresh().pipe(
          switchMap(() => {
            const retried = req.clone({
              setHeaders: { Authorization: `Bearer ${auth.accessToken}` },
            });
            return next(retried);
          }),
          catchError((refreshErr) => {
            auth.logout();
            router.navigate(['/auth/sign-in']);
            return throwError(() => refreshErr);
          }),
        );
      }
      return throwError(() => err);
    }),
  );
};
