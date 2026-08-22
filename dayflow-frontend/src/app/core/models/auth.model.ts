import { Role } from './common.model';

export interface LoginRequest {
  loginId: string; // Login ID or Email — SRS §3.1.2
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  role: Role;
  mustChangePassword: boolean;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface CreateEmployeeRequest {
  companyName?: string;
  companyLogoUrl?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
}

export interface EmployeeResponse {
  id: number;
  loginId: string; // e.g. "OIJODO20220001" — SRS §3.1.1
  tempPasswordIssued: boolean;
}
