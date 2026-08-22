import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-config';
import { Payslip, SalaryStructure, SalaryStructureRequest } from '../models/payroll.model';

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private readonly http = inject(HttpClient);

  /** Read-only for the owner; full detail for Admin — SRS §3.6.1/§3.6.2 */
  getSalaryStructure(employeeId: number): Observable<SalaryStructure> {
    return this.http.get<SalaryStructure>(`${API_BASE_URL}/employees/${employeeId}/salary`);
  }

  /** Admin-only. Backend recomputes components; Fixed Allowance absorbs the remainder. */
  updateSalaryStructure(employeeId: number, request: SalaryStructureRequest): Observable<SalaryStructure> {
    return this.http.put<SalaryStructure>(`${API_BASE_URL}/employees/${employeeId}/salary`, request);
  }

  generatePayslip(employeeId: number, month: number, year: number): Observable<Payslip> {
    return this.http.post<Payslip>(`${API_BASE_URL}/payslips/generate`, { employeeId, month, year });
  }

  getMyPayslips(): Observable<Payslip[]> {
    return this.http.get<Payslip[]>(`${API_BASE_URL}/payslips/me`);
  }

  getPayslipsFor(employeeId: number): Observable<Payslip[]> {
    return this.http.get<Payslip[]>(`${API_BASE_URL}/payslips/${employeeId}`);
  }

  downloadPayslip(id: number): Observable<Blob> {
    return this.http.get(`${API_BASE_URL}/payslips/${id}/download`, { responseType: 'blob' });
  }
}
