import { TestBed } from '@angular/core/testing';
import { UrlTree, provideRouter } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { adminGuard } from './role.guard';

describe('adminGuard', () => {
  function setup(isAdmin: boolean) {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isAdmin: () => isAdmin } },
      ],
    });
  }

  it('allows navigation through for an Admin', () => {
    setup(true);
    const result = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));
    expect(result).toBe(true);
  });

  it('redirects a non-Admin back to the employee directory', () => {
    setup(false);
    const result = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));
    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/employees');
  });
});
