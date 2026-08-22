export type ComponentType =
  | 'BASIC'
  | 'HRA'
  | 'STANDARD_ALLOWANCE'
  | 'PERFORMANCE_BONUS'
  | 'LTA'
  | 'FIXED_ALLOWANCE';

export type ComputationType = 'FIXED' | 'PERCENTAGE';

export interface SalaryComponent {
  type: ComponentType;
  computationType: ComputationType;
  value: number; // amount if FIXED, percentage if PERCENTAGE
  computedAmount: number;
}

export interface SalaryStructure {
  employeeId: number;
  monthlyWage: number;
  yearlyWage: number;
  workingDaysPerWeek: number;
  breakHours: number;
  components: SalaryComponent[];
  pfEmployeePercent: number;
  pfEmployerPercent: number;
  professionalTax: number;
}

/** Editable subset sent on PUT /employees/{id}/salary — FIXED_ALLOWANCE is server-derived (LLD §2.5) */
export interface SalaryStructureRequest {
  monthlyWage: number;
  workingDaysPerWeek: number;
  breakHours: number;
  components: Array<{
    type: Exclude<ComponentType, 'FIXED_ALLOWANCE'>;
    computationType: ComputationType;
    value: number;
  }>;
  pfEmployeePercent: number;
  pfEmployerPercent: number;
  professionalTax: number;
}

export interface Payslip {
  id: number;
  employeeId: number;
  month: number;
  year: number;
  payableDays: number;
  grossSalary: number;
  totalDeductions: number;
  netSalary: number;
  pdfUrl?: string;
  generatedAt: string;
}
