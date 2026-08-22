import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../config/app-config';
import { LeaveService } from './leave.service';

describe('LeaveService', () => {
  let service: LeaveService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LeaveService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getTypes() and getMyBalances() hit their read-only endpoints', () => {
    service.getTypes().subscribe();
    httpMock.expectOne(`${API_BASE_URL}/leave-types`).flush([]);

    service.getMyBalances().subscribe();
    httpMock.expectOne(`${API_BASE_URL}/leave-balances/me`).flush([]);
  });

  describe('apply()', () => {
    it('builds multipart form data with every field, including the file when present', () => {
      const file = new File(['cert'], 'sick-note.pdf');
      service
        .apply({ leaveTypeId: 2, startDate: '2026-09-01', endDate: '2026-09-02', numDays: 2, attachment: file })
        .subscribe();

      const req = httpMock.expectOne(`${API_BASE_URL}/leave-requests`);
      expect(req.request.method).toBe('POST');
      const body = req.request.body as FormData;
      expect(body instanceof FormData).toBe(true);
      expect(body.get('leaveTypeId')).toBe('2');
      expect(body.get('startDate')).toBe('2026-09-01');
      expect(body.get('endDate')).toBe('2026-09-02');
      expect(body.get('numDays')).toBe('2');
      expect(body.get('attachment')).toBe(file);
      req.flush({ id: 1, status: 'PENDING' });
    });

    it('omits the attachment field entirely when none is given', () => {
      service.apply({ leaveTypeId: 1, startDate: '2026-09-01', endDate: '2026-09-01', numDays: 1 }).subscribe();
      const req = httpMock.expectOne(`${API_BASE_URL}/leave-requests`);
      const body = req.request.body as FormData;
      expect(body.get('attachment')).toBeNull();
      req.flush({ id: 1, status: 'PENDING' });
    });
  });

  it('getMyCalendar() sends the requested year', () => {
    service.getMyCalendar(2026).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${API_BASE_URL}/leave-requests/me`);
    expect(req.request.params.get('year')).toBe('2026');
    req.flush({ year: 2026, requests: [], publicHolidays: [] });
  });

  it('getAllRequests() only includes status when given', () => {
    service.getAllRequests().subscribe();
    let req = httpMock.expectOne((r) => r.url === `${API_BASE_URL}/leave-requests`);
    expect(req.request.params.has('status')).toBe(false);
    req.flush([]);

    service.getAllRequests('PENDING').subscribe();
    req = httpMock.expectOne((r) => r.url === `${API_BASE_URL}/leave-requests`);
    expect(req.request.params.get('status')).toBe('PENDING');
    req.flush([]);
  });

  it('approve() and reject() PATCH with the optional comment', () => {
    service.approve(9, 'ok').subscribe();
    let req = httpMock.expectOne(`${API_BASE_URL}/leave-requests/9/approve`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ comment: 'ok' });
    req.flush(null);

    service.reject(9).subscribe();
    req = httpMock.expectOne(`${API_BASE_URL}/leave-requests/9/reject`);
    expect(req.request.body).toEqual({ comment: undefined });
    req.flush(null);
  });

  it('getAllocations() / updateAllocations() target the per-employee sub-resource', () => {
    service.getAllocations(3).subscribe();
    httpMock.expectOne(`${API_BASE_URL}/leave-allocations/3`).flush([]);

    service.updateAllocations(3, []).subscribe();
    const req = httpMock.expectOne(`${API_BASE_URL}/leave-allocations/3`);
    expect(req.request.method).toBe('PUT');
    req.flush([]);
  });

  it('getPublicHolidays() sends the requested year', () => {
    service.getPublicHolidays(2026).subscribe();
    const req = httpMock.expectOne((r) => r.url === `${API_BASE_URL}/public-holidays`);
    expect(req.request.params.get('year')).toBe('2026');
    req.flush([]);
  });
});
