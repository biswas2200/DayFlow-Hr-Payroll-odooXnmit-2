import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Route-level mirror of the backend's @PreAuthorize checks (LLD §4 "defense in depth").
 * Usage: { path: 'approvals', canActivate: [authGuard, adminGuard], ... }
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAdmin()) {
    return true;
  }
  return router.createUrlTree(['/employees']);
};
