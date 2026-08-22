import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AttendanceService } from '../../core/services/attendance.service';
import { AuthService } from '../../core/services/auth.service';
import { Attendance, AttendanceRow, AttendanceSummary } from '../../core/models/attendance.model';

/** Attendance — day-wise self view (SRS §3.4.2) for Employees, all-employees-for-a-day
 * view for Admin/HR. Kept as one role-aware component per the wireframe's shared tab. */
@Component({
  selector: 'app-attendance-page',
  imports: [FormsModule],
  templateUrl: './attendance-page.html',
  styleUrl: './attendance-page.scss',
})
export class AttendancePage implements OnInit {
  private readonly attendanceService = inject(AttendanceService);
  private readonly auth = inject(AuthService);

  readonly isAdmin = this.auth.isAdmin;
  readonly loading = signal(true);

  // Employee view
  readonly month = signal(new Date().getMonth() + 1);
  readonly year = signal(new Date().getFullYear());
  readonly myRows = signal<Attendance[]>([]);
  readonly summary = signal<AttendanceSummary | null>(null);

  // Admin view
  readonly selectedDate = signal(new Date().toISOString().slice(0, 10));
  readonly search = signal('');
  readonly adminRows = signal<AttendanceRow[]>([]);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    if (this.isAdmin()) {
      this.attendanceService.getForDate(this.selectedDate(), this.search()).subscribe({
        next: (rows) => {
          this.adminRows.set(rows);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    } else {
      this.attendanceService.getMine(this.month(), this.year()).subscribe({
        next: (rows) => {
          this.myRows.set(rows);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
      this.attendanceService.getMySummary(this.month(), this.year()).subscribe({
        next: (s) => this.summary.set(s),
        error: () => void 0,
      });
    }
  }

  shiftMonth(delta: number): void {
    let m = this.month() + delta;
    let y = this.year();
    if (m > 12) {
      m = 1;
      y += 1;
    } else if (m < 1) {
      m = 12;
      y -= 1;
    }
    this.month.set(m);
    this.year.set(y);
    this.load();
  }

  shiftDate(delta: number): void {
    const d = new Date(this.selectedDate());
    d.setDate(d.getDate() + delta);
    this.selectedDate.set(d.toISOString().slice(0, 10));
    this.load();
  }

  onSearchChange(value: string): void {
    this.search.set(value);
    this.load();
  }

  onDateChange(value: string): void {
    this.selectedDate.set(value);
    this.load();
  }
}
