import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../config/app-config';
import { SalaryStructureRequest } from '../models/payroll.model';
import { PayrollService } from './payroll.service';

describe('PayrollService', () => {
  let service: PayrollService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PayrollService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getSalaryStructure() GETs the per-employee salary path', () => {
    service.getSalaryStructure(2).subscribe();
    httpMock.expectOne(`${API_BASE_URL}/employees/2/salary`).flush({});
  });

  it('updateSalaryStructure() PUTs the request body as-is', () => {
    const request: SalaryStructureRequest = {
      monthlyWage: 50000,
      workingDaysPerWeek: 5,
      breakHours: 1,
      components: [{ type: 'BASIC', computationType: 'PERCENTAGE', value: 50 }],
      pfEmployeePercent: 12,
      pfEmployerPercent: 12,
      professionalTax: 200,
    };
    service.updateSalaryStructure(2, request).subscribe();
    const req = httpMock.expectOne(`${API_BASE_URL}/employees/2/salary`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush({});
  });

  it('generatePayslip() POSTs employeeId/month/year', () => {
    service.generatePayslip(2, 8, 2026).subscribe();
    const req = httpMock.expectOne(`${API_BASE_URL}/payslips/generate`);
    expect(req.request.body).toEqual({ employeeId: 2, month: 8, year: 2026 });
    req.flush({});
  });

  it('getMyPayslips() and getPayslipsFor() hit the right paths', () => {
    service.getMyPayslips().subscribe();
    httpMock.expectOne(`${API_BASE_URL}/payslips/me`).flush([]);

    service.getPayslipsFor(2).subscribe();
    httpMock.expectOne(`${API_BASE_URL}/payslips/2`).flush([]);
  });

  it('downloadPayslip() requests a blob response type', () => {
    service.downloadPayslip(11).subscribe();
    const req = httpMock.expectOne(`${API_BASE_URL}/payslips/11/download`);
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['%PDF']));
  });
});
