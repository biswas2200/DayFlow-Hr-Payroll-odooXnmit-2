import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, OnDestroy, inject, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { tap } from 'rxjs';
import { API_BASE_URL, WS_URL } from '../config/app-config';
import { Notification, NotificationPreference } from '../models/notification.model';
import { AuthService } from './auth.service';

/**
 * In-app bell (SRS §3.7). Polls REST for the initial unread list, then keeps a
 * live STOMP connection open on /topic/notifications/{userId} for push updates
 * (LLD §2.6 NotificationWebSocketHandler).
 */
@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private stompClient?: Client;

  private readonly _notifications = signal<Notification[]>([]);
  private readonly _unreadCount = signal(0);
  readonly notifications = this._notifications.asReadonly();
  readonly unreadCount = this._unreadCount.asReadonly();

  listMine(unreadOnly = false) {
    let params = new HttpParams();
    if (unreadOnly) {
      params = params.set('unreadOnly', true);
    }
    return this.http.get<Notification[]>(`${API_BASE_URL}/notifications/me`, { params });
  }

  /** Hydrates the bell from REST — call once after login/on app start. */
  loadInitial(): void {
    this.listMine().subscribe({
      next: (list) => {
        this._notifications.set(list);
        this._unreadCount.set(list.filter((n) => !n.read).length);
      },
      error: () => void 0,
    });
  }

  markRead(id: number) {
    return this.http.patch<void>(`${API_BASE_URL}/notifications/${id}/read`, {}).pipe(
      tap(() => {
        this._notifications.update((list) => list.map((n) => (n.id === id ? { ...n, read: true } : n)));
        this._unreadCount.update((n) => Math.max(0, n - 1));
      }),
    );
  }

  getPreferences() {
    return this.http.get<NotificationPreference[]>(`${API_BASE_URL}/notification-preferences/me`);
  }

  updatePreferences(preferences: NotificationPreference[]) {
    return this.http.put<void>(`${API_BASE_URL}/notification-preferences/me`, preferences);
  }

  /** Call once after login; safe to call multiple times (no-ops if already connected). */
  connect(): void {
    if (this.stompClient?.active || !this.auth.accessToken) {
      return;
    }
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    this.stompClient = new Client({
      brokerURL: `${protocol}://${location.host}${WS_URL}`,
      connectHeaders: { Authorization: `Bearer ${this.auth.accessToken}` },
      reconnectDelay: 5000,
      onConnect: () => {
        this.stompClient?.subscribe('/user/topic/notifications', (frame) => {
          const notification: Notification = JSON.parse(frame.body);
          this._notifications.update((list) => [notification, ...list]);
          this._unreadCount.update((n) => n + 1);
        });
      },
    });
    this.stompClient.activate();
  }

  disconnect(): void {
    this.stompClient?.deactivate();
    this.stompClient = undefined;
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
