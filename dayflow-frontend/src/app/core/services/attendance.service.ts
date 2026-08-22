import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-config';
import { Attendance, AttendanceRow, AttendanceSummary } from '../models/attendance.model';

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_BASE_URL}/attendance`;

  checkIn(): Observable<Attendance> {
    return this.http.post<Attendance>(`${this.base}/check-in`, {});
  }

  checkOut(): Observable<Attendance> {
    return this.http.post<Attendance>(`${this.base}/check-out`, {});
  }

  /** Day-wise, current month by default — SRS §3.4.1 */
  getMine(month: number, year: number): Observable<Attendance[]> {
    const params = new HttpParams().set('month', month).set('year', year);
    return this.http.get<Attendance[]>(`${this.base}/me`, { params });
  }

  getMySummary(month: number, year: number): Observable<AttendanceSummary> {
    const params = new HttpParams().set('month', month).set('year', year);
    return this.http.get<AttendanceSummary>(`${this.base}/me/summary`, { params });
  }

  /** Admin-only: all employees for a selected date — SRS §3.4.2 */
  getForDate(date: string, search: string): Observable<AttendanceRow[]> {
    let params = new HttpParams().set('date', date);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<AttendanceRow[]>(this.base, { params });
  }
}
