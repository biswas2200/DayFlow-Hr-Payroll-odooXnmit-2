import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ReportService } from '../../core/services/report.service';
import { Dashboard } from './dashboard';

describe('Dashboard', () => {
  let fixture: ComponentFixture<Dashboard>;
  let component: Dashboard;
  let reportServiceStub: { getDashboard: ReturnType<typeof vi.fn> };

  function setup() {
    TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [{ provide: ReportService, useValue: reportServiceStub }],
    });
    fixture = TestBed.createComponent(Dashboard);
    component = fixture.componentInstance;
  }

  it('loads the summary on init', () => {
    reportServiceStub = {
      getDashboard: vi.fn().mockReturnValue(
        of({ attendancePercentToday: 82, leaveTrends: [], headcountByDepartment: [] }),
      ),
    };
    setup();
    component.ngOnInit();
    expect(component.summary()?.attendancePercentToday).toBe(82);
    expect(component.loading()).toBe(false);
  });

  it('stops loading even when the request fails', () => {
    reportServiceStub = { getDashboard: vi.fn().mockReturnValue(throwError(() => new Error('boom'))) };
    setup();
    component.ngOnInit();
    expect(component.loading()).toBe(false);
    expect(component.summary()).toBeNull();
  });

  describe('maxTrend() / maxHeadcount()', () => {
    beforeEach(() => {
      reportServiceStub = { getDashboard: vi.fn().mockReturnValue(of(null)) };
      setup();
    });

    it('returns the largest count so bar heights can be scaled', () => {
      expect(component.maxTrend([{ month: 'Apr', count: 3 }, { month: 'May', count: 7 }])).toBe(7);
      expect(component.maxHeadcount([{ department: 'Eng', count: 2 }, { department: 'HR', count: 1 }])).toBe(2);
    });

    it('floors at 1 for an empty list, so a division by it never produces Infinity', () => {
      expect(component.maxTrend([])).toBe(1);
      expect(component.maxHeadcount([])).toBe(1);
    });
  });
});
