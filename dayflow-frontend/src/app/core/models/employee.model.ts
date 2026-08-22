export type StatusDot = 'GREEN' | 'AIRPLANE' | 'YELLOW';
export type EmployeeStatus = 'ACTIVE' | 'INACTIVE';

export interface EmployeeCard {
  id: number;
  name: string;
  profilePictureUrl?: string;
  statusDot: StatusDot;
}

export interface EmployeeCardPage {
  content: EmployeeCard[];
  totalElements: number;
  page: number;
  size: number;
}

export interface Certification {
  name: string;
  issuer: string;
  issueDate: string;
}

export interface Resume {
  about?: string;
  whatILoveAboutMyJob?: string;
  interestsAndHobbies?: string;
  skills: string[];
  certifications: Certification[];
}

export interface BankDetails {
  accountNumber?: string;
  bankName?: string;
  ifscCode?: string;
  panNo?: string;
  uanNo?: string;
  empCode?: string;
}

export interface PrivateInfo {
  dateOfBirth?: string;
  residingAddress?: string;
  nationality?: string;
  personalEmail?: string;
  gender?: string;
  maritalStatus?: string;
  dateOfJoining?: string;
  bankDetails: BankDetails;
}

export interface EmployeeProfile {
  id: number;
  loginId: string;
  firstName: string;
  lastName: string;
  jobPosition?: string;
  department?: string;
  manager?: string;
  location?: string;
  email: string;
  mobile?: string;
  profilePictureUrl?: string;
  status: EmployeeStatus;
  resume: Resume;
  privateInfo: PrivateInfo;
  /** true if the caller may view the Salary Info tab at all (owner or Admin) — SRS §3.3.2 */
  salaryVisible: boolean;
  /** true only for ADMIN — Salary Info edit rights are Admin-only (SRS §4.2) */
  salaryEditable: boolean;
}

export interface EmployeeSelfEditRequest {
  phone?: string;
  residingAddress?: string;
  profilePictureUrl?: string;
}

export interface EmployeeAdminEditRequest extends EmployeeSelfEditRequest {
  jobPosition?: string;
  department?: string;
  managerId?: number;
  status?: EmployeeStatus;
}
