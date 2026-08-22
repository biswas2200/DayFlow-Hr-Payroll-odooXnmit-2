import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API_BASE_URL } from '../config/app-config';
import { DashboardSummary } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);

  getDashboard() {
    return this.http.get<DashboardSummary>(`${API_BASE_URL}/reports/dashboard`);
  }

  getAttendanceSummaryReport() {
    return this.http.get(`${API_BASE_URL}/reports/attendance-summary`);
  }

  getLeaveUtilizationReport() {
    return this.http.get(`${API_BASE_URL}/reports/leave-utilization`);
  }
}
