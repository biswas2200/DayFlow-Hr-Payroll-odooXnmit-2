import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { LeaveService } from '../../core/services/leave.service';
import { EmployeeService } from '../../core/services/employee.service';
import {
  LeaveAllocation,
  LeaveBalance,
  LeaveRequest,
  LeaveStatus,
  LeaveType,
  PublicHoliday,
} from '../../core/models/leave.model';
import { EmployeeCard } from '../../core/models/employee.model';

type AdminSubTab = 'requests' | 'allocation';

/**
 * Time Off — employee balances/calendar/apply (SRS §3.5.1–3.5.2) and the Admin
 * approval queue + allocation management (SRS §3.5.3), matching the wireframe's
 * "Time Off | Allocation" sub-tabs for Admin/HR.
 */
@Component({
  selector: 'app-time-off-page',
  imports: [FormsModule],
  templateUrl: './time-off-page.html',
  styleUrl: './time-off-page.scss',
})
export class TimeOffPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly leaveService = inject(LeaveService);
  private readonly employeeService = inject(EmployeeService);

  readonly isAdmin = this.auth.isAdmin;
  readonly loading = signal(true);
  readonly adminSubTab = signal<AdminSubTab>('requests');

  // Employee view
  readonly leaveTypes = signal<LeaveType[]>([]);
  readonly balances = signal<LeaveBalance[]>([]);
  readonly myRequests = signal<LeaveRequest[]>([]);
  readonly publicHolidays = signal<PublicHoliday[]>([]);
  readonly showApplyForm = signal(false);
  readonly applyBusy = signal(false);
  readonly applyError = signal<string | null>(null);
  readonly applyModel = { leaveTypeId: 0, startDate: '', endDate: '', numDays: 1 };
  attachmentFile: File | null = null;

  // Admin view
  readonly statusFilter = signal<LeaveStatus | ''>('PENDING');
  readonly allRequests = signal<LeaveRequest[]>([]);
  readonly employees = signal<EmployeeCard[]>([]);
  readonly selectedEmployeeId = signal<number | null>(null);
  readonly allocations = signal<LeaveAllocation[]>([]);

  ngOnInit(): void {
    if (this.isAdmin()) {
      this.loadAdminRequests();
      this.employeeService.getDirectory('', 0, 100).subscribe((page) => this.employees.set(page.content));
    } else {
      this.loadEmployeeView();
    }
  }

  private loadEmployeeView(): void {
    this.loading.set(true);
    this.leaveService.getTypes().subscribe((types) => this.leaveTypes.set(types));
    this.leaveService.getMyBalances().subscribe((balances) => this.balances.set(balances));
    const year = new Date().getFullYear();
    this.leaveService.getMyCalendar(year).subscribe({
      next: (cal) => {
        this.myRequests.set(cal.requests);
        this.publicHolidays.set(cal.publicHolidays);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  loadAdminRequests(): void {
    this.loading.set(true);
    this.leaveService.getAllRequests(this.statusFilter() || undefined).subscribe({
      next: (rows) => {
        this.allRequests.set(rows);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onStatusFilterChange(status: LeaveStatus | ''): void {
    this.statusFilter.set(status);
    this.loadAdminRequests();
  }

  readonly commentDrafts = signal<Record<number, string>>({});

  commentFor(id: number): string {
    return this.commentDrafts()[id] ?? '';
  }

  setComment(id: number, value: string): void {
    this.commentDrafts.update((drafts) => ({ ...drafts, [id]: value }));
  }

  approve(id: number): void {
    this.leaveService.approve(id, this.commentFor(id) || undefined).subscribe(() => this.loadAdminRequests());
  }

  reject(id: number): void {
    this.leaveService.reject(id, this.commentFor(id) || undefined).subscribe(() => this.loadAdminRequests());
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.attachmentFile = input.files?.[0] ?? null;
  }

  submitApply(): void {
    if (!this.applyModel.leaveTypeId || !this.applyModel.startDate || !this.applyModel.endDate) {
      this.applyError.set('Please fill in Type, Start Date and End Date.');
      return;
    }
    this.applyBusy.set(true);
    this.applyError.set(null);
    this.leaveService
      .apply({
        leaveTypeId: this.applyModel.leaveTypeId,
        startDate: this.applyModel.startDate,
        endDate: this.applyModel.endDate,
        numDays: this.applyModel.numDays,
        attachment: this.attachmentFile ?? undefined,
      })
      .subscribe({
        next: (req) => {
          this.myRequests.update((list) => [req, ...list]);
          this.showApplyForm.set(false);
          this.applyBusy.set(false);
          this.leaveService.getMyBalances().subscribe((b) => this.balances.set(b));
        },
        error: (err) => {
          this.applyBusy.set(false);
          this.applyError.set(err?.error?.message ?? 'Could not submit the request.');
        },
      });
  }

  selectEmployeeForAllocation(id: number): void {
    this.selectedEmployeeId.set(id);
    this.leaveService.getAllocations(id).subscribe((allocations) => this.allocations.set(allocations));
  }

  saveAllocations(): void {
    const id = this.selectedEmployeeId();
    if (!id) return;
    this.leaveService.updateAllocations(id, this.allocations()).subscribe((updated) => this.allocations.set(updated));
  }
}
