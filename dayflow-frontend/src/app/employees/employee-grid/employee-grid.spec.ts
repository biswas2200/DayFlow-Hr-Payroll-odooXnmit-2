import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeGrid } from './employee-grid';

describe('EmployeeGrid', () => {
  let fixture: ComponentFixture<EmployeeGrid>;
  let component: EmployeeGrid;
  let employeeServiceStub: { getDirectory: ReturnType<typeof vi.fn> };

  function setup(isAdmin = false) {
    employeeServiceStub = {
      getDirectory: vi.fn().mockReturnValue(
        of({
          content: [{ id: 1, name: 'Jon Doe', profilePictureUrl: '', statusDot: 'GREEN' }],
          totalElements: 1,
          page: 0,
          size: 24,
        }),
      ),
    };
    TestBed.configureTestingModule({
      imports: [EmployeeGrid],
      providers: [
        provideRouter([]),
        { provide: EmployeeService, useValue: employeeServiceStub },
        { provide: AuthService, useValue: { isAdmin: () => isAdmin } },
      ],
    });
    fixture = TestBed.createComponent(EmployeeGrid);
    component = fixture.componentInstance;
  }

  it('loads the directory with an empty search on init', () => {
    setup();
    component.ngOnInit();
    expect(employeeServiceStub.getDirectory).toHaveBeenCalledWith('');
    expect(component.cards().length).toBe(1);
    expect(component.loading()).toBe(false);
  });

  it('reloads with the new term when the search box changes', () => {
    setup();
    component.ngOnInit();
    component.onSearchChange('jon');
    expect(component.search()).toBe('jon');
    expect(employeeServiceStub.getDirectory).toHaveBeenLastCalledWith('jon');
  });

  it('exposes isAdmin from AuthService for the New button', () => {
    setup(true);
    expect(component.isAdmin()).toBe(true);
  });

  it('stops loading even if the request fails', () => {
    setup();
    employeeServiceStub.getDirectory.mockReturnValue(throwError(() => new Error('boom')));
    component.ngOnInit();
    expect(component.loading()).toBe(false);
  });
});
