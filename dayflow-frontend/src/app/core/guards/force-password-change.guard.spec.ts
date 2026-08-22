import { TestBed } from '@angular/core/testing';
import { UrlTree, provideRouter } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { forcePasswordChangeGuard } from './force-password-change.guard';

describe('forcePasswordChangeGuard', () => {
  function setup(mustChangePassword: boolean) {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { mustChangePassword: () => mustChangePassword } },
      ],
    });
  }

  it('lets navigation through when no password change is pending', () => {
    setup(false);
    const result = TestBed.runInInjectionContext(() => forcePasswordChangeGuard({} as never, {} as never));
    expect(result).toBe(true);
  });

  it('redirects to My Profile with forceSecurity=1 when a temp password is still active', () => {
    setup(true);
    const result = TestBed.runInInjectionContext(() => forcePasswordChangeGuard({} as never, {} as never));
    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/employees/me?forceSecurity=1');
  });
});
