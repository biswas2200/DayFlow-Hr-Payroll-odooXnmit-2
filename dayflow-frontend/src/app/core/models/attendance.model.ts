export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'HALF_DAY' | 'ON_LEAVE';

export interface Attendance {
  id: number;
  date: string;
  checkInTime?: string;
  checkOutTime?: string;
  workHours?: string;
  extraHours?: string;
  status: AttendanceStatus;
}

export interface AttendanceRow extends Attendance {
  employeeId: number;
  employeeName: string;
}

export interface AttendanceSummary {
  daysPresent: number;
  leavesCount: number;
  totalWorkingDays: number;
}
