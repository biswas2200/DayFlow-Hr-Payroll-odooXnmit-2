import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-config';
import {
  ApplyLeaveRequest,
  LeaveAllocation,
  LeaveBalance,
  LeaveCalendar,
  LeaveRequest,
  LeaveStatus,
  LeaveType,
  PublicHoliday,
} from '../models/leave.model';

@Injectable({ providedIn: 'root' })
export class LeaveService {
  private readonly http = inject(HttpClient);

  getTypes(): Observable<LeaveType[]> {
    return this.http.get<LeaveType[]>(`${API_BASE_URL}/leave-types`);
  }

  getMyBalances(): Observable<LeaveBalance[]> {
    return this.http.get<LeaveBalance[]>(`${API_BASE_URL}/leave-balances/me`);
  }

  /** Apply for leave (multipart — optional attachment for e.g. sick leave) — SRS §3.5.2 */
  apply(request: ApplyLeaveRequest): Observable<LeaveRequest> {
    const form = new FormData();
    form.append('leaveTypeId', String(request.leaveTypeId));
    form.append('startDate', request.startDate);
    form.append('endDate', request.endDate);
    form.append('numDays', String(request.numDays));
    if (request.attachment) {
      form.append('attachment', request.attachment);
    }
    return this.http.post<LeaveRequest>(`${API_BASE_URL}/leave-requests`, form);
  }

  getMyCalendar(year: number): Observable<LeaveCalendar> {
    const params = new HttpParams().set('year', year);
    return this.http.get<LeaveCalendar>(`${API_BASE_URL}/leave-requests/me`, { params });
  }

  /** Admin-only approval queue — SRS §3.5.3 */
  getAllRequests(status?: LeaveStatus, page = 0): Observable<LeaveRequest[]> {
    let params = new HttpParams().set('page', page);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<LeaveRequest[]>(`${API_BASE_URL}/leave-requests`, { params });
  }

  approve(id: number, comment?: string): Observable<void> {
    return this.http.patch<void>(`${API_BASE_URL}/leave-requests/${id}/approve`, { comment });
  }

  reject(id: number, comment?: string): Observable<void> {
    return this.http.patch<void>(`${API_BASE_URL}/leave-requests/${id}/reject`, { comment });
  }

  getAllocations(employeeId: number): Observable<LeaveAllocation[]> {
    return this.http.get<LeaveAllocation[]>(`${API_BASE_URL}/leave-allocations/${employeeId}`);
  }

  updateAllocations(employeeId: number, allocations: LeaveAllocation[]): Observable<LeaveAllocation[]> {
    return this.http.put<LeaveAllocation[]>(`${API_BASE_URL}/leave-allocations/${employeeId}`, allocations);
  }

  getPublicHolidays(year: number): Observable<PublicHoliday[]> {
    const params = new HttpParams().set('year', year);
    return this.http.get<PublicHoliday[]>(`${API_BASE_URL}/public-holidays`, { params });
  }
}
