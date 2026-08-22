import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-config';
import { CreateEmployeeRequest, EmployeeResponse } from '../models/auth.model';
import {
  EmployeeAdminEditRequest,
  EmployeeCardPage,
  EmployeeProfile,
  EmployeeSelfEditRequest,
  EmployeeStatus,
} from '../models/employee.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_BASE_URL}/employees`;

  /** Admin-only account provisioning — SRS §3.1.1 */
  create(request: CreateEmployeeRequest): Observable<EmployeeResponse> {
    return this.http.post<EmployeeResponse>(this.base, request);
  }

  /** Directory card grid — SRS §3.2.1 */
  getDirectory(search: string, page = 0, size = 24): Observable<EmployeeCardPage> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<EmployeeCardPage>(this.base, { params });
  }

  getById(id: number): Observable<EmployeeProfile> {
    return this.http.get<EmployeeProfile>(`${this.base}/${id}`);
  }

  getMe(): Observable<EmployeeProfile> {
    return this.http.get<EmployeeProfile>(`${this.base}/me`);
  }

  updateMe(request: EmployeeSelfEditRequest): Observable<void> {
    return this.http.put<void>(`${this.base}/me`, request);
  }

  updateAsAdmin(id: number, request: EmployeeAdminEditRequest): Observable<void> {
    return this.http.put<void>(`${this.base}/${id}`, request);
  }

  setStatus(id: number, status: EmployeeStatus): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/status`, { status });
  }

  uploadMyPhoto(file: File): Observable<{ profilePictureUrl: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ profilePictureUrl: string }>(`${this.base}/me/photo`, form);
  }
}
