import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../config/app-config';
import { EmployeeService } from './employee.service';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;
  const base = `${API_BASE_URL}/employees`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('create() POSTs to the base employees path', () => {
    service
      .create({ firstName: 'Jon', lastName: 'Doe', email: 'j@x.com', phone: '9', password: 'p', confirmPassword: 'p' })
      .subscribe();
    const req = httpMock.expectOne(base);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, loginId: 'OIJODO20220001', tempPasswordIssued: true });
  });

  it('getDirectory() sends page/size always, and search only when non-empty', () => {
    service.getDirectory('', 2, 10).subscribe();
    let req = httpMock.expectOne((r) => r.url === base);
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.has('search')).toBe(false);
    req.flush({ content: [], totalElements: 0, page: 2, size: 10 });

    service.getDirectory('jon').subscribe();
    req = httpMock.expectOne((r) => r.url === base);
    expect(req.request.params.get('search')).toBe('jon');
    req.flush({ content: [], totalElements: 0, page: 0, size: 24 });
  });

  it('getById() and getMe() hit the right paths', () => {
    service.getById(5).subscribe();
    httpMock.expectOne(`${base}/5`).flush({});

    service.getMe().subscribe();
    httpMock.expectOne(`${base}/me`).flush({});
  });

  it('updateMe() PUTs to /employees/me with the self-edit payload', () => {
    service.updateMe({ phone: '999' }).subscribe();
    const req = httpMock.expectOne(`${base}/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ phone: '999' });
    req.flush(null);
  });

  it('updateAsAdmin() PUTs to /employees/{id}', () => {
    service.updateAsAdmin(7, { status: 'INACTIVE' }).subscribe();
    const req = httpMock.expectOne(`${base}/7`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ status: 'INACTIVE' });
    req.flush(null);
  });

  it('setStatus() PATCHes the status sub-resource', () => {
    service.setStatus(7, 'ACTIVE').subscribe();
    const req = httpMock.expectOne(`${base}/7/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'ACTIVE' });
    req.flush(null);
  });

  it('uploadMyPhoto() sends the file as multipart form data', () => {
    const file = new File(['x'], 'avatar.png', { type: 'image/png' });
    service.uploadMyPhoto(file).subscribe();
    const req = httpMock.expectOne(`${base}/me/photo`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBe(true);
    req.flush({ profilePictureUrl: '/x.png' });
  });
});
