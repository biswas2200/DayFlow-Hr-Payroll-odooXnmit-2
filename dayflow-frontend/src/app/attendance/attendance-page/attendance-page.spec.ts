import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AttendanceService } from '../../core/services/attendance.service';
import { AuthService } from '../../core/services/auth.service';
import { AttendancePage } from './attendance-page';

describe('AttendancePage', () => {
  let fixture: ComponentFixture<AttendancePage>;
  let component: AttendancePage;
  let attendanceServiceStub: {
    getMine: ReturnType<typeof vi.fn>;
    getMySummary: ReturnType<typeof vi.fn>;
    getForDate: ReturnType<typeof vi.fn>;
  };

  function setup(isAdmin: boolean) {
    attendanceServiceStub = {
      getMine: vi.fn().mockReturnValue(of([{ id: 1, date: '2026-08-22', status: 'PRESENT' }])),
      getMySummary: vi.fn().mockReturnValue(of({ daysPresent: 1, leavesCount: 0, totalWorkingDays: 22 })),
      getForDate: vi.fn().mockReturnValue(of([{ id: 1, employeeId: 2, employeeName: 'Jon Doe', date: '2026-08-22', status: 'PRESENT' }])),
    };
    TestBed.configureTestingModule({
      imports: [AttendancePage],
      providers: [
        { provide: AttendanceService, useValue: attendanceServiceStub },
        { provide: AuthService, useValue: { isAdmin: () => isAdmin } },
      ],
    });
    fixture = TestBed.createComponent(AttendancePage);
    component = fixture.componentInstance;
  }

  describe('as Employee', () => {
    beforeEach(() => setup(false));

    it('loads own attendance and summary for the current month, not the admin view', () => {
      component.ngOnInit();
      expect(attendanceServiceStub.getMine).toHaveBeenCalledWith(component.month(), component.year());
      expect(attendanceServiceStub.getMySummary).toHaveBeenCalled();
      expect(attendanceServiceStub.getForDate).not.toHaveBeenCalled();
      expect(component.myRows().length).toBe(1);
      expect(component.summary()?.daysPresent).toBe(1);
    });

    it('shiftMonth() wraps from December to January of the next year', () => {
      component.month.set(12);
      component.year.set(2026);
      component.shiftMonth(1);
      expect(component.month()).toBe(1);
      expect(component.year()).toBe(2027);
    });

    it('shiftMonth() wraps from January back to December of the previous year', () => {
      component.month.set(1);
      component.year.set(2026);
      component.shiftMonth(-1);
      expect(component.month()).toBe(12);
      expect(component.year()).toBe(2025);
    });
  });

  describe('as Admin', () => {
    beforeEach(() => setup(true));

    it('loads all-employees attendance for the selected date, not the self view', () => {
      component.ngOnInit();
      expect(attendanceServiceStub.getForDate).toHaveBeenCalledWith(component.selectedDate(), '');
      expect(attendanceServiceStub.getMine).not.toHaveBeenCalled();
      expect(component.adminRows().length).toBe(1);
    });

    it('onSearchChange() reloads with the new search term', () => {
      component.ngOnInit();
      component.onSearchChange('jon');
      expect(attendanceServiceStub.getForDate).toHaveBeenLastCalledWith(component.selectedDate(), 'jon');
    });

    it('shiftDate() moves the selected date by the given number of days', () => {
      component.selectedDate.set('2026-08-22');
      component.shiftDate(1);
      expect(component.selectedDate()).toBe('2026-08-23');
      component.shiftDate(-2);
      expect(component.selectedDate()).toBe('2026-08-21');
    });
  });
});
