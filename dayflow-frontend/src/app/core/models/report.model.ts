export interface DashboardSummary {
  attendancePercentToday: number;
  leaveTrends: Array<{ month: string; count: number }>;
  headcountByDepartment: Array<{ department: string; count: number }>;
}
