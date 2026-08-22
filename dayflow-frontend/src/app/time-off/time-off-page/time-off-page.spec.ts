import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { EmployeeService } from '../../core/services/employee.service';
import { LeaveService } from '../../core/services/leave.service';
import { TimeOffPage } from './time-off-page';

describe('TimeOffPage', () => {
  let fixture: ComponentFixture<TimeOffPage>;
  let component: TimeOffPage;
  let leaveServiceStub: {
    getTypes: ReturnType<typeof vi.fn>;
    getMyBalances: ReturnType<typeof vi.fn>;
    getMyCalendar: ReturnType<typeof vi.fn>;
    getAllRequests: ReturnType<typeof vi.fn>;
    approve: ReturnType<typeof vi.fn>;
    reject: ReturnType<typeof vi.fn>;
    apply: ReturnType<typeof vi.fn>;
    getAllocations: ReturnType<typeof vi.fn>;
    updateAllocations: ReturnType<typeof vi.fn>;
  };
  let employeeServiceStub: { getDirectory: ReturnType<typeof vi.fn> };

  function setup(isAdmin: boolean) {
    leaveServiceStub = {
      getTypes: vi.fn().mockReturnValue(of([{ id: 1, name: 'Paid Time Off', isPaid: true, requiresAttachment: false }])),
      getMyBalances: vi.fn().mockReturnValue(of([{ leaveTypeId: 1, leaveTypeName: 'Paid Time Off', allocatedDays: 24, usedDays: 6, availableDays: 18 }])),
      getMyCalendar: vi.fn().mockReturnValue(of({ year: 2026, requests: [], publicHolidays: [] })),
      getAllRequests: vi.fn().mockReturnValue(of([{ id: 5, employeeId: 2, employeeName: 'Jon Doe', leaveType: 'Paid Time Off', startDate: '2026-09-01', endDate: '2026-09-01', numDays: 1, status: 'PENDING' }])),
      approve: vi.fn().mockReturnValue(of(undefined)),
      reject: vi.fn().mockReturnValue(of(undefined)),
      apply: vi.fn(),
      getAllocations: vi.fn().mockReturnValue(of([{ leaveTypeId: 1, leaveTypeName: 'Paid Time Off', year: 2026, allocatedDays: 24, usedDays: 6 }])),
      updateAllocations: vi.fn().mockReturnValue(of([])),
    };
    employeeServiceStub = {
      getDirectory: vi.fn().mockReturnValue(of({ content: [{ id: 2, name: 'Jon Doe', statusDot: 'GREEN' }], totalElements: 1, page: 0, size: 100 })),
    };
    TestBed.configureTestingModule({
      imports: [TimeOffPage],
      providers: [
        { provide: LeaveService, useValue: leaveServiceStub },
        { provide: EmployeeService, useValue: employeeServiceStub },
        { provide: AuthService, useValue: { isAdmin: () => isAdmin } },
      ],
    });
    fixture = TestBed.createComponent(TimeOffPage);
    component = fixture.componentInstance;
  }

  describe('as Employee', () => {
    beforeEach(() => setup(false));

    it('loads types, balances and the calendar, not the admin queue', () => {
      component.ngOnInit();
      expect(leaveServiceStub.getTypes).toHaveBeenCalled();
      expect(leaveServiceStub.getMyBalances).toHaveBeenCalled();
      expect(leaveServiceStub.getMyCalendar).toHaveBeenCalled();
      expect(leaveServiceStub.getAllRequests).not.toHaveBeenCalled();
      expect(component.balances()[0].availableDays).toBe(18);
    });

    describe('submitApply()', () => {
      beforeEach(() => component.ngOnInit());

      it('blocks submission and shows an error when type/dates are missing', () => {
        component.submitApply();
        expect(leaveServiceStub.apply).not.toHaveBeenCalled();
        expect(component.applyError()).toContain('Please fill in');
      });

      it('submits, prepends the new request, closes the form, and refreshes balances', () => {
        component.applyModel.leaveTypeId = 1;
        component.applyModel.startDate = '2026-09-10';
        component.applyModel.endDate = '2026-09-10';
        component.applyModel.numDays = 1;
        leaveServiceStub.apply.mockReturnValue(
          of({ id: 9, employeeId: 2, employeeName: 'Jon Doe', leaveType: 'Paid Time Off', startDate: '2026-09-10', endDate: '2026-09-10', numDays: 1, status: 'PENDING' }),
        );

        component.submitApply();

        expect(component.myRequests()[0].id).toBe(9);
        expect(component.showApplyForm()).toBe(false);
        expect(leaveServiceStub.getMyBalances).toHaveBeenCalledTimes(2); // once on init, once after apply
      });

      it('surfaces the server-provided error message on failure (e.g. missing attachment)', () => {
        component.applyModel.leaveTypeId = 2;
        component.applyModel.startDate = '2026-09-10';
        component.applyModel.endDate = '2026-09-10';
        leaveServiceStub.apply.mockReturnValue(
          throwError(() => ({ error: { message: 'Attachment required for this leave type' } })),
        );

        component.submitApply();

        expect(component.applyError()).toBe('Attachment required for this leave type');
        expect(component.applyBusy()).toBe(false);
      });
    });
  });

  describe('as Admin', () => {
    beforeEach(() => setup(true));

    it('loads the pending queue and the employee list, not the self view', () => {
      component.ngOnInit();
      expect(leaveServiceStub.getAllRequests).toHaveBeenCalledWith('PENDING');
      expect(employeeServiceStub.getDirectory).toHaveBeenCalled();
      expect(leaveServiceStub.getMyBalances).not.toHaveBeenCalled();
      expect(component.allRequests().length).toBe(1);
    });

    it('approve() sends the drafted comment and reloads the queue', () => {
      component.ngOnInit();
      component.setComment(5, 'Enjoy!');
      component.approve(5);
      expect(leaveServiceStub.approve).toHaveBeenCalledWith(5, 'Enjoy!');
      expect(leaveServiceStub.getAllRequests).toHaveBeenCalledTimes(2);
    });

    it('reject() sends undefined when no comment was drafted', () => {
      component.ngOnInit();
      component.reject(5);
      expect(leaveServiceStub.reject).toHaveBeenCalledWith(5, undefined);
    });

    it('onStatusFilterChange() updates the filter and reloads', () => {
      component.ngOnInit();
      component.onStatusFilterChange('APPROVED');
      expect(component.statusFilter()).toBe('APPROVED');
      expect(leaveServiceStub.getAllRequests).toHaveBeenLastCalledWith('APPROVED');
    });

    it('selecting an employee loads their allocation, and saving persists it', () => {
      component.selectEmployeeForAllocation(2);
      expect(leaveServiceStub.getAllocations).toHaveBeenCalledWith(2);
      expect(component.selectedEmployeeId()).toBe(2);

      // Capture before saveAllocations() runs — its success handler resets the
      // allocations signal from the (mocked, empty) response, so reading the
      // signal after the call would just reflect that reset, not what was sent.
      const loadedAllocations = component.allocations();
      component.saveAllocations();
      expect(leaveServiceStub.updateAllocations).toHaveBeenCalledWith(2, loadedAllocations);
      expect(loadedAllocations[0].allocatedDays).toBe(24);
    });

    it('saveAllocations() is a no-op when no employee is selected', () => {
      component.saveAllocations();
      expect(leaveServiceStub.updateAllocations).not.toHaveBeenCalled();
    });
  });
});
