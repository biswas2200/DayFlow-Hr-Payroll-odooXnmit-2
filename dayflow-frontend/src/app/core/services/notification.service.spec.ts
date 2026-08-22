import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_BASE_URL } from '../config/app-config';
import { Notification } from '../models/notification.model';
import { NotificationService } from './notification.service';

// The live STOMP/WebSocket push path (connect()'s onConnect subscription) is
// deliberately not exercised here — jsdom doesn't implement WebSocket, and that
// path was already verified against a real two-tab browser session (see
// project memory). These tests cover the REST-driven half of the service:
// hydrating the bell, marking read, and preferences.
describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;

  const sample: Notification[] = [
    { id: 1, type: 'LEAVE_APPROVED', message: 'Approved', read: false, createdAt: '2026-08-22T10:00:00Z' },
    { id: 2, type: 'REMINDER', message: 'Check out', read: true, createdAt: '2026-08-21T10:00:00Z' },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('starts with an empty list and zero unread', () => {
    expect(service.notifications()).toEqual([]);
    expect(service.unreadCount()).toBe(0);
  });

  it('listMine() omits unreadOnly by default and includes it when requested', () => {
    service.listMine().subscribe();
    let req = httpMock.expectOne((r) => r.url === `${API_BASE_URL}/notifications/me`);
    expect(req.request.params.has('unreadOnly')).toBe(false);
    req.flush([]);

    service.listMine(true).subscribe();
    req = httpMock.expectOne((r) => r.url === `${API_BASE_URL}/notifications/me`);
    expect(req.request.params.get('unreadOnly')).toBe('true');
    req.flush([]);
  });

  describe('loadInitial()', () => {
    it('hydrates notifications and derives unreadCount from the read flags', () => {
      service.loadInitial();
      httpMock.expectOne(`${API_BASE_URL}/notifications/me`).flush(sample);

      expect(service.notifications()).toEqual(sample);
      expect(service.unreadCount()).toBe(1);
    });

    it('fails quietly, leaving state untouched, if the request errors', () => {
      service.loadInitial();
      httpMock.expectOne(`${API_BASE_URL}/notifications/me`).flush(null, { status: 500, statusText: 'Server Error' });

      expect(service.notifications()).toEqual([]);
      expect(service.unreadCount()).toBe(0);
    });
  });

  describe('markRead()', () => {
    it('PATCHes the notification and optimistically flips it read + decrements the count', () => {
      service.loadInitial();
      httpMock.expectOne(`${API_BASE_URL}/notifications/me`).flush(sample);
      expect(service.unreadCount()).toBe(1);

      service.markRead(1).subscribe();
      const req = httpMock.expectOne(`${API_BASE_URL}/notifications/1/read`);
      expect(req.request.method).toBe('PATCH');
      req.flush(null);

      expect(service.unreadCount()).toBe(0);
      expect(service.notifications().find((n) => n.id === 1)?.read).toBe(true);
    });

    it('never lets unreadCount go negative', () => {
      service.markRead(999).subscribe();
      httpMock.expectOne(`${API_BASE_URL}/notifications/999/read`).flush(null);
      expect(service.unreadCount()).toBe(0);
    });
  });

  it('getPreferences() / updatePreferences() hit the preferences endpoint', () => {
    service.getPreferences().subscribe();
    httpMock.expectOne(`${API_BASE_URL}/notification-preferences/me`).flush([]);

    service.updatePreferences([]).subscribe();
    const req = httpMock.expectOne(`${API_BASE_URL}/notification-preferences/me`);
    expect(req.request.method).toBe('PUT');
    req.flush(null);
  });

  describe('connect() / disconnect() guard clauses', () => {
    it('connect() is a no-op when there is no access token', () => {
      expect(() => service.connect()).not.toThrow();
    });

    it('disconnect() is safe to call before any connection was made', () => {
      expect(() => service.disconnect()).not.toThrow();
    });
  });
});
