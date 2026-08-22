import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../config/app-config';
import { ReportService } from './report.service';

describe('ReportService', () => {
  let service: ReportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ReportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getDashboard() GETs /reports/dashboard', () => {
    service.getDashboard().subscribe();
    httpMock.expectOne(`${API_BASE_URL}/reports/dashboard`).flush({});
  });

  it('getAttendanceSummaryReport() and getLeaveUtilizationReport() hit their paths', () => {
    service.getAttendanceSummaryReport().subscribe();
    httpMock.expectOne(`${API_BASE_URL}/reports/attendance-summary`).flush({});

    service.getLeaveUtilizationReport().subscribe();
    httpMock.expectOne(`${API_BASE_URL}/reports/leave-utilization`).flush({});
  });
});
