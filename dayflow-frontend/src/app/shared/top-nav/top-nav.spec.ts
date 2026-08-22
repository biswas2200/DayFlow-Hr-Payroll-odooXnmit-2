import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AttendanceService } from '../../core/services/attendance.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { TopNav } from './top-nav';

describe('TopNav', () => {
  let fixture: ComponentFixture<TopNav>;
  let component: TopNav;
  let attendanceServiceStub: { getMine: ReturnType<typeof vi.fn>; checkIn: ReturnType<typeof vi.fn>; checkOut: ReturnType<typeof vi.fn> };
  let notificationServiceStub: { loadInitial: ReturnType<typeof vi.fn>; connect: ReturnType<typeof vi.fn>; disconnect: ReturnType<typeof vi.fn>; markRead: ReturnType<typeof vi.fn> };
  let authStub: { isAdmin: () => boolean; logout: ReturnType<typeof vi.fn> };
  let navigateSpy: ReturnType<typeof vi.fn>;

  function setup(todayRows: unknown[] = []) {
    attendanceServiceStub = {
      getMine: vi.fn().mockReturnValue(of(todayRows)),
      checkIn: vi.fn(),
      checkOut: vi.fn(),
    };
    notificationServiceStub = {
      loadInitial: vi.fn(),
      connect: vi.fn(),
      disconnect: vi.fn(),
      markRead: vi.fn().mockReturnValue(of(undefined)),
    };
    authStub = { isAdmin: () => false, logout: vi.fn() };

    TestBed.configureTestingModule({
      imports: [TopNav],
      providers: [
        provideRouter([]),
        { provide: AttendanceService, useValue: attendanceServiceStub },
        { provide: AuthService, useValue: authStub },
        { provide: NotificationService, useValue: notificationServiceStub },
      ],
    });
    fixture = TestBed.createComponent(TopNav);
    component = fixture.componentInstance;
    navigateSpy = vi.spyOn(TestBed.inject(Router), 'navigate').mockImplementation(() => Promise.resolve(true));
  }

  afterEach(() => vi.useRealTimers());

  it('ngOnInit hydrates notifications, connects, and checks today\'s attendance status', () => {
    setup();
    component.ngOnInit();
    expect(notificationServiceStub.loadInitial).toHaveBeenCalled();
    expect(notificationServiceStub.connect).toHaveBeenCalled();
    expect(attendanceServiceStub.getMine).toHaveBeenCalled();
    expect(component.checkedIn()).toBe(false);
  });

  it('picks up an already-checked-in-but-not-out row for today and starts the timer', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-08-22T10:30:00'));
    const today = new Date().toISOString().slice(0, 10);
    setup([{ id: 1, date: today, checkInTime: '10:00', checkOutTime: null, status: 'PRESENT' }]);

    component.ngOnInit();

    expect(component.checkedIn()).toBe(true);
    expect(component.checkInTime()).toBe('10:00');
    expect(component.elapsed()).toBe('00:30');
  });

  it('ignores a today row that has already been checked out', () => {
    const today = new Date().toISOString().slice(0, 10);
    setup([{ id: 1, date: today, checkInTime: '10:00', checkOutTime: '18:00', status: 'PRESENT' }]);
    component.ngOnInit();
    expect(component.checkedIn()).toBe(false);
  });

  describe('checkIn() / checkOut()', () => {
    beforeEach(() => setup());

    it('checkIn() marks checkedIn and captures the returned check-in time', () => {
      attendanceServiceStub.checkIn.mockReturnValue(of({ id: 1, date: '2026-08-22', checkInTime: '09:15', status: 'PRESENT' }));
      component.checkIn();
      expect(component.checkedIn()).toBe(true);
      expect(component.checkInTime()).toBe('09:15');
      expect(component.busy()).toBe(false);
    });

    it('checkIn() resets busy on failure without marking checked in', () => {
      attendanceServiceStub.checkIn.mockReturnValue({ subscribe: (h: { error: (e: unknown) => void }) => h.error(new Error('409')) });
      component.checkIn();
      expect(component.checkedIn()).toBe(false);
      expect(component.busy()).toBe(false);
    });

    it('checkOut() clears checkedIn and the captured check-in time', () => {
      attendanceServiceStub.checkIn.mockReturnValue(of({ id: 1, date: '2026-08-22', checkInTime: '09:15', status: 'PRESENT' }));
      component.checkIn();
      attendanceServiceStub.checkOut.mockReturnValue(of({ id: 1, date: '2026-08-22', checkInTime: '09:15', checkOutTime: '18:00', status: 'PRESENT' }));

      component.checkOut();

      expect(component.checkedIn()).toBe(false);
      expect(component.checkInTime()).toBeNull();
    });
  });

  describe('menus', () => {
    beforeEach(() => setup());

    it('opening the avatar menu closes the notification dropdown, and vice versa', () => {
      component.toggleNotif();
      expect(component.notifOpen()).toBe(true);

      component.toggleMenu();
      expect(component.menuOpen()).toBe(true);
      expect(component.notifOpen()).toBe(false);

      component.toggleNotif();
      expect(component.notifOpen()).toBe(true);
      expect(component.menuOpen()).toBe(false);
    });

    it('markNotifRead() delegates to the notification service', () => {
      component.markNotifRead(3);
      expect(notificationServiceStub.markRead).toHaveBeenCalledWith(3);
    });
  });

  it('logout() disconnects notifications, clears the session, and navigates to sign-in', () => {
    setup();
    component.logout();
    expect(notificationServiceStub.disconnect).toHaveBeenCalled();
    expect(authStub.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/auth/sign-in']);
  });

  it('clears the interval timer on destroy so it does not leak', () => {
    vi.useFakeTimers();
    setup();
    attendanceServiceStub.checkIn.mockReturnValue(of({ id: 1, date: '2026-08-22', checkInTime: '09:00', status: 'PRESENT' }));
    component.checkIn();
    const clearSpy = vi.spyOn(globalThis, 'clearInterval');

    component.ngOnDestroy();

    expect(clearSpy).toHaveBeenCalled();
  });
});
