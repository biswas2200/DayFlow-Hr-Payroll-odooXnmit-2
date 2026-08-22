export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface LeaveType {
  id: number;
  name: string; // "Paid Time Off" | "Sick Leave" | "Unpaid Leave" — SRS §3.5.1
  isPaid: boolean;
  requiresAttachment: boolean;
}

export interface LeaveBalance {
  leaveTypeId: number;
  leaveTypeName: string;
  allocatedDays: number;
  usedDays: number;
  availableDays: number;
}

export interface LeaveRequest {
  id: number;
  employeeId: number;
  employeeName: string;
  leaveType: string;
  startDate: string;
  endDate: string;
  numDays: number;
  status: LeaveStatus;
  approverComment?: string;
  attachmentUrl?: string;
}

export interface ApplyLeaveRequest {
  leaveTypeId: number;
  startDate: string;
  endDate: string;
  numDays: number;
  attachment?: File;
}

export interface PublicHoliday {
  name: string;
  date: string;
}

export interface LeaveCalendar {
  year: number;
  requests: LeaveRequest[];
  publicHolidays: PublicHoliday[];
}

export interface LeaveAllocation {
  leaveTypeId: number;
  leaveTypeName?: string;
  year: number;
  allocatedDays: number;
  usedDays: number;
}
