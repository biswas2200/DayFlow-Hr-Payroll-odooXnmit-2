import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../config/app-config';
import { AttendanceService } from './attendance.service';

describe('AttendanceService', () => {
  let service: AttendanceService;
  let httpMock: HttpTestingController;
  const base = `${API_BASE_URL}/attendance`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AttendanceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('checkIn() POSTs an empty body to /attendance/check-in', () => {
    service.checkIn().subscribe();
    const req = httpMock.expectOne(`${base}/check-in`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({ id: 1, date: '2026-08-22', status: 'PRESENT' });
  });

  it('checkOut() POSTs to /attendance/check-out', () => {
    service.checkOut().subscribe();
    const req = httpMock.expectOne(`${base}/check-out`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, date: '2026-08-22', status: 'PRESENT' });
  });

  it('getMine() sends month and year as query params', () => {
    service.getMine(8, 2026).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${base}/me`);
    expect(req.request.params.get('month')).toBe('8');
    expect(req.request.params.get('year')).toBe('2026');
    req.flush([]);
  });

  it('getMySummary() hits the summary sub-resource', () => {
    service.getMySummary(8, 2026).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${base}/me/summary`);
    req.flush({ daysPresent: 0, leavesCount: 0, totalWorkingDays: 22 });
  });

  it('getForDate() sends date always, search only when provided', () => {
    service.getForDate('2026-08-22', '').subscribe();
    let req = httpMock.expectOne((r) => r.url === base);
    expect(req.request.params.get('date')).toBe('2026-08-22');
    expect(req.request.params.has('search')).toBe(false);
    req.flush([]);

    service.getForDate('2026-08-22', 'jon').subscribe();
    req = httpMock.expectOne((r) => r.url === base);
    expect(req.request.params.get('search')).toBe('jon');
    req.flush([]);
  });
});
