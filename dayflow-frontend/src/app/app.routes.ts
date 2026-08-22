import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/role.guard';
import { forcePasswordChangeGuard } from './core/guards/force-password-change.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'auth/sign-in',
    loadComponent: () => import('./auth/sign-in/sign-in').then((m) => m.SignIn),
  },
  {
    path: 'employees',
    canActivate: [authGuard, forcePasswordChangeGuard],
    loadComponent: () => import('./employees/employee-grid/employee-grid').then((m) => m.EmployeeGrid),
  },
  {
    path: 'employees/new',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./employees/employee-onboarding/employee-onboarding').then((m) => m.EmployeeOnboarding),
  },
  {
    path: 'employees/me',
    canActivate: [authGuard],
    loadComponent: () => import('./employees/employee-profile/employee-profile').then((m) => m.EmployeeProfile),
  },
  {
    path: 'employees/:id',
    canActivate: [authGuard, forcePasswordChangeGuard],
    loadComponent: () => import('./employees/employee-profile/employee-profile').then((m) => m.EmployeeProfile),
  },
  {
    path: 'attendance',
    canActivate: [authGuard, forcePasswordChangeGuard],
    loadComponent: () => import('./attendance/attendance-page/attendance-page').then((m) => m.AttendancePage),
  },
  {
    path: 'time-off',
    canActivate: [authGuard, forcePasswordChangeGuard],
    loadComponent: () => import('./time-off/time-off-page/time-off-page').then((m) => m.TimeOffPage),
  },
  {
    path: 'reports',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./reports/dashboard/dashboard').then((m) => m.Dashboard),
  },
  { path: '**', redirectTo: 'employees' },
];
