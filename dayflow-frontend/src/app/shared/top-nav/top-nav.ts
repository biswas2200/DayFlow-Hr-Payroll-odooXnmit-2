import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AttendanceService } from '../../core/services/attendance.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

/**
 * Top navigation — Company Logo, Employees/Attendance/Time Off tabs, Check In/Out
 * systray, notification bell, avatar dropdown (My Profile / Log Out). SRS §3.2.1–3.2.2.
 */
@Component({
  selector: 'app-top-nav',
  imports: [RouterLink, RouterLinkActive, DatePipe],
  templateUrl: './top-nav.html',
  styleUrl: './top-nav.scss',
})
export class TopNav implements OnInit, OnDestroy {
  private readonly auth = inject(AuthService);
  private readonly attendanceService = inject(AttendanceService);
  private readonly router = inject(Router);
  readonly notificationService = inject(NotificationService);

  readonly isAdmin = this.auth.isAdmin;
  readonly menuOpen = signal(false);
  readonly notifOpen = signal(false);
  readonly checkedIn = signal(false);
  readonly checkInTime = signal<string | null>(null);
  readonly elapsed = signal('00:00');
  readonly busy = signal(false);

  private timer?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    this.notificationService.loadInitial();
    this.notificationService.connect();
    this.loadTodayStatus();
  }

  toggleNotif(): void {
    this.notifOpen.update((open) => !open);
    this.menuOpen.set(false);
  }

  markNotifRead(id: number): void {
    this.notificationService.markRead(id).subscribe();
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  private loadTodayStatus(): void {
    const now = new Date();
    this.attendanceService.getMine(now.getMonth() + 1, now.getFullYear()).subscribe({
      next: (rows) => {
        const today = now.toISOString().slice(0, 10);
        const todayRow = rows.find((r) => r.date === today);
        if (todayRow?.checkInTime && !todayRow.checkOutTime) {
          this.checkedIn.set(true);
          this.checkInTime.set(todayRow.checkInTime);
          this.startTimer();
        }
      },
      error: () => void 0,
    });
  }

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
    this.notifOpen.set(false);
  }

  checkIn(): void {
    this.busy.set(true);
    this.attendanceService.checkIn().subscribe({
      next: (att) => {
        this.checkedIn.set(true);
        this.checkInTime.set(att.checkInTime ?? null);
        this.startTimer();
        this.busy.set(false);
      },
      error: () => this.busy.set(false),
    });
  }

  checkOut(): void {
    this.busy.set(true);
    this.attendanceService.checkOut().subscribe({
      next: () => {
        this.checkedIn.set(false);
        this.checkInTime.set(null);
        if (this.timer) {
          clearInterval(this.timer);
        }
        this.busy.set(false);
      },
      error: () => this.busy.set(false),
    });
  }

  logout(): void {
    this.notificationService.disconnect();
    this.auth.logout();
    this.router.navigate(['/auth/sign-in']);
  }

  private startTimer(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
    const update = () => {
      const time = this.checkInTime();
      if (!time) return;
      const [h, m] = time.split(':').map(Number);
      const start = new Date();
      start.setHours(h, m, 0, 0);
      const diffMs = Math.max(0, Date.now() - start.getTime());
      const hh = String(Math.floor(diffMs / 3_600_000)).padStart(2, '0');
      const mm = String(Math.floor((diffMs % 3_600_000) / 60_000)).padStart(2, '0');
      this.elapsed.set(`${hh}:${mm}`);
    };
    update();
    this.timer = setInterval(update, 30_000);
  }
}
