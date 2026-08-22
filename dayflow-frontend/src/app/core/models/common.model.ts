// Shared shapes — mirrors documentation/design/09-api-documentation.md §3 components/schemas

export type Role = 'ADMIN' | 'EMPLOYEE';

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  page: number;
  size: number;
}
