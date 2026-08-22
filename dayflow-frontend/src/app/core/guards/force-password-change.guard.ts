import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Blocks navigation anywhere except My Profile while a temp password is still
 * active — Process Flow §2 "Force redirect to Security tab, block other navigation".
 */
export const forcePasswordChangeGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.mustChangePassword()) {
    return router.createUrlTree(['/employees/me'], { queryParams: { forceSecurity: 1 } });
  }
  return true;
};
