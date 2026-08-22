// Relative paths — Nginx (prod) / the dev proxy (proxy.conf.json) both forward these
// to the Spring Boot backend, per documentation/design/01-system-architecture.md §3.
export const API_BASE_URL = '/api/v1';
export const WS_URL = '/ws/notifications';

export const ACCESS_TOKEN_KEY = 'dayflow.accessToken';
export const REFRESH_TOKEN_KEY = 'dayflow.refreshToken';
export const ROLE_KEY = 'dayflow.role';
